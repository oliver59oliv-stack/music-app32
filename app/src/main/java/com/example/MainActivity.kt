package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
    object AddMusic : Screen("add_music")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicyTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalNavController provides navController) {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val playerViewModel: PlayerViewModel = viewModel()
                    val currentSong by playerViewModel.currentSong.collectAsStateWithLifecycle()
                    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
                    val isPlayerOpen by playerViewModel.isPlayerOpen.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = Color(0xFF121212),
                                drawerShape = RoundedCornerShape(0.dp),
                                modifier = Modifier.width(300.dp).fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp)
                                ) {
                                    // Drawer Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = CircleShape,
                                            color = Color(0xFF1B3D2F),
                                            border = BorderStroke(2.dp, Color(0xFF22C55E))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.RadioButtonChecked,
                                                    contentDescription = null,
                                                    tint = Color(0xFF22C55E),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.6f))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(48.dp))

                                    // Drawer Items
                                    DrawerItem(
                                        icon = Icons.Default.Home,
                                        label = "Home Dashboard",
                                        isActive = navController.currentDestination?.route == Screen.Home.route,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate(Screen.Home.route)
                                        }
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    DrawerItem(
                                        icon = Icons.Default.LibraryMusic,
                                        label = "Music Library",
                                        isActive = navController.currentDestination?.route == Screen.Library.route,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate(Screen.Library.route)
                                        }
                                    )
                                    DrawerItem(
                                        icon = Icons.Default.Radio,
                                        label = "Live Radio",
                                        isActive = navController.currentDestination?.route == Screen.LiveRadio.route,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate(Screen.LiveRadio.route)
                                        }
                                    )
                                    DrawerItem(
                                        icon = Icons.Default.CloudUpload,
                                        label = "add music",
                                        isActive = navController.currentDestination?.route == Screen.AddMusic.route,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate(Screen.AddMusic.route)
                                        }
                                    )
                                    DrawerItem(icon = Icons.Default.Settings, label = "Configure Live Ra...")
                                    DrawerItem(
                                        icon = Icons.Default.VideoLibrary,
                                        label = "YouTube Companion",
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate(Screen.AddMusic.route)
                                        }
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Footer
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        color = Color.Black.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("English", color = Color.White, fontSize = 14.sp)
                                            Icon(
                                                imageVector = Icons.Default.RadioButtonChecked,
                                                contentDescription = null,
                                                tint = Color(0xFF22C55E),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Text(
                                        text = "© 2026 SPOTIFYY CLIENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    ) {
                        NavHost(navController = navController, startDestination = Screen.Home.route) {
                            composable(Screen.Home.route) {
                                HomeDashboard(
                                    navController = navController,
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable(Screen.Library.route) {
                                val musicViewModel: MusicViewModel = viewModel()
                                MusicLibraryScreen(
                                    onBack = { navController.popBackStack() },
                                    onMenuClick = { scope.launch { drawerState.open() } },
                                    viewModel = musicViewModel,
                                    onSongClick = playerViewModel::playSong
                                )
                            }
                            composable(Screen.LiveRadio.route) {
                                val radioViewModel: RadioViewModel = viewModel()
                                LiveRadioScreen(
                                    onMenuClick = { scope.launch { drawerState.open() } },
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
                            composable(Screen.AddMusic.route) {
                                AddMusicScreen(
                                    onMenuClick = { scope.launch { drawerState.open() } }
                                )
                            }
                        }
                    }

                    // MiniPlayer
                    if (currentSong != null && !isPlayerOpen) {
                        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)) {
                            MiniPlayer(
                                song = currentSong!!,
                                isPlaying = isPlaying,
                                onTogglePlay = playerViewModel::togglePlay,
                                onOpen = { playerViewModel.setPlayerOpen(true) }
                            )
                        }
                    }

                    // FullPlayer
                    if (currentSong != null && isPlayerOpen) {
                        FullPlayer(
                            song = currentSong!!,
                            isPlaying = isPlaying,
                            onTogglePlay = playerViewModel::togglePlay,
                            onCollapse = { playerViewModel.setPlayerOpen(false) }
                        )
                    }
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboard(navController: NavController, onMenuClick: () -> Unit) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_scaffold"),
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
                            contentDescription = "Home",
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "HOME DASHBOARD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Hero Card
            item {
                HeroCard()
            }

            item {
                Text(
                    text = "CATEGORIES",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Category Cards
            item {
                CategoryCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Music Library",
                    subtitle = "Listen to your favorites",
                    icon = Icons.Default.LibraryMusic,
                    backgroundColor = Color(0xFF14352D),
                    testTag = "music_library_card",
                    onClick = { navController.navigate(Screen.Library.route) }
                )
            }

            item {
                CategoryCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Live Radio",
                    subtitle = "Global stations on-air",
                    icon = Icons.Default.Radio,
                    backgroundColor = Color(0xFF1E293B),
                    testTag = "live_radio_card",
                    onClick = { navController.navigate(Screen.LiveRadio.route) }
                )
            }

            item {
                CategoryCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Add & Upload",
                    subtitle = "Fetch from YouTube",
                    icon = Icons.Default.VideoLibrary,
                    backgroundColor = Color(0xFF451A1A),
                    testTag = "add_upload_card",
                    onClick = { navController.navigate(Screen.AddMusic.route) }
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = if (isActive) Color(0xFF14352D) else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) Color(0xFF22C55E) else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = label,
                color = if (isActive) Color(0xFF22C55E) else Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun HeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2D1E), Color(0xFF000000))
                )
            )
            .padding(24.dp)
    ) {
        // Subtle Music Note in background
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.05f),
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 30.dp, y = 20.dp)
        )

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Good vibrations,",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your high-fidelity gateway to universal sound and rhythm.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(end = 40.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("play_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Play", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Surface(
                    onClick = { /* TODO */ },
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("add_hero_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Add", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .testTag(testTag)
            .border(1.dp, Color.White.copy(alpha = 0.03f), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
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
                NavigationTab("radio", "Radio", Icons.Default.Radio),
                NavigationTab("add_music", "Add", Icons.Default.AddCircle)
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
                                    popUpTo("home") { saveState = true }
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
