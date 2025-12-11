package com.gollan.fadesleeptimer.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

    enum class TtsInitResult {
        SUCCESS,
        LANGUAGE_MISSING,
        INITIALIZATION_FAILED
    }

    class TextToSpeechHelper(
        context: Context, 
        private val gender: String = "DEFAULT",
        private val onInit: ((TtsInitResult) -> Unit)? = null,
        private val onComplete: (() -> Unit)? = null
    ) {
        var hasMultipleVoices: Boolean = false
            private set

        private var tts: TextToSpeech? = null
        private var isInitialized = false
    
        init {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported")
                        onInit?.invoke(TtsInitResult.LANGUAGE_MISSING)
                    } else {
                        isInitialized = true
                        tts?.setSpeechRate(0.82f) // Slower for soothing effect
                        tts?.setPitch(0.92f) // Slightly lower pitch to reduce robotic "tinny" sound
                        
                        // Set up listener for completion
                        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                // No-op
                            }
    
                            override fun onDone(utteranceId: String?) {
                                if (utteranceId == "BORING_STORY") {
                                    onComplete?.invoke()
                                }
                            }
    
                            override fun onError(utteranceId: String?) {
                                Log.e("TTS", "Error in utterance: $utteranceId")
                            }
                        })
    
                        // Check for multiple voices
                        val voiceCount = tts?.voices?.filter { it.locale == Locale.US }?.size ?: 0
                        hasMultipleVoices = voiceCount > 1
                        
                        setVoice(gender) // Apply requested voice
                        onInit?.invoke(TtsInitResult.SUCCESS)
                    }
                } else {
                    Log.e("TTS", "Initialization failed")
                    onInit?.invoke(TtsInitResult.INITIALIZATION_FAILED)
                }
            }
        }

    fun setVoice(gender: String) {
        if (!isInitialized) return
        
        // If default, just use default and return
        if (gender == "DEFAULT") {
            tts?.voice = tts?.defaultVoice
            return
        }
        
        val voices = tts?.voices ?: return
        
        // Filter for US voices that match the requested gender
        val candidates = voices.filter { voice -> 
            voice.locale == Locale.US && when(gender) {
                "MALE" -> voice.name.contains("male", ignoreCase = true) || voice.features.contains("male")
                "FEMALE" -> voice.name.contains("female", ignoreCase = true) || voice.features.contains("female")
                else -> false
            }
        }
        
        // Strategy: 
        // 1. Prioritize OFFLINE voices (reliability for sleep mode)
        // 2. Prioritize QUALITY (getting rid of robotic sound)
        val bestVoice = candidates.sortedWith(
            compareByDescending<android.speech.tts.Voice> { !it.isNetworkConnectionRequired }
                .thenByDescending { it.quality }
        ).firstOrNull()
        
        if (bestVoice != null) {
            Log.d("TTS_SELECTION", "Selected voice: ${bestVoice.name}, Quality: ${bestVoice.quality}, Offline: ${!bestVoice.isNetworkConnectionRequired}")
            tts?.voice = bestVoice
        } else {
            // Fallback if no specific gender match found
            Log.w("TTS_SELECTION", "No matching voice found for gender: $gender")
            tts?.voice = tts?.defaultVoice
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "BORING_STORY")
        } else {
            // Retry briefly or log
             Log.w("TTS", "TTS not initialized yet")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
