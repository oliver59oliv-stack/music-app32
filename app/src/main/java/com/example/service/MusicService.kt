package com.example.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.SongDto
import com.example.viewmodel.PlaybackManager
import kotlinx.coroutines.*

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isReceiverRegistered = false
    private var mediaSession: MediaSessionCompat? = null

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "MusicyMediaSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    toggle()
                }

                override fun onPause() {
                    toggle()
                }

                override fun onSkipToNext() {
                    playNext()
                }

                override fun onSkipToPrevious() {
                    playPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    seekTo(pos)
                    updateNotification()
                }

                override fun onStop() {
                    stopPlaybackService()
                }
            })
            isActive = true
        }
    }

    private fun playPrevious() {
        PlaybackManager.playPrevious(this)
    }

    private fun playNext() {
        PlaybackManager.playNext(this)
    }

    private fun updatePlaybackState() {
        val state = if (PlaybackManager.isPlaying.value) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }
        
        val position = mediaPlayer?.currentPosition?.toLong() ?: 0L
        val speed = if (PlaybackManager.isPlaying.value) 1.0f else 0.0f
        
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP
                
        val playbackState = PlaybackStateCompat.Builder()
            .setState(state, position, speed)
            .setActions(actions)
            .build()
            
        mediaSession?.setPlaybackState(playbackState)
    }

    private var currentArtwork: android.graphics.Bitmap? = null

    private fun updateMetadata(song: SongDto) {
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist ?: "Unknown Artist")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, PlaybackManager.duration.value)
        currentArtwork?.let {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it)
        }
        mediaSession?.setMetadata(metadataBuilder.build())
    }

    private fun loadArtworkAndNotify(song: SongDto) {
        val thumbUrl = song.thumb
        if (thumbUrl.isNullOrBlank()) {
            currentArtwork = getDefaultArtwork()
            updateMetadata(song)
            updateNotification()
            return
        }
        serviceScope.launch {
            try {
                val loader = coil.Coil.imageLoader(this@MusicService)
                val request = coil.request.ImageRequest.Builder(this@MusicService)
                    .data(thumbUrl)
                    .allowHardware(false)
                    .build()
                val result = withContext(Dispatchers.IO) {
                    loader.execute(request)
                }
                if (result is coil.request.SuccessResult) {
                    val drawable = result.drawable
                    if (drawable is android.graphics.drawable.BitmapDrawable) {
                        currentArtwork = drawable.bitmap
                    } else {
                        currentArtwork = getDefaultArtwork()
                    }
                } else {
                    currentArtwork = getDefaultArtwork()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                currentArtwork = getDefaultArtwork()
            }
            updateMetadata(song)
            updateNotification()
        }
    }

    private fun getDefaultArtwork(): android.graphics.Bitmap {
        val size = 256
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1A1A1A")
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        
        val circlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#22C55E")
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 4f, circlePaint)
        return bitmap
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.setVolume(0.2f, 0.2f)
                    }
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.let { player ->
                    player.setVolume(1.0f, 1.0f)
                }
            }
        }
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = focusRequest
            audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (::audioManager.isInitialized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let {
                    audioManager.abandonAudioFocusRequest(it)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
        }
    }

    private fun registerNoisyReceiver() {
        if (!isReceiverRegistered) {
            registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            isReceiverRegistered = true
        }
    }

    private fun unregisterNoisyReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(noisyReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }
    }

    companion object {
        const val CHANNEL_ID = "musicy_playback_channel"
        const val NOTIFICATION_ID = 80112

        const val ACTION_PLAY = "com.example.action.PLAY"
        const val ACTION_PAUSE = "com.example.action.PAUSE"
        const val ACTION_TOGGLE = "com.example.action.TOGGLE"
        const val ACTION_SEEK = "com.example.action.SEEK"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_NEXT = "com.example.action.NEXT"

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
        setupMediaSession()
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
            ACTION_PREVIOUS -> {
                playPrevious()
            }
            ACTION_NEXT -> {
                playNext()
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
                    if (requestAudioFocus()) {
                        start()
                        PlaybackManager.updateIsPlaying(true)
                        val musicDuration = duration.toLong()
                        PlaybackManager.updateDuration(if (musicDuration > 0) musicDuration else 0L)
                        currentArtwork = getDefaultArtwork()
                        updateMetadata(song)
                        updatePlaybackState()
                        startProgressTracker()
                        startForegroundNotification()
                        registerNoisyReceiver()
                        loadArtworkAndNotify(song)
                    } else {
                        PlaybackManager.updateIsPlaying(false)
                    }
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
                    updatePlaybackState()
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
                updatePlaybackState()
                updateNotification()
            }
        }
        abandonAudioFocus()
        unregisterNoisyReceiver()
    }

    private fun toggle() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                PlaybackManager.updateIsPlaying(false)
                progressJob?.cancel()
                abandonAudioFocus()
                unregisterNoisyReceiver()
            } else {
                try {
                    if (requestAudioFocus()) {
                        player.start()
                        PlaybackManager.updateIsPlaying(true)
                        startProgressTracker()
                        registerNoisyReceiver()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            updatePlaybackState()
            updateNotification()
        }
    }

    private fun seekTo(position: Long) {
        mediaPlayer?.let { player ->
            try {
                player.seekTo(position.toInt())
                PlaybackManager.updateCurrentPosition(position)
                updatePlaybackState()
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
                        updatePlaybackState()
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
        val prevIntent = Intent(this, MusicService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(
            this, 3, prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, MusicService::class.java).apply { action = ACTION_TOGGLE }
        val togglePendingIntent = PendingIntent.getService(
            this, 1, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, MusicService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this, 4, nextIntent,
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

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(song.title)
            .setContentText(song.artist ?: "Unknown Artist")
            .setContentIntent(activityPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent) // index 0
            .addAction(playPauseIcon, playPauseTitle, togglePendingIntent) // index 1
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent) // index 2
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent) // index 3
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession?.sessionToken)
            )
            .setOngoing(isPlaying)

        currentArtwork?.let {
            builder.setLargeIcon(it)
        }

        return builder.build()
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
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        abandonAudioFocus()
        unregisterNoisyReceiver()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressJob?.cancel()
        serviceScope.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        abandonAudioFocus()
        unregisterNoisyReceiver()
    }
}
