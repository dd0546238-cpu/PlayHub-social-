package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.MediaType
import com.example.model.TransactionStatus
import com.example.model.TransactionType

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
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
    val tagsCsv: String = ""
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: TransactionType,
    val amountCoins: Long,
    val methodOrTitle: String,
    val status: TransactionStatus,
    val timestamp: Long,
    val referenceId: String,
    val note: String = ""
)

@Entity(tableName = "race_records")
data class RaceRecordEntity(
    @PrimaryKey val id: String,
    val distanceMeters: Int,
    val coinsCollected: Long,
    val speedBonusCoins: Long,
    val totalCoinsEarned: Long,
    val maxSpeedKmh: Int,
    val bikeName: String,
    val timestamp: Long
)

@Entity(tableName = "wallet_state")
data class WalletStateEntity(
    @PrimaryKey val id: Int = 1,
    val coinBalance: Long = 1_500_000L, // Welcome bonus of 1.5M coins ($1.50)
    val totalEarnedCoins: Long = 1_500_000L,
    val totalRechargedCoins: Long = 0L,
    val totalWithdrawnCoins: Long = 0L,
    val unlockedBikesCsv: String = "bike_street"
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: Long
)
