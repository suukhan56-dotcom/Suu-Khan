package com.example.mtaatok.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mtaatok.model.AppNotification
import com.example.mtaatok.model.Comment
import com.example.mtaatok.model.NotificationType
import com.example.mtaatok.model.VideoPost

@Entity(tableName = "video_posts")
data class VideoPostEntity(
    @PrimaryKey val id: String,
    val creatorId: String,
    val creatorUsername: String,
    val creatorDisplayName: String,
    val creatorAvatarUrl: String,
    val videoUrl: String,
    val caption: String,
    val hashtagsCsv: String,
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
    val visibility: String = "Public",
    val filterEffect: String = "Normal",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toModel(): VideoPost = VideoPost(
        id = id,
        creatorId = creatorId,
        creatorUsername = creatorUsername,
        creatorDisplayName = creatorDisplayName,
        creatorAvatarUrl = creatorAvatarUrl,
        videoUrl = videoUrl,
        caption = caption,
        hashtags = if (hashtagsCsv.isBlank()) emptyList() else hashtagsCsv.split(","),
        musicTitle = musicTitle,
        musicArtist = musicArtist,
        likesCount = likesCount,
        commentsCount = commentsCount,
        sharesCount = sharesCount,
        savesCount = savesCount,
        viewsCount = viewsCount,
        isLiked = isLiked,
        isSaved = isSaved,
        isFollowingCreator = isFollowingCreator,
        allowsDownload = allowsDownload,
        visibility = visibility,
        filterEffect = filterEffect,
        timestamp = timestamp
    )

    companion object {
        fun fromModel(model: VideoPost): VideoPostEntity = VideoPostEntity(
            id = model.id,
            creatorId = model.creatorId,
            creatorUsername = model.creatorUsername,
            creatorDisplayName = model.creatorDisplayName,
            creatorAvatarUrl = model.creatorAvatarUrl,
            videoUrl = model.videoUrl,
            caption = model.caption,
            hashtagsCsv = model.hashtags.joinToString(","),
            musicTitle = model.musicTitle,
            musicArtist = model.musicArtist,
            likesCount = model.likesCount,
            commentsCount = model.commentsCount,
            sharesCount = model.sharesCount,
            savesCount = model.savesCount,
            viewsCount = model.viewsCount,
            isLiked = model.isLiked,
            isSaved = model.isSaved,
            isFollowingCreator = model.isFollowingCreator,
            allowsDownload = model.allowsDownload,
            visibility = model.visibility,
            filterEffect = model.filterEffect,
            timestamp = model.timestamp
        )
    }
}

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val username: String,
    val userAvatarUrl: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val replyCount: Int = 0
) {
    fun toModel(): Comment = Comment(
        id = id,
        postId = postId,
        userId = userId,
        username = username,
        userAvatarUrl = userAvatarUrl,
        text = text,
        timestamp = timestamp,
        likesCount = likesCount,
        isLiked = isLiked,
        replyCount = replyCount
    )

    companion object {
        fun fromModel(model: Comment): CommentEntity = CommentEntity(
            id = model.id,
            postId = model.postId,
            userId = model.userId,
            username = model.username,
            userAvatarUrl = model.userAvatarUrl,
            text = model.text,
            timestamp = model.timestamp,
            likesCount = model.likesCount,
            isLiked = model.isLiked,
            replyCount = model.replyCount
        )
    }
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val typeName: String,
    val title: String,
    val message: String,
    val userId: String,
    val userAvatarUrl: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetPostId: String? = null
) {
    fun toModel(): AppNotification = AppNotification(
        id = id,
        type = try { NotificationType.valueOf(typeName) } catch (e: Exception) { NotificationType.SYSTEM },
        title = title,
        message = message,
        userId = userId,
        userAvatarUrl = userAvatarUrl,
        timestamp = timestamp,
        isRead = isRead,
        targetPostId = targetPostId
    )

    companion object {
        fun fromModel(model: AppNotification): NotificationEntity = NotificationEntity(
            id = model.id,
            typeName = model.type.name,
            title = model.title,
            message = model.message,
            userId = model.userId,
            userAvatarUrl = model.userAvatarUrl,
            timestamp = model.timestamp,
            isRead = model.isRead,
            targetPostId = model.targetPostId
        )
    }
}
