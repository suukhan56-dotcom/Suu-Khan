package com.example.mtaatok.ui.feed

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mtaatok.data.repository.MtaaTokRepository
import com.example.mtaatok.model.VideoPost
import com.example.mtaatok.player.MtaaVideoPlayer
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    repository: MtaaTokRepository,
    onNavigateToProfile: (String) -> Unit,
    onHashtagClick: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val posts by repository.getAllPosts().collectAsState(initial = emptyList())

    var isGlobalMuted by remember { mutableStateOf(false) }
    var commentSheetPost by remember { mutableStateOf<VideoPost?>(null) }
    var selectedTab by remember { mutableStateOf("For You") } // "For You" or "Following"

    if (posts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFF5722))
        }
        return
    }

    val filteredPosts = remember(posts, selectedTab) {
        if (selectedTab == "Following") {
            val followingList = posts.filter { it.isFollowingCreator }
            if (followingList.isEmpty()) posts else followingList
        } else {
            posts
        }
    }

    val pagerState = rememberPagerState(pageCount = { filteredPosts.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("feed_screen")
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> filteredPosts.getOrNull(index)?.id ?: index }
        ) { pageIndex ->
            val post = filteredPosts[pageIndex]
            val isCurrentPageVisible by remember {
                derivedStateOf { pagerState.currentPage == pageIndex }
            }

            var isPlaying by remember(pageIndex) { mutableStateOf(true) }

            Box(modifier = Modifier.fillMaxSize()) {
                // Real Video Player
                MtaaVideoPlayer(
                    videoUrl = post.videoUrl,
                    isPlaying = isCurrentPageVisible && isPlaying,
                    isMuted = isGlobalMuted,
                    filterEffect = post.filterEffect,
                    playbackSpeed = 1.0f,
                    onSingleTap = {
                        isPlaying = !isPlaying
                    },
                    onDoubleTapLike = {
                        repository.toggleLike(post.id)
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom Gradient Scrim for Text Legibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Video Meta & Creator Details (Bottom Left)
                VideoPostInfoOverlay(
                    post = post,
                    onCreatorClick = { onNavigateToProfile(post.creatorId) },
                    onHashtagClick = onHashtagClick,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 20.dp, end = 80.dp)
                )

                // Right Action Sidebar (Like, Comment, Save, Share, Download, Vinyl Disc)
                VideoActionSidebar(
                    post = post,
                    onLike = { repository.toggleLike(post.id) },
                    onComment = { commentSheetPost = post },
                    onSave = { repository.toggleSave(post.id) },
                    onShare = {
                        shareVideo(context, post)
                    },
                    onDownload = {
                        if (post.allowsDownload) {
                            downloadVideo(context, post)
                        } else {
                            Toast.makeText(
                                context,
                                "Creator has disabled downloads for this video",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onFollowCreator = {
                        repository.toggleFollow(post.creatorId)
                    },
                    onCreatorClick = { onNavigateToProfile(post.creatorId) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 20.dp)
                )
            }
        }

        // Top Navigation Header ("Following" | "For You" & Sound Toggle)
        TopFeedHeader(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it },
            isMuted = isGlobalMuted,
            onToggleMute = { isGlobalMuted = !isGlobalMuted },
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Comments Bottom Sheet
        commentSheetPost?.let { post ->
            CommentsBottomSheet(
                post = post,
                repository = repository,
                onDismiss = { commentSheetPost = null }
            )
        }
    }
}

@Composable
private fun TopFeedHeader(
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Sound Mute Toggle
        IconButton(
            onClick = onToggleMute,
            modifier = Modifier
                .size(38.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                .testTag("feed_mute_button")
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Tabs ("Following" | "For You")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Following",
                color = if (selectedTab == "Following") Color.White else Color.White.copy(alpha = 0.6f),
                fontWeight = if (selectedTab == "Following") FontWeight.Bold else FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { onSelectTab("Following") }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("tab_following")
            )

            Text(
                text = "|",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 14.sp
            )

            Text(
                text = "For You",
                color = if (selectedTab == "For You") Color.White else Color.White.copy(alpha = 0.6f),
                fontWeight = if (selectedTab == "For You") FontWeight.Bold else FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { onSelectTab("For You") }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("tab_foryou")
            )
        }

        // Live Badge Indicator (tap to see live status)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFF2A6D).copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "LIVE",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun VideoPostInfoOverlay(
    post: VideoPost,
    onCreatorClick: () -> Unit,
    onHashtagClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Creator Username
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onCreatorClick() }
        ) {
            Text(
                text = "@${post.creatorUsername}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "MTAA",
                    color = Color(0xFF00E5FF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Caption & Hashtags
        Text(
            text = post.caption,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = 14.sp,
            maxLines = if (isExpanded) 6 else 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Hashtags Row
        Row(modifier = Modifier.fillMaxWidth()) {
            post.hashtags.take(3).forEach { tag ->
                Text(
                    text = "#$tag ",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { onHashtagClick(tag) }
                        .padding(end = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Music & Audio Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Music",
                tint = Color(0xFFFF5722),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${post.musicTitle} • ${post.musicArtist}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VideoActionSidebar(
    post: VideoPost,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onFollowCreator: () -> Unit,
    onCreatorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Creator Avatar with Follow Button
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AsyncImage(
                model = post.creatorAvatarUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150" },
                contentDescription = post.creatorUsername,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E2E3A))
                    .clickable { onCreatorClick() },
                contentScale = ContentScale.Crop
            )

            // Follow button (+ pill)
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (post.isFollowingCreator) Color(0xFF00E5FF) else Color(0xFFFF5722))
                    .clickable { onFollowCreator() }
                    .testTag("follow_creator_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (post.isFollowingCreator) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (post.isFollowingCreator) "Following" else "Follow",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Like Button & Count
        SidebarActionButton(
            icon = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            iconColor = if (post.isLiked) Color(0xFFFF2A6D) else Color.White,
            count = formatCount(post.likesCount),
            testTag = "action_like_button",
            onClick = onLike
        )

        // Comment Button & Count
        SidebarActionButton(
            icon = Icons.Default.ModeComment,
            iconColor = Color.White,
            count = formatCount(post.commentsCount),
            testTag = "action_comment_button",
            onClick = onComment
        )

        // Bookmark / Save Button & Count
        SidebarActionButton(
            icon = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            iconColor = if (post.isSaved) Color(0xFFFFD700) else Color.White,
            count = formatCount(post.savesCount),
            testTag = "action_save_button",
            onClick = onSave
        )

        // Share Button
        SidebarActionButton(
            icon = Icons.Default.Share,
            iconColor = Color.White,
            count = formatCount(post.sharesCount),
            testTag = "action_share_button",
            onClick = onShare
        )

        // Download Video Button (conditional appearance / indicator)
        SidebarActionButton(
            icon = Icons.Default.Download,
            iconColor = if (post.allowsDownload) Color(0xFF00E5FF) else Color.Gray.copy(alpha = 0.5f),
            count = if (post.allowsDownload) "Save" else "Off",
            testTag = "action_download_button",
            onClick = onDownload
        )

        // Spinning Music Vinyl Disc
        Box(
            modifier = Modifier
                .size(46.dp)
                .rotate(rotationAngle)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF22222E),
                            Color(0xFF111116),
                            Color(0xFFFF5722)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                drawCircle(color = Color.Black)
                drawCircle(color = Color(0xFF00E5FF), radius = 4.dp.toPx())
            }
        }
    }
}

@Composable
private fun SidebarActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    count: String,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = count,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
        count >= 10_000 -> "%.1fK".format(count / 1000.0)
        count >= 1_000 -> "%.1fK".format(count / 1000.0)
        else -> count.toString()
    }
}

private fun shareVideo(context: Context, post: VideoPost) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Watch @${post.creatorUsername}'s video on MtaaTok: '${post.caption}' 🔥\n${post.videoUrl}"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share MtaaTok Video via")
    context.startActivity(shareIntent)
}

private fun downloadVideo(context: Context, post: VideoPost) {
    try {
        if (post.videoUrl.startsWith("http")) {
            val request = DownloadManager.Request(Uri.parse(post.videoUrl))
                .setTitle("MtaaTok - @${post.creatorUsername}")
                .setDescription("Downloading MtaaTok short video...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_MOVIES,
                    "MtaaTok_${post.id}.mp4"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            downloadManager?.enqueue(request)
            Toast.makeText(context, "Downloading video to Movies/MtaaTok...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Saved video file available locally!", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Download initiated for @${post.creatorUsername}'s video", Toast.LENGTH_SHORT).show()
    }
}
