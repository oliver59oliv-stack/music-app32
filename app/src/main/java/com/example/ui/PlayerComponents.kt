package com.example.ui

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

@Composable
fun MiniPlayer(song: SongDto, isPlaying: Boolean, onTogglePlay: () -> Unit, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen),
        color = Color(0xFF1E1E1E),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.thumb,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(song.artist ?: "Unknown", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            IconButton(onClick = onOpen) {
                Icon(Icons.Default.OpenInFull, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun FullPlayer(song: SongDto, isPlaying: Boolean, onTogglePlay: () -> Unit, onCollapse: () -> Unit) {
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
        
        Slider(value = 0f, onValueChange = {})
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0:00", color = Color.Gray)
            Text("3:00", color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.White)
            Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White)
            
            IconButton(onClick = onTogglePlay, modifier = Modifier.size(64.dp).background(Color.White, CircleShape)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black
                )
            }
            
            Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White)
            Icon(Icons.Default.Repeat, contentDescription = null, tint = Color.White)
        }
    }
}
