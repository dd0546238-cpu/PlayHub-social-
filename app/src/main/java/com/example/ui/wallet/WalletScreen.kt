package com.example.ui.wallet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WalletStateEntity
import com.example.model.CoinRates
import com.example.model.TransactionItem
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.ui.theme.CyberPink
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
    wallet: WalletStateEntity,
    transactions: List<TransactionItem>,
    onRecharge: (amountUsd: Double, coins: Long, method: String, accountRef: String) -> Unit,
    onWithdraw: (coins: Long, method: String, recipientAccount: String) -> Unit
) {
    var showRechargeModal by remember { mutableStateOf(false) }
    var showWithdrawModal by remember { mutableStateOf(false) }
    var filterTab by remember { mutableIntStateOf(0) }

    val filterOptions = listOf("All", "Recharges", "Withdraws", "Race Earnings", "Gifts")

    val filteredTransactions = when (filterTab) {
        1 -> transactions.filter { it.type == TransactionType.RECHARGE }
        2 -> transactions.filter { it.type == TransactionType.WITHDRAW }
        3 -> transactions.filter { it.type == TransactionType.GAME_EARNED }
        4 -> transactions.filter { it.type == TransactionType.GIFT_SENT || it.type == TransactionType.GIFT_RECEIVED }
        else -> transactions
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rider Wallet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Exchange: 1,000,000 Coins = $1.00 USD",
                            fontSize = 11.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Cyber Wallet Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(NeonCyan, ElectricGold, CyberPink))
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE BALANCE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElectricGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, ElectricGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "1M COINS = $1.00",
                                color = ElectricGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Coins Value
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = ElectricGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "%,d".format(wallet.coinBalance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "≈ $%.2f USD".format(wallet.coinBalance / 1_000_000.0),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(start = 46.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Mini Stats: Earned, Recharged, Withdrawn
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Won", color = Color(0xFF64748B), fontSize = 11.sp)
                            Text(
                                "%,d".format(wallet.totalEarnedCoins),
                                color = NeonGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Recharged", color = Color(0xFF64748B), fontSize = 11.sp)
                            Text(
                                "%,d".format(wallet.totalRechargedCoins),
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Withdrawn", color = Color(0xFF64748B), fontSize = 11.sp)
                            Text(
                                "%,d".format(wallet.totalWithdrawnCoins),
                                color = CyberPink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Big Action Buttons: Recharge & Withdraw
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showRechargeModal = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("wallet_recharge_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF00363D)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RECHARGE",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00363D),
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = { showWithdrawModal = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("wallet_withdraw_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGold)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFF3E2723)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WITHDRAW",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF3E2723),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Transactions History Header & Filters
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Transaction History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions.indices.toList()) { idx ->
                        val selected = filterTab == idx
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) NeonCyan else DarkSurfaceElevated,
                            border = BorderStroke(1.dp, if (selected) NeonCyan else DarkSurfaceBorder),
                            modifier = Modifier.clickable { filterTab = idx }
                        ) {
                            Text(
                                text = filterOptions[idx],
                                color = if (selected) Color(0xFF00363D) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Transaction Items List
        if (filteredTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found in this category.",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionRow(tx = tx)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Recharge Dialog Modal
    if (showRechargeModal) {
        RechargeDialog(
            onConfirm = { usd, coins, method, account ->
                onRecharge(usd, coins, method, account)
                showRechargeModal = false
            },
            onDismiss = { showRechargeModal = false }
        )
    }

    // Withdraw Dialog Modal
    if (showWithdrawModal) {
        WithdrawDialog(
            currentBalanceCoins = wallet.coinBalance,
            onConfirm = { coins, method, recipient ->
                onWithdraw(coins, method, recipient)
                showWithdrawModal = false
            },
            onDismiss = { showWithdrawModal = false }
        )
    }
}

@Composable
fun TransactionRow(tx: TransactionItem) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val isPositive = tx.type == TransactionType.RECHARGE || tx.type == TransactionType.GAME_EARNED || tx.type == TransactionType.GIFT_RECEIVED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkSurfaceBorder)
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
                    shape = CircleShape,
                    color = when (tx.type) {
                        TransactionType.RECHARGE -> NeonCyan.copy(alpha = 0.2f)
                        TransactionType.WITHDRAW -> CyberPink.copy(alpha = 0.2f)
                        TransactionType.GAME_EARNED -> NeonGreen.copy(alpha = 0.2f)
                        TransactionType.GIFT_SENT -> ElectricGold.copy(alpha = 0.2f)
                        TransactionType.GIFT_RECEIVED -> CyberPink.copy(alpha = 0.2f)
                    },
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (tx.type) {
                                TransactionType.RECHARGE -> Icons.Default.ArrowDownward
                                TransactionType.WITHDRAW -> Icons.Default.ArrowUpward
                                TransactionType.GAME_EARNED -> Icons.Default.Speed
                                else -> Icons.Default.CardGiftcard
                            },
                            contentDescription = null,
                            tint = when (tx.type) {
                                TransactionType.RECHARGE -> NeonCyan
                                TransactionType.WITHDRAW -> CyberPink
                                TransactionType.GAME_EARNED -> NeonGreen
                                else -> ElectricGold
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tx.methodOrTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${dateFormat.format(Date(tx.timestamp))} • Ref: ${tx.referenceId}",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else "-"}%,d".format(tx.amountCoins),
                    color = if (isPositive) NeonGreen else CyberPink,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = tx.formattedUsd,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    }
}

data class RechargePreset(val usd: Double, val coins: Long, val bonusText: String = "")

private val RECHARGE_PRESETS = listOf(
    RechargePreset(1.0, 1_000_000L, "Starter ($1 = 1M Coins)"),
    RechargePreset(5.0, 5_200_000L, "+4% Free Bonus"),
    RechargePreset(10.0, 11_000_000L, "+10% Bonus 🔥"),
    RechargePreset(20.0, 23_000_000L, "+15% Bonus ⚡"),
    RechargePreset(50.0, 60_000_000L, "+20% Mega Bonus 👑")
)

private val PAYMENT_METHODS = listOf("bKash", "Nagad", "Rocket", "UPI", "Card (Visa/MC)", "PayPal", "USDT")

@Composable
fun RechargeDialog(
    onConfirm: (Double, Long, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf(RECHARGE_PRESETS.first()) }
    var selectedMethod by remember { mutableStateOf(PAYMENT_METHODS.first()) }
    var accountReference by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💳 Recharge Coins",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Official Rate: $1.00 USD = 1,000,000 Coins",
                    fontSize = 12.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Package:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Packages Grid / List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RECHARGE_PRESETS.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else DarkSurfaceElevated,
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else DarkSurfaceBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPreset = preset }
                                .testTag("recharge_preset_${preset.usd.toInt()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = ElectricGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "%,d Coins".format(preset.coins),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (preset.bonusText.isNotBlank()) {
                                            Text(
                                                text = preset.bonusText,
                                                color = NeonGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "$%.2f USD".format(preset.usd),
                                    color = if (isSelected) NeonCyan else Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Method Selector
                Text("Select Payment Gateway:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PAYMENT_METHODS) { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricGold else DarkSurfaceElevated,
                            border = BorderStroke(1.dp, if (isSelected) ElectricGold else DarkSurfaceBorder),
                            modifier = Modifier.clickable { selectedMethod = method }
                        ) {
                            Text(
                                text = method,
                                color = if (isSelected) Color(0xFF3E2723) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phone or Account Reference
                OutlinedTextField(
                    value = accountReference,
                    onValueChange = { accountReference = it },
                    label = { Text("$selectedMethod Account / Phone / TrxID", fontSize = 12.sp) },
                    placeholder = { Text("e.g. 017xxxxxxxx or wallet address", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recharge_account_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ref = accountReference.ifBlank { "APP-PAY-" + (1000..9999).random() }
                    onConfirm(selectedPreset.usd, selectedPreset.coins, selectedMethod, ref)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_recharge_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "PAY $${selectedPreset.usd.toInt()} & GET %,d COINS".format(selectedPreset.coins),
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00363D)
                )
            }
        }
    )
}

private val WITHDRAW_METHODS = listOf("bKash", "Nagad", "Rocket", "UPI / GPay", "PayPal", "Bank Payout")

@Composable
fun WithdrawDialog(
    currentBalanceCoins: Long,
    onConfirm: (Long, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountCoinsInput by remember { mutableStateOf("1000000") }
    var selectedMethod by remember { mutableStateOf(WITHDRAW_METHODS.first()) }
    var recipientAccount by remember { mutableStateOf("") }

    val enteredCoins = amountCoinsInput.toLongOrNull() ?: 0L
    val enteredUsd = enteredCoins / 1_000_000.0
    val isValidAmount = enteredCoins in 1_000_000L..currentBalanceCoins

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💸 Withdraw Earnings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Your Balance", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(
                                "%,d Coins".format(currentBalanceCoins),
                                color = ElectricGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            "≈ $%.2f USD".format(currentBalanceCoins / 1_000_000.0),
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Method
                Text("Select Payout Method:", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WITHDRAW_METHODS) { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricGold else DarkSurfaceElevated,
                            border = BorderStroke(1.dp, if (isSelected) ElectricGold else DarkSurfaceBorder),
                            modifier = Modifier.clickable { selectedMethod = method }
                        ) {
                            Text(
                                text = method,
                                color = if (isSelected) Color(0xFF3E2723) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recipient Account
                OutlinedTextField(
                    value = recipientAccount,
                    onValueChange = { recipientAccount = it },
                    label = { Text("$selectedMethod Account Number", fontSize = 12.sp) },
                    placeholder = { Text("e.g. 018xxxxxxxx", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_recipient_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricGold,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Coins to Withdraw
                OutlinedTextField(
                    value = amountCoinsInput,
                    onValueChange = { amountCoinsInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Amount in Coins (Min 1,000,000)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricGold,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Converted USD preview
                Text(
                    text = "💵 You will receive: $%.2f USD".format(enteredUsd),
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (enteredCoins < 1_000_000L) {
                    Text(
                        text = "⚠️ Minimum withdrawal is 1,000,000 Coins ($1.00 USD)",
                        color = Color(0xFFFF6D00),
                        fontSize = 11.sp
                    )
                } else if (enteredCoins > currentBalanceCoins) {
                    Text(
                        text = "⚠️ Amount exceeds current balance!",
                        color = Color(0xFFFF1744),
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValidAmount && recipientAccount.isNotBlank()) {
                        onConfirm(enteredCoins, selectedMethod, recipientAccount.trim())
                    }
                },
                enabled = isValidAmount && recipientAccount.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_withdraw_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricGold),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "SUBMIT WITHDRAWAL ($%.2f USD)".format(enteredUsd),
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF3E2723)
                )
            }
        }
    )
}
