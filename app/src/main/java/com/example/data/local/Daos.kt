package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("UPDATE posts SET likesCount = :newCount, isLiked = :liked WHERE id = :postId")
    suspend fun updateLike(postId: String, newCount: Int, liked: Boolean)

    @Query("UPDATE posts SET isBookmarked = :bookmarked WHERE id = :postId")
    suspend fun updateBookmark(postId: String, bookmarked: Boolean)

    @Query("UPDATE posts SET giftsCount = giftsCount + 1, totalGiftCoins = totalGiftCoins + :coins WHERE id = :postId")
    suspend fun addGiftToPost(postId: String, coins: Long)

    @Query("UPDATE posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentCount(postId: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}

@Dao
interface RaceRecordDao {
    @Query("SELECT * FROM race_records ORDER BY timestamp DESC")
    fun getAllRaceRecords(): Flow<List<RaceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaceRecord(record: RaceRecordEntity)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_state WHERE id = 1")
    fun getWalletState(): Flow<WalletStateEntity?>

    @Query("SELECT * FROM wallet_state WHERE id = 1")
    suspend fun getWalletStateSync(): WalletStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setWalletState(state: WalletStateEntity)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
}
