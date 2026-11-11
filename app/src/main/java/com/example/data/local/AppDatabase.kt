package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.MediaType
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PostEntity::class,
        TransactionEntity::class,
        RaceRecordEntity::class,
        WalletStateEntity::class,
        CommentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun transactionDao(): TransactionDao
    abstract fun raceRecordDao(): RaceRecordDao
    abstract fun walletDao(): WalletDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rider_social_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            
            // Seed Wallet
            database.walletDao().setWalletState(
                WalletStateEntity(
                    id = 1,
                    coinBalance = 2_000_000L, // $2.00 starting balance
                    totalEarnedCoins = 2_000_000L,
                    totalRechargedCoins = 0L,
                    totalWithdrawnCoins = 0L,
                    unlockedBikesCsv = "bike_street"
                )
            )

            // Seed Welcome Bonus Transaction
            database.transactionDao().insertTransaction(
                TransactionEntity(
                    id = "tx_welcome_bonus",
                    type = TransactionType.GAME_EARNED,
                    amountCoins = 2_000_000L,
                    methodOrTitle = "🎁 Welcome Starter Bonus ($2.00)",
                    status = TransactionStatus.COMPLETED,
                    timestamp = now - 3600000,
                    referenceId = "WB-99248",
                    note = "Welcome to RiderSocial! Use coins to race, upgrade, or send gifts."
                )
            )

            // Seed Initial Posts
            val seedPosts = listOf(
                PostEntity(
                    id = "post_1",
                    authorId = "user_valkyrie",
                    authorName = "Zoe Cyber",
                    authorUsername = "@zoe_speed",
                    authorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&fit=crop&q=80",
                    mediaUri = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?w=900&fit=crop&q=80",
                    mediaType = MediaType.PHOTO,
                    caption = "Midnight highway run with the customized Neon Beast! 🏍️💨 Maxed out the speed on the coastal bridge tonight! Who wants to challenge my time?",
                    timestamp = now - 1800000,
                    likesCount = 342,
                    commentsCount = 28,
                    giftsCount = 14,
                    totalGiftCoins = 85_000L,
                    isLiked = true,
                    isBookmarked = false,
                    tagsCsv = "bikelife,neonnight,speedracer,superbike"
                ),
                PostEntity(
                    id = "post_2",
                    authorId = "user_drifter",
                    authorName = "Tariq Khan",
                    authorUsername = "@tariq_drift",
                    authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&fit=crop&q=80",
                    mediaUri = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?w=900&fit=crop&q=80",
                    mediaType = MediaType.VIDEO,
                    caption = "Insane 360 burnout and corner drift trick on track! ⚡🔥 Won 450,000 Coins in the championship race today! Withdraw request placed on bKash 💸",
                    timestamp = now - 7200000,
                    likesCount = 890,
                    commentsCount = 64,
                    giftsCount = 35,
                    totalGiftCoins = 320_000L,
                    isLiked = false,
                    isBookmarked = true,
                    tagsCsv = "stunt,drift,earnmoney,bkash,bikerace"
                ),
                PostEntity(
                    id = "post_3",
                    authorId = "user_ghost",
                    authorName = "Rhea Storm",
                    authorUsername = "@rhea_stunts",
                    authorAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&fit=crop&q=80",
                    mediaUri = "https://images.unsplash.com/photo-1558980664-769d59546b3d?w=900&fit=crop&q=80",
                    mediaType = MediaType.PHOTO,
                    caption = "Golden hour aesthetic with my matte black cafe racer 🌅 Send me a Nitro Booster gift and I will review your bike setup in the next video! 🚀",
                    timestamp = now - 18000000,
                    likesCount = 512,
                    commentsCount = 37,
                    giftsCount = 22,
                    totalGiftCoins = 140_000L,
                    isLiked = false,
                    isBookmarked = false,
                    tagsCsv = "caferacer,custombike,gifting,goldenhour"
                ),
                PostEntity(
                    id = "post_4",
                    authorId = "user_blaze",
                    authorName = "Marcus Blaze",
                    authorUsername = "@blaze_nitro",
                    authorAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&fit=crop&q=80",
                    mediaUri = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=900&fit=crop&q=80",
                    mediaType = MediaType.VIDEO,
                    caption = "Highway traffic lane splitting at 200 km/h! 🚨 Pure adrenaline rush. Test your reflexes in the Bike Race mini-game now to earn real coins! 🎮💰",
                    timestamp = now - 86400000,
                    likesCount = 1205,
                    commentsCount = 98,
                    giftsCount = 58,
                    totalGiftCoins = 650_000L,
                    isLiked = true,
                    isBookmarked = false,
                    tagsCsv = "adrenaline,lanesplitting,trafficrider,coinrush"
                )
            )
            database.postDao().insertPosts(seedPosts)

            // Seed Initial Comments
            val seedComments = listOf(
                CommentEntity("c1", "post_1", "Marcus Blaze", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&fit=crop&q=80", "That corner angle is legendary!! 🔥", now - 1600000),
                CommentEntity("c2", "post_1", "Rhea Storm", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&fit=crop&q=80", "Sending you a Nitro gift right now 🚀", now - 1400000),
                CommentEntity("c3", "post_2", "Zoe Cyber", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&fit=crop&q=80", "The tire smoke looks unreal bro! Congrats on the win!", now - 6500000)
            )
            seedComments.forEach { database.commentDao().insertComment(it) }
        }
    }
}
