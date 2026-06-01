package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.SongDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerViewModel : ViewModel() {
    private val _currentSong = MutableStateFlow<SongDto?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlayerOpen = MutableStateFlow(false)
    val isPlayerOpen = _isPlayerOpen.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    fun playSong(song: SongDto) {
        _currentSong.value = song
        _isPlaying.value = true
        _isPlayerOpen.value = true
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setPlayerOpen(open: Boolean) {
        _isPlayerOpen.value = open
    }
}
