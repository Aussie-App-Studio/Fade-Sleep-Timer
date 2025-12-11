package com.gollan.fadesleeptimer.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerRepository {
    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration = _totalDuration.asStateFlow()

    fun updateTimeLeft(seconds: Long) {
        _timeLeft.value = seconds
    }

    fun setRunningState(running: Boolean) {
        _isRunning.value = running
    }
    
    fun setTotalDuration(minutes: Long) {
        _totalDuration.value = minutes * 60
    }

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying = _isAudioPlaying.asStateFlow()

    fun setAudioPlayingState(playing: Boolean) {
        _isAudioPlaying.value = playing
    }
}
