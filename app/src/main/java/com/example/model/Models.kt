package com.example.model

import java.util.Locale

enum class MediaType {
    PHOTO,
    VIDEO
}

enum class TransactionType {
    RECHARGE,
    WITHDRAW,
    GAME_EARNED,
    GIFT_SENT,
    GIFT_RECEIVED
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED
}

data class UserProfile(
    val id: String = "user_me",
    val name: String = "Alex Apex",
    val username: String = "@apex_rider",
    val bio: String = "🏍️ Pro Stunt Rider | 🏁 Highway Racer | Gaming & Vlogging",
    val avatarUrl: String = "https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200&fit=crop&q=80",
    val followers: Int = 12480,
    val following: Int = 340,
    val racesWon: Int = 89,
    val totalCoinsEarned: Long = 4850000L
)

data class PostItem(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatar: String,
    val mediaUri: String,
    val mediaType: MediaType,
    val caption: String,
    val timestamp: Long,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val giftsCount: Int = 0,
    val totalGiftCoins: Long = 0L,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val tags: List<String> = emptyList()
)

data class CommentItem(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: Long
)

data class GiftItem(
    val id: String,
    val name: String,
    val emoji: String,
    val coinCost: Long,
    val description: String
) {
    val usdValue: Double
        get() = coinCost / 1_000_000.0
        
    val formattedUsd: String
        get() = String.format(Locale.US, "$%.3f", usdValue).trimEnd('0').let {
            if (it.endsWith(".")) it + "00" else it
        }
}

val AVAILABLE_GIFTS = listOf(
    GiftItem("rose", "Red Rose", "🌹", 1_000L, "Show some love"),
    GiftItem("chocolate", "Sweet Treat", "🍫", 5_000L, "Warm appreciation"),
    GiftItem("nitro", "Nitro Booster", "🚀", 25_000L, "High octane fuel"),
    GiftItem("helmet", "Neon Helmet", "🪖", 50_000L, "For safety & style"),
    GiftItem("diamond", "Diamond Gem", "💎", 100_000L, "Precious sparkle"),
    GiftItem("bike", "Super Bike", "🏍️", 500_000L, "Pure race machine"),
    GiftItem("crown", "Royal Cyber Crown", "👑", 1_000_000L, "Ultimate King ($1.00)")
)

data class TransactionItem(
    val id: String,
    val type: TransactionType,
    val amountCoins: Long,
    val methodOrTitle: String,
    val status: TransactionStatus,
    val timestamp: Long,
    val referenceId: String,
    val note: String = ""
) {
    val usdEquivalent: Double
        get() = amountCoins / 1_000_000.0
        
    val formattedUsd: String
        get() = String.format(Locale.US, "$%.2f", usdEquivalent)
}

data class BikeRaceRecord(
    val id: String,
    val distanceMeters: Int,
    val coinsCollected: Long,
    val speedBonusCoins: Long,
    val totalCoinsEarned: Long,
    val maxSpeedKmh: Int,
    val bikeName: String,
    val timestamp: Long
)

data class BikeSpec(
    val id: String,
    val name: String,
    val topSpeedKmh: Int,
    val acceleration: Float,
    val handling: Float,
    val priceCoins: Long,
    val isUnlocked: Boolean,
    val colorPrimaryHex: Long,
    val description: String
)

val DEFAULT_BIKES = listOf(
    BikeSpec("bike_street", "Street Hawk 400", 180, 0.65f, 0.70f, 0L, true, 0xFF00E5FF, "Nimble starter racer with great handling"),
    BikeSpec("bike_neon", "Neon Thunder 800", 220, 0.85f, 0.80f, 500_000L, false, 0xFFFF2A6D, "Turbo charged twin-engine road beast"),
    BikeSpec("bike_cyber", "Cyber Phantom 1200", 260, 0.95f, 0.90f, 1_500_000L, false, 0xFFFFB300, "Elite prototype with magnetic traction")
)

object CoinRates {
    const val COINS_PER_USD = 1_000_000L
    
    fun coinsToUsd(coins: Long): Double = coins.toDouble() / COINS_PER_USD.toDouble()
    
    fun formatCoins(coins: Long): String {
        return String.format(Locale.US, "%,d", coins)
    }
    
    fun formatUsd(usd: Double): String {
        return String.format(Locale.US, "$%.2f", usd)
    }
    
    fun coinsToFormattedUsd(coins: Long): String {
        return formatUsd(coinsToUsd(coins))
    }
}

data class RoomSeat(
    val seatNumber: Int,
    val occupantId: String? = null,
    val occupantName: String? = null,
    val occupantAvatar: String? = null,
    val isMuted: Boolean = false,
    val isHost: Boolean = false,
    val coinsEarnedInRoom: Long = 0L
)

data class RoomChatMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val text: String,
    val isSystem: Boolean = false,
    val isGift: Boolean = false
)

data class GamingRoom(
    val id: String,
    val title: String,
    val hostId: String,
    val hostName: String,
    val hostAvatar: String,
    val gameTitle: String = "🏍️ Highway Bike Race Live",
    val totalMicSeats: Int = 8,
    val seats: List<RoomSeat> = emptyList(),
    val audienceMembers: List<UserProfile> = emptyList(),
    val audienceCount: Int = 24,
    val coinPool: Long = 250_000L,
    val isLiveGameActive: Boolean = true,
    val liveDistanceMeters: Int = 1450,
    val chatMessages: List<RoomChatMessage> = emptyList(),
    val tags: List<String> = listOf("BikeRace", "LiveVoice", "AudienceSeats")
)

