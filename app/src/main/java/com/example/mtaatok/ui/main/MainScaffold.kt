package com.example.mtaatok.ui.main

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtaatok.data.repository.MtaaTokRepository
import com.example.mtaatok.ui.creator.CreatorDashboardScreen
import com.example.mtaatok.ui.discover.DiscoverScreen
import com.example.mtaatok.ui.feed.FeedScreen
import com.example.mtaatok.ui.inbox.InboxScreen
import com.example.mtaatok.ui.live.LiveScreen
import com.example.mtaatok.ui.post.PostVideoScreen
import com.example.mtaatok.ui.profile.ProfileScreen

enum class MainTab {
    FEED, DISCOVER, POST, INBOX, LIVE, PROFILE, CREATOR_DASHBOARD
}

@Composable
fun MainScaffold(
    repository: MtaaTokRepository,
    onLogout: () -> Unit
) {
    var currentTab by remember { mutableStateOf(MainTab.FEED) }
    var selectedUserProfileId by remember { mutableStateOf<String?>(null) }
    var discoverInitialQuery by remember { mutableStateOf("") }

    val notifications by repository.getNotifications().collectAsState(initial = emptyList())
    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    Scaffold(
        bottomBar = {
            // Hide bottom bar when posting or inside creator dashboard
            if (currentTab != MainTab.POST && currentTab != MainTab.CREATOR_DASHBOARD) {
                MtaaBottomNavigationBar(
                    currentTab = currentTab,
                    unreadCount = unreadNotificationsCount,
                    onSelectTab = { tab ->
                        if (tab == MainTab.PROFILE) {
                            selectedUserProfileId = null
                        }
                        currentTab = tab
                    }
                )
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (currentTab != MainTab.POST && currentTab != MainTab.CREATOR_DASHBOARD) 56.dp else 0.dp
                )
        ) {
            when (currentTab) {
                MainTab.FEED -> {
                    FeedScreen(
                        repository = repository,
                        onNavigateToProfile = { userId ->
                            selectedUserProfileId = userId
                            currentTab = MainTab.PROFILE
                        },
                        onHashtagClick = { hashtag ->
                            discoverInitialQuery = "#$hashtag"
                            currentTab = MainTab.DISCOVER
                        }
                    )
                }

                MainTab.DISCOVER -> {
                    DiscoverScreen(
                        repository = repository,
                        initialQuery = discoverInitialQuery,
                        onNavigateToProfile = { userId ->
                            selectedUserProfileId = userId
                            currentTab = MainTab.PROFILE
                        }
                    )
                }

                MainTab.POST -> {
                    PostVideoScreen(
                        repository = repository,
                        onPostSuccess = {
                            currentTab = MainTab.FEED
                        },
                        onCancel = {
                            currentTab = MainTab.FEED
                        }
                    )
                }

                MainTab.INBOX -> {
                    InboxScreen(
                        repository = repository,
                        onNavigateToProfile = { userId ->
                            selectedUserProfileId = userId
                            currentTab = MainTab.PROFILE
                        }
                    )
                }

                MainTab.LIVE -> {
                    LiveScreen(
                        repository = repository,
                        onNavigateToProfile = { userId ->
                            selectedUserProfileId = userId
                            currentTab = MainTab.PROFILE
                        }
                    )
                }

                MainTab.PROFILE -> {
                    ProfileScreen(
                        repository = repository,
                        targetUserId = selectedUserProfileId,
                        onNavigateToDashboard = {
                            currentTab = MainTab.CREATOR_DASHBOARD
                        },
                        onLogout = onLogout
                    )
                }

                MainTab.CREATOR_DASHBOARD -> {
                    CreatorDashboardScreen(
                        repository = repository,
                        onBack = {
                            currentTab = MainTab.PROFILE
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MtaaBottomNavigationBar(
    currentTab: MainTab,
    unreadCount: Int,
    onSelectTab: (MainTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF09090D))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Home / Feed Tab
            BottomNavItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = currentTab == MainTab.FEED,
                testTag = "nav_tab_home",
                onClick = { onSelectTab(MainTab.FEED) }
            )

            // 2. Discover Tab
            BottomNavItem(
                title = "Discover",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                isSelected = currentTab == MainTab.DISCOVER,
                testTag = "nav_tab_discover",
                onClick = { onSelectTab(MainTab.DISCOVER) }
            )

            // 3. Central Post Button (+ Mtaa Button)
            Box(
                modifier = Modifier
                    .size(width = 46.dp, height = 32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF00E5FF),
                                Color(0xFFFF5722),
                                Color(0xFFFF2A6D)
                            )
                        )
                    )
                    .clickable { onSelectTab(MainTab.POST) }
                    .testTag("nav_tab_post"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Post Video",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 4. Live Tab
            BottomNavItem(
                title = "Live",
                selectedIcon = Icons.Filled.Videocam,
                unselectedIcon = Icons.Outlined.Videocam,
                isSelected = currentTab == MainTab.LIVE,
                testTag = "nav_tab_live",
                onClick = { onSelectTab(MainTab.LIVE) }
            )

            // 5. Inbox Tab (with badge)
            BottomNavItem(
                title = "Inbox",
                selectedIcon = Icons.Filled.Chat,
                unselectedIcon = Icons.Outlined.Chat,
                isSelected = currentTab == MainTab.INBOX,
                badgeCount = unreadCount,
                testTag = "nav_tab_inbox",
                onClick = { onSelectTab(MainTab.INBOX) }
            )

            // 6. Profile Tab
            BottomNavItem(
                title = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                isSelected = currentTab == MainTab.PROFILE,
                testTag = "nav_tab_profile",
                onClick = { onSelectTab(MainTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = Color(0xFFFF2A6D),
                        contentColor = Color.White
                    ) {
                        Text(if (badgeCount > 9) "9+" else badgeCount.toString(), fontSize = 9.sp)
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = title,
                tint = if (isSelected) Color.White else Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
