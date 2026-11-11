package com.example.ui.feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaType
import com.example.ui.theme.CyberPink
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan

// Sample presets for quick testing if device has no local photos/videos
private val SAMPLE_MEDIA_PRESETS = listOf(
    Pair("https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=900&fit=crop&q=80", MediaType.PHOTO),
    Pair("https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=900&fit=crop&q=80", MediaType.VIDEO),
    Pair("https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=900&fit=crop&q=80", MediaType.VIDEO),
    Pair("https://images.unsplash.com/photo-1558980664-769d59546b3d?w=900&fit=crop&q=80", MediaType.PHOTO)
)

private val AVAILABLE_TAGS = listOf("BikeLife", "SpeedRacer", "StuntTrick", "NitroRush", "HighwayDrift", "CoinEarn")

@Composable
fun CreatePostScreen(
    onPublish: (caption: String, mediaUri: String, mediaType: MediaType, tags: List<String>) -> Unit,
    onBack: () -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var selectedMediaUri by remember { mutableStateOf<String?>(SAMPLE_MEDIA_PRESETS.first().first) }
    var selectedMediaType by remember { mutableStateOf(MediaType.PHOTO) }
    val selectedTags = remember { mutableStateListOf("BikeLife", "SpeedRacer") }

    // Modern Android Photo Picker (Images and Videos)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                selectedMediaUri = uri.toString()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("upload_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Post",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = {
                    val uri = selectedMediaUri ?: SAMPLE_MEDIA_PRESETS.first().first
                    if (caption.isNotBlank() || uri.isNotBlank()) {
                        onPublish(
                            caption.ifBlank { "Check out my new bike ride! 🏍️⚡" },
                            uri,
                            selectedMediaType,
                            selectedTags.toList()
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier.testTag("publish_post_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Publish,
                    contentDescription = null,
                    tint = Color(0xFF00363D),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SHARE",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00363D),
                    fontSize = 13.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Media Type Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (selectedMediaType == MediaType.PHOTO) NeonCyan else DarkSurfaceElevated,
                    border = BorderStroke(1.dp, if (selectedMediaType == MediaType.PHOTO) NeonCyan else DarkSurfaceBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMediaType = MediaType.PHOTO }
                        .testTag("select_photo_type")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = if (selectedMediaType == MediaType.PHOTO) Color(0xFF00363D) else Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Photo 📸",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedMediaType == MediaType.PHOTO) Color(0xFF00363D) else Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (selectedMediaType == MediaType.VIDEO) CyberPink else DarkSurfaceElevated,
                    border = BorderStroke(1.dp, if (selectedMediaType == MediaType.VIDEO) CyberPink else DarkSurfaceBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMediaType = MediaType.VIDEO }
                        .testTag("select_video_type")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video 🎥",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Media Preview & Pick Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (selectedMediaUri != null) {
                        AsyncImage(
                            model = selectedMediaUri,
                            contentDescription = "Selected Media Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Floating Change Button
                    Button(
                        onClick = {
                            val mimeType = if (selectedMediaType == MediaType.VIDEO) {
                                ActivityResultContracts.PickVisualMedia.VideoOnly
                            } else {
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            }
                            photoPickerLauncher.launch(PickVisualMediaRequest(mimeType))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier.testTag("pick_from_device_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedMediaType == MediaType.VIDEO) "Choose Video from Device" else "Choose Photo from Device",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Preset Selection (for fast testing)
            Text(
                text = "Or choose a rider preset gallery asset:",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(SAMPLE_MEDIA_PRESETS) { preset ->
                    val isSelected = selectedMediaUri == preset.first
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonCyan else Color(0xFF334155),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedMediaUri = preset.first
                                selectedMediaType = preset.second
                            }
                    ) {
                        AsyncImage(
                            model = preset.first,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (preset.second == MediaType.VIDEO) {
                            Surface(
                                shape = RoundedCornerShape(bottomStart = 8.dp),
                                color = CyberPink.copy(alpha = 0.85f),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(
                                    "▶",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Caption Field
            Text(
                text = "Caption & Story",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                placeholder = {
                    Text(
                        "Share your ride stats, tricks, or ask followers for gifts...",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("caption_input_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hashtags selector
            Text(
                text = "Tags",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AVAILABLE_TAGS) { tag ->
                    val isChecked = selectedTags.contains(tag)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isChecked) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (isChecked) NeonCyan else DarkSurfaceBorder),
                        modifier = Modifier.clickable {
                            if (isChecked) selectedTags.remove(tag) else selectedTags.add(tag)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "#$tag",
                                color = if (isChecked) NeonCyan else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
