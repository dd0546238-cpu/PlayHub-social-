package com.example.ui.game

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AVAILABLE_GIFTS
import com.example.model.GamingRoom
import com.example.model.GiftItem
import com.example.model.RoomSeat
import com.example.model.UserProfile
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen

@Composable
fun LiveGamingRoomScreen(
    room: GamingRoom,
    currentUser: UserProfile,
    userSeatNumber: Int?,
    onTakeSeat: (Int) -> Unit,
    onLeaveSeat: () -> Unit,
    onToggleMic: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendGift: (targetUser: String, GiftItem) -> Unit,
    onLeaveRoom: () -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    var showGiftSelector by remember { mutableStateOf(false) }
    var selectedGiftTarget by remember { mutableStateOf(room.hostName) }

    val isUserOnMicSeat = userSeatNumber != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. ROOM HEADER
        Surface(
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box {
                        AsyncImage(
                            model = room.hostAvatar,
                            contentDescription = "Host",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(2.dp, ElectricGold, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            shape = CircleShape,
                            color = ElectricGold,
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👑", fontSize = 8.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = room.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Host: ${room.hostName}",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyberPink.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "👁️ ${room.audienceCount} in Audience",
                                    color = CyberPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Leave Room Button
                IconButton(
                    onClick = onLeaveRoom,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .testTag("leave_room_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Leave Room",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 2. LIVE GAMEPLAY STREAM BANNER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CyberPink.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🏍️", fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = room.gameTitle,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Live Match • 220 KM/H • Highway Run",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Room Coin Pool
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, ElectricGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = ElectricGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "%,d".format(room.coinPool),
                                    color = ElectricGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. STAGE SEATS (8 MIC SEATS)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.5.dp, DarkSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stage Seats (${room.seats.count { it.occupantName != null }}/${room.totalMicSeats})",
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }

                            if (isUserOnMicSeat) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = onToggleMic,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = Color(0xFF00363D),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("MIC", color = Color(0xFF00363D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = onLeaveSeat,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("leave_mic_seat_button")
                                    ) {
                                        Text("LEAVE SEAT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 8 Seats Grid (2 rows of 4)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                room.seats.take(4).forEach { seat ->
                                    SeatItem(
                                        seat = seat,
                                        isCurrentUserSeat = userSeatNumber == seat.seatNumber,
                                        onTakeSeat = { onTakeSeat(seat.seatNumber) }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                room.seats.drop(4).take(4).forEach { seat ->
                                    SeatItem(
                                        seat = seat,
                                        isCurrentUserSeat = userSeatNumber == seat.seatNumber,
                                        onTakeSeat = { onTakeSeat(seat.seatNumber) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. AUDIENCE SEATS ("audience sit ar option dao")
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, ElectricGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Chair,
                                    contentDescription = null,
                                    tint = ElectricGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Audience Seats 👥 (${room.audienceCount} Sitting)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }

                            if (!isUserOnMicSeat) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NeonGreen.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, NeonGreen)
                                ) {
                                    Text(
                                        text = "YOU ARE IN AUDIENCE SEAT ✓",
                                        color = NeonGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = onLeaveSeat,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGold),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("SIT IN AUDIENCE", color = Color(0xFF3E2723), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Audience Seats Row
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User's own audience seat avatar
                            item {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box {
                                        AsyncImage(
                                            model = currentUser.avatarUrl,
                                            contentDescription = "You",
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, if (!isUserOnMicSeat) NeonGreen else Color(0xFF334155), CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = NeonGreen,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .align(Alignment.BottomEnd)
                                        ) {}
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "You",
                                        color = if (!isUserOnMicSeat) NeonGreen else Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Other audience members
                            items(room.audienceMembers) { member ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(
                                        model = member.avatarUrl,
                                        contentDescription = member.name,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, DarkSurfaceBorder, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = member.name.split(" ").first(),
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. LIVE ROOM CHAT STREAM
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, DarkSurfaceBorder)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(room.chatMessages.reversed()) { msg ->
                            if (msg.isSystem) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonCyan.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📢 ${msg.text}",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else if (msg.isGift) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElectricGold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, ElectricGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🎁 ${msg.senderName} ${msg.text}",
                                        color = ElectricGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "${msg.senderName}: ",
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = msg.text,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // QUICK GIFT SELECTION BAR
        AnimatedVisibility(visible = showGiftSelector) {
            Surface(
                color = Color(0xFF131A2E),
                border = BorderStroke(1.dp, ElectricGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🎁 Send Gift to $selectedGiftTarget:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(AVAILABLE_GIFTS) { gift ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurfaceElevated,
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable {
                                        onSendGift(selectedGiftTarget, gift)
                                        showGiftSelector = false
                                    }
                                    .padding(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(gift.emoji, fontSize = 22.sp)
                                    Text(gift.name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("%,d".format(gift.coinCost), color = ElectricGold, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. BOTTOM ACTION BAR (Chat Input + Gift Button)
        Surface(
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Chat with riders in room...", color = Color(0xFF64748B), fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("room_chat_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Send chat button
                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            onSendMessage(messageInput)
                            messageInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(NeonCyan, CircleShape)
                        .testTag("room_send_chat_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF00363D),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Gift Button
                Button(
                    onClick = { showGiftSelector = !showGiftSelector },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGold),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("room_gift_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Gift",
                        tint = Color(0xFF3E2723),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GIFT", color = Color(0xFF3E2723), fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SeatItem(
    seat: RoomSeat,
    isCurrentUserSeat: Boolean,
    onTakeSeat: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onTakeSeat() }
            .testTag("seat_${seat.seatNumber}")
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (seat.occupantName != null) {
                // Occupied Mic Seat
                AsyncImage(
                    model = seat.occupantAvatar ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&fit=crop&q=80",
                    contentDescription = seat.occupantName,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            if (seat.isHost) ElectricGold else if (isCurrentUserSeat) NeonGreen else NeonCyan,
                            CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )

                // Host or mic icon
                Surface(
                    shape = CircleShape,
                    color = if (seat.isHost) ElectricGold else if (seat.isMuted) Color(0xFFE53935) else NeonCyan,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (seat.isHost) {
                            Text("👑", fontSize = 9.sp)
                        } else if (seat.isMuted) {
                            Icon(imageVector = Icons.Default.MicOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(10.dp))
                        }
                    }
                }
            } else {
                // Empty Seat: Tap to sit
                Surface(
                    shape = CircleShape,
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.5.dp, Color(0xFF334155)),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Sit Here",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Seat ${seat.seatNumber}",
                                color = Color(0xFF94A3B8),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = seat.occupantName?.split(" ")?.first() ?: "Empty",
            color = if (seat.occupantName != null) Color.White else Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
