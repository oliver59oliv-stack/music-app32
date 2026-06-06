package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SongDto
import com.example.viewmodel.PlaybackManager

@Composable
fun MiniPlayer(
    song: SongDto,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onPlayPrevious: () -> Unit,
    onPlayNext: () -> Unit,
    onOpen: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xFF161616),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: Album Art, Info, and App Brand Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art on the left
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E2E2E)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!song.thumb.isNullOrBlank()) {
                        AsyncImage(
                            model = song.thumb,
                            contentDescription = "Artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Note",
                            tint = Color.LightGray.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Song Info in the middle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist ?: "Unknown Artist",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                // App Name in the top right
                Text(
                    text = "Music App",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Top)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Progress timeline with Green active color and White round thumb
            Slider(
                value = if (duration > 0f) currentPosition.toFloat() else 0f,
                onValueChange = { value -> onSeek(value.toLong()) },
                valueRange = 0f..(if (duration > 0f) duration.toFloat() else 1f),
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF22C55E),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                    thumbColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )

            // Timeline Labels (Current and Total position) positioned below the seek bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = if (duration > 0) formatTime(duration) else "0:00",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: Control actions (Previous, Play/Pause circle, Next) centered under the timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Previous
                IconButton(
                    onClick = onPlayPrevious,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Play / Pause Circle (Vivid green circle container, black background/icon content)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                        .clickable(onClick = onTogglePlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Skip Next
                IconButton(
                    onClick = onPlayNext,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
fun FullPlayer(
    song: SongDto,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    shuffleMode: PlaybackManager.ShuffleMode,
    repeatMode: PlaybackManager.RepeatMode,
    onSeek: (Long) -> Unit,
    onTogglePlay: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onCollapse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse", tint = Color.White)
            }
            Text("NOW PLAYING", color = Color.Gray, fontWeight = FontWeight.Bold)
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        AsyncImage(
            model = song.thumb,
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(song.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(song.artist ?: "Unknown", color = Color.Gray, fontSize = 18.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Slider(
            value = if (duration > 0f) currentPosition.toFloat() else 0f,
            onValueChange = { value -> onSeek(value.toLong()) },
            valueRange = 0f..(if (duration > 0f) duration.toFloat() else 1f),
            colors = SliderDefaults.colors(
                activeTrackColor = Color(0xFF22C55E),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                thumbColor = Color(0xFF22C55E)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentPosition), color = Color.Gray)
            Text(if (duration > 0) formatTime(duration) else "Live Stream", color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle/Sequential toggle
            Surface(
                onClick = onToggleShuffle,
                color = Color(0xFF1E1E1E).copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.height(48.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleMode == PlaybackManager.ShuffleMode.RANDOM) Color(0xFF22C55E) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (shuffleMode == PlaybackManager.ShuffleMode.RANDOM) "R" else "S",
                        color = if (shuffleMode == PlaybackManager.ShuffleMode.RANDOM) Color(0xFF22C55E) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Skip Previous
            Surface(
                onClick = onPlayPrevious,
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Play/Pause button (Wide Pill-shape)
            Surface(
                onClick = onTogglePlay,
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.width(96.dp).height(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Skip Next
            Surface(
                onClick = onPlayNext,
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Repeat toggle with A / 1 overlaid
            Surface(
                onClick = onToggleRepeat,
                color = Color(0xFF1E1E1E).copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.height(48.dp).width(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode == PlaybackManager.RepeatMode.ONE) Color(0xFF22C55E) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (repeatMode == PlaybackManager.RepeatMode.ONE) "1" else "A",
                        color = if (repeatMode == PlaybackManager.RepeatMode.ONE) Color(0xFF22C55E) else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.offset(y = (-0.5).dp)
                    )
                }
            }
        }
    }
}
