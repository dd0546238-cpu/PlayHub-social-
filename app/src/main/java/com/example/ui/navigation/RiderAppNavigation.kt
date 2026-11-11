package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BikeSpec
import com.example.model.DEFAULT_BIKES
import com.example.model.GiftItem
import com.example.model.PostItem
import com.example.ui.GiftCelebrationEvent
import com.example.ui.MainViewModel
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.GiftBottomSheet
import com.example.ui.components.GiftCelebrationOverlay
import com.example.ui.feed.CreatePostScreen
import com.example.ui.feed.FeedScreen
import com.example.ui.game.BikeRaceGameScreen
import com.example.ui.game.GamesHubScreen
import com.example.ui.game.LiveGamingRoomScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan
import com.example.ui.wallet.WalletScreen

enum class AppScreen {
    FEED,             // Separate Feeds Screen
    GAMES_HUB,        // Separate Games Box / Arcade Zone
    WALLET,           // Wallet & Recharge / Withdraw
    PROFILE,          // Separate User Profile Screen
    PLAY_BIKE_RACE,   // Active 2D Bike Race Gameplay
    LIVE_GAMING_ROOM, // Live Gaming Room with Stage Seats & Audience Seating
    CREATE_POST       // Photo / Video Upload
}

@Composable
fun RiderAppNavigation(
    viewModel: MainViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.FEED) }
    var selectedBike by remember { mutableStateOf<BikeSpec>(DEFAULT_BIKES.first()) }

    // Bottom sheet dialog states
    var giftingPost by remember { mutableStateOf<PostItem?>(null) }
    var commentingPost by remember { mutableStateOf<PostItem?>(null) }

    // Floating celebration animation event
    var currentCelebration by remember { mutableStateOf<GiftCelebrationEvent?>(null) }

    val posts by viewModel.posts.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val raceRecords by viewModel.raceRecords.collectAsState()
    val gamingRooms by viewModel.gamingRooms.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    val userSeatNumber by viewModel.userSeatNumber.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.giftCelebration.collect { celebration ->
            currentCelebration = celebration
        }
    }

    // Dynamic Back Handler
    BackHandler(enabled = currentScreen != AppScreen.FEED) {
        currentScreen = when (currentScreen) {
            AppScreen.PLAY_BIKE_RACE -> AppScreen.GAMES_HUB
            AppScreen.LIVE_GAMING_ROOM -> {
                viewModel.leaveRoom()
                AppScreen.GAMES_HUB
            }
            AppScreen.CREATE_POST -> AppScreen.FEED
            else -> AppScreen.FEED
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Show bottom navigation on primary separate tabs (Feed, Games Box, Wallet, Profile)
            val isPrimaryTab = currentScreen in listOf(
                AppScreen.FEED,
                AppScreen.GAMES_HUB,
                AppScreen.WALLET,
                AppScreen.PROFILE
            )

            if (isPrimaryTab) {
                Surface(
                    color = DarkSurface,
                    border = BorderStroke(1.dp, DarkSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBar(
                        containerColor = DarkSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                    ) {
                        // 1. Dedicated Feeds Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.FEED,
                            onClick = { currentScreen = AppScreen.FEED },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.DynamicFeed,
                                    contentDescription = "Feeds"
                                )
                            },
                            label = { Text("Feeds", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00363D),
                                selectedTextColor = NeonCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("nav_feed")
                        )

                        // 2. Dedicated Games Box Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.GAMES_HUB,
                            onClick = { currentScreen = AppScreen.GAMES_HUB },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = "Games Box"
                                )
                            },
                            label = { Text("Games Box", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF3E2723),
                                selectedTextColor = ElectricGold,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = ElectricGold
                            ),
                            modifier = Modifier.testTag("nav_games_hub")
                        )

                        // 3. Dedicated Wallet Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.WALLET,
                            onClick = { currentScreen = AppScreen.WALLET },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet"
                                )
                            },
                            label = { Text("Wallet", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00363D),
                                selectedTextColor = NeonCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("nav_wallet")
                        )

                        // 4. Dedicated Separate User Profile Tab
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.PROFILE,
                            onClick = { currentScreen = AppScreen.PROFILE },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile"
                                )
                            },
                            label = { Text("Profile", fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00363D),
                                selectedTextColor = NeonCyan,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = NeonCyan
                            ),
                            modifier = Modifier.testTag("nav_profile")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                // 1. SEPARATE FEEDS SCREEN
                AppScreen.FEED -> {
                    FeedScreen(
                        posts = posts,
                        coinBalance = wallet.coinBalance,
                        onLikePost = { post -> viewModel.toggleLike(post) },
                        onBookmarkPost = { post -> viewModel.toggleBookmark(post) },
                        onOpenGifting = { post -> giftingPost = post },
                        onOpenComments = { post -> commentingPost = post },
                        onNavigateToUpload = { currentScreen = AppScreen.CREATE_POST },
                        onNavigateToWallet = { currentScreen = AppScreen.WALLET }
                    )
                }

                // 2. SEPARATE GAMES BOX SCREEN
                AppScreen.GAMES_HUB -> {
                    GamesHubScreen(
                        wallet = wallet,
                        raceRecords = raceRecords,
                        gamingRooms = gamingRooms,
                        selectedBike = selectedBike,
                        onSelectBike = { bike -> selectedBike = bike },
                        onUnlockBike = { bike ->
                            viewModel.unlockBike(bike.id, bike.priceCoins) { success ->
                                if (success) selectedBike = bike
                            }
                        },
                        onStartBikeRace = { currentScreen = AppScreen.PLAY_BIKE_RACE },
                        onCreateGamingRoom = { title, seats, gameType ->
                            viewModel.createGamingRoom(title, seats, gameType)
                            currentScreen = AppScreen.LIVE_GAMING_ROOM
                        },
                        onJoinGamingRoom = { room ->
                            viewModel.joinRoom(room)
                            currentScreen = AppScreen.LIVE_GAMING_ROOM
                        },
                        onClaimDailyReward = { viewModel.claimDailyBonus() },
                        onNavigateToWallet = { currentScreen = AppScreen.WALLET }
                    )
                }

                // 3. SEPARATE USER PROFILE SCREEN
                AppScreen.PROFILE -> {
                    val myPosts = posts.filter { it.authorId == currentUser.id }
                    ProfileScreen(
                        user = currentUser,
                        wallet = wallet,
                        userPosts = myPosts,
                        onNavigateToWallet = { currentScreen = AppScreen.WALLET },
                        onNavigateToUpload = { currentScreen = AppScreen.CREATE_POST }
                    )
                }

                // 4. WALLET & RECHARGE / WITHDRAW SCREEN
                AppScreen.WALLET -> {
                    WalletScreen(
                        wallet = wallet,
                        transactions = transactions,
                        onRecharge = { usd, coins, method, account ->
                            viewModel.rechargeWallet(usd, coins, method, account)
                        },
                        onWithdraw = { coins, method, recipient ->
                            viewModel.withdrawWallet(coins, method, recipient) { _, _ -> }
                        }
                    )
                }

                // SUB-SCREEN: LIVE GAMING ROOM (STAGE SEATS & AUDIENCE SEATS)
                AppScreen.LIVE_GAMING_ROOM -> {
                    activeRoom?.let { room ->
                        LiveGamingRoomScreen(
                            room = room,
                            currentUser = currentUser,
                            userSeatNumber = userSeatNumber,
                            onTakeSeat = { seatNum -> viewModel.takeSeat(seatNum) },
                            onLeaveSeat = { viewModel.leaveSeat() },
                            onToggleMic = { viewModel.toggleMicMute() },
                            onSendMessage = { text -> viewModel.sendRoomMessage(text) },
                            onSendGift = { target, gift -> viewModel.sendRoomGift(target, gift) },
                            onLeaveRoom = {
                                viewModel.leaveRoom()
                                currentScreen = AppScreen.GAMES_HUB
                            }
                        )
                    }
                }

                // SUB-SCREEN: BIKE RACE GAMEPLAY
                AppScreen.PLAY_BIKE_RACE -> {
                    BikeRaceGameScreen(
                        selectedBike = selectedBike,
                        onClaimReward = { distance, coins, bonus, maxSpeed, bikeName ->
                            viewModel.onRaceFinished(distance, coins, bonus, maxSpeed, bikeName) {
                                currentScreen = AppScreen.GAMES_HUB
                            }
                        },
                        onBack = { currentScreen = AppScreen.GAMES_HUB }
                    )
                }

                // SUB-SCREEN: CREATE POST (UPLOAD PHOTO / VIDEO)
                AppScreen.CREATE_POST -> {
                    CreatePostScreen(
                        onPublish = { caption, mediaUri, mediaType, tags ->
                            viewModel.createPost(caption, mediaUri, mediaType, tags)
                            currentScreen = AppScreen.FEED
                        },
                        onBack = { currentScreen = AppScreen.FEED }
                    )
                }
            }

            // Gifting Bottom Sheet
            giftingPost?.let { post ->
                GiftBottomSheet(
                    post = post,
                    currentCoinBalance = wallet.coinBalance,
                    onSendGift = { gift, multiplier ->
                        viewModel.sendGift(post.id, post.authorName, gift, multiplier)
                    },
                    onNavigateToRecharge = {
                        giftingPost = null
                        currentScreen = AppScreen.WALLET
                    },
                    onDismiss = { giftingPost = null }
                )
            }

            // Comments Bottom Sheet
            commentingPost?.let { post ->
                CommentsBottomSheet(
                    post = post,
                    viewModel = viewModel,
                    onDismiss = { commentingPost = null }
                )
            }

            // Floating Gift Celebration Overlay
            GiftCelebrationOverlay(
                event = currentCelebration,
                onDismiss = { currentCelebration = null }
            )
        }
    }
}
