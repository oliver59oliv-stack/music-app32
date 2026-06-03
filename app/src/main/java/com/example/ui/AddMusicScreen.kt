package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import coil.compose.AsyncImage
import com.example.MusicyBottomNavigation
import com.example.data.MusicRepository
import com.example.data.YoutubeVideoDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMusicScreen(onMenuClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0 for Search, 1 for Link
    var youtubeUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Preview state managers
    var previewVideoId by remember { mutableStateOf<String?>(null) }
    var previewVideoTitle by remember { mutableStateOf<String?>(null) }

    // API state managers
    val coroutineScope = rememberCoroutineScope()
    var searchResults by remember { mutableStateOf<List<YoutubeVideoDto>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var currentDownloadingVideoId by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isMessageError by remember { mutableStateOf(false) }

    val onSearchClick: () -> Unit = {
        if (searchQuery.isNotBlank()) {
            coroutineScope.launch {
                isSearching = true
                statusMessage = null
                searchResults = emptyList()
                try {
                    val response = MusicRepository.searchYoutube(searchQuery)
                    val videos = response.contents?.mapNotNull { it.video } ?: emptyList()
                    searchResults = videos
                    if (videos.isEmpty()) {
                        statusMessage = "No results found on YouTube."
                        isMessageError = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    statusMessage = "YouTube search failed: ${e.localizedMessage ?: "Unknown error"}"
                    isMessageError = true
                } finally {
                    isSearching = false
                }
            }
        }
    }

    val onDownloadLink: () -> Unit = {
        if (youtubeUrl.isNotBlank()) {
            coroutineScope.launch {
                isDownloading = true
                statusMessage = "Downloading audio assets & saving to server..."
                isMessageError = false
                val success = MusicRepository.downloadSong(youtubeUrl)
                if (success) {
                    statusMessage = "Download succeeded! The song will be available in your library shortly."
                    isMessageError = false
                    youtubeUrl = "" // Reset field
                } else {
                    statusMessage = "Failed to download. Please check the URL and try again."
                    isMessageError = true
                }
                isDownloading = false
            }
        }
    }

    val onDownloadVideo: (String) -> Unit = { videoId ->
        coroutineScope.launch {
            currentDownloadingVideoId = videoId
            isDownloading = true
            statusMessage = "Downloading YouTube track..."
            isMessageError = false
            val url = "https://www.youtube.com/watch?v=$videoId"
            val success = MusicRepository.downloadSong(url)
            if (success) {
                statusMessage = "Download queued successfully! Song is saving to server."
                isMessageError = false
            } else {
                statusMessage = "Failed to download this video. Please try again."
                isMessageError = true
            }
            isDownloading = false
            currentDownloadingVideoId = null
        }
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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
                        onClick = {
                            selectedTab = 0
                            statusMessage = null
                        }
                    )
                    TabButton(
                        text = "Upload by Link",
                        isSelected = selectedTab == 1,
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedTab = 1
                            statusMessage = null
                        }
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
                        SearchModeContent(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            isSearching = isSearching,
                            onSearchClick = onSearchClick
                        )
                    } else {
                        val videoId = extractYoutubeVideoId(youtubeUrl)
                        LinkModeContent(
                            url = youtubeUrl,
                            onUrlChange = { youtubeUrl = it },
                            isDownloading = isDownloading,
                            onDownloadClick = onDownloadLink,
                            onPreviewClick = {
                                videoId?.let {
                                    previewVideoId = it
                                    previewVideoTitle = "YouTube Link Preview"
                                }
                            },
                            hasValidPreview = videoId != null
                        )
                    }
                }
            }

            // Status Banner
            if (statusMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isMessageError) Color(0xFF451A1A) else Color(0xFF14352D),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isMessageError) Color.Red.copy(alpha = 0.2f) else Color(0xFF22C55E).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isMessageError) Icons.Default.Error else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isMessageError) Color.Red else Color(0xFF22C55E)
                        )
                        Text(
                            text = statusMessage!!,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Search Results List
            if (selectedTab == 0 && searchResults.isNotEmpty()) {
                Text(
                    text = "YOUTUBE RESULTS (${searchResults.size})",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    searchResults.forEach { video ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { video.videoId?.let { previewVideoId = it; previewVideoTitle = video.title } },
                            color = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = video.thumbnails?.firstOrNull()?.url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Preview",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = video.title ?: "Unknown video",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = video.author?.title ?: "YouTube Artist",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp
                                    )
                                }

                                if (isDownloading && currentDownloadingVideoId == video.videoId) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF22C55E),
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    IconButton(
                                        onClick = { video.videoId?.let { onDownloadVideo(it) } },
                                        enabled = !isDownloading
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = "Upload to Server",
                                            tint = Color(0xFF22C55E)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (previewVideoId != null) {
        AlertDialog(
            onDismissRequest = { previewVideoId = null },
            confirmButton = {
                Button(
                    onClick = {
                        val vidId = previewVideoId!!
                        previewVideoId = null
                        onDownloadVideo(vidId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Download & Upload", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { previewVideoId = null }) {
                    Text("Close", color = Color.White)
                }
            },
            title = {
                Text(
                    text = previewVideoTitle ?: "Video Preview",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webViewClient = WebViewClient()
                                webChromeClient = WebChromeClient()
                                loadUrl("https://www.youtube.com/embed/${previewVideoId}?autoplay=1")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(24.dp)
        )
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
fun SearchModeContent(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    onSearchClick: () -> Unit
) {
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
                onClick = onSearchClick,
                enabled = query.isNotBlank() && !isSearching,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E),
                    disabledContainerColor = Color(0xFF22C55E).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Search", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LinkModeContent(
    url: String,
    onUrlChange: (String) -> Unit,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit,
    onPreviewClick: () -> Unit,
    hasValidPreview: Boolean
) {
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
            onClick = onDownloadClick,
            enabled = url.isNotBlank() && !isDownloading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E),
                disabledContainerColor = Color(0xFF22C55E).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (isDownloading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("Download & Send to Server", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        if (hasValidPreview) {
            OutlinedButton(
                onClick = onPreviewClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF22C55E)
                ),
                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF22C55E))
                Spacer(Modifier.width(8.dp))
                Text("Watch / Listen Preview first", fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun extractYoutubeVideoId(url: String): String? {
    val pattern = "^(?:https?:\\/\\/)?(?:www\\.)?(?:youtube\\.com\\/(?:[^/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})"
    val regex = pattern.toRegex()
    val matchResult = regex.find(url)
    return matchResult?.groupValues?.getOrNull(1)
}
