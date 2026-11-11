package com.example.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaType
import com.example.model.PostItem
import com.example.ui.theme.CyberPink
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan

@Composable
fun FeedScreen(
    posts: List<PostItem>,
    coinBalance: Long,
    onLikePost: (PostItem) -> Unit,
    onBookmarkPost: (PostItem) -> Unit,
    onOpenGifting: (PostItem) -> Unit,
    onOpenComments: (PostItem) -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All Feeds 🔥", "Photos 📸", "Videos 🎥", "Trending ⚡")

    val filteredPosts = when (selectedTab) {
        1 -> posts.filter { it.mediaType == MediaType.PHOTO }
        2 -> posts.filter { it.mediaType == MediaType.VIDEO }
        3 -> posts.sortedByDescending { it.likesCount + it.giftsCount * 5 }
        else -> posts
    }

    ScaffoldFeed(
        coinBalance = coinBalance,
        onNavigateToWallet = onNavigateToWallet,
        onNavigateToUpload = onNavigateToUpload
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Active Riders Stories Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current User's Add Story
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onNavigateToUpload() }
                                .testTag("add_story_item")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = CircleShape,
                                    color = DarkSurfaceElevated,
                                    border = BorderStroke(2.dp, NeonCyan),
                                    modifier = Modifier.size(58.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("📸", fontSize = 22.sp)
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = NeonCyan,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = Color(0xFF00363D),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Your Story", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Friends & Pro Riders Stories
                    val sampleRiders = listOf(
                        Triple("Zoe Cyber", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&fit=crop&q=80", true),
                        Triple("Tariq Drift", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&fit=crop&q=80", true),
                        Triple("Marcus Blaze", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&fit=crop&q=80", false),
                        Triple("Rhea Stunt", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&fit=crop&q=80", false)
                    )

                    items(sampleRiders) { (name, avatar, isLive) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box {
                                AsyncImage(
                                    model = avatar,
                                    contentDescription = name,
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.5.dp,
                                            if (isLive) CyberPink else NeonCyan,
                                            CircleShape
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                if (isLive) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CyberPink,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    ) {
                                        Text(
                                            "LIVE",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = name.split(" ").first(),
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Category Tabs Filter Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tabs.indices.toList()) { index ->
                        val selected = selectedTab == index
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) NeonCyan else DarkSurfaceElevated,
                            border = BorderStroke(1.dp, if (selected) NeonCyan else DarkSurfaceBorder),
                            modifier = Modifier
                                .clickable { selectedTab = index }
                                .testTag("tab_filter_$index")
                        ) {
                            Text(
                                text = tabs[index],
                                color = if (selected) Color(0xFF00363D) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Post Cards
            items(filteredPosts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    onLike = { onLikePost(post) },
                    onBookmark = { onBookmarkPost(post) },
                    onGift = { onOpenGifting(post) },
                    onComment = { onOpenComments(post) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun ScaffoldFeed(
    coinBalance: Long,
    onNavigateToWallet: () -> Unit,
    onNavigateToUpload: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🏍️", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RiderSocial",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Feed & Bike Racing",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Balance Pill (Clickable -> Opens Wallet)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, ElectricGold),
                    modifier = Modifier
                        .clickable { onNavigateToWallet() }
                        .testTag("feed_wallet_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Wallet",
                            tint = ElectricGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%,d".format(coinBalance),
                                color = ElectricGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "$%.2f USD".format(coinBalance / 1_000_000.0),
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            content(PaddingValues(0.dp))
        }

        // Floating Upload Action Button
        FloatingActionButton(
            onClick = onNavigateToUpload,
            containerColor = NeonCyan,
            contentColor = Color(0xFF00363D),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("fab_upload_post")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Upload Photo or Video",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PostCard(
    post: PostItem,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onGift: () -> Unit,
    onComment: () -> Unit
) {
    var isVideoPlaying by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Post Author Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = post.authorAvatar,
                        contentDescription = "Author Avatar",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonCyan, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Text(
                            text = "${post.authorUsername} • 2h ago",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    }
                }

                // Media Type Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (post.mediaType == MediaType.VIDEO) CyberPink.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f),
                    border = BorderStroke(
                        1.dp,
                        if (post.mediaType == MediaType.VIDEO) CyberPink.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (post.mediaType == MediaType.VIDEO) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = CyberPink, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VIDEO", color = CyberPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("PHOTO 📸", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Post Caption
            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    color = Color(0xFFE2E8F0),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            // Tags Row
            if (post.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    post.tags.take(3).forEach { tag ->
                        Text(
                            text = "#$tag",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Media Preview (Photo / Video)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 11f)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = post.mediaUri,
                    contentDescription = "Post Media",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Video Player UI Overlay
                if (post.mediaType == MediaType.VIDEO) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isVideoPlaying) Color.Transparent else Color.Black.copy(alpha = 0.35f))
                            .clickable { isVideoPlaying = !isVideoPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isVideoPlaying) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.7f),
                                border = BorderStroke(2.dp, NeonCyan),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        // Bottom video bar
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { if (isVideoPlaying) 0.65f else 0.25f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = NeonCyan,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isVideoPlaying) "▶ Playing 0:14 / 0:32" else "0:32 HD 60FPS",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Gifts Received Banner (if any)
            if (post.giftsCount > 0) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎁", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${post.giftsCount} Gifts awarded",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = ElectricGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%,d Coins ($%.3f)".format(
                                    post.totalGiftCoins,
                                    post.totalGiftCoins / 1_000_000.0
                                ),
                                color = ElectricGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons Bar (Like, Comment, Gift, Bookmark)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like button
                    val heartColor by animateColorAsState(
                        targetValue = if (post.isLiked) CyberPink else Color(0xFF94A3B8),
                        label = "like_color"
                    )
                    IconButton(
                        onClick = onLike,
                        modifier = Modifier.testTag("like_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = heartColor
                        )
                    }
                    Text(
                        text = "${post.likesCount}",
                        color = heartColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Comment button
                    IconButton(
                        onClick = onComment,
                        modifier = Modifier.testTag("comment_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                    Text(
                        text = "${post.commentsCount}",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Gifting Action Button (Golden Highlighted)
                Button(
                    onClick = onGift,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGold),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("gift_btn_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Gift",
                        tint = Color(0xFF3E2723),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GIFT",
                        color = Color(0xFF3E2723),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                // Bookmark button
                IconButton(
                    onClick = onBookmark,
                    modifier = Modifier.testTag("bookmark_btn_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (post.isBookmarked) NeonCyan else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
