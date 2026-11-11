package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CommentEntity
import com.example.data.local.PostEntity
import com.example.data.local.RaceRecordEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.WalletStateEntity
import com.example.model.CommentItem
import com.example.model.MediaType
import com.example.model.PostItem
import com.example.model.TransactionItem
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AppRepository(private val database: AppDatabase) {

    val allPosts: Flow<List<PostItem>> = database.postDao().getAllPosts().map { entities ->
        entities.map { it.toModel() }
    }

    val walletState: Flow<WalletStateEntity> = database.walletDao().getWalletState().map {
        it ?: WalletStateEntity()
    }

    val transactions: Flow<List<TransactionItem>> = database.transactionDao().getAllTransactions().map { list ->
        list.map { it.toModel() }
    }

    val raceRecords: Flow<List<RaceRecordEntity>> = database.raceRecordDao().getAllRaceRecords()

    fun getCommentsForPost(postId: String): Flow<List<CommentItem>> {
        return database.commentDao().getCommentsForPost(postId).map { list ->
            list.map { CommentItem(it.id, it.postId, it.authorName, it.authorAvatar, it.text, it.timestamp) }
        }
    }

    suspend fun createPost(
        caption: String,
        mediaUri: String,
        mediaType: MediaType,
        tags: List<String>,
        user: UserProfile
    ) {
        val newPost = PostEntity(
            id = "post_" + UUID.randomUUID().toString().take(8),
            authorId = user.id,
            authorName = user.name,
            authorUsername = user.username,
            authorAvatar = user.avatarUrl,
            mediaUri = mediaUri,
            mediaType = mediaType,
            caption = caption,
            timestamp = System.currentTimeMillis(),
            likesCount = 0,
            commentsCount = 0,
            giftsCount = 0,
            totalGiftCoins = 0L,
            isLiked = false,
            isBookmarked = false,
            tagsCsv = tags.joinToString(",")
        )
        database.postDao().insertPost(newPost)
    }

    suspend fun toggleLike(post: PostItem) {
        val newLiked = !post.isLiked
        val newCount = if (newLiked) post.likesCount + 1 else maxOf(0, post.likesCount - 1)
        database.postDao().updateLike(post.id, newCount, newLiked)
    }

    suspend fun toggleBookmark(post: PostItem) {
        database.postDao().updateBookmark(post.id, !post.isBookmarked)
    }

    suspend fun addComment(postId: String, text: String, user: UserProfile) {
        val comment = CommentEntity(
            id = "c_" + UUID.randomUUID().toString().take(8),
            postId = postId,
            authorName = user.name,
            authorAvatar = user.avatarUrl,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        database.commentDao().insertComment(comment)
        database.postDao().incrementCommentCount(postId)
    }

    suspend fun sendGift(postId: String, authorName: String, giftName: String, giftEmoji: String, coins: Long): Boolean {
        val currentWallet = database.walletDao().getWalletStateSync() ?: WalletStateEntity()
        if (currentWallet.coinBalance < coins) {
            return false // Insufficient balance
        }

        val updatedWallet = currentWallet.copy(
            coinBalance = currentWallet.coinBalance - coins
        )
        database.walletDao().setWalletState(updatedWallet)
        database.postDao().addGiftToPost(postId, coins)

        // Record gift transaction
        val tx = TransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            type = TransactionType.GIFT_SENT,
            amountCoins = coins,
            methodOrTitle = "Sent $giftEmoji $giftName to $authorName",
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            referenceId = "GF-" + UUID.randomUUID().toString().take(6).uppercase(),
            note = "Gift awarded on post #$postId"
        )
        database.transactionDao().insertTransaction(tx)
        return true
    }

    suspend fun rechargeWallet(amountUsd: Double, coins: Long, paymentMethod: String, accountRef: String) {
        val currentWallet = database.walletDao().getWalletStateSync() ?: WalletStateEntity()
        val updated = currentWallet.copy(
            coinBalance = currentWallet.coinBalance + coins,
            totalRechargedCoins = currentWallet.totalRechargedCoins + coins
        )
        database.walletDao().setWalletState(updated)

        val tx = TransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            type = TransactionType.RECHARGE,
            amountCoins = coins,
            methodOrTitle = "Recharge via $paymentMethod ($accountRef)",
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            referenceId = "RC-" + UUID.randomUUID().toString().take(6).uppercase(),
            note = "Deposited $$amountUsd -> +$coins Coins"
        )
        database.transactionDao().insertTransaction(tx)
    }

    suspend fun withdrawWallet(coins: Long, method: String, recipientAccount: String): Boolean {
        val currentWallet = database.walletDao().getWalletStateSync() ?: WalletStateEntity()
        if (currentWallet.coinBalance < coins) {
            return false
        }
        val updated = currentWallet.copy(
            coinBalance = currentWallet.coinBalance - coins,
            totalWithdrawnCoins = currentWallet.totalWithdrawnCoins + coins
        )
        database.walletDao().setWalletState(updated)

        val usdVal = coins / 1_000_000.0
        val tx = TransactionEntity(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            type = TransactionType.WITHDRAW,
            amountCoins = coins,
            methodOrTitle = "Withdraw to $method ($recipientAccount)",
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            referenceId = "WD-" + UUID.randomUUID().toString().take(6).uppercase(),
            note = "Payout $$usdVal transferred to $recipientAccount"
        )
        database.transactionDao().insertTransaction(tx)
        return true
    }

    suspend fun recordRaceEarnings(
        distanceMeters: Int,
        coinsCollected: Long,
        speedBonusCoins: Long,
        maxSpeedKmh: Int,
        bikeName: String
    ): Long {
        val totalEarned = coinsCollected + speedBonusCoins
        if (totalEarned > 0) {
            val currentWallet = database.walletDao().getWalletStateSync() ?: WalletStateEntity()
            val updated = currentWallet.copy(
                coinBalance = currentWallet.coinBalance + totalEarned,
                totalEarnedCoins = currentWallet.totalEarnedCoins + totalEarned
            )
            database.walletDao().setWalletState(updated)

            val tx = TransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                type = TransactionType.GAME_EARNED,
                amountCoins = totalEarned,
                methodOrTitle = "🏍️ Bike Race Reward ($bikeName)",
                status = TransactionStatus.COMPLETED,
                timestamp = System.currentTimeMillis(),
                referenceId = "BR-" + UUID.randomUUID().toString().take(6).uppercase(),
                note = "Distance: ${distanceMeters}m | Coins: $coinsCollected | Bonus: $speedBonusCoins"
            )
            database.transactionDao().insertTransaction(tx)
        }

        database.raceRecordDao().insertRaceRecord(
            RaceRecordEntity(
                id = "race_" + UUID.randomUUID().toString().take(8),
                distanceMeters = distanceMeters,
                coinsCollected = coinsCollected,
                speedBonusCoins = speedBonusCoins,
                totalCoinsEarned = totalEarned,
                maxSpeedKmh = maxSpeedKmh,
                bikeName = bikeName,
                timestamp = System.currentTimeMillis()
            )
        )
        return totalEarned
    }

    suspend fun unlockBike(bikeId: String, costCoins: Long): Boolean {
        val currentWallet = database.walletDao().getWalletStateSync() ?: WalletStateEntity()
        if (currentWallet.coinBalance < costCoins) return false

        val currentUnlocked = currentWallet.unlockedBikesCsv.split(",").toMutableSet()
        currentUnlocked.add(bikeId)

        val updated = currentWallet.copy(
            coinBalance = currentWallet.coinBalance - costCoins,
            unlockedBikesCsv = currentUnlocked.joinToString(",")
        )
        database.walletDao().setWalletState(updated)
        return true
    }

    private fun PostEntity.toModel(): PostItem {
        return PostItem(
            id = id,
            authorId = authorId,
            authorName = authorName,
            authorUsername = authorUsername,
            authorAvatar = authorAvatar,
            mediaUri = mediaUri,
            mediaType = mediaType,
            caption = caption,
            timestamp = timestamp,
            likesCount = likesCount,
            commentsCount = commentsCount,
            giftsCount = giftsCount,
            totalGiftCoins = totalGiftCoins,
            isLiked = isLiked,
            isBookmarked = isBookmarked,
            tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(",")
        )
    }

    private fun TransactionEntity.toModel(): TransactionItem {
        return TransactionItem(
            id = id,
            type = type,
            amountCoins = amountCoins,
            methodOrTitle = methodOrTitle,
            status = status,
            timestamp = timestamp,
            referenceId = referenceId,
            note = note
        )
    }
}
