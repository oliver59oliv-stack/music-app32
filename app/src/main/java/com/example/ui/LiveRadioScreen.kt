package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MusicyBottomNavigation

data class RadioStation(
    val name: String,
    val imageUrl: String
)

val sampleStations = listOf(
    RadioStation("977 Hits", "https://i.scdn.co/image/ab67616d0000b273b53a0df47b37f44d9f7a77e1"),
    RadioStation("DJ Radio ...", "https://i.scdn.co/image/ab67616d0000b2734b4b2b2b2b2b2b2b2b2b2b2b"),
    RadioStation("Dance Wa...", "https://i.scdn.co/image/ab67616d0000b273c5c5c5c5c5c5c5c5c5c5c5c5"),
    RadioStation("1.FM Top 40", "https://i.scdn.co/image/ab67616d0000b273d6d6d6d6d6d6d6d6d6d6d6d6"),
    RadioStation("Trance H...", "https://i.scdn.co/image/ab67616d0000b273e7e7e7e7e7e7e7e7e7e7e7e7"),
    RadioStation("Rihanna -...", "https://i.scdn.co/image/ab67616d0000b2731872199b50b8655761a2936a"),
    RadioStation("Rihanna 2...", "https://i.scdn.co/image/ab67616d0000b2731872199b50b8655761a2936a")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveRadioScreen(onMenuClick: () -> Unit) {
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
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "LIVE RADIO",
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleStations) { station ->
                RadioStationCard(station)
            }
        }
    }
}

@Composable
fun RadioStationCard(station: RadioStation) {
    Surface(
        color = Color(0xFF1E1E1E).copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = station.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(android.R.drawable.ic_menu_report_image),
                placeholder = painterResource(android.R.drawable.ic_menu_gallery)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = station.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp).clickable { /* TODO */ }
            )
        }
    }
}
