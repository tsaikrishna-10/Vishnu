package com.vishnu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Void = Color(0xFF07090B)
private val Panel = Color(0xFF11171A)
private val Emerald = Color(0xFF20E3A2)
private val Mint = Color(0xFF9BFFD9)
private val Muted = Color(0xFF91A49D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VishnuApp() }
    }
}

private data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
private val navItems = listOf(
    NavItem("Home", Icons.Filled.Home),
    NavItem("Discover", Icons.Filled.Explore),
    NavItem("My List", Icons.Filled.Bookmark),
    NavItem("Downloads", Icons.Filled.Download),
    NavItem("Profile", Icons.Filled.Person)
)

@Composable
private fun VishnuApp() {
    var selected by remember { mutableStateOf("Home") }
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Void) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0C1715), Void, Void)))
            ) {
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "main-content",
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        "Home" -> HomeScreen(onDiscover = { selected = "Discover" })
                        "Discover" -> DiscoverScreen()
                        "My List" -> MyListScreen()
                        "Downloads" -> DownloadsScreen()
                        else -> PlaceholderScreen(page)
                    }
                }
                BottomBar(selected = selected, onSelect = { selected = it })
            }
        }
    }
}

@Composable
private fun HomeScreen(onDiscover: () -> Unit) {
    var trending by remember { mutableStateOf<List<TmdbTitle>>(emptyList()) }
    var catalogueMessage by remember { mutableStateOf("Connecting to VISHNU catalogue…") }

    LaunchedEffect(Unit) {
        try {
            val response = TmdbClient.api.getCatalogue("/trending/all/week?language=en-US")
            trending = response.results
            catalogueMessage = if (trending.isEmpty()) "No trending titles returned." else ""
        } catch (_: Exception) {
            catalogueMessage = "Catalogue temporarily unavailable. Check the connection and retry in a later build."
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Emerald, Color(0xFF087B62)))),
                contentAlignment = Alignment.Center
            ) {
                Text("V", color = Void, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("V I S H N U", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text("YOUR CINEMA UNIVERSE", color = Muted, fontSize = 9.sp, letterSpacing = 2.sp)
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Panel)
                    .clickable(onClick = onDiscover),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Mint) }
        }

        Spacer(Modifier.height(26.dp))
        Text("CURATED FOR YOU", color = Emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF174D3D), Color(0xFF172A2C), Color(0xFF101318))
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text("VISHNU ORIGINAL EXPERIENCE", color = Mint, fontSize = 9.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                Text("Stories that\\nstay with you.", color = Color.White, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text("Discover your next obsession.", color = Color(0xFFC0D0CA), fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                Text(
                    "EXPLORE  →",
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Emerald)
                        .clickable(onClick = onDiscover)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    color = Void,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Your universe", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("VIEW ALL  →", color = Emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(13.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf("Movies", "TV Shows", "Anime", "K-Dramas", "Documentaries")) { label ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Panel)
                        .clickable(onClick = onDiscover)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(label, color = Mint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Trending now", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("TMDB", color = Emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(12.dp))
        if (trending.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(trending.take(12)) { title ->
                    Column(modifier = Modifier.width(116.dp)) {
                        coil.compose.AsyncImage(
                            model = title.poster_path?.let { "https://image.tmdb.org/t/p/w342$it" },
                            contentDescription = title.displayTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(166.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Panel)
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(title.displayTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("★ ${"%.1f".format(title.vote_average ?: 0.0)}", color = Mint, fontSize = 10.sp)
                    }
                }
            }
        } else {
            Text(catalogueMessage, color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(26.dp))
        Text("THE NEXT CHAPTER", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Text("A smarter way to discover.", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Personalised collections, deep title insights, playback controls and a unified library are coming online in the next build stages.",
            color = Muted,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}


@Composable
private fun DiscoverScreen() {
    var input by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<TmdbTitle>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Search movies, TV shows and anime.") }
    val context = LocalContext.current

    LaunchedEffect(submitted) {
        if (submitted.isBlank()) return@LaunchedEffect
        loading = true
        message = "Searching the VISHNU catalogue…"
        try {
            val encoded = android.net.Uri.encode(submitted.trim())
            val response = TmdbClient.api.getCatalogue("/search/multi?query=$encoded&include_adult=false&language=en-US&page=1")
            results = response.results.filter { it.media_type != "person" }
            message = if (results.isEmpty()) "No titles found. Try another search." else ""
        } catch (_: Exception) {
            results = emptyList()
            message = "Search unavailable. Check your connection and try again."
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        Text("DISCOVER", color = Emerald, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Find your next favourite.", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Movie, series or anime") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Emerald,
                unfocusedBorderColor = Muted,
                focusedLabelColor = Emerald,
                unfocusedLabelColor = Muted,
                cursorColor = Emerald
            )
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = { submitted = input.trim() },
            enabled = input.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SEARCH VISHNU")
        }
        Spacer(Modifier.height(18.dp))
        if (loading) {
            CircularProgressIndicator(color = Emerald, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        if (results.isEmpty()) {
            Text(message, color = Muted, fontSize = 13.sp)
        } else {
            Text("${results.size} RESULTS", color = Emerald, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            results.forEach { title ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Panel)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    coil.compose.AsyncImage(
                        model = title.poster_path?.let { "https://image.tmdb.org/t/p/w185$it" },
                        contentDescription = title.displayTitle,
                        modifier = Modifier
                            .width(66.dp)
                            .height(96.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Void)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title.displayTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text((title.media_type ?: "title").uppercase(), color = Emerald, fontSize = 9.sp, letterSpacing = 1.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(title.overview ?: "No synopsis available.", color = Muted, fontSize = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                        Spacer(Modifier.height(7.dp))
                        OutlinedButton(onClick = { SavedTitleStore.add(context, title) }) {
                            Text("＋ MY LIST", color = Mint, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}


@Composable
private fun MyListScreen() {
    val context = LocalContext.current
    var saved by remember { mutableStateOf(SavedTitleStore.read(context)) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        Text("YOUR COLLECTION", color = Emerald, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("My List", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        Text("${saved.size} saved titles on this device", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        if (saved.isEmpty()) {
            Text("Your watchlist is empty. Discover a title and add it to My List.", color = Muted, fontSize = 13.sp)
        } else {
            saved.forEach { title ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Panel)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    coil.compose.AsyncImage(
                        model = title.poster_path?.let { "https://image.tmdb.org/t/p/w185$it" },
                        contentDescription = title.displayTitle,
                        modifier = Modifier.width(58.dp).height(86.dp).clip(RoundedCornerShape(8.dp)).background(Void)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title.displayTitle, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text((title.media_type ?: "title").uppercase(), color = Emerald, fontSize = 9.sp)
                    }
                    OutlinedButton(onClick = {
                        SavedTitleStore.remove(context, title)
                        saved = SavedTitleStore.read(context)
                    }) { Text("REMOVE", color = Muted, fontSize = 9.sp) }
                }
            }
        }
    }
}


@Composable
private fun DownloadsScreen() {
    val context = LocalContext.current
    var url by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Downloads are handled by Android's system Download Manager.") }
    var filename by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        Text("OFFLINE LIBRARY", color = Emerald, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Downloads", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text("Download direct media files that you own or are authorised to save.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Direct media URL") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Emerald,
                unfocusedBorderColor = Muted,
                focusedLabelColor = Emerald,
                unfocusedLabelColor = Muted,
                cursorColor = Emerald
            )
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val parsed = runCatching { android.net.Uri.parse(url.trim()) }.getOrNull()
                if (parsed == null || parsed.scheme !in listOf("http", "https") || parsed.host.isNullOrBlank()) {
                    status = "Enter a valid direct HTTP or HTTPS file URL."
                } else {
                    try {
                        val request = android.app.DownloadManager.Request(parsed)
                            .setTitle(parsed.lastPathSegment?.takeIf { it.isNotBlank() } ?: "VISHNU media")
                            .setDescription("Downloading with VISHNU")
                            .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(false)
                        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                        val downloadId = manager.enqueue(request)
                        status = "Download queued successfully. Android download ID: $downloadId"
                        url = ""
                    } catch (_: Exception) {
                        status = "Could not start this download. Check the URL and device storage."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = url.isNotBlank()
        ) { Text("START DOWNLOAD") }
        Spacer(Modifier.height(16.dp))
        Text(status, color = Mint, fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(20.dp))
        Text("DOWNLOAD NOTES", color = Emerald, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "• Android manages progress, notifications and completed files.\n• Some websites require sign-in or special headers and may not provide direct file URLs.\n• VISHNU does not extract media from protected players or bypass access controls.",
            color = Muted,
            fontSize = 12.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun PlaceholderScreen(page: String) {
    val subtitle = when (page) {
        "Discover" -> "Search and explore the full catalogue."
        "My List" -> "Your saved movies and series will live here."
        "Downloads" -> "Manage offline media and download activity."
        else -> "Account, integrations and playback preferences."
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(page.uppercase(), color = Emerald, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("This module is part of the VISHNU Android build roadmap.", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun BottomBar(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xE611171A))
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        navItems.forEach { item ->
            val active = selected == item.label
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (active) Color(0x3320E3A2) else Color.Transparent)
                    .clickable { onSelect(item.label) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(item.icon, contentDescription = item.label, tint = if (active) Emerald else Muted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.height(3.dp))
                Text(item.label, color = if (active) Mint else Muted, fontSize = 9.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Clip)
            }
        }
    }
}
