package com.example.mtaatok.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MtaaTokDao {
    @Query("SELECT * FROM video_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<VideoPostEntity>>

    @Query("SELECT * FROM video_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: String): VideoPostEntity?

    @Query("SELECT * FROM video_posts WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedPosts(): Flow<List<VideoPostEntity>>

    @Query("SELECT * FROM video_posts WHERE isLiked = 1 ORDER BY timestamp DESC")
    fun getLikedPosts(): Flow<List<VideoPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: VideoPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<VideoPostEntity>)

    @Update
    suspend fun updatePost(post: VideoPostEntity)

    // Comments
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Query("SELECT * FROM comments WHERE id = :id LIMIT 1")
    suspend fun getCommentById(id: String): CommentEntity?

    @Update
    suspend fun updateComment(comment: CommentEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)
}
