package com.example.mtaatok.data.repository

import com.example.mtaatok.data.local.CommentEntity
import com.example.mtaatok.data.local.MtaaTokDao
import com.example.mtaatok.data.local.NotificationEntity
import com.example.mtaatok.data.local.SessionManager
import com.example.mtaatok.data.local.VideoPostEntity
import com.example.mtaatok.model.AppNotification
import com.example.mtaatok.model.ChatMessage
import com.example.mtaatok.model.Comment
import com.example.mtaatok.model.LiveStream
import com.example.mtaatok.model.MonetizationConfig
import com.example.mtaatok.model.MusicTrack
import com.example.mtaatok.model.NotificationType
import com.example.mtaatok.model.VideoPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class MtaaTokRepository(
    private val dao: MtaaTokDao,
    val sessionManager: SessionManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _liveStreams = MutableStateFlow<List<LiveStream>>(emptyList())
    val liveStreams: StateFlow<List<LiveStream>> = _liveStreams.asStateFlow()

    private val _directMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val directMessages: StateFlow<List<ChatMessage>> = _directMessages.asStateFlow()

    private val _monetizationConfig = MutableStateFlow(MonetizationConfig())
    val monetizationConfig: StateFlow<MonetizationConfig> = _monetizationConfig.asStateFlow()

    val royaltyFreeTracks = listOf(
        MusicTrack(
            id = "track_1",
            title = "Nairobi Sunset Groove",
            artist = "Dj Shoki",
            durationSeconds = 60,
            coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=100",
            audioUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        ),
        MusicTrack(
            id = "track_2",
            title = "Eastlands Drill Trap",
            artist = "Mtaa Sound Lab",
            durationSeconds = 45,
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=100",
            audioUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        MusicTrack(
            id = "track_3",
            title = "Afro Beat Elevation",
            artist = "Kilimani Chill",
            durationSeconds = 50,
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=100",
            audioUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        )
    )

    init {
        scope.launch {
            seedSampleDataIfEmpty()
        }
    }

    fun getAllPosts(): Flow<List<VideoPost>> = dao.getAllPosts().map { list -> list.map { it.toModel() } }

    fun getSavedPosts(): Flow<List<VideoPost>> = dao.getSavedPosts().map { list -> list.map { it.toModel() } }

    fun getLikedPosts(): Flow<List<VideoPost>> = dao.getLikedPosts().map { list -> list.map { it.toModel() } }

    fun getCommentsForPost(postId: String): Flow<List<Comment>> =
        dao.getCommentsForPost(postId).map { list -> list.map { it.toModel() } }

    fun getNotifications(): Flow<List<AppNotification>> =
        dao.getAllNotifications().map { list -> list.map { it.toModel() } }

    fun toggleLike(postId: String) {
        scope.launch {
            val entity = dao.getPostById(postId) ?: return@launch
            val isLikedNow = !entity.isLiked
            val countChange = if (isLikedNow) 1 else -1
            val updated = entity.copy(
                isLiked = isLikedNow,
                likesCount = maxOf(0, entity.likesCount + countChange)
            )
            dao.updatePost(updated)

            if (isLikedNow) {
                dao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID()}",
                        typeName = NotificationType.LIKE.name,
                        title = "New Like on your post!",
                        message = "Someone in Mtaa enjoyed your video",
                        userId = entity.creatorId,
                        userAvatarUrl = entity.creatorAvatarUrl,
                        targetPostId = postId
                    )
                )
            }
        }
    }

    fun toggleSave(postId: String) {
        scope.launch {
            val entity = dao.getPostById(postId) ?: return@launch
            val isSavedNow = !entity.isSaved
            val countChange = if (isSavedNow) 1 else -1
            val updated = entity.copy(
                isSaved = isSavedNow,
                savesCount = maxOf(0, entity.savesCount + countChange)
            )
            dao.updatePost(updated)
        }
    }

    fun toggleFollow(creatorId: String) {
        scope.launch {
            val posts = dao.getAllPosts().firstOrNull() ?: return@launch
            posts.filter { it.creatorId == creatorId }.forEach { post ->
                val newStatus = !post.isFollowingCreator
                dao.updatePost(post.copy(isFollowingCreator = newStatus))
            }
        }
    }

    fun addComment(postId: String, text: String) {
        val user = sessionManager.currentUser.value ?: return
        scope.launch {
            val commentEntity = CommentEntity(
                id = "comm_${UUID.randomUUID()}",
                postId = postId,
                userId = user.id,
                username = user.username,
                userAvatarUrl = user.avatarUrl,
                text = text,
                timestamp = System.currentTimeMillis(),
                likesCount = 0,
                isLiked = false,
                replyCount = 0
            )
            dao.insertComment(commentEntity)

            val post = dao.getPostById(postId)
            if (post != null) {
                dao.updatePost(post.copy(commentsCount = post.commentsCount + 1))
            }
        }
    }

    fun toggleCommentLike(commentId: String) {
        scope.launch {
            val comment = dao.getCommentById(commentId) ?: return@launch
            val isLiked = !comment.isLiked
            val count = if (isLiked) comment.likesCount + 1 else maxOf(0, comment.likesCount - 1)
            dao.updateComment(comment.copy(isLiked = isLiked, likesCount = count))
        }
    }

    fun publishPost(
        videoUrl: String,
        caption: String,
        hashtags: List<String>,
        musicTitle: String,
        musicArtist: String,
        allowsDownload: Boolean = true,
        visibility: String = "Public",
        filterEffect: String = "Normal"
    ) {
        val user = sessionManager.currentUser.value
        scope.launch {
            val post = VideoPostEntity(
                id = "post_${UUID.randomUUID()}",
                creatorId = user?.id ?: "creator_me",
                creatorUsername = user?.username ?: "mtaacreator",
                creatorDisplayName = user?.displayName ?: "Mtaa Creator",
                creatorAvatarUrl = user?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                videoUrl = videoUrl,
                caption = caption,
                hashtagsCsv = hashtags.joinToString(","),
                musicTitle = musicTitle,
                musicArtist = musicArtist,
                likesCount = 0,
                commentsCount = 0,
                sharesCount = 0,
                savesCount = 0,
                viewsCount = 1,
                isLiked = false,
                isSaved = false,
                isFollowingCreator = false,
                allowsDownload = allowsDownload,
                visibility = visibility,
                filterEffect = filterEffect,
                timestamp = System.currentTimeMillis()
            )
            dao.insertPost(post)
        }
    }

    fun markNotificationAsRead(id: String) {
        scope.launch {
            dao.markNotificationAsRead(id)
        }
    }

    fun startLiveStream(title: String, tags: List<String>): LiveStream {
        val user = sessionManager.currentUser.value
        val stream = LiveStream(
            id = "live_${UUID.randomUUID()}",
            creatorId = user?.id ?: "creator_me",
            creatorUsername = user?.username ?: "mtaacreator",
            creatorDisplayName = user?.displayName ?: "Mtaa Creator",
            creatorAvatarUrl = user?.avatarUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
            title = title,
            viewerCount = 985,
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            tags = tags,
            isLive = true
        )
        _liveStreams.value = listOf(stream) + _liveStreams.value
        return stream
    }

    fun sendDirectMessage(senderId: String, receiverId: String, text: String) {
        val msg = ChatMessage(
            id = "msg_${UUID.randomUUID()}",
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        _directMessages.value = _directMessages.value + msg
    }

    fun updateMonetizationConfig(minAmount: Double, milestone: Int, rpm: Double) {
        _monetizationConfig.value = MonetizationConfig(
            minimumWithdrawalAmount = minAmount,
            liveMilestoneTarget = milestone,
            estimatedRpmRate = rpm
        )
    }

    private suspend fun seedSampleDataIfEmpty() {
        val existing = dao.getAllPosts().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val samplePosts = listOf(
                VideoPostEntity(
                    id = "post_nairobi_dance",
                    creatorId = "creator_zari",
                    creatorUsername = "zari_vibes",
                    creatorDisplayName = "Zari Njeri",
                    creatorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    caption = "Eastlands dance session with the crew! Pure energy in Nairobi today 🇰🇪🔥 Who is hitting this step?",
                    hashtagsCsv = "MtaaVibes,EastlandsDance,NairobiTok,GengetoneVibes",
                    musicTitle = "Nairobi Sunset Groove",
                    musicArtist = "Dj Shoki",
                    likesCount = 14200,
                    commentsCount = 890,
                    sharesCount = 2100,
                    savesCount = 940,
                    viewsCount = 88400,
                    isLiked = false,
                    isSaved = false,
                    isFollowingCreator = false,
                    allowsDownload = true,
                    visibility = "Public",
                    filterEffect = "Normal",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                VideoPostEntity(
                    id = "post_skate_street",
                    creatorId = "creator_babu",
                    creatorUsername = "babu_skater",
                    creatorDisplayName = "Babu Otieno",
                    creatorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    caption = "Kickflip down the Uhuru Park stairs! Third attempt was butter 🛹⚡️ Mtaa Tok skate culture taking over!",
                    hashtagsCsv = "SkateMtaa,UhuruPark,StreetStyle,SkateLife",
                    musicTitle = "Eastlands Drill Trap",
                    musicArtist = "Mtaa Sound Lab",
                    likesCount = 8950,
                    commentsCount = 420,
                    sharesCount = 1350,
                    savesCount = 670,
                    viewsCount = 52300,
                    isLiked = true,
                    isSaved = true,
                    isFollowingCreator = true,
                    allowsDownload = true,
                    visibility = "Public",
                    filterEffect = "Cyberpunk",
                    timestamp = System.currentTimeMillis() - 7200000
                ),
                VideoPostEntity(
                    id = "post_street_food",
                    creatorId = "creator_chefmwangi",
                    creatorUsername = "chef_mwangi",
                    creatorDisplayName = "Mwangi Bites",
                    creatorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    caption = "Making the ultimate smokie pasua with kachumbari & pili pili in Westlands! Best street food ever 🌭🌶️",
                    hashtagsCsv = "MtaaEats,SmokiePasua,NairobiFood,KenyanStreetFood",
                    musicTitle = "Afro Beat Elevation",
                    musicArtist = "Kilimani Chill",
                    likesCount = 6120,
                    commentsCount = 310,
                    sharesCount = 780,
                    savesCount = 410,
                    viewsCount = 34900,
                    isLiked = false,
                    isSaved = false,
                    isFollowingCreator = false,
                    allowsDownload = false,
                    visibility = "Public",
                    filterEffect = "Golden Hour",
                    timestamp = System.currentTimeMillis() - 12000000
                )
            )
            dao.insertPosts(samplePosts)

            // Seed sample comments
            val sampleComments = listOf(
                CommentEntity("c1", "post_nairobi_dance", "user_1", "juma_mtaa", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100", "Energy is unmatched bro!! 🔥🔥", System.currentTimeMillis() - 1800000, 42, false, 2),
                CommentEntity("c2", "post_nairobi_dance", "user_2", "amina_k", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100", "That footwork at 0:15 is legendary! 👏", System.currentTimeMillis() - 900000, 18, true, 0),
                CommentEntity("c3", "post_skate_street", "user_3", "kevo_skates", "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=100", "Cleeean pop over the ledge!!", System.currentTimeMillis() - 2500000, 29, true, 1)
            )
            dao.insertComments(sampleComments)

            // Seed sample notifications
            val sampleNotifs = listOf(
                NotificationEntity("n1", NotificationType.LIKE.name, "zari_vibes liked your video", "Your street vlog got a like", "creator_zari", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", System.currentTimeMillis() - 500000, false, "post_nairobi_dance"),
                NotificationEntity("n2", NotificationType.FOLLOW.name, "babu_skater started following you", "Check out their profile and videos", "creator_babu", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", System.currentTimeMillis() - 1500000, false, null),
                NotificationEntity("n3", NotificationType.SYSTEM.name, "Welcome to MtaaTok!", "Your creator profile is active. You are eligible for Creator Studio!", "system", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100", System.currentTimeMillis() - 86400000, true, null)
            )
            dao.insertNotifications(sampleNotifs)
        }

        // Initialize sample live streams
        _liveStreams.value = listOf(
            LiveStream(
                id = "live_zari",
                creatorId = "creator_zari",
                creatorUsername = "zari_vibes",
                creatorDisplayName = "Zari Njeri",
                creatorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                title = "Live Freestyle Dance Jam in Nairobi 💃🔥",
                viewerCount = 988,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                tags = listOf("Dance", "LiveJam", "Nairobi")
            ),
            LiveStream(
                id = "live_babu",
                creatorId = "creator_babu",
                creatorUsername = "babu_skater",
                creatorDisplayName = "Babu Otieno",
                creatorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                title = "Night Skate Session & Q&A 🛹🌙",
                viewerCount = 450,
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4",
                tags = listOf("Skate", "NightVibes")
            )
        )

        // Initialize sample direct messages
        _directMessages.value = listOf(
            ChatMessage("m1", "babu_skater", "me", "Yo bro, saw your post! Keep the vibes going!"),
            ChatMessage("m2", "me", "babu_skater", "Appreciate it my guy! Let's shoot some skate clips this weekend."),
            ChatMessage("m3", "babu_skater", "me", "Done deal! See you at Uhuru Park at 3pm 🛹")
        )
    }
}
