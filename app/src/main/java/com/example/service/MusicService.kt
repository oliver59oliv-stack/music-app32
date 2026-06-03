package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.SongDto
import com.example.viewmodel.PlaybackManager
import kotlinx.coroutines.*

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "musicy_playback_channel"
        const val NOTIFICATION_ID = 80112

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_TOGGLE = "com.example.action.TOGGLE"
        const val ACTION_SEEK = "com.example.action.SEEK"
        const val ACTION_STOP = "com.example.action.STOP"

        const val EXTRA_SONG_ID = "song_id"
        const val EXTRA_SONG_TITLE = "song_title"
        const val EXTRA_SONG_ARTIST = "song_artist"
        const val EXTRA_SONG_URL = "song_url"
        const val EXTRA_SONG_THUMB = "song_thumb"
        const val EXTRA_SEEK_POSITION = "seek_position"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val id = intent.getStringExtra(EXTRA_SONG_ID) ?: ""
                val title = intent.getStringExtra(EXTRA_SONG_TITLE) ?: ""
                val artist = intent.getStringExtra(EXTRA_SONG_ARTIST) ?: ""
                val url = intent.getStringExtra(EXTRA_SONG_URL) ?: ""
                val thumb = intent.getStringExtra(EXTRA_SONG_THUMB)

                val song = SongDto(id, title, artist, thumb, url, "")
                play(song)
            }
            ACTION_PAUSE -> {
                pause()
            }
            ACTION_TOGGLE -> {
                toggle()
            }
            ACTION_SEEK -> {
                val position = intent.getLongExtra(EXTRA_SEEK_POSITION, 0L)
                seekTo(position)
            }
            ACTION_STOP -> {
                stopPlaybackService()
            }
        }
        return START_NOT_STICKY
    }

    private fun play(song: SongDto) {
        PlaybackManager.updateCurrentSong(song)
        PlaybackManager.updateIsPlaying(false)
        PlaybackManager.updateCurrentPosition(0L)
        PlaybackManager.updateDuration(0L)

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
                    PlaybackManager.updateIsPlaying(true)
                    val musicDuration = duration.toLong()
                    PlaybackManager.updateDuration(if (musicDuration > 0) musicDuration else 0L)
                    startProgressTracker()
                    startForegroundNotification()
                }
                setOnCompletionListener {
                    if (PlaybackManager.repeatMode.value == PlaybackManager.RepeatMode.ONE) {
                        PlaybackManager.currentSong.value?.let { current ->
                            play(current)
                        }
                    } else {
                        PlaybackManager.playNext(this@MusicService)
                    }
                }
                setOnErrorListener { _, _, _ ->
                    PlaybackManager.updateIsPlaying(false)
                    progressJob?.cancel()
                    updateNotification()
                    false
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                PlaybackManager.updateIsPlaying(false)
                progressJob?.cancel()
                updateNotification()
            }
        }
    }

    private fun toggle() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                PlaybackManager.updateIsPlaying(false)
                progressJob?.cancel()
            } else {
                try {
                    player.start()
                    PlaybackManager.updateIsPlaying(true)
                    startProgressTracker()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            updateNotification()
        }
    }

    private fun seekTo(position: Long) {
        mediaPlayer?.let { player ->
            try {
                player.seekTo(position.toInt())
                PlaybackManager.updateCurrentPosition(position)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        PlaybackManager.updateCurrentPosition(player.currentPosition.toLong())
                        val musicDuration = player.duration.toLong()
                        PlaybackManager.updateDuration(if (musicDuration > 0) musicDuration else 0L)
                    }
                }
                delay(1000)
            }
        }
    }

    private fun startForegroundNotification() {
        val song = PlaybackManager.currentSong.value ?: return
        val notification = buildMusicNotification(song, PlaybackManager.isPlaying.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val song = PlaybackManager.currentSong.value ?: return
        val notification = buildMusicNotification(song, PlaybackManager.isPlaying.value)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildMusicNotification(song: SongDto, isPlaying: Boolean): Notification {
        val toggleIntent = Intent(this, MusicService::class.java).apply { action = ACTION_TOGGLE }
        val togglePendingIntent = PendingIntent.getService(
            this, 1, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setContentIntent(activityPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(playPauseIcon, playPauseTitle, togglePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )
            .setOngoing(isPlaying)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls and status of the current song"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun stopPlaybackService() {
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        PlaybackManager.updateIsPlaying(false)
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressJob?.cancel()
        serviceScope.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
