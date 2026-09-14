package com.example.mtaatok.ui.post

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtaatok.data.repository.MtaaTokRepository
import com.example.mtaatok.model.MusicTrack
import com.example.mtaatok.player.MtaaVideoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostVideoScreen(
    repository: MtaaTokRepository,
    onPostSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var hashtagInput by remember { mutableStateOf("") }
    val hashtagsList = remember { mutableStateOf(listOf("MtaaTok", "NairobiVibes", "StreetCreatives")) }

    // Video editor state
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }
    var selectedFilter by remember { mutableStateOf("Normal") }
    var selectedMusic by remember { mutableStateOf<MusicTrack?>(null) }
    var originalVolume by remember { mutableFloatStateOf(1.0f) }
    var musicVolume by remember { mutableFloatStateOf(0.7f) }
    var trimStartSeconds by remember { mutableFloatStateOf(0f) }
    var trimEndSeconds by remember { mutableFloatStateOf(30f) }
    var overlayText by remember { mutableStateOf("") }

    // Post settings
    var visibility by remember { mutableStateOf("Public") } // Public, Followers Only, Private
    var allowDownloads by remember { mutableStateOf(true) }

    // Upload & processing state
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadStatusMessage by remember { mutableStateOf("") }

    // Media Picker Launcher (Modern Android Photo/Video Picker)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
        }
    }

    // Fallback file picker
    val genericVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
        }
    }

    val availableFilters = listOf("Normal", "Cyberpunk", "Golden Hour", "Noir", "Sepia", "Vivid Boost")
    val speedOptions = listOf(0.5f, 1.0f, 1.5f, 2.0f)

    fun handlePublish() {
        val uri = selectedVideoUri ?: return
        if (caption.isBlank()) {
            Toast.makeText(context, "Please add a caption for your video", Toast.LENGTH_SHORT).show()
            return
        }

        isUploading = true
        coroutineScope.launch {
            uploadStatusMessage = "Analyzing video format & encoding..."
            uploadProgress = 0.2f
            delay(500)

            uploadStatusMessage = "Applying ${selectedFilter} filter & audio mixing..."
            uploadProgress = 0.5f
            delay(600)

            uploadStatusMessage = "Uploading video file to MtaaTok storage..."
            uploadProgress = 0.85f
            delay(600)

            uploadStatusMessage = "Indexing post and tags..."
            uploadProgress = 1.0f
            delay(300)

            // Save post into Room database and state!
            repository.publishPost(
                videoUrl = uri.toString(),
                caption = caption,
                hashtags = hashtagsList.value,
                musicTitle = selectedMusic?.title ?: "Original Audio",
                musicArtist = selectedMusic?.artist ?: "Mtaa Creator",
                allowsDownload = allowDownloads,
                visibility = visibility,
                filterEffect = selectedFilter
            )

            isUploading = false
            Toast.makeText(context, "🎉 Video published successfully to MtaaTok!", Toast.LENGTH_LONG).show()
            onPostSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D12))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("post_video_screen")
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
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "Create Mtaa Post",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                if (selectedVideoUri != null && !isUploading) {
                    Button(
                        onClick = { handlePublish() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("post_publish_button")
                    ) {
                        Text("Post", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Scrollable Form Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Video Picker & Preview Section
                if (selectedVideoUri == null) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clickable {
                                try {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                } catch (e: Exception) {
                                    genericVideoPicker.launch("video/*")
                                }
                            }
                            .testTag("select_video_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0xFFFF5722).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "Select Video",
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Select Video from Gallery",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Supports MP4, WebM, and standard video files",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    try {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    } catch (e: Exception) {
                                        genericVideoPicker.launch("video/*")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28283C)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Choose Video", color = Color(0xFF00E5FF), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    // Video Preview with Active Filters & Speed
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            MtaaVideoPlayer(
                                videoUrl = selectedVideoUri.toString(),
                                isPlaying = true,
                                isMuted = false,
                                filterEffect = selectedFilter,
                                playbackSpeed = selectedSpeed,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Overlay text preview
                            if (overlayText.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = overlayText,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Change Video Button
                            TextButton(
                                onClick = {
                                    try {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    } catch (e: Exception) {
                                        genericVideoPicker.launch("video/*")
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            ) {
                                Text("Change Video", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Video Editor Controls (Filters, Speed, Trim, Audio)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Mtaa Video Editor",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Playback Speed Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Speed:", color = Color.White, fontSize = 13.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    speedOptions.forEach { spd ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selectedSpeed == spd) Color(0xFFFF5722) else Color(0xFF252536))
                                                .clickable { selectedSpeed = spd }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${spd}x",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Visual Filter Presets
                            Text("Visual Filters:", color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(availableFilters) { filterName ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selectedFilter == filterName) Color(0xFFFF5722) else Color(0xFF252536))
                                            .border(
                                                width = 1.dp,
                                                color = if (selectedFilter == filterName) Color(0xFFFF7043) else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedFilter = filterName }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = filterName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedFilter == filterName) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Royalty-Free Music Selection
                            Text("Soundtrack (Royalty-Free):", color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selectedMusic == null) Color(0xFF00E5FF).copy(alpha = 0.3f) else Color(0xFF252536))
                                            .clickable { selectedMusic = null }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("Original Audio", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                                items(repository.royaltyFreeTracks) { track ->
                                    val isSelected = selectedMusic?.id == track.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.3f) else Color(0xFF252536))
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color(0xFF00E5FF) else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedMusic = track }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("${track.title} (${track.artist})", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Volume Balancer
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Original Audio: ${(originalVolume * 100).toInt()}%", color = Color.LightGray, fontSize = 12.sp)
                            }
                            Slider(
                                value = originalVolume,
                                onValueChange = { originalVolume = it },
                                colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5722), activeTrackColor = Color(0xFFFF5722))
                            )

                            // Video Text Overlay
                            OutlinedTextField(
                                value = overlayText,
                                onValueChange = { overlayText = it },
                                label = { Text("Add Text Overlay sticker (Optional)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF333344),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Caption & Hashtags Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161622)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Post Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = caption,
                                onValueChange = { caption = it },
                                label = { Text("Write a caption...") },
                                minLines = 2,
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF5722),
                                    unfocusedBorderColor = Color(0xFF333344),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_caption_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Hashtags
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = hashtagInput,
                                    onValueChange = { hashtagInput = it },
                                    label = { Text("Add hashtag") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00E5FF),
                                        unfocusedBorderColor = Color(0xFF333344),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val clean = hashtagInput.trim().removePrefix("#")
                                        if (clean.isNotBlank() && !hashtagsList.value.contains(clean)) {
                                            hashtagsList.value = hashtagsList.value + clean
                                            hashtagInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                hashtagsList.value.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF222232))
                                            .clickable {
                                                hashtagsList.value = hashtagsList.value.filter { it != tag }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("#$tag ✕", color = Color(0xFF00E5FF), fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Visibility Options
                            Text("Who can view this video:", color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Public", "Followers", "Private").forEach { vis ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (visibility == vis) Color(0xFFFF5722) else Color(0xFF252536))
                                            .clickable { visibility = vis }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(vis, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Allow Downloads Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Allow Downloads", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Let other MtaaTok users save this video", color = Color.Gray, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = allowDownloads,
                                    onCheckedChange = { allowDownloads = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFFFF5722)
                                    ),
                                    modifier = Modifier.testTag("allow_downloads_switch")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Upload Progress Bar overlay when posting
                    AnimatedVisibility(visible = isUploading) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF221730)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = uploadStatusMessage,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress },
                                    color = Color(0xFFFF5722),
                                    trackColor = Color(0xFF333346),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }

                    // Main Post Submit Button
                    Button(
                        onClick = { handlePublish() },
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("post_submit_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isUploading) "Uploading Video..." else "Publish Video to MtaaTok",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
