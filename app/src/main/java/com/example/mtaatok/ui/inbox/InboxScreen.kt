package com.example.mtaatok.ui.inbox

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mtaatok.data.repository.MtaaTokRepository
import com.example.mtaatok.model.AppNotification
import com.example.mtaatok.model.ChatMessage
import com.example.mtaatok.model.NotificationType

@Composable
fun InboxScreen(
    repository: MtaaTokRepository,
    onNavigateToProfile: (String) -> Unit
) {
    var selectedMainTab by remember { mutableIntStateOf(0) } // 0 = Activity, 1 = Messages
    val notifications by repository.getNotifications().collectAsState(initial = emptyList())
    val directMessages by repository.directMessages.collectAsState()

    var activeChatPartner by remember { mutableStateOf<String?>(null) } // partner id/name
    var activeChatPartnerName by remember { mutableStateOf("") }
    var activeChatPartnerAvatar by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C10))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("inbox_screen")
    ) {
        if (activeChatPartner != null) {
            // Direct Message Conversation Screen
            DirectChatView(
                partnerName = activeChatPartnerName,
                partnerAvatar = activeChatPartnerAvatar,
                messages = directMessages,
                onSendMessage = { text ->
                    repository.sendDirectMessage("me", activeChatPartner ?: "user", text)
                },
                onBack = { activeChatPartner = null }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header with Tabs
                TabRow(
                    selectedTabIndex = selectedMainTab,
                    containerColor = Color(0xFF14141D),
                    contentColor = Color(0xFFFF5722),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                            color = Color(0xFFFF5722),
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedMainTab == 0,
                        onClick = { selectedMainTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        },
                        modifier = Modifier.testTag("inbox_tab_notifications")
                    )
                    Tab(
                        selected = selectedMainTab == 1,
                        onClick = { selectedMainTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Messages (${directMessages.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        },
                        modifier = Modifier.testTag("inbox_tab_messages")
                    )
                }

                if (selectedMainTab == 0) {
                    NotificationsListView(
                        notifications = notifications,
                        onNotificationClick = { notif ->
                            repository.markNotificationAsRead(notif.id)
                        }
                    )
                } else {
                    DirectMessagesListView(
                        messages = directMessages,
                        onOpenChat = { name, avatar ->
                            activeChatPartner = name
                            activeChatPartnerName = name
                            activeChatPartnerAvatar = avatar
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsListView(
    notifications: List<AppNotification>,
    onNotificationClick: (AppNotification) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Likes", "Comments", "Followers", "System")

    val filteredList = remember(notifications, selectedFilter) {
        if (selectedFilter == "All") notifications else {
            notifications.filter {
                when (selectedFilter) {
                    "Likes" -> it.type == NotificationType.LIKE
                    "Comments" -> it.type == NotificationType.COMMENT
                    "Followers" -> it.type == NotificationType.FOLLOW
                    "System" -> it.type == NotificationType.SYSTEM
                    else -> true
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFFFF5722) else Color(0xFF1B1B26))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications in this category", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { notif ->
                    NotificationRow(notif = notif, onClick = { onNotificationClick(notif) })
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notif: AppNotification,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notif.isRead) Color(0xFF14141E) else Color(0xFF1F1D2B)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = notif.userAvatarUrl.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF28283C)),
                    contentScale = ContentScale.Crop
                )

                // Type badge
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            when (notif.type) {
                                NotificationType.LIKE -> Color(0xFFFF2A6D)
                                NotificationType.COMMENT -> Color(0xFF00E5FF)
                                NotificationType.FOLLOW -> Color(0xFFFF5722)
                                NotificationType.SYSTEM -> Color(0xFFFFD700)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (notif.type) {
                            NotificationType.LIKE -> Icons.Default.Favorite
                            NotificationType.COMMENT -> Icons.Default.ModeComment
                            NotificationType.FOLLOW -> Icons.Default.PersonAdd
                            NotificationType.SYSTEM -> Icons.Default.Campaign
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notif.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notif.message,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!notif.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFFF5722), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun DirectMessagesListView(
    messages: List<ChatMessage>,
    onOpenChat: (username: String, avatar: String) -> Unit
) {
    val sampleConversations = listOf(
        Pair("babu_skater", "Yo bro, which camera did you use for the Nairobi skate clip?"),
        Pair("zari_official", "Loved your new Mtaa choreography!! Collab soon?"),
        Pair("eastlands_beatmaker", "Sent you two exclusive beats on email, check them out! 🎧")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(sampleConversations) { (username, lastMsg) ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onOpenChat(username, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100")
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF28283C)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("@$username", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(lastMsg, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF222232))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Chat", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectChatView(
    partnerName: String,
    partnerAvatar: String,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14141E))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            AsyncImage(
                model = partnerAvatar.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100" },
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("@$partnerName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Online on MtaaTok", color = Color(0xFF00E5FF), fontSize = 11.sp)
            }
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == "me"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                )
                            )
                            .background(if (isMe) Color(0xFFFF5722) else Color(0xFF222232))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Input field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14141E))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Type a message...", color = Color.Gray, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF5722),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF1E1E2C),
                    unfocusedContainerColor = Color(0xFF1E1E2C)
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFFF5722), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
