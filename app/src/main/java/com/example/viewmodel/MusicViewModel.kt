package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicRepository
import com.example.data.model.SongDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel : ViewModel() {
    private val _songs = MutableStateFlow<List<SongDto>>(emptyList())
    val songs = _songs.asStateFlow()

    init {
        fetchSongs()
    }

    private fun fetchSongs() {
        viewModelScope.launch {
            try {
                val fetchedSongs = MusicRepository.getSongs()
                _songs.value = fetchedSongs
                PlaybackManager.setPlaylist(fetchedSongs)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
