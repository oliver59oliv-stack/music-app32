package com.example.viewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SongDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null

    private val _currentSong = MutableStateFlow<SongDto?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlayerOpen = MutableStateFlow(false)
    val isPlayerOpen = _isPlayerOpen.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private var progressJob: Job? = null

    fun playSong(song: SongDto) {
        _currentSong.value = song
        _isPlayerOpen.value = true
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L

        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(song.url)
                setOnPreparedListener {
                    start()
                    _isPlaying.value = true
                    val songDuration = duration.toLong()
                    _duration.value = if (songDuration > 0) songDuration else 0L
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = _duration.value
                    progressJob?.cancel()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    progressJob?.cancel()
                    false
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlay() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            progressJob?.cancel()
        } else {
            try {
                player.start()
                _isPlaying.value = true
                startProgressTracker()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition.toLong()
                        val songDuration = player.duration.toLong()
                        _duration.value = if (songDuration > 0) songDuration else 0L
                    }
                }
                delay(1000)
            }
        }
    }

    fun seekTo(position: Long) {
        mediaPlayer?.let { player ->
            try {
                player.seekTo(position.toInt())
                _currentPosition.value = position
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setPlayerOpen(open: Boolean) {
        _isPlayerOpen.value = open
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
