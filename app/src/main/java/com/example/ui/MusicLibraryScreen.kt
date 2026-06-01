package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.MusicyBottomNavigation
import com.example.data.model.SongDto
import com.example.viewmodel.MusicViewModel

data class Song(
    val title: String,
    val artist: String,
    val duration: String,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicLibraryScreen(
    onBack: () -> Unit, 
    onMenuClick: () -> Unit, 
    viewModel: MusicViewModel = viewModel(),
    onSongClick: (SongDto) -> Unit
) {
    val songsDto by viewModel.songs.collectAsStateWithLifecycle()
    val songs = songsDto.map { 
        Song(
            title = it.title,
            artist = it.artist ?: "Unknown Artist",
            duration = "0:00", // Not provided by API
            imageUrl = it.thumb ?: ""
        )
    }
    
    Scaffold(
        containerColor = Color(0xFF0A0A0A),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "MUSIC LIBRARY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            MusicyBottomNavigation()
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            itemsIndexed(songs) { index, song ->
                SongItem(index + 1, song, onClick = { onSongClick(songsDto[index]) })
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SongItem(index: Int, song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index
        Text(
            text = index.toString(),
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 14.sp,
            modifier = Modifier.width(32.dp)
        )

        // Thumbnail
        AsyncImage(
            model = song.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(android.R.drawable.ic_menu_report_image),
            placeholder = painterResource(android.R.drawable.ic_menu_gallery)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title and Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = song.artist,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                maxLines = 1
            )
        }

        // Duration
        Text(
            text = song.duration,
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 14.sp
        )
    }
}
