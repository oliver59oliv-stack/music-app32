package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.*
import com.example.viewmodel.MusicViewModel
import com.example.viewmodel.PlayerViewModel
import com.example.viewmodel.RadioViewModel
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState

val LocalNavController = staticCompositionLocalOf<NavController?> { null }

data class NavigationTab(val route: String, val label: String, val icon: ImageVector)

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object LiveRadio : Screen("radio")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        setContent {
            MusicyTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavController provides navController) {
                    val playerViewModel: PlayerViewModel = viewModel()
                    val currentSong by playerViewModel.currentSong.collectAsStateWithLifecycle()
                    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
                    val isPlayerOpen by playerViewModel.isPlayerOpen.collectAsStateWithLifecycle()
                    val currentPosition by playerViewModel.currentPosition.collectAsStateWithLifecycle()
                    val duration by playerViewModel.duration.collectAsStateWithLifecycle()
                    val shuffleMode by playerViewModel.shuffleMode.collectAsStateWithLifecycle()
                    val repeatMode by playerViewModel.repeatMode.collectAsStateWithLifecycle()

                    // Handle back presses smoothly to control panels and back transitions
                    androidx.activity.compose.BackHandler(enabled = isPlayerOpen) {
                        playerViewModel.setPlayerOpen(false)
                    }

                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route
                    val canPop = navController.previousBackStackEntry != null
                    androidx.activity.compose.BackHandler(enabled = !isPlayerOpen && currentRoute != null && currentRoute != Screen.Home.route) {
                        if (canPop) {
                            navController.popBackStack()
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(navController = navController, startDestination = Screen.Home.route) {
                            composable(Screen.Home.route) {
                                val musicViewModel: MusicViewModel = viewModel()
                                val radioViewModel: RadioViewModel = viewModel()
                                HomeScreen(
                                    musicViewModel = musicViewModel,
                                    radioViewModel = radioViewModel,
                                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                                    onNavigateToRadio = { navController.navigate(Screen.LiveRadio.route) },
                                    onSongClick = playerViewModel::playSong,
                                    onRadioClick = { radio ->
                                        playerViewModel.playSong(
                                            com.example.data.model.SongDto(
                                                id = "radio_" + radio.name,
                                                title = radio.name,
                                                artist = "Live Radio Station",
                                                thumb = radio.logo,
                                                url = radio.url,
                                                source = "radio"
                                            )
                                        )
                                    }
                                )
                            }
                            composable(Screen.Library.route) {
                                val musicViewModel: MusicViewModel = viewModel()
                                MusicLibraryScreen(
                                    viewModel = musicViewModel,
                                    onSongClick = playerViewModel::playSong
                                )
                            }
                            composable(Screen.LiveRadio.route) {
                                val radioViewModel: RadioViewModel = viewModel()
                                LiveRadioScreen(
                                    viewModel = radioViewModel,
                                    onRadioClick = { radio ->
                                        playerViewModel.playSong(
                                            com.example.data.model.SongDto(
                                                id = "radio_" + radio.name,
                                                title = radio.name,
                                                artist = "Live Radio Station",
                                                thumb = radio.logo,
                                                url = radio.url,
                                                source = "radio"
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        // MiniPlayer
                        if (currentSong != null && !isPlayerOpen) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)) {
                                MiniPlayer(
                                    song = currentSong!!,
                                    isPlaying = isPlaying,
                                    currentPosition = currentPosition,
                                    duration = duration,
                                    onSeek = playerViewModel::seekTo,
                                    onTogglePlay = playerViewModel::togglePlay,
                                    onPlayPrevious = playerViewModel::playPrevious,
                                    onPlayNext = playerViewModel::playNext,
                                    onOpen = { playerViewModel.setPlayerOpen(true) }
                                )
                            }
                        }

                        // FullPlayer
                        if (currentSong != null && isPlayerOpen) {
                            FullPlayer(
                                song = currentSong!!,
                                isPlaying = isPlaying,
                                currentPosition = currentPosition,
                                duration = duration,
                                shuffleMode = shuffleMode,
                                repeatMode = repeatMode,
                                onSeek = playerViewModel::seekTo,
                                onTogglePlay = playerViewModel::togglePlay,
                                onToggleShuffle = playerViewModel::toggleShuffleMode,
                                onToggleRepeat = playerViewModel::toggleRepeatMode,
                                onPlayNext = playerViewModel::playNext,
                                onPlayPrevious = playerViewModel::playPrevious,
                                onCollapse = { playerViewModel.setPlayerOpen(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MusicyBottomNavigation() {
    val navController = LocalNavController.current
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .navigationBarsPadding(),
        color = Color(0xFF101010),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                NavigationTab("home", "Home", Icons.Default.Home),
                NavigationTab("library", "Music", Icons.Default.LibraryMusic),
                NavigationTab("radio", "Radio", Icons.Default.Radio)
            )

            tabs.forEach { tab ->
                val isActive = currentRoute == tab.route
                val tintColor = if (isActive) Color(0xFF22C55E) else Color.White.copy(alpha = 0.5f)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (currentRoute != tab.route) {
                                navController?.navigate(tab.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tintColor,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
