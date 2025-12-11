package com.gollan.fadesleeptimer.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.gollan.fadesleeptimer.ui.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioViewModel : ViewModel() {

    private val _activeSound = MutableStateFlow<String?>(null)
    val activeSound = _activeSound.asStateFlow()

    private val _volume = MutableStateFlow(60f)
    val volume = _volume.asStateFlow()

    fun setSound(id: String?) { _activeSound.value = id }
    
    fun initVolume(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val current = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        _volume.value = (current.toFloat() / max.toFloat()) * 150f
    }

    fun updateVolume(context: Context, v: Float, settings: AppSettings) {
        var requestedVol = v
        
        // Apply Volume Safety Cap if enabled (40% of max = 60 out of 150)
        if (settings.volumeSafetyCap) {
            val maxSafe = 150f * 0.4f // 60
            if (requestedVol > maxSafe) {
                requestedVol = maxSafe
                Toast.makeText(context, "Safety Cap: Volume limited to 40%", Toast.LENGTH_SHORT).show()
            }
        }
        
        _volume.value = requestedVol
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val max = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        val newVol = (requestedVol / 150f * max).toInt()
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVol, 0)
    }
}
