package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.model.SongDto

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val currentSong = PlaybackManager.currentSong
    val isPlayerOpen = PlaybackManager.isPlayerOpen
    val isPlaying = PlaybackManager.isPlaying
    val currentPosition = PlaybackManager.currentPosition
    val duration = PlaybackManager.duration
    val shuffleMode = PlaybackManager.shuffleMode
    val repeatMode = PlaybackManager.repeatMode

    fun playSong(song: SongDto) {
        PlaybackManager.playSong(getApplication(), song)
    }

    fun togglePlay() {
        PlaybackManager.togglePlay(getApplication())
    }

    fun seekTo(position: Long) {
        PlaybackManager.seekTo(getApplication(), position)
    }

    fun setPlayerOpen(open: Boolean) {
        PlaybackManager.setPlayerOpen(open)
    }

    fun toggleShuffleMode() {
        PlaybackManager.toggleShuffleMode()
    }

    fun toggleRepeatMode() {
        PlaybackManager.toggleRepeatMode()
    }

    fun playNext() {
        PlaybackManager.playNext(getApplication())
    }

    fun playPrevious() {
        PlaybackManager.playPrevious(getApplication())
    }
}
