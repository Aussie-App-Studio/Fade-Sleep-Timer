package com.gollan.fadesleeptimer.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.gollan.fadesleeptimer.ui.AppSettings
import com.gollan.fadesleeptimer.viewmodel.TimerViewModel
import kotlin.math.sqrt

class SensorHelper(
    private val context: Context,
    private val onFaceDown: (() -> Unit)? = null,
    private val onShake: (() -> Unit)? = null,
    private val onFaceDownChanged: ((Boolean) -> Unit)? = null
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val proximity = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private var faceDownStartTimestamp: Long? = null
    private var lastMovementTimestamp: Long = 0L
    private var lastShakeTimestamp: Long = 0L
    
    // Sensitivity Thresholds (Delta G-Force)
    // 1.0 is gravity. Delta is abs(current - last).
    private var movementThreshold: Float = 0.8f // Default Medium

    var onPocketModeChanged: ((Boolean) -> Unit)? = null

    private var isFaceDownEnabled = false
    private var isShakeEnabled = false
    private var isPocketModeEnabled = false
    private var isMovementDetectionEnabled = false

    fun updateConfig(faceDown: Boolean, shake: Boolean, pocketMode: Boolean, movementDetection: Boolean = false, sensitivity: String = "MEDIUM") {
        isFaceDownEnabled = faceDown
        isShakeEnabled = shake
        isPocketModeEnabled = pocketMode
        isMovementDetectionEnabled = movementDetection
        
        movementThreshold = when(sensitivity) {
            "HIGH" -> 0.3f
            "LOW" -> 1.5f
            else -> 0.8f // MEDIUM
        }

        stopListening()
        if (isEnabled()) {
            startListening()
        }
    }

    private fun isEnabled(): Boolean = isFaceDownEnabled || isShakeEnabled || isPocketModeEnabled || isMovementDetectionEnabled

    private fun startListening() {
        if (isFaceDownEnabled || isShakeEnabled || isMovementDetectionEnabled) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (isPocketModeEnabled) {
            sensorManager?.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun cleanup() {
        stopListening()
    }
    
    fun hasMovedRecently(durationMillis: Long): Boolean {
        if (lastMovementTimestamp == 0L) return false
        return System.currentTimeMillis() - lastMovementTimestamp <= durationMillis
    }

    private var lastAccValues: FloatArray? = null

    var isFaceDown = false
        private set

    // Movement Detection Logic
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val currentTime = System.currentTimeMillis()

            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                
                // Update public state immediately
                val currentFaceDown = z < -8.0 && sqrt(x*x + y*y) < 2.0
                if (currentFaceDown != isFaceDown) {
                    isFaceDown = currentFaceDown
                    onFaceDownChanged?.invoke(isFaceDown)
                }
                
                // Movement Detection Logic
                if (isMovementDetectionEnabled) {
                    if (lastAccValues != null) {
                        val deltaX = kotlin.math.abs(x - lastAccValues!![0])
                        val deltaY = kotlin.math.abs(y - lastAccValues!![1])
                        val deltaZ = kotlin.math.abs(z - lastAccValues!![2])
                        
                        // Simple Manhattan distance or Euclidean could work. 
                        // Using Max Delta component is often stable enough for "did it shift?"
                        val maxDelta = maxOf(deltaX, deltaY, deltaZ)
                        
                        if (maxDelta > movementThreshold) {
                            lastMovementTimestamp = currentTime
                        }
                    }
                    lastAccValues = it.values.clone()
                }

                // Face Down Logic (Wait 5s)
                if (isFaceDownEnabled) {
                    // Logic updated above in generic section, using property now
                    
                    if (isFaceDown) {
                        if (faceDownStartTimestamp == null) {
                            faceDownStartTimestamp = currentTime
                        } else if (currentTime - faceDownStartTimestamp!! > 5000) {
                            // 5 seconds passed
                            triggerHapticFeedback()
                            onFaceDown?.invoke()
                            faceDownStartTimestamp = null // Reset
                        }
                    } else {
                        faceDownStartTimestamp = null
                    }
                } else {
                    faceDownStartTimestamp = null
                }

                // Shake Logic (Debounced)
                if (isShakeEnabled) {
                    val gForce = sqrt(x*x + y*y + z*z) / SensorManager.GRAVITY_EARTH
                    if (gForce > 2.5) {
                        if (currentTime - lastShakeTimestamp > 2000) {
                            triggerHapticFeedback() 
                            onShake?.invoke()
                            lastShakeTimestamp = currentTime
                        }
                    }
                }
            }

            if (it.sensor.type == Sensor.TYPE_PROXIMITY) {
                if (isPocketModeEnabled) {
                    val isPocket = it.values[0] < it.sensor.maximumRange
                    onPocketModeChanged?.invoke(isPocket)
                } else {
                    onPocketModeChanged?.invoke(false)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun triggerHapticFeedback() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        }
        
        vibrator?.let {
            if (it.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(200)
                }
            }
        }
    }
}
