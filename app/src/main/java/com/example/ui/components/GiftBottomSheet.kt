package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AVAILABLE_GIFTS
import com.example.model.GiftItem
import com.example.model.PostItem
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(
    post: PostItem,
    currentCoinBalance: Long,
    onSendGift: (GiftItem, Int) -> Unit,
    onNavigateToRecharge: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedGift by remember { mutableStateOf(AVAILABLE_GIFTS.first()) }
    var selectedMultiplier by remember { mutableIntStateOf(1) }

    val totalCost = selectedGift.coinCost * selectedMultiplier
    val canAfford = currentCoinBalance >= totalCost

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            // Header: Recipient & Current Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎁 Send Gift to ${post.authorName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Support creator & boost post ranking",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Balance Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, ElectricGold.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable { onNavigateToRecharge() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = ElectricGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%,d".format(currentCoinBalance),
                            color = ElectricGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Recharge",
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gifts Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(280.dp),
                contentPadding = PaddingValues(2.dp)
            ) {
                items(AVAILABLE_GIFTS) { gift ->
                    val isSelected = gift.id == selectedGift.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else DarkSurfaceElevated)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonCyan else Color(0xFF263354),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedGift = gift }
                            .padding(12.dp)
                            .testTag("gift_item_${gift.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = gift.emoji,
                                fontSize = 36.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = gift.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = ElectricGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "%,d".format(gift.coinCost),
                                    color = ElectricGold,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = gift.formattedUsd,
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multiplier Selector (x1, x5, x10, x50, x99)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Count:",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                listOf(1, 5, 10, 50, 99).forEach { multiplier ->
                    val selected = selectedMultiplier == multiplier
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) NeonCyan else DarkSurfaceElevated,
                        border = BorderStroke(1.dp, if (selected) NeonCyan else Color(0xFF334155)),
                        modifier = Modifier
                            .clickable { selectedMultiplier = multiplier }
                            .testTag("multiplier_$multiplier")
                    ) {
                        Text(
                            text = "x$multiplier",
                            color = if (selected) Color(0xFF00363D) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Send Action or Recharge Alert
            if (canAfford) {
                Button(
                    onClick = {
                        onSendGift(selectedGift, selectedMultiplier)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("send_gift_confirm_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricGold)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFF3E2723)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SEND ${selectedGift.emoji} (%,d COINS ≈ $%.3f USD)".format(
                            totalCost,
                            totalCost / 1_000_000.0
                        ),
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3E2723),
                        fontSize = 14.sp
                    )
                }
            } else {
                Column {
                    Text(
                        text = "⚠️ Insufficient coins! You need %,d more Coins.".format(totalCost - currentCoinBalance),
                        color = Color(0xFFFF6D00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Button(
                        onClick = {
                            onDismiss()
                            onNavigateToRecharge()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("recharge_now_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF00363D)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECHARGE COINS (1M Coins = $1.00)",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00363D),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
