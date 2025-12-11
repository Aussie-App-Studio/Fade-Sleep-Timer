package com.gollan.fadesleeptimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.gollan.fadesleeptimer.MainActivity
import com.gollan.fadesleeptimer.R
import com.gollan.fadesleeptimer.data.TimerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class TimerService : Service() {

    private var audioManager: AudioManager? = null
    private var timer: CountDownTimer? = null
    private var initialVolume: Int = 0
    private var currentTotalDurationMillis: Long = 0
    private var currentTimeLeftMillis: Long = 0
    private var internalVolumeTracker: Float? = null
    private var previousInterruptionFilter: Int? = null
    
    // New audio feature fields
    private var fadeDurationMinutes: Int = 5
    private var smartWaitEnabled: Boolean = false

    private var batteryGuardEnabled: Boolean = false
    private var batteryGuardThreshold: Int = 15
    private var isWaitingForTrackEnd: Boolean = false
    private var extensionsCount: Int = 0
    private var batteryReceiver: android.content.BroadcastReceiver? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val CHANNEL_ID = "SleepTimerChannel"
    private val NOTIFICATION_ID = 1

    private var sensorHelper: com.gollan.fadesleeptimer.util.SensorHelper? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_EXTEND = "ACTION_EXTEND"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_TOGGLE_AUDIO = "ACTION_TOGGLE_AUDIO"
        const val EXTRA_DURATION_MINUTES = "DURATION_MINUTES"
        const val EXTRA_AUDIO_SELECTION = "AUDIO_SELECTION"
        const val EXTRA_PLAY_ON_START = "PLAY_ON_START"
        const val EXTRA_ONLY_AUDIO = "ONLY_AUDIO" // New Flag
        const val EXTRA_VOICE_GENDER = "VOICE_GENDER" // New
        const val EXTRA_DND_ENABLED = "DND_ENABLED"
        const val EXTRA_FADE_DURATION = "FADE_DURATION"
        const val EXTRA_SMART_WAIT = "SMART_WAIT"

        const val EXTRA_BATTERY_GUARD = "BATTERY_GUARD"
        const val EXTRA_MONOCHROME_MODE = "MONOCHROME_MODE"
        const val EXTRA_SHAKE_TO_EXTEND = "SHAKE_TO_EXTEND"
        
        // Smart Extend (Movement)
        const val EXTRA_SMART_EXTEND = "SMART_EXTEND"
        const val EXTRA_SMART_EXTEND_SENSITIVITY = "SMART_EXTEND_SENSITIVITY"
        
        // Anti-Doomscroll Extras
        const val EXTRA_DOOMSCROLL_ENABLED = "DOOMSCROLL_ENABLED"
        const val EXTRA_DOOMSCROLL_APPS = "DOOMSCROLL_APPS" // Arraylist of strings
        const val EXTRA_DOOMSCROLL_GRACE = "DOOMSCROLL_GRACE"

        // Static State for ScreenLockService to read cheap & fast
        var doomscrollLockEnabled: Boolean = false
        var doomscrollBlockedApps: Set<String> = emptySet()
        var doomscrollGraceMinutes: Int = 0
        
        // Basic "Is Running" check for Accessibility Service
        val isRunning: Boolean 
            get() = TimerRepository.isRunning.value
    }

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var ttsHelper: com.gollan.fadesleeptimer.util.TextToSpeechHelper? = null
    private var audioSelection: String = "SYSTEM"
    private var voiceGender: String = "DEFAULT" // New

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        
        sensorHelper = com.gollan.fadesleeptimer.util.SensorHelper(this, onShake = {
            extendTimer()
            // Track usage for onboarding
            val prefs = getSharedPreferences("controls_prefs", Context.MODE_PRIVATE)
            val usage = prefs.getInt("shake_usage", 0) + 1
            prefs.edit().putInt("shake_usage", usage).apply()
        })
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Fade out and stop if another app takes focus
                if (TimerRepository.isAudioPlaying.value) {
                    stopPlayback(fadeOut = true)
                }
            }
        }
    }
    
    // Store Smart Extend Config
    private var smartExtendEnabled = false
    private var smartExtendSensitivity = "MEDIUM"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: intent?.extras?.getString("ACTION")
        
        when (action) {
            ACTION_STOP -> {
                stopTimerService(manualCancel = true)
            }
            ACTION_PAUSE -> {
                stopPlayback(fadeOut = false)
            }
            ACTION_EXTEND -> {
                extendTimer()
            }
            ACTION_TOGGLE_AUDIO -> {
                if (TimerRepository.isAudioPlaying.value) {
                    stopPlayback(fadeOut = true)
                } else if (audioSelection != "SYSTEM") {
                    startPlaybackWithFadeIn()
                }
            }
            else -> {
                // Default to START if no action or explicit START
                val durationMinutes = intent?.getLongExtra(EXTRA_DURATION_MINUTES, 10) ?: 10
                val playOnStart = intent?.getBooleanExtra(EXTRA_PLAY_ON_START, false) ?: false
                val onlyAudio = intent?.getBooleanExtra(EXTRA_ONLY_AUDIO, false) ?: false // Read flag

                val dndEnabled = intent?.getBooleanExtra(EXTRA_DND_ENABLED, false) ?: false
                fadeDurationMinutes = intent?.getIntExtra(EXTRA_FADE_DURATION, 5) ?: 5
                smartWaitEnabled = intent?.getBooleanExtra(EXTRA_SMART_WAIT, false) ?: false
                audioSelection = intent?.getStringExtra(EXTRA_AUDIO_SELECTION) ?: "SYSTEM"
                voiceGender = intent?.getStringExtra(EXTRA_VOICE_GENDER) ?: "DEFAULT" // Read

                batteryGuardEnabled = intent?.getBooleanExtra(EXTRA_BATTERY_GUARD, false) ?: false
                batteryGuardThreshold = intent?.getIntExtra("BATTERY_GUARD_THRESHOLD", 15) ?: 15
                val monochromeMode = intent?.getStringExtra(EXTRA_MONOCHROME_MODE) ?: "OFF"
                val dndModeName = intent?.getStringExtra("DND_MODE") ?: "PRIORITY"
                
                // Shake & Smart Extend Feature
                val shakeToExtend = intent?.getBooleanExtra(EXTRA_SHAKE_TO_EXTEND, false) ?: false
                
                // Read Smart Extend Config
                smartExtendEnabled = intent?.getBooleanExtra(EXTRA_SMART_EXTEND, false) ?: false
                smartExtendSensitivity = intent?.getStringExtra(EXTRA_SMART_EXTEND_SENSITIVITY) ?: "MEDIUM"

                sensorHelper?.updateConfig(
                    faceDown = false, 
                    shake = shakeToExtend, 
                    pocketMode = false,
                    movementDetection = smartExtendEnabled,
                    sensitivity = smartExtendSensitivity
                )

                startTimer(durationMinutes, playOnStart, dndEnabled, monochromeMode, dndModeName, onlyAudio)
                
                // Update Static State for Accessibility Service
                doomscrollLockEnabled = intent?.getBooleanExtra(EXTRA_DOOMSCROLL_ENABLED, false) ?: false
                val appsList = intent?.getStringArrayListExtra(EXTRA_DOOMSCROLL_APPS)
                doomscrollBlockedApps = appsList?.toSet() ?: emptySet()
                doomscrollGraceMinutes = intent?.getIntExtra(EXTRA_DOOMSCROLL_GRACE, 5) ?: 5
            }
        }

        return START_NOT_STICKY
    }

    private fun startTimer(durationMinutes: Long, playOnStart: Boolean, dndEnabled: Boolean, monochromeModeName: String, dndModeName: String, onlyAudio: Boolean) {
        // 1. Setup Data (Only if Timer)
        if (!onlyAudio) {
            currentTotalDurationMillis = durationMinutes * 60 * 1000L
            currentTimeLeftMillis = currentTotalDurationMillis
            TimerRepository.setRunningState(true) // Running = TRUE (Timer Active)
            TimerRepository.setTotalDuration(durationMinutes)
             TimerRepository.updateTimeLeft(currentTotalDurationMillis / 1000) // Force initial update
        } else {
             // Only Audio Mode: Ensure Running state is false so UI doesn't think Timer is active
             TimerRepository.setRunningState(false)
        }
        
        extensionsCount = 0
        
        // 2. Start Foreground
        val notificationText = if (onlyAudio) "Sleepy Info Playing" else "Timer Running: $durationMinutes min"
        startForeground(NOTIFICATION_ID, createNotification(notificationText))

        // 3. Save initial volume
        initialVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0

        // 5. Play Audio logic
        // If "Built-in Noise" is selected (NOT SYSTEM)
        if (audioSelection != "SYSTEM") {
            // SAFE CHECK: Is music already playing?
            // If WE are playing (isAudioPlaying), we allow restart/switch.
            // If SOMEONE ELSE is playing, we respect it.
            if (audioManager?.isMusicActive == true && !TimerRepository.isAudioPlaying.value) {
                // If yes (and not us), DO NOT start built-in noise.
            } else {
                startPlaybackWithFadeIn(voiceGender)
            }
        } else {
            // If SYSTEM selected, handle "Resume Music" (playOnStart)
            if (playOnStart) {
                 if (audioManager?.isMusicActive == false) {
                    simulateMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
                }
            }
        }

        // 6. Start Countdown (Only if Timer)
        if (!onlyAudio) {
            startFadeTimer(currentTotalDurationMillis)
        } else {
            // If upgrading from Audio Only -> Timer, we might need to cancel existing timer? 
            // But if onlyAudio=true, we DON'T start the timer.
            // If old timer was running? 
            // If caller wanted OnlyAudio, we should probably stop any existing timer.
            timer?.cancel()
            TimerRepository.updateTimeLeft(0) // Clear time left
        }

        // 7. Enable DND if requested (Only if Timer) because DND is for sleeping
        if (dndEnabled && !onlyAudio) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val currentFilter = notificationManager.currentInterruptionFilter
                
                // Smart Activation: Only activate if currently in Normal mode (ALL)
                // This prevents overriding existing user preference or downgrading stricter modes
                if (currentFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                    previousInterruptionFilter = currentFilter
                    
                    val targetFilter = if (dndModeName == "ALARMS_ONLY") {
                        NotificationManager.INTERRUPTION_FILTER_ALARMS
                    } else {
                        NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    }
                    
                    notificationManager.setInterruptionFilter(targetFilter)
                }
            }
        }

        // 8. System Monochrome Modes
        when (monochromeModeName) {
            "SYSTEM_NIGHT_FILTER" -> startNightFilter()
        }

        // 9. Battery Guard
        if (batteryGuardEnabled) {
            batteryReceiver = object : android.content.BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
                    if (level != -1 && scale != -1) {
                        val batteryPct = level * 100 / scale.toFloat()
                        if (batteryPct < batteryGuardThreshold) {
                            stopTimerService()
                        }
                    }
                }
            }
            val filter = android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(batteryReceiver, filter)
        }
    }

    private fun extendTimer(minutes: Int = 15) {
        extensionsCount++
        // Add minutes
        val addMillis = minutes * 60 * 1000L
        currentTimeLeftMillis += addMillis
        currentTotalDurationMillis += addMillis // Extend total too so progress bar doesn't break

        // Update Repository with new total
        TimerRepository.setTotalDuration(currentTotalDurationMillis / 1000 / 60)
        
        // Restart timer with new time
        startFadeTimer(currentTimeLeftMillis)
        updateWidgets()
        
        // Update notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val reason = if (minutes == 10) "Timer Auto-Extended (Movement)" else "Timer Extended"
        notificationManager.notify(NOTIFICATION_ID, createNotification(reason))
    }

    private fun startFadeTimer(durationMillis: Long) {
        timer?.cancel()

        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currentTimeLeftMillis = millisUntilFinished
                
                // Update Repository
                TimerRepository.updateTimeLeft(millisUntilFinished / 1000)

                // --- SMART FADE LOGIC (Configurable Duration) ---
                val FADE_WINDOW_MILLIS = fadeDurationMinutes * 60 * 1000L

                // Check if we were fading but now have escaped the fade window (Extended)
                if (millisUntilFinished > FADE_WINDOW_MILLIS && internalVolumeTracker != null) {
                    // RESTORE VOLUME GRADUALLY (3 seconds)
                    val startVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                    val targetVol = internalVolumeTracker!!.toInt()
                    
                    if (startVol < targetVol) {
                        serviceScope.launch {
                            val duration = 3000L
                            val steps = 10
                            for (i in 1..steps) {
                                val progress = i / steps.toFloat()
                                val newVol = (startVol + ((targetVol - startVol) * progress)).toInt()
                                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                delay(duration/steps)
                            }
                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        }
                    } else {
                         audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                    }

                    internalVolumeTracker = null
                     // Reset Re-anchoring state as we are out of fade window
                    fadeAnchorDuration = null
                    fadeAnchorStartVol = null
                }

                if (millisUntilFinished <= FADE_WINDOW_MILLIS) {
                    val currentSysVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                    
                     // 1. Initialize Tracker for Restore
                    if (internalVolumeTracker == null) {
                        internalVolumeTracker = currentSysVol.toFloat()
                    }
                    
                    // 2. Detect Manual Override (Re-anchoring)
                    // If targetVol (last set) differs from currentSysVol significantly, user touched it.
                    if (lastSetTargetVol != -1 && kotlin.math.abs(lastSetTargetVol - currentSysVol) > 1) {
                        // User Intervened! Re-anchor fade curve.
                        fadeAnchorDuration = millisUntilFinished
                        fadeAnchorStartVol = currentSysVol.toFloat()
                    }

                    // 3. Calculate Target Volume
                    val fadeDuration = fadeAnchorDuration ?: FADE_WINDOW_MILLIS
                    val startVol = fadeAnchorStartVol ?: internalVolumeTracker!!
                    
                    // Ensure we don't divide by zero
                    if (fadeDuration > 0) {
                        val progress = millisUntilFinished / fadeDuration.toFloat() // 1.0 -> 0.0
                        // Clamp progress to 1.0 just in case
                        val safeProgress = progress.coerceIn(0f, 1f)
                        
                        val targetVolFloat = startVol * safeProgress
                        val targetVol = targetVolFloat.toInt()
                        
                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        lastSetTargetVol = targetVol
                    }
                } else {
                    // Outside fade window, ensure state is clean
                    lastSetTargetVol = -1
                }
                
                updateWidgets()
            }
            
            // State for Re-anchoring
            private var fadeAnchorDuration: Long? = null
            private var fadeAnchorStartVol: Float? = null
            private var lastSetTargetVol: Int = -1

            override fun onFinish() {
                // Smart Extend Check
                if (smartExtendEnabled && sensorHelper?.hasMovedRecently(30000) == true) {
                     extendTimer(10)
                     return
                }

                if (smartWaitEnabled) {
                    isWaitingForTrackEnd = true
                    startWaitingForTrackEnd()
                } else {
                    stopTimerService()
                }
            }
        }.start()
    }
    
    // Smart Wait: Silence Buffer (5s)
    private var silenceStartTimestamp: Long? = null
    
    private fun startWaitingForTrackEnd() {
        // Update notification to inform user
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification("Smart Wait: Waiting for audio to end..."))

        serviceScope.launch {
            while (isWaitingForTrackEnd) {
                delay(500) // Check every 0.5 second for responsiveness
                
                if (audioManager?.isMusicActive != true) {
                    // It is silent.
                    if (silenceStartTimestamp == null) {
                        silenceStartTimestamp = System.currentTimeMillis()
                    } else if (System.currentTimeMillis() - silenceStartTimestamp!! >= 5000) {
                        // 5 seconds of silence -> Stop
                        isWaitingForTrackEnd = false
                        stopTimerService()
                        silenceStartTimestamp = null
                        break
                    }
                } else {
                    // Music is active, reset silence timer
                    silenceStartTimestamp = null
                }
            }
        }
    }

    private fun stopTimerService(manualCancel: Boolean = false) {
        timer?.cancel()
        isWaitingForTrackEnd = false
        TimerRepository.setRunningState(false)
        TimerRepository.updateTimeLeft(0)
        
        updateWidgets()
        
        // Logic to execute AFTER potentially fading up
        val finalCleanup = {
            // Restore DND
            if (previousInterruptionFilter != null) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager.isNotificationPolicyAccessGranted) {
                    notificationManager.setInterruptionFilter(previousInterruptionFilter!!)
                }
                previousInterruptionFilter = null
            }
            
            stopNightFilter()
            
            // Unregister Battery Receiver
            if (batteryReceiver != null) {
                try {
                    unregisterReceiver(batteryReceiver)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                batteryReceiver = null
            }
            
            // Remove notification immediately
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            
            stopSelf()
        }

        if (manualCancel) {
            // MANUAL CANCEL: Do NOT stop playback. Fade volume UP if needed.
            // If we were fading (internalVolumeTracker != null), restore it.
            // If not fading, we are just done.
            
            if (internalVolumeTracker != null) {
                 val startVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                 val targetVol = internalVolumeTracker!!.toInt()
                 
                 if (startVol < targetVol) {
                     serviceScope.launch {
                        val duration = 3000L
                        val steps = 10
                        for (i in 1..steps) {
                            val progress = i / steps.toFloat()
                            val newVol = (startVol + ((targetVol - startVol) * progress)).toInt()
                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                            delay(duration/steps)
                        }
                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        finalCleanup()
                     }
                 } else {
                     finalCleanup()
                 }
            } else {
                finalCleanup()
            }
            
        } else {
            // NATURAL FINISH (or Battery/SmartWait Stop)
            
            // Calculate and Persist Saved Minutes (Gamification)
            if (currentTimeLeftMillis <= 1000) { 
                val durationMinutes = currentTotalDurationMillis / 1000 / 60
                val averageRunawayTime = 180L
                val saved = max(0L, averageRunawayTime - durationMinutes).toInt()
                
                if (saved > 0) {
                    val prefs = getSharedPreferences("app_stats", Context.MODE_PRIVATE)
                    val current = prefs.getInt("saved_minutes", 0)
                    prefs.edit().putInt("saved_minutes", current + saved).apply()
                }

                // Record Sleep History (Room Database)
                try {
                    val db = com.gollan.fadesleeptimer.data.local.SleepDatabase.getDatabase(applicationContext)
                    val session = com.gollan.fadesleeptimer.data.local.SleepSession(
                        timestamp_end = System.currentTimeMillis(),
                        duration_minutes = durationMinutes.toInt(),
                        day_of_week = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) java.time.LocalDate.now().dayOfWeek.name else "Unknown"
                    )
                    serviceScope.launch(Dispatchers.IO) {
                        db.sleepDao().insert(session)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Save Session Data for Morning Check
            val prefs = getSharedPreferences("app_stats", Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("last_session_timestamp", System.currentTimeMillis())
                .putInt("last_session_extended_count", extensionsCount)
                .apply()
    
            stopPlayback()
            finalCleanup()
        }
    }

    private fun startPlaybackWithFadeIn(voiceGender: String = "DEFAULT") {
        // Check for WIKI mode first
        if (audioSelection.startsWith("WIKI_")) {
             // Request Focus
             val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
             try {
                 val result = audioManager.requestAudioFocus(
                    audioFocusChangeListener, 
                    AudioManager.STREAM_MUSIC, 
                    AudioManager.AUDIOFOCUS_GAIN
                 )
                 
                 if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                     initializeAndPlayTTS(voiceGender)
                     TimerRepository.setAudioPlayingState(true)
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
             return
        }
        
        // SYSTEM Mode (or unknown) - Do nothing for playback start
    }
    
    private fun initializeAndPlayTTS(voiceGender: String) {
        if (ttsHelper == null) {
            ttsHelper = com.gollan.fadesleeptimer.util.TextToSpeechHelper(
                context = this, 
                gender = voiceGender,
                onInit = { result ->
                    when (result) {
                        com.gollan.fadesleeptimer.util.TtsInitResult.SUCCESS -> {
                            serviceScope.launch(Dispatchers.Main) {
                                playNextStory()
                            }
                        }
                        com.gollan.fadesleeptimer.util.TtsInitResult.LANGUAGE_MISSING -> {
                            serviceScope.launch(Dispatchers.Main) {
                                Toast.makeText(applicationContext, getString(R.string.tts_error_language_missing), Toast.LENGTH_LONG).show()
                                ttsHelper = null // Reset so we can try again later if they fix it
                                stopSelf() // Stop service since we can't do anything
                            }
                        }
                        com.gollan.fadesleeptimer.util.TtsInitResult.INITIALIZATION_FAILED -> {
                           serviceScope.launch(Dispatchers.Main) {
                                Toast.makeText(applicationContext, getString(R.string.tts_error_init_failed), Toast.LENGTH_SHORT).show()
                                stopSelf()
                           }
                        }
                    }
                },
                onComplete = {
                    // This runs on a binder thread, switch to Main or Default for Coroutine
                    serviceScope.launch(Dispatchers.Main) {
                        onStoryCompleted()
                    }
                }
            )
        } else {
            ttsHelper?.setVoice(voiceGender)
            
            // Add a small delay/yield to ensure voice switch propagates if needed
            serviceScope.launch(Dispatchers.Main) {
                delay(50) 
                playNextStory()
            }
        }
    }

    private fun playNextStory() {
        if (!TimerRepository.isAudioPlaying.value) return // User stopped it?

        val story = com.gollan.fadesleeptimer.data.BoringTalesRepository.getTale(audioSelection)
        ttsHelper?.speak(story)
    }

    private suspend fun onStoryCompleted() {
        if (!TimerRepository.isAudioPlaying.value) return

        // 3 Second Pause
        delay(3000)

        // Check again if we should still be playing (timer might have ended)
        if (TimerRepository.isAudioPlaying.value) {
            playNextStory()
        }
    }


    private fun stopPlayback(fadeOut: Boolean = false) {
        if (ttsHelper != null) {
            ttsHelper?.stop() // TTS doesn't fade nicely, just stop
            stopPlaybackInternal()
            return
        }
        
        if (fadeOut && mediaPlayer?.isPlaying == true) {
            serviceScope.launch {
                val duration = 1500L
                val steps = 15
                for (i in steps downTo 1) {
                    if (mediaPlayer?.isPlaying != true) break
                    val vol = i / steps.toFloat()
                    try { mediaPlayer?.setVolume(vol, vol) } catch(e:Exception){}
                    delay(duration/steps)
                }
                 stopPlaybackInternal()
            }
        } else {
            stopPlaybackInternal()
        }
    }

    private fun stopPlaybackInternal() {
        // Stop Internal Player
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
            
            ttsHelper?.stop()
            
            TimerRepository.setAudioPlayingState(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Mute
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)

        // Request Focus to Pause (Abandon Focus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener) // Match the one we used
                .build()
             audioManager?.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(audioFocusChangeListener)
        }
        
        // Also send PAUSE key for good measure
        simulateMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
        
        // Restore initial volume after pausing
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, 0)
    }

    private fun simulateMediaKey(keyCode: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val eventTime = SystemClock.uptimeMillis()
        
        val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
        audioManager.dispatchMediaKeyEvent(downEvent)
        
        val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
        audioManager.dispatchMediaKeyEvent(upEvent)
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, TimerService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val extendIntent = Intent(this, TimerService::class.java).apply { action = ACTION_EXTEND }
        val extendPendingIntent = PendingIntent.getService(
            this, 1, extendIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fade Sleep Timer")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .addAction(android.R.drawable.ic_input_add, "+15m", extendPendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Timer Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private var overlayView: android.view.View? = null

        private fun startNightFilter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) return

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager

        if (overlayView == null) {
            overlayView = android.view.View(this).apply {
                setBackgroundColor(0x55FF0000.toInt()) // Transparent Red (User Requirement: #55FF0000)
            }
            val params = android.view.WindowManager.LayoutParams(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT
            )
            params.gravity = android.view.Gravity.TOP or android.view.Gravity.START
            
            try {
                windowManager.addView(overlayView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopNightFilter() {
        if (overlayView != null) {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }





    override fun onDestroy() {
        timer?.cancel()
        TimerRepository.setRunningState(false)
        TimerRepository.setAudioPlayingState(false) // Fix: Ensure audio state is reset
        doomscrollLockEnabled = false // Disable lock when timer stops

        stopNightFilter()
        ttsHelper?.shutdown()
        sensorHelper?.cleanup()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // --- Widget Integration ---
    private fun updateWidgets() {
        serviceScope.launch {
            val context = applicationContext
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val widget = com.gollan.fadesleeptimer.widget.FadeWidget()
            try {
                val glanceIds = manager.getGlanceIds(widget.javaClass)
                
                val isRunningState = TimerRepository.isRunning.value
                val timeString = if (isRunningState) {
                    val totalSeconds = currentTimeLeftMillis / 1000
                    val m = totalSeconds / 60
                    val s = totalSeconds % 60
                    String.format("%02d:%02d", m, s)
                } else {
                    "--:--"
                }

                glanceIds.forEach { glanceId ->
                    androidx.glance.appwidget.state.updateAppWidgetState(
                        context,
                        androidx.glance.state.PreferencesGlanceStateDefinition,
                        glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[androidx.datastore.preferences.core.booleanPreferencesKey("is_running")] = isRunningState
                            this[androidx.datastore.preferences.core.stringPreferencesKey("time_left")] = timeString
                        }
                    }
                    widget.update(context, glanceId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
