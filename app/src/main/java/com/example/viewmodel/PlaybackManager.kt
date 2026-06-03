package com.example.viewmodel

import android.content.Context
import android.content.Intent
import com.example.data.model.SongDto
import com.example.service.MusicService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlaybackManager {

    enum class ShuffleMode {
        SEQUENTIAL, // S
        RANDOM      // R
    }

    enum class RepeatMode {
        ALL,   // A
        ONE    // 1
    }

    private val _currentSong = MutableStateFlow<SongDto?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _isPlayerOpen = MutableStateFlow(false)
    val isPlayerOpen = _isPlayerOpen.asStateFlow()

    private val _shuffleMode = MutableStateFlow(ShuffleMode.SEQUENTIAL)
    val shuffleMode = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode = _repeatMode.asStateFlow()

    private var currentPlaylist = emptyList<SongDto>()
    private var currentIndex = 0

    fun setPlaylist(songs: List<SongDto>) {
        currentPlaylist = songs
    }

    fun updateCurrentSong(song: SongDto?) {
        _currentSong.value = song
    }

    fun updateIsPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun updateCurrentPosition(position: Long) {
        _currentPosition.value = position
    }

    fun updateDuration(playerDuration: Long) {
        _duration.value = playerDuration
    }

    fun setPlayerOpen(open: Boolean) {
        _isPlayerOpen.value = open
    }

    fun toggleShuffleMode() {
        _shuffleMode.value = if (_shuffleMode.value == ShuffleMode.SEQUENTIAL) {
            ShuffleMode.RANDOM
        } else {
            ShuffleMode.SEQUENTIAL
        }
    }

    fun toggleRepeatMode() {
        _repeatMode.value = if (_repeatMode.value == RepeatMode.ALL) {
            RepeatMode.ONE
        } else {
            RepeatMode.ALL
        }
    }

    fun playSong(context: Context, song: SongDto) {
        _isPlayerOpen.value = true
        
        val index = currentPlaylist.indexOfFirst { it.id == song.id }
        if (index != -1) {
            currentIndex = index
        } else {
            currentPlaylist = listOf(song)
            currentIndex = 0
        }

        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_PLAY
            putExtra(MusicService.EXTRA_SONG_ID, song.id)
            putExtra(MusicService.EXTRA_SONG_TITLE, song.title)
            putExtra(MusicService.EXTRA_SONG_ARTIST, song.artist)
            putExtra(MusicService.EXTRA_SONG_URL, song.url)
            putExtra(MusicService.EXTRA_SONG_THUMB, song.thumb)
        }
        context.startService(intent)
    }

    fun playNext(context: Context) {
        if (currentPlaylist.isEmpty()) return
        
        val nextIndex = when (_shuffleMode.value) {
            ShuffleMode.SEQUENTIAL -> {
                (currentIndex + 1) % currentPlaylist.size
            }
            ShuffleMode.RANDOM -> {
                if (currentPlaylist.size > 1) {
                    var next = (0 until currentPlaylist.size).random()
                    while (next == currentIndex) {
                        next = (0 until currentPlaylist.size).random()
                    }
                    next
                } else {
                    0
                }
            }
        }
        
        currentIndex = nextIndex
        playSong(context, currentPlaylist[nextIndex])
    }

    fun playPrevious(context: Context) {
        if (currentPlaylist.isEmpty()) return
        
        val prevIndex = when (_shuffleMode.value) {
            ShuffleMode.SEQUENTIAL -> {
                (currentIndex - 1 + currentPlaylist.size) % currentPlaylist.size
            }
            ShuffleMode.RANDOM -> {
                if (currentPlaylist.size > 1) {
                    var prev = (0 until currentPlaylist.size).random()
                    while (prev == currentIndex) {
                        prev = (0 until currentPlaylist.size).random()
                    }
                    prev
                } else {
                    0
                }
            }
        }
        
        currentIndex = prevIndex
        playSong(context, currentPlaylist[prevIndex])
    }

    fun togglePlay(context: Context) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_TOGGLE
        }
        context.startService(intent)
    }

    fun seekTo(context: Context, position: Long) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK
            putExtra(MusicService.EXTRA_SEEK_POSITION, position)
        }
        context.startService(intent)
    }
}
