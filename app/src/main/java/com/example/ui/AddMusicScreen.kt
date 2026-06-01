package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MusicyBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMusicScreen(onMenuClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0 for Search, 1 for Link
    var youtubeUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

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
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "ADD MUSIC",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Tab Selector
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TabButton(
                        text = "Upload by Search",
                        isSelected = selectedTab == 0,
                        icon = Icons.Default.Search,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 0 }
                    )
                    TabButton(
                        text = "Upload by Link",
                        isSelected = selectedTab == 1,
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 1 }
                    )
                }
            }

            // Content Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E1E1E).copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    if (selectedTab == 0) {
                        SearchModeContent(searchQuery) { searchQuery = it }
                    } else {
                        LinkModeContent(youtubeUrl) { youtubeUrl = it }
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Color(0xFF22C55E) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxHeight()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SearchModeContent(query: String, onQueryChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14352D)),
                contentAlignment = Alignment.Center
            ) {
                Text("1", color = Color(0xFF22C55E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Interactive Music Search & Host Finder",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Enter song name, artist...", color = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF22C55E),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    cursorColor = Color(0xFF22C55E)
                ),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                },
                singleLine = true
            )

            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Search", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LinkModeContent(url: String, onUrlChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14352D)),
                contentAlignment = Alignment.Center
            ) {
                Text("2", color = Color(0xFF22C55E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Transfer YouTube Asset by pasting URL",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "YOUTUBE VIDEO URL:",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                placeholder = { Text("Paste YouTube link here...", color = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF22C55E),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    cursorColor = Color(0xFF22C55E)
                ),
                singleLine = true
            )
        }

        Button(
            onClick = { /* TODO */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("Download & Send to Server", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Follow workflow on GitHub Actions \u2197",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable { /* TODO */ }
        )
    }
}
