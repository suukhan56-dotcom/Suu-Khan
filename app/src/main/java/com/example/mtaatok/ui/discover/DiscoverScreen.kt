package com.example.mtaatok.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.mtaatok.data.repository.MtaaTokRepository
import com.example.mtaatok.model.VideoPost
import com.example.mtaatok.player.MtaaVideoPlayer

@Composable
fun DiscoverScreen(
    repository: MtaaTokRepository,
    initialQuery: String = "",
    onNavigateToProfile: (String) -> Unit
) {
    val posts by repository.getAllPosts().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedCategory by remember { mutableStateOf("All") }
    var playingPost by remember { mutableStateOf<VideoPost?>(null) }

    val categories = listOf("All", "Street Vibes", "Afro Dance", "Music & Beats", "Skate & Stunts", "Street Food", "Comedy")

    val trendingHashtags = listOf(
        Pair("MtaaVibes", "148.5M views"),
        Pair("NairobiNights", "92.1M views"),
        Pair("DanceChallenge", "310.2M views"),
        Pair("EastlandsDrill", "45.8M views"),
        Pair("SmokiePasua", "18.3M views")
    )

    // Recommended creators
    val recommendedCreators = remember(posts) {
        posts.distinctBy { it.creatorId }.take(5)
    }

    // Filter posts based on search query and category
    val filteredPosts = remember(posts, searchQuery, selectedCategory) {
        posts.filter { post ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                post.caption.contains(searchQuery, ignoreCase = true) ||
                        post.creatorUsername.contains(searchQuery, ignoreCase = true) ||
                        post.creatorDisplayName.contains(searchQuery, ignoreCase = true) ||
                        post.hashtags.any { it.contains(searchQuery.removePrefix("#"), ignoreCase = true) }
            }

            val matchesCategory = if (selectedCategory == "All") true else {
                when (selectedCategory) {
                    "Street Vibes" -> post.caption.contains("street", ignoreCase = true) || post.caption.contains("vibe", ignoreCase = true)
                    "Afro Dance" -> post.caption.contains("dance", ignoreCase = true) || post.caption.contains("footwork", ignoreCase = true)
                    "Music & Beats" -> post.caption.contains("drill", ignoreCase = true) || post.caption.contains("beat", ignoreCase = true) || post.musicTitle.isNotBlank()
                    "Skate & Stunts" -> post.caption.contains("skate", ignoreCase = true)
                    "Street Food" -> post.caption.contains("food", ignoreCase = true) || post.caption.contains("smokie", ignoreCase = true)
                    else -> true
                }
            }

            matchesQuery && matchesCategory
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C10))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("discover_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Input Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search creators, sounds, #hashtags...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFFF5722))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.LightGray)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF191924),
                        unfocusedContainerColor = Color(0xFF191924)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("discover_search_input")
                )
            }

            // Categories LazyRow
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.width(8.dp)) }
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFFFF5722) else Color(0xFF1A1A26))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                item { Spacer(modifier = Modifier.width(8.dp)) }
            }

            // If not searching, show Trending Hashtags & Recommended Creators
            if (searchQuery.isBlank() && selectedCategory == "All") {
                // Trending Hashtags Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trending on MtaaTok", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.width(8.dp)) }
                    items(trendingHashtags) { (tag, views) ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1829)),
                            modifier = Modifier
                                .clickable { searchQuery = tag }
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text("#$tag", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(views, color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.width(8.dp)) }
                }

                // Recommended Creators
                Text(
                    text = "Featured Creators",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.width(8.dp)) }
                    items(recommendedCreators) { creatorPost ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onNavigateToProfile(creatorPost.creatorId) }
                                .padding(4.dp)
                        ) {
                            AsyncImage(
                                model = creatorPost.creatorAvatarUrl,
                                contentDescription = creatorPost.creatorUsername,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF28283C)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "@${creatorPost.creatorUsername}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.width(8.dp)) }
                }
            }

            // Results Grid Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "Search Results (${filteredPosts.size})" else "Explore Videos",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Videos Grid
            if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No videos found matching your search.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151520)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.7f)
                                .clickable { playingPost = post }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = post.creatorAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.8f)
                                                )
                                            )
                                        )
                                )

                                // Play icon
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(36.dp)
                                        .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }

                                // Bottom Meta
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = post.caption,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "@${post.creatorUsername}",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen Playable Video Dialog
        playingPost?.let { post ->
            Dialog(
                onDismissRequest = { playingPost = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    MtaaVideoPlayer(
                        videoUrl = post.videoUrl,
                        isPlaying = true,
                        isMuted = false,
                        filterEffect = post.filterEffect,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = { playingPost = null },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "@${post.creatorUsername}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.caption,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
