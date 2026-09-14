package com.example.mtaatok.ui.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.mtaatok.model.LiveStream
import com.example.mtaatok.player.MtaaVideoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class LiveChatMessage(
    val id: String,
    val username: String,
    val message: String,
    val isSystem: Boolean = false
)

@Composable
fun LiveScreen(
    repository: MtaaTokRepository,
    onNavigateToProfile: (String) -> Unit
) {
    val liveStreams by repository.liveStreams.collectAsState()
    val monetizationConfig by repository.monetizationConfig.collectAsState()

    var activeStreamToWatch by remember { mutableStateOf<LiveStream?>(null) }
    var showStartLiveDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C10))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("live_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF2A6D))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("LIVE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mtaa Streams", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Button(
                    onClick = { showStartLiveDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("start_live_button")
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Go Live", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Notice about viewer milestones & creator monetization
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1729)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Viewer Milestone Policy",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Milestones celebrate community growth (e.g. 999 viewers alert). Earnings are subject to creator program verification.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Streams list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(liveStreams, key = { it.id }) { stream ->
                    LiveStreamCard(
                        stream = stream,
                        onClick = { activeStreamToWatch = stream }
                    )
                }
            }
        }

        // Watching Active Live Room Dialog
        activeStreamToWatch?.let { stream ->
            LiveStreamRoomDialog(
                stream = stream,
                targetMilestone = monetizationConfig.liveMilestoneTarget,
                onDismiss = { activeStreamToWatch = null },
                onFollow = { repository.toggleFollow(stream.creatorId) }
            )
        }

        // Start Live Dialog
        if (showStartLiveDialog) {
            StartLiveDialog(
                onDismiss = { showStartLiveDialog = false },
                onStart = { title, tags ->
                    val newStream = repository.startLiveStream(title, tags)
                    showStartLiveDialog = false
                    activeStreamToWatch = newStream
                }
            )
        }
    }
}

@Composable
private fun LiveStreamCard(
    stream: LiveStream,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = stream.creatorAvatarUrl,
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
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Live badge & viewers count top row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF2A6D))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${stream.viewerCount} watching", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Streamer details bottom row
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = stream.creatorAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = stream.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "@${stream.creatorUsername}",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStreamRoomDialog(
    stream: LiveStream,
    targetMilestone: Int,
    onDismiss: () -> Unit,
    onFollow: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentViewers by remember { mutableIntStateOf(stream.viewerCount) }
    var milestoneCelebrationVisible by remember { mutableStateOf(false) }
    var isFollowingCreator by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            LiveChatMessage("c1", "babu_fan", "Let's goooo! Tunapiga shoo leo 🔥"),
            LiveChatMessage("c2", "zari_crew", "Queen on screen! Watching from Kilimani 👑"),
            LiveChatMessage("c3", "nairobi_skater", "Sound is crisp!! Drop the track name bro")
        )
    }

    // Dynamic viewers increment simulation & milestone detection
    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            currentViewers += (3..8).random()
            if (currentViewers >= targetMilestone && !milestoneCelebrationVisible) {
                milestoneCelebrationVisible = true
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .testTag("live_room_screen")
        ) {
            // Live Stream Video Player
            MtaaVideoPlayer(
                videoUrl = stream.streamUrl,
                isPlaying = true,
                isMuted = false,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top Stream Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Host pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = stream.creatorAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("@${stream.creatorUsername}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$currentViewers viewers", color = Color(0xFF00E5FF), fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isFollowingCreator = !isFollowingCreator
                            onFollow()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowingCreator) Color(0xFF333346) else Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(if (isFollowingCreator) "Following" else "Follow", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Default.Flag, contentDescription = "Report Live", tint = Color.LightGray)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Live", tint = Color.White)
                    }
                }
            }

            // Configurable 999+ Viewer Milestone Celebration Banner
            AnimatedVisibility(
                visible = milestoneCelebrationVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp, start = 16.dp, end = 16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF281C3D).copy(alpha = 0.95f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎉 VIEWER MILESTONE REACHED: $targetMilestone+ VIEWERS!",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This live stream is featured on the MtaaTok Discover Radar. Monetization eligibility is subject to verification.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Live Chat Messages (Bottom Left)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 72.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.height(180.dp),
                    reverseLayout = true
                ) {
                    items(chatMessages.reversed()) { msg ->
                        Row(
                            modifier = Modifier
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "@${msg.username}: ",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = msg.message,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Comment in live...", color = Color.Gray, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1D1D28),
                        unfocusedContainerColor = Color(0xFF1D1D28)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            chatMessages.add(LiveChatMessage("c_${UUID.randomUUID()}", "me", chatInput))
                            chatInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFF5722), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Heart tap
                IconButton(
                    onClick = {
                        chatMessages.add(LiveChatMessage("c_${UUID.randomUUID()}", "me", "Sent ❤️ to host!"))
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFF2A6D), CircleShape)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = "Send Heart", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Report Dialog
            if (showReportDialog) {
                AlertDialog(
                    onDismissRequest = { showReportDialog = false },
                    title = { Text("Report Live Stream") },
                    text = {
                        Text("Are you sure you want to report this stream? Our moderation team reviews reported streams in real time.")
                    },
                    confirmButton = {
                        Button(
                            onClick = { showReportDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text("Submit Report")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1C1C28),
                    titleContentColor = Color.White,
                    textContentColor = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun StartLiveDialog(
    onDismiss: () -> Unit,
    onStart: (title: String, tags: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var tagInput by remember { mutableStateOf("MtaaVibes,LiveMusic") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Live Stream", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Engage with your Mtaa community in real time!", color = Color.LightGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Stream Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF333346),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("Tags (comma separated)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
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
                onClick = {
                    val finalTitle = title.ifBlank { "Live Mtaa Session with Community 🎙️" }
                    val tags = tagInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onStart(finalTitle, tags)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
            ) {
                Text("Go Live Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF181826)
    )
}
