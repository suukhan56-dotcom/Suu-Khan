package com.example.mtaatok.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String,
    val bio: String = "",
    val avatarUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val totalLikesCount: Int = 0,
    val isVerified: Boolean = false,
    val isFollowing: Boolean = false,
    val isCreatorEligible: Boolean = true
)

data class VideoPost(
    val id: String,
    val creatorId: String,
    val creatorUsername: String,
    val creatorDisplayName: String,
    val creatorAvatarUrl: String,
    val videoUrl: String,
    val caption: String,
    val hashtags: List<String>,
    val musicTitle: String,
    val musicArtist: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val savesCount: Int = 0,
    val viewsCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isFollowingCreator: Boolean = false,
    val allowsDownload: Boolean = true,
    val visibility: String = "Public", // Public, Followers, Private
    val filterEffect: String = "Normal",
    val timestamp: Long = System.currentTimeMillis()
)

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val userAvatarUrl: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replyCount: Int = 0
)

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val coverUrl: String,
    val audioUrl: String
)

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, SYSTEM
}

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val userId: String,
    val userAvatarUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetPostId: String? = null
)

data class LiveStream(
    val id: String,
    val creatorId: String,
    val creatorUsername: String,
    val creatorDisplayName: String,
    val creatorAvatarUrl: String,
    val title: String,
    val viewerCount: Int = 980,
    val streamUrl: String,
    val tags: List<String> = listOf("MtaaVibes", "Live"),
    val isLive: Boolean = true,
    val milestoneCelebrated: Boolean = false
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MonetizationConfig(
    val minimumWithdrawalAmount: Double = 50.0,
    val liveMilestoneTarget: Int = 999,
    val estimatedRpmRate: Double = 0.45,
    val requiresVerification: Boolean = true
)
