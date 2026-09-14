package com.example.mtaatok.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.mtaatok.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mtaatok_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val hasInitializedFirstRun = prefs.getBoolean(KEY_FIRST_RUN, false)

        if (isLoggedIn || !hasInitializedFirstRun) {
            // First run or logged in: set up persistent active session
            val userId = prefs.getString(KEY_USER_ID, "user_mtaa_alpha") ?: "user_mtaa_alpha"
            val username = prefs.getString(KEY_USERNAME, "mtaacreator") ?: "mtaacreator"
            val displayName = prefs.getString(KEY_DISPLAY_NAME, "Mtaa Creator") ?: "Mtaa Creator"
            val email = prefs.getString(KEY_EMAIL, "creator@mtaatok.app") ?: "creator@mtaatok.app"
            val bio = prefs.getString(KEY_BIO, "Nairobi Street Culture, Visuals & Music 🔥🇰🇪") ?: "Nairobi Street Culture, Visuals & Music 🔥🇰🇪"
            val avatarUrl = prefs.getString(KEY_AVATAR_URL, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200") ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"
            val followers = prefs.getInt(KEY_FOLLOWERS, 1420)
            val following = prefs.getInt(KEY_FOLLOWING, 230)
            val likes = prefs.getInt(KEY_LIKES, 8920)

            val user = User(
                id = userId,
                username = username,
                displayName = displayName,
                email = email,
                bio = bio,
                avatarUrl = avatarUrl,
                followersCount = followers,
                followingCount = following,
                totalLikesCount = likes,
                isVerified = true,
                isCreatorEligible = true
            )
            _currentUser.value = user
            saveUserSession(user)
            prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply()
        } else {
            _currentUser.value = null
        }
    }

    fun saveUserSession(user: User) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_FIRST_RUN, true)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_BIO, user.bio)
            .putString(KEY_AVATAR_URL, user.avatarUrl)
            .putInt(KEY_FOLLOWERS, user.followersCount)
            .putInt(KEY_FOLLOWING, user.followingCount)
            .putInt(KEY_LIKES, user.totalLikesCount)
            .apply()

        _currentUser.value = user
    }

    fun updateProfile(displayName: String, bio: String, avatarUrl: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            displayName = displayName,
            bio = bio,
            avatarUrl = if (avatarUrl.isNotBlank()) avatarUrl else current.avatarUrl
        )
        saveUserSession(updated)
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        _currentUser.value = null
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_FIRST_RUN = "key_first_run"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USERNAME = "key_username"
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_BIO = "key_bio"
        private const val KEY_AVATAR_URL = "key_avatar_url"
        private const val KEY_FOLLOWERS = "key_followers"
        private const val KEY_FOLLOWING = "key_following"
        private const val KEY_LIKES = "key_likes"
    }
}
