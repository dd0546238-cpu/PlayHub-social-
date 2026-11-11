package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.RaceRecordEntity
import com.example.data.local.WalletStateEntity
import com.example.data.repository.AppRepository
import com.example.model.CommentItem
import com.example.model.DEFAULT_BIKES
import com.example.model.GamingRoom
import com.example.model.GiftItem
import com.example.model.MediaType
import com.example.model.PostItem
import com.example.model.RoomChatMessage
import com.example.model.RoomSeat
import com.example.model.TransactionItem
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GiftCelebrationEvent(
    val giftEmoji: String,
    val giftName: String,
    val coins: Long,
    val recipient: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppRepository(database)
    }

    val posts: StateFlow<List<PostItem>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallet: StateFlow<WalletStateEntity> = repository.walletState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletStateEntity())

    val transactions: StateFlow<List<TransactionItem>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val raceRecords: StateFlow<List<RaceRecordEntity>> = repository.raceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow(UserProfile())
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _giftCelebration = MutableSharedFlow<GiftCelebrationEvent>()
    val giftCelebration: SharedFlow<GiftCelebrationEvent> = _giftCelebration.asSharedFlow()

    fun getComments(postId: String): StateFlow<List<CommentItem>> {
        return repository.getCommentsForPost(postId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun toggleLike(post: PostItem) {
        viewModelScope.launch {
            repository.toggleLike(post)
        }
    }

    fun toggleBookmark(post: PostItem) {
        viewModelScope.launch {
            repository.toggleBookmark(post)
        }
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(postId, text.trim(), _currentUser.value)
            _userMessage.emit("Comment posted!")
        }
    }

    fun sendGift(postId: String, authorName: String, gift: GiftItem, multiplier: Int = 1) {
        viewModelScope.launch {
            val totalCost = gift.coinCost * multiplier
            val success = repository.sendGift(postId, authorName, gift.name, gift.emoji, totalCost)
            if (success) {
                _giftCelebration.emit(
                    GiftCelebrationEvent(
                        giftEmoji = gift.emoji,
                        giftName = "${gift.name} x$multiplier",
                        coins = totalCost,
                        recipient = authorName
                    )
                )
                _userMessage.emit("Sent ${gift.emoji} ${gift.name} (%,d Coins) to $authorName!".format(totalCost))
            } else {
                _userMessage.emit("Insufficient coin balance! Please recharge to send gifts.")
            }
        }
    }

    fun createPost(caption: String, mediaUri: String, mediaType: MediaType, tags: List<String>) {
        viewModelScope.launch {
            repository.createPost(caption, mediaUri, mediaType, tags, _currentUser.value)
            _userMessage.emit("Your post has been published to RiderSocial! 🏍️")
        }
    }

    fun rechargeWallet(amountUsd: Double, coins: Long, method: String, accountRef: String) {
        viewModelScope.launch {
            repository.rechargeWallet(amountUsd, coins, method, accountRef)
            _userMessage.emit("Recharge successful! Added %,d Coins ($%.2f USD)".format(coins, amountUsd))
        }
    }

    fun withdrawWallet(coins: Long, method: String, recipientAccount: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val currentBalance = wallet.value.coinBalance
            if (coins < 1_000_000L) {
                val msg = "Minimum withdrawal is 1,000,000 Coins ($1.00 USD)"
                _userMessage.emit(msg)
                onComplete(false, msg)
                return@launch
            }
            if (currentBalance < coins) {
                val msg = "Insufficient balance! You have %,d Coins".format(currentBalance)
                _userMessage.emit(msg)
                onComplete(false, msg)
                return@launch
            }

            val success = repository.withdrawWallet(coins, method, recipientAccount)
            if (success) {
                val usdVal = coins / 1_000_000.0
                val msg = "Withdrawal request of $%.2f USD (%,d Coins) submitted via $method!".format(usdVal, coins)
                _userMessage.emit(msg)
                onComplete(true, msg)
            } else {
                val msg = "Withdrawal failed. Please try again."
                _userMessage.emit(msg)
                onComplete(false, msg)
            }
        }
    }

    fun onRaceFinished(
        distanceMeters: Int,
        coinsCollected: Long,
        speedBonusCoins: Long,
        maxSpeedKmh: Int,
        bikeName: String,
        onFinished: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val totalCoins = repository.recordRaceEarnings(
                distanceMeters,
                coinsCollected,
                speedBonusCoins,
                maxSpeedKmh,
                bikeName
            )
            val updatedUser = _currentUser.value.copy(
                racesWon = _currentUser.value.racesWon + 1,
                totalCoinsEarned = _currentUser.value.totalCoinsEarned + totalCoins
            )
            _currentUser.value = updatedUser
            _userMessage.emit("🏁 Race finished! You earned %,d Coins ($%.2f)!".format(totalCoins, totalCoins / 1_000_000.0))
            onFinished(totalCoins)
        }
    }

    fun claimDailyBonus(coins: Long = 50_000L) {
        viewModelScope.launch {
            repository.recordRaceEarnings(
                distanceMeters = 0,
                coinsCollected = coins,
                speedBonusCoins = 0L,
                maxSpeedKmh = 0,
                bikeName = "Daily Gamer Bonus Box"
            )
            _userMessage.emit("🎁 Claimed %,d Coins ($%.3f USD) from Daily Games Box!".format(coins, coins / 1_000_000.0))
        }
    }

    fun unlockBike(bikeId: String, costCoins: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.unlockBike(bikeId, costCoins)
            if (success) {
                _userMessage.emit("🎉 Bike unlocked successfully! Ready to race.")
            } else {
                _userMessage.emit("Not enough coins to unlock this bike!")
            }
            onResult(success)
        }
    }

    // --- GAMING ROOMS & AUDIENCE SEATS ---

    private val _gamingRooms = MutableStateFlow(createInitialGamingRooms())
    val gamingRooms: StateFlow<List<GamingRoom>> = _gamingRooms.asStateFlow()

    private val _activeRoom = MutableStateFlow<GamingRoom?>(null)
    val activeRoom: StateFlow<GamingRoom?> = _activeRoom.asStateFlow()

    private val _userSeatNumber = MutableStateFlow<Int?>(null)
    val userSeatNumber: StateFlow<Int?> = _userSeatNumber.asStateFlow()

    fun createGamingRoom(title: String, totalSeats: Int = 8, gameType: String = "🏍️ Highway Bike Race Live") {
        val user = _currentUser.value
        val hostSeat = RoomSeat(
            seatNumber = 1,
            occupantId = user.id,
            occupantName = user.name,
            occupantAvatar = user.avatarUrl,
            isMuted = false,
            isHost = true,
            coinsEarnedInRoom = 0L
        )
        val emptySeats = (2..totalSeats).map { RoomSeat(seatNumber = it) }
        val newRoom = GamingRoom(
            id = "room_" + System.currentTimeMillis(),
            title = title.ifBlank { "${user.name}'s Gaming Lounge" },
            hostId = user.id,
            hostName = user.name,
            hostAvatar = user.avatarUrl,
            gameTitle = gameType,
            totalMicSeats = totalSeats,
            seats = listOf(hostSeat) + emptySeats,
            audienceMembers = emptyList(),
            audienceCount = 1,
            coinPool = 50_000L,
            isLiveGameActive = true,
            liveDistanceMeters = 0,
            chatMessages = listOf(
                RoomChatMessage("sys_1", "System", "", "Welcome to the Gaming Room! Audience can take a seat or watch live.", isSystem = true)
            )
        )
        _gamingRooms.value = listOf(newRoom) + _gamingRooms.value
        _activeRoom.value = newRoom
        _userSeatNumber.value = 1
        viewModelScope.launch {
            _userMessage.emit("🎉 Gaming Room '${newRoom.title}' created! You are on Host Seat 1.")
        }
    }

    fun joinRoom(room: GamingRoom) {
        _activeRoom.value = room
        _userSeatNumber.value = null
        val user = _currentUser.value
        val updatedAudience = if (room.audienceMembers.none { it.id == user.id }) {
            room.audienceMembers + user
        } else {
            room.audienceMembers
        }
        val joinMsg = RoomChatMessage("sys_${System.currentTimeMillis()}", "System", "", "${user.name} joined the audience!", isSystem = true)
        val updated = room.copy(
            audienceMembers = updatedAudience,
            audienceCount = room.audienceCount + 1,
            chatMessages = room.chatMessages + joinMsg
        )
        _activeRoom.value = updated
        updateRoomInList(updated)
    }

    fun leaveRoom() {
        val room = _activeRoom.value ?: return
        val user = _currentUser.value
        val currentSeat = _userSeatNumber.value
        val updatedSeats = if (currentSeat != null) {
            room.seats.map {
                if (it.seatNumber == currentSeat && !it.isHost) RoomSeat(seatNumber = it.seatNumber) else it
            }
        } else room.seats

        val updated = room.copy(
            seats = updatedSeats,
            audienceMembers = room.audienceMembers.filter { it.id != user.id },
            audienceCount = maxOf(0, room.audienceCount - 1)
        )
        updateRoomInList(updated)
        _activeRoom.value = null
        _userSeatNumber.value = null
    }

    fun takeSeat(seatNumber: Int) {
        val room = _activeRoom.value ?: return
        val user = _currentUser.value
        val targetSeat = room.seats.firstOrNull { it.seatNumber == seatNumber }

        if (targetSeat != null && targetSeat.occupantName != null) {
            viewModelScope.launch {
                _userMessage.emit("Seat #$seatNumber is already taken!")
            }
            return
        }

        val updatedSeats = room.seats.map { seat ->
            if (seat.seatNumber == seatNumber) {
                seat.copy(
                    occupantId = user.id,
                    occupantName = user.name,
                    occupantAvatar = user.avatarUrl,
                    isMuted = false
                )
            } else if (seat.occupantId == user.id && !seat.isHost) {
                RoomSeat(seatNumber = seat.seatNumber)
            } else {
                seat
            }
        }

        val seatMsg = RoomChatMessage("sys_${System.currentTimeMillis()}", "System", "", "${user.name} took Seat #$seatNumber! 🎙️", isSystem = true)
        val updated = room.copy(
            seats = updatedSeats,
            chatMessages = room.chatMessages + seatMsg
        )
        _activeRoom.value = updated
        _userSeatNumber.value = seatNumber
        updateRoomInList(updated)
        viewModelScope.launch {
            _userMessage.emit("You are now seated on Mic Seat #$seatNumber!")
        }
    }

    fun leaveSeat() {
        val room = _activeRoom.value ?: return
        val currentSeat = _userSeatNumber.value ?: return
        val user = _currentUser.value

        val updatedSeats = room.seats.map { seat ->
            if (seat.seatNumber == currentSeat && !seat.isHost) {
                RoomSeat(seatNumber = seat.seatNumber)
            } else seat
        }

        val msg = RoomChatMessage("sys_${System.currentTimeMillis()}", "System", "", "${user.name} moved to Audience Seats.", isSystem = true)
        val updated = room.copy(
            seats = updatedSeats,
            chatMessages = room.chatMessages + msg
        )
        _activeRoom.value = updated
        _userSeatNumber.value = null
        updateRoomInList(updated)
        viewModelScope.launch {
            _userMessage.emit("You left the mic seat and are now sitting in the Audience.")
        }
    }

    fun toggleMicMute() {
        val room = _activeRoom.value ?: return
        val seatNum = _userSeatNumber.value ?: return
        val updatedSeats = room.seats.map {
            if (it.seatNumber == seatNum) it.copy(isMuted = !it.isMuted) else it
        }
        val isMutedNow = updatedSeats.firstOrNull { it.seatNumber == seatNum }?.isMuted == true
        val updated = room.copy(seats = updatedSeats)
        _activeRoom.value = updated
        updateRoomInList(updated)
        viewModelScope.launch {
            _userMessage.emit(if (isMutedNow) "Microphone muted" else "Microphone unmuted 🎙️")
        }
    }

    fun sendRoomMessage(text: String) {
        if (text.isBlank()) return
        val room = _activeRoom.value ?: return
        val user = _currentUser.value
        val chatMsg = RoomChatMessage(
            id = "msg_" + System.currentTimeMillis(),
            senderName = user.name,
            senderAvatar = user.avatarUrl,
            text = text.trim()
        )
        val updated = room.copy(chatMessages = room.chatMessages + chatMsg)
        _activeRoom.value = updated
        updateRoomInList(updated)
    }

    fun sendRoomGift(targetRecipient: String, gift: GiftItem, multiplier: Int = 1) {
        val room = _activeRoom.value ?: return
        val user = _currentUser.value
        val totalCost = gift.coinCost * multiplier

        viewModelScope.launch {
            val currentWallet = repository.walletState
            val success = repository.sendGift(
                postId = room.id,
                authorName = targetRecipient,
                giftName = gift.name,
                giftEmoji = gift.emoji,
                coins = totalCost
            )
            if (success) {
                val giftMsg = RoomChatMessage(
                    id = "gift_${System.currentTimeMillis()}",
                    senderName = user.name,
                    senderAvatar = user.avatarUrl,
                    text = "sent ${gift.emoji} ${gift.name} x$multiplier (%,d Coins) to $targetRecipient!".format(totalCost),
                    isGift = true
                )
                val updatedSeats = room.seats.map {
                    if (it.occupantName == targetRecipient) {
                        it.copy(coinsEarnedInRoom = it.coinsEarnedInRoom + totalCost)
                    } else it
                }
                val updated = room.copy(
                    coinPool = room.coinPool + totalCost,
                    seats = updatedSeats,
                    chatMessages = room.chatMessages + giftMsg
                )
                _activeRoom.value = updated
                updateRoomInList(updated)

                _giftCelebration.emit(
                    GiftCelebrationEvent(
                        giftEmoji = gift.emoji,
                        giftName = "${gift.name} x$multiplier",
                        coins = totalCost,
                        recipient = targetRecipient
                    )
                )
            } else {
                _userMessage.emit("Insufficient coin balance to send gift in room!")
            }
        }
    }

    private fun updateRoomInList(updated: GamingRoom) {
        _gamingRooms.value = _gamingRooms.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    private fun createInitialGamingRooms(): List<GamingRoom> {
        val r1Seats = listOf(
            RoomSeat(1, "host_1", "Zoe Cyber", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&fit=crop&q=80", false, true, 85000L),
            RoomSeat(2, "p_2", "Marcus Blaze", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&fit=crop&q=80", false, false, 35000L),
            RoomSeat(3, "p_3", "Rhea Stunt", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&fit=crop&q=80", true, false, 15000L),
            RoomSeat(4, null, null, null, false, false, 0L),
            RoomSeat(5, null, null, null, false, false, 0L),
            RoomSeat(6, null, null, null, false, false, 0L),
            RoomSeat(7, null, null, null, false, false, 0L),
            RoomSeat(8, null, null, null, false, false, 0L)
        )
        val r1Audience = listOf(
            UserProfile("aud_1", "Kavita Roy", "@kavita", "", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&fit=crop&q=80"),
            UserProfile("aud_2", "Sabbir Hasan", "@sabbir", "", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&fit=crop&q=80"),
            UserProfile("aud_3", "Imran Khan", "@imran_bd", "", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200&fit=crop&q=80")
        )
        val r1Messages = listOf(
            RoomChatMessage("m1", "System", "", "Welcome to Midnight Highway Championship Room! 🏍️", isSystem = true),
            RoomChatMessage("m2", "Zoe Cyber", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&fit=crop&q=80", "Who is ready for the 240 KM/H speed battle?"),
            RoomChatMessage("m3", "Marcus Blaze", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&fit=crop&q=80", "My Nitro bike is loaded! Take empty seats guys!"),
            RoomChatMessage("m4", "Kavita Roy", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&fit=crop&q=80", "Cheering from the audience seats! 👑🔥")
        )

        val room1 = GamingRoom(
            id = "room_midnight_race",
            title = "🏍️ Midnight Highway Race & Gifting Lounge",
            hostId = "host_1",
            hostName = "Zoe Cyber",
            hostAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&fit=crop&q=80",
            gameTitle = "🏍️ Highway Bike Race (8 Stage Seats)",
            totalMicSeats = 8,
            seats = r1Seats,
            audienceMembers = r1Audience,
            audienceCount = 38,
            coinPool = 450_000L,
            isLiveGameActive = true,
            liveDistanceMeters = 3420,
            chatMessages = r1Messages
        )

        val r2Seats = listOf(
            RoomSeat(1, "host_2", "Tariq Drift", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&fit=crop&q=80", false, true, 120000L),
            RoomSeat(2, null, null, null, false, false, 0L),
            RoomSeat(3, null, null, null, false, false, 0L),
            RoomSeat(4, null, null, null, false, false, 0L)
        )
        val room2 = GamingRoom(
            id = "room_stunt_battle",
            title = "🔥 1 Million Coins Drift & Stunt Battle",
            hostId = "host_2",
            hostName = "Tariq Drift",
            hostAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&fit=crop&q=80",
            gameTitle = "⚡ Speed & Drift Voice Arena",
            totalMicSeats = 4,
            seats = r2Seats,
            audienceMembers = emptyList(),
            audienceCount = 19,
            coinPool = 800_000L,
            isLiveGameActive = true,
            liveDistanceMeters = 1890,
            chatMessages = listOf(
                RoomChatMessage("m2_1", "System", "", "Welcome to 1M Coins Battle Room! Tap an empty seat to speak.", isSystem = true)
            )
        )

        return listOf(room1, room2)
    }
}

