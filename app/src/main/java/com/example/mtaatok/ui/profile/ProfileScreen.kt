package com.example.mtaatok.ui.profile

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.mtaatok.data.repository.MtaaTokRepository
import com.example.mtaatok.model.User
import com.example.mtaatok.model.VideoPost
import com.example.mtaatok.player.MtaaVideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: MtaaTokRepository,
    targetUserId: String? = null,
    onNavigateToDashboard: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by repository.sessionManager.currentUser.collectAsState()
    val allPosts by repository.getAllPosts().collectAsState(initial = emptyList())
    val savedPosts by repository.getSavedPosts().collectAsState(initial = emptyList())
    val likedPosts by repository.getLikedPosts().collectAsState(initial = emptyList())

    val isSelfProfile = targetUserId == null || targetUserId == currentUser?.id

    val displayedUser = remember(currentUser, targetUserId, allPosts) {
        if (isSelfProfile) {
            currentUser ?: User("guest", "mtaacreator", "Mtaa Creator", "creator@mtaatok.app")
        } else {
            val matchingPost = allPosts.firstOrNull { it.creatorId == targetUserId }
            User(
                id = targetUserId ?: "unknown",
                username = matchingPost?.creatorUsername ?: "creator",
                displayName = matchingPost?.creatorDisplayName ?: "Mtaa Creator",
                email = "creator@mtaatok.app",
                bio = matchingPost?.caption ?: "Dropping real Mtaa heat 🎥",
                avatarUrl = matchingPost?.creatorAvatarUrl ?: "",
                followersCount = 4820,
                followingCount = 310,
                totalLikesCount = matchingPost?.likesCount ?: 2100,
                isVerified = true,
                isFollowing = matchingPost?.isFollowingCreator ?: false
            )
        }
    }

    val userVideos = remember(allPosts, displayedUser.id) {
        if (isSelfProfile) {
            allPosts.filter { it.creatorId == displayedUser.id || it.creatorUsername == displayedUser.username }
        } else {
            allPosts.filter { it.creatorId == displayedUser.id }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Videos, 1 = Saved, 2 = Liked
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    var playingVideoPost by remember { mutableStateOf<VideoPost?>(null) }
    var showPrivacyControlsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C10))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("profile_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "@${displayedUser.username}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (isSelfProfile) {
                    IconButton(
                        onClick = { showSettingsModal = true },
                        modifier = Modifier.testTag("profile_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // User Info Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = displayedUser.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200" },
                        contentDescription = displayedUser.displayName,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF242432)),
                        contentScale = ContentScale.Crop
                    )
                    if (displayedUser.isVerified) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Display Name
                Text(
                    text = displayedUser.displayName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bio
                Text(
                    text = displayedUser.bio,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row (Following, Followers, Likes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(count = displayedUser.followingCount.toString(), label = "Following")
                    ProfileStatItem(count = formatNumber(displayedUser.followersCount), label = "Followers")
                    ProfileStatItem(count = formatNumber(displayedUser.totalLikesCount), label = "Likes")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button (Edit Profile or Follow/Following)
                if (isSelfProfile) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showEditProfileDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222232)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onNavigateToDashboard,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("creator_dashboard_nav_button")
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { repository.toggleFollow(displayedUser.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (displayedUser.isFollowing) Color(0xFF222232) else Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = if (displayedUser.isFollowing) "Following" else "Follow",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs Row (Videos, Saved, Liked)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF14141D),
                contentColor = Color(0xFFFF5722),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFFF5722),
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GridOn, contentDescription = "Videos", modifier = Modifier.size(20.dp)) },
                    text = { Text("Videos (${userVideos.size})", fontSize = 12.sp) }
                )
                if (isSelfProfile) {
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved", modifier = Modifier.size(20.dp)) },
                        text = { Text("Saved (${savedPosts.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Liked", modifier = Modifier.size(20.dp)) },
                        text = { Text("Liked (${likedPosts.size})", fontSize = 12.sp) }
                    )
                }
            }

            // Grid Content
            val currentGridItems = when (selectedTab) {
                0 -> userVideos
                1 -> savedPosts
                2 -> likedPosts
                else -> userVideos
            }

            if (currentGridItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 0) "No videos posted yet" else "No saved videos yet",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(2.dp)
                ) {
                    items(currentGridItems, key = { it.id }) { post ->
                        ProfileVideoGridItem(
                            post = post,
                            onClick = { playingVideoPost = post }
                        )
                    }
                }
            }
        }

        // Full-screen real video player dialog when tapping a grid item
        playingVideoPost?.let { post ->
            Dialog(
                onDismissRequest = { playingVideoPost = null },
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

                    // Close button
                    IconButton(
                        onClick = { playingVideoPost = null },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    // Caption overlay
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

        // Edit Profile Modal
        if (showEditProfileDialog) {
            EditProfileDialog(
                user = displayedUser,
                onDismiss = { showEditProfileDialog = false },
                onSave = { newName, newBio, newAvatar ->
                    repository.sessionManager.updateProfile(newName, newBio, newAvatar)
                    showEditProfileDialog = false
                }
            )
        }

        // Settings & Privacy BottomSheet
        if (showSettingsModal) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsModal = false },
                containerColor = Color(0xFF181824)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("Settings & Privacy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsRow(
                        icon = Icons.Default.MonetizationOn,
                        title = "Creator Monetization & Earnings",
                        onClick = {
                            showSettingsModal = false
                            onNavigateToDashboard()
                        }
                    )

                    SettingsRow(
                        icon = Icons.Default.Security,
                        title = "Privacy & Content Controls",
                        onClick = {
                            showSettingsModal = false
                            showPrivacyControlsDialog = true
                        }
                    )

                    SettingsRow(
                        icon = Icons.Default.Logout,
                        title = "Log Out of MtaaTok",
                        iconColor = Color(0xFFFF5252),
                        textColor = Color(0xFFFF5252),
                        onClick = {
                            showSettingsModal = false
                            repository.sessionManager.logout()
                            onLogout()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Privacy Controls Dialog
        if (showPrivacyControlsDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyControlsDialog = false },
                title = { Text("Privacy Controls") },
                text = {
                    Column {
                        Text("• Account Privacy: Public Creator Mode Active")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Direct Messages: Everyone can message")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Video Downloads: Allowed on your posts")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Moderation: Automated filter for offensive comments enabled")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyControlsDialog = false }) {
                        Text("Done", color = Color(0xFFFF5722))
                    }
                },
                containerColor = Color(0xFF1E1E2C),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}

@Composable
private fun ProfileVideoGridItem(
    post: VideoPost,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(1.dp)
            .aspectRatio(0.75f)
            .background(Color(0xFF1A1A26))
            .clickable { onClick() }
    ) {
        // Thumbnail or video preview
        AsyncImage(
            model = post.creatorAvatarUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200" },
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )

        // View count badge
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = formatNumber(post.viewsCount),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconColor: Color = Color.White,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (displayName: String, bio: String, avatarUrl: String) -> Unit
) {
    var displayName by remember { mutableStateOf(user.displayName) }
    var bio by remember { mutableStateOf(user.bio) }
    var avatarUrl by remember { mutableStateOf(user.avatarUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF333346),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF333346),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(displayName, bio, avatarUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.LightGray)
            }
        },
        containerColor = Color(0xFF1C1C2A)
    )
}

private fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> "%.1fM".format(number / 1_000_000.0)
        number >= 1_000 -> "%.1fK".format(number / 1000.0)
        else -> number.toString()
    }
}
