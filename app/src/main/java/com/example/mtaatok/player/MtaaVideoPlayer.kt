package com.example.mtaatok.player

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

@Composable
fun MtaaVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    filterEffect: String = "Normal",
    isPlaying: Boolean = true,
    isMuted: Boolean = false,
    playbackSpeed: Float = 1.0f,
    onSingleTap: () -> Unit = {},
    onDoubleTapLike: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isBuffering by remember { mutableStateOf(true) }
    var isPrepared by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableStateOf(0) }
    var isPausedManually by remember { mutableStateOf(false) }
    var showHeartBurst by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var surfaceObj by remember { mutableStateOf<Surface?>(null) }

    val filterPaint = remember(filterEffect) {
        val paint = Paint()
        val cm = ColorMatrix()
        when (filterEffect.lowercase()) {
            "cyberpunk" -> {
                cm.set(
                    floatArrayOf(
                        1.4f, 0f, 0.4f, 0f, 10f,
                        0f, 1.2f, 0.3f, 0f, 0f,
                        0.3f, 0f, 1.7f, 0f, 20f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "noir" -> {
                cm.setSaturation(0.0f)
            }
            "golden hour" -> {
                cm.set(
                    floatArrayOf(
                        1.3f, 0.1f, 0f, 0f, 20f,
                        0f, 1.1f, 0.1f, 0f, 10f,
                        0f, 0f, 0.7f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "emerald" -> {
                cm.set(
                    floatArrayOf(
                        0.8f, 0f, 0f, 0f, 0f,
                        0f, 1.4f, 0f, 0f, 15f,
                        0f, 0.2f, 1.1f, 0f, 10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            else -> {
                cm.reset()
            }
        }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        paint
    }

    DisposableEffect(videoUrl, retryTrigger) {
        isPrepared = false
        isBuffering = true
        hasPlaybackError = false

        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            isLooping = true
            setOnPreparedListener { mp ->
                isPrepared = true
                isBuffering = false
                hasPlaybackError = false
                if (isPlaying && !isPausedManually) {
                    runCatching { mp.start() }
                }
            }
            setOnBufferingUpdateListener { _, percent ->
                if (percent >= 99) {
                    isBuffering = false
                }
            }
            setOnErrorListener { _, _, _ ->
                isBuffering = false
                isPrepared = false
                hasPlaybackError = true
                true
            }
        }

        surfaceObj?.let { surface ->
            runCatching { player.setSurface(surface) }
        }

        try {
            val uri = Uri.parse(videoUrl)
            if (videoUrl.startsWith("http://") || videoUrl.startsWith("https://")) {
                val headers = mapOf("User-Agent" to "Mozilla/5.0 (Linux; Android 14) MtaaTok/1.0")
                player.setDataSource(context, uri, headers)
            } else {
                player.setDataSource(context, uri)
            }
            player.prepareAsync()
        } catch (e: Exception) {
            isBuffering = false
            isPrepared = false
            hasPlaybackError = true
        }

        mediaPlayer = player

        onDispose {
            isPrepared = false
            runCatching {
                if (player.isPlaying) {
                    player.pause()
                }
                player.reset()
                player.release()
            }
            mediaPlayer = null
        }
    }

    // React to play/pause updates safely only after prepared
    LaunchedEffect(isPlaying, isPausedManually, isPrepared) {
        if (!isPrepared) return@LaunchedEffect
        mediaPlayer?.let { mp ->
            runCatching {
                if (isPlaying && !isPausedManually) {
                    if (!mp.isPlaying) mp.start()
                } else {
                    if (mp.isPlaying) mp.pause()
                }
            }
        }
    }

    LaunchedEffect(isMuted, isPrepared) {
        if (!isPrepared) return@LaunchedEffect
        mediaPlayer?.let { mp ->
            runCatching {
                val vol = if (isMuted) 0f else 1f
                mp.setVolume(vol, vol)
            }
        }
    }

    LaunchedEffect(playbackSpeed, isPrepared) {
        if (!isPrepared) return@LaunchedEffect
        try {
            mediaPlayer?.let { mp ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    val params = mp.playbackParams
                    params.speed = playbackSpeed
                    mp.playbackParams = params
                }
            }
        } catch (e: Exception) {
            // gracefully fallback
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTapLike()
                        showHeartBurst = true
                        coroutineScope.launch {
                            heartScale.snapTo(0f)
                            heartScale.animateTo(
                                targetValue = 1.3f,
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            )
                            heartScale.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(250)
                            )
                            showHeartBurst = false
                        }
                    },
                    onTap = {
                        isPausedManually = !isPausedManually
                        onSingleTap()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(st: android.graphics.SurfaceTexture, width: Int, height: Int) {
                            val surface = Surface(st)
                            surfaceObj = surface
                            runCatching { mediaPlayer?.setSurface(surface) }
                        }

                        override fun onSurfaceTextureSizeChanged(st: android.graphics.SurfaceTexture, width: Int, height: Int) {}

                        override fun onSurfaceTextureDestroyed(st: android.graphics.SurfaceTexture): Boolean {
                            runCatching { mediaPlayer?.setSurface(null) }
                            runCatching { surfaceObj?.release() }
                            surfaceObj = null
                            return true
                        }

                        override fun onSurfaceTextureUpdated(st: android.graphics.SurfaceTexture) {}
                    }
                    setLayerType(View.LAYER_TYPE_HARDWARE, filterPaint)
                }
            },
            update = { textureView ->
                textureView.setLayerType(View.LAYER_TYPE_HARDWARE, filterPaint)
                surfaceObj?.let { s ->
                    runCatching { mediaPlayer?.setSurface(s) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading spinner
        if (isBuffering && !hasPlaybackError) {
            CircularProgressIndicator(
                color = Color(0xFFFF5722),
                modifier = Modifier.size(44.dp)
            )
        }

        // Playback Error / Stream Reconnect Prompt
        if (hasPlaybackError) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            retryTrigger++
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry playback",
                    tint = Color(0xFFFF5722),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to load stream",
                    color = Color.White.copy(alpha = 0.85f),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Pause overlay
        AnimatedVisibility(
            visible = isPausedManually,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        // Animated double-tap heart burst
        if (showHeartBurst) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF2A6D),
                modifier = Modifier
                    .size(100.dp)
                    .scale(heartScale.value)
            )
        }
    }
}
