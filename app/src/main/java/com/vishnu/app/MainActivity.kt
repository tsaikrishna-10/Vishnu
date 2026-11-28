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
import androidx.compose.foundation.lazy.LazyRow
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
