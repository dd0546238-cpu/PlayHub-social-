package com.example.ui.game

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BikeSpec
import com.example.model.DEFAULT_BIKES
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricGold
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import kotlin.math.sin
import kotlin.random.Random

enum class EntityType {
    COIN,
    NITRO,
    MAGNET,
    CAR_RED,
    CAR_YELLOW,
    TRUCK_BLUE,
    CONE
}

data class GameEntity(
    val id: Long,
    val type: EntityType,
    var x: Float, // Normalized 0.2f .. 0.8f
    var y: Float, // Normalized -0.2f .. 1.2f
    val speed: Float,
    val sizePx: Float = 40f
)

@Composable
fun BikeRaceGameScreen(
    selectedBike: BikeSpec = DEFAULT_BIKES.first(),
    onClaimReward: (distance: Int, coins: Long, bonus: Long, maxSpeed: Int, bikeName: String) -> Unit,
    onBack: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    // Bike Dynamics
    var playerX by remember { mutableFloatStateOf(0.5f) } // 0.2f to 0.8f
    var targetPlayerX by remember { mutableFloatStateOf(0.5f) }
    var speedKmh by remember { mutableFloatStateOf(100f) }
    var maxSpeedReached by remember { mutableIntStateOf(100) }
    var distanceMeters by remember { mutableFloatStateOf(0f) }
    var coinsCollectedThisRun by remember { mutableLongStateOf(0L) }

    // Boost & Powerups
    var nitroTimeRemainingMs by remember { mutableLongStateOf(0L) }
    var magnetTimeRemainingMs by remember { mutableLongStateOf(0L) }
    var nitroCharges by remember { mutableIntStateOf(3) }
    var roadScrollOffset by remember { mutableFloatStateOf(0f) }

    // Entities (Coins, Traffic, Hazards)
    val entities = remember { mutableStateListOf<GameEntity>() }
    var entityCounter by remember { mutableLongStateOf(0L) }

    // Steering buttons state
    var isSteeringLeft by remember { mutableStateOf(false) }
    var isSteeringRight by remember { mutableStateOf(false) }
    var isAccelerating by remember { mutableStateOf(false) }
    var isBraking by remember { mutableStateOf(false) }

    fun resetGame() {
        playerX = 0.5f
        targetPlayerX = 0.5f
        speedKmh = 100f
        maxSpeedReached = 100
        distanceMeters = 0f
        coinsCollectedThisRun = 0L
        nitroTimeRemainingMs = 0L
        magnetTimeRemainingMs = 0L
        nitroCharges = 3
        entities.clear()
        isGameOver = false
        isPaused = false
        isPlaying = true
    }

    // Main Game Loop using withFrameNanos
    LaunchedEffect(isPlaying, isPaused, isGameOver) {
        if (!isPlaying || isPaused || isGameOver) return@LaunchedEffect

        var lastFrameTimeNanos = 0L
        var spawnTimer = 0f

        while (isPlaying && !isPaused && !isGameOver) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos == 0L) {
                    lastFrameTimeNanos = frameTimeNanos
                    return@withFrameNanos
                }

                val dtSeconds = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastFrameTimeNanos = frameTimeNanos

                // Steering logic
                if (isSteeringLeft) {
                    targetPlayerX = (targetPlayerX - 0.7f * dtSeconds).coerceIn(0.24f, 0.76f)
                }
                if (isSteeringRight) {
                    targetPlayerX = (targetPlayerX + 0.7f * dtSeconds).coerceIn(0.24f, 0.76f)
                }
                // Smooth bike interpolation
                playerX += (targetPlayerX - playerX) * 12f * dtSeconds

                // Acceleration & Nitro
                val isNitroActive = nitroTimeRemainingMs > 0
                if (isNitroActive) {
                    nitroTimeRemainingMs = maxOf(0L, nitroTimeRemainingMs - (dtSeconds * 1000).toLong())
                    speedKmh = (speedKmh + 90f * dtSeconds).coerceAtMost(selectedBike.topSpeedKmh.toFloat() + 40f)
                } else if (isAccelerating) {
                    speedKmh = (speedKmh + 45f * dtSeconds).coerceAtMost(selectedBike.topSpeedKmh.toFloat())
                } else if (isBraking) {
                    speedKmh = (speedKmh - 90f * dtSeconds).coerceAtLeast(50f)
                } else {
                    // Cruise speed
                    if (speedKmh > 110f) {
                        speedKmh -= 15f * dtSeconds
                    } else if (speedKmh < 100f) {
                        speedKmh += 15f * dtSeconds
                    }
                }

                if (speedKmh.toInt() > maxSpeedReached) {
                    maxSpeedReached = speedKmh.toInt()
                }

                // Distance update
                val speedMps = speedKmh * 0.277778f
                distanceMeters += speedMps * dtSeconds
                roadScrollOffset = (roadScrollOffset + speedMps * 4f * dtSeconds) % 200f

                // Powerup timers
                if (magnetTimeRemainingMs > 0) {
                    magnetTimeRemainingMs = maxOf(0L, magnetTimeRemainingMs - (dtSeconds * 1000).toLong())
                }

                // Entity Spawner
                spawnTimer += dtSeconds
                val spawnInterval = (200f / speedKmh).coerceIn(0.4f, 1.4f)
                if (spawnTimer >= spawnInterval) {
                    spawnTimer = 0f
                    val lane = when (Random.nextInt(3)) {
                        0 -> 0.32f
                        1 -> 0.50f
                        else -> 0.68f
                    }

                    val randType = Random.nextFloat()
                    val type = when {
                        randType < 0.45f -> EntityType.COIN
                        randType < 0.55f -> EntityType.NITRO
                        randType < 0.60f -> EntityType.MAGNET
                        randType < 0.75f -> EntityType.CAR_RED
                        randType < 0.90f -> EntityType.CAR_YELLOW
                        else -> EntityType.TRUCK_BLUE
                    }
                    val speed = if (type == EntityType.COIN || type == EntityType.NITRO || type == EntityType.MAGNET) {
                        0f
                    } else {
                        Random.nextFloat() * 40f + 40f // traffic speed
                    }

                    entities.add(
                        GameEntity(
                            id = ++entityCounter,
                            type = type,
                            x = lane,
                            y = -0.15f,
                            speed = speed
                        )
                    )
                }

                // Entity Movement & Collision
                val iterator = entities.listIterator()
                while (iterator.hasNext()) {
                    val e = iterator.next()
                    val relativeSpeedMps = (speedKmh - e.speed) * 0.277778f
                    e.y += (relativeSpeedMps * 0.007f) * dtSeconds

                    // Magnet pulling coins
                    if (magnetTimeRemainingMs > 0 && e.type == EntityType.COIN && e.y in 0.2f..0.85f) {
                        e.x += (playerX - e.x) * 4f * dtSeconds
                    }

                    // Check Collision with player bike (Bike is around y = 0.78f, width ~ 0.12f, height ~ 0.14f)
                    val dx = kotlin.math.abs(e.x - playerX)
                    val dy = kotlin.math.abs(e.y - 0.78f)

                    if (dx < 0.085f && dy < 0.08f) {
                        when (e.type) {
                            EntityType.COIN -> {
                                coinsCollectedThisRun += 5_000L // 5,000 Coins per pickup!
                                iterator.remove()
                            }
                            EntityType.NITRO -> {
                                nitroTimeRemainingMs = 4000L
                                nitroCharges = minOf(5, nitroCharges + 1)
                                coinsCollectedThisRun += 15_000L
                                iterator.remove()
                            }
                            EntityType.MAGNET -> {
                                magnetTimeRemainingMs = 6000L
                                coinsCollectedThisRun += 10_000L
                                iterator.remove()
                            }
                            EntityType.CAR_RED, EntityType.CAR_YELLOW, EntityType.TRUCK_BLUE, EntityType.CONE -> {
                                if (isNitroActive) {
                                    // Invulnerable during Nitro: Smash obstacles for bonus coins!
                                    coinsCollectedThisRun += 25_000L
                                    iterator.remove()
                                } else {
                                    // Crash!
                                    isGameOver = true
                                    break
                                }
                            }
                        }
                    } else if (e.y > 1.25f || e.y < -0.3f) {
                        // Remove offscreen
                        iterator.remove()
                    }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Canvas Rendering Road, Traffic, Bike, Effects
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isPlaying) {
                    if (!isPlaying) return@pointerInput
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        targetPlayerX = (targetPlayerX + dragAmount.x / size.width).coerceIn(0.24f, 0.76f)
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw Road Background & City Horizon
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050811),
                        Color(0xFF0B1426),
                        Color(0xFF131D33)
                    ),
                    startY = 0f,
                    endY = h * 0.35f
                ),
                size = Size(w, h * 0.35f)
            )

            // Distant neon horizon glow line
            drawLine(
                color = CyberPink.copy(alpha = 0.6f),
                start = Offset(0f, h * 0.35f),
                end = Offset(w, h * 0.35f),
                strokeWidth = 3f
            )

            // 2. Highway surface
            val roadLeft = w * 0.16f
            val roadRight = w * 0.84f
            val roadWidth = roadRight - roadLeft

            drawRect(
                color = Color(0xFF161E2E),
                topLeft = Offset(roadLeft, h * 0.35f),
                size = Size(roadWidth, h * 0.65f)
            )

            // Neon road borders
            drawLine(
                color = NeonCyan,
                start = Offset(roadLeft, h * 0.35f),
                end = Offset(roadLeft, h),
                strokeWidth = 6f
            )
            drawLine(
                color = NeonCyan,
                start = Offset(roadRight, h * 0.35f),
                end = Offset(roadRight, h),
                strokeWidth = 6f
            )

            // 3. Lane Dash Lines
            val lane1X = roadLeft + roadWidth / 3f
            val lane2X = roadLeft + (roadWidth * 2f / 3f)
            val dashHeight = 40f
            val gapHeight = 40f
            val period = dashHeight + gapHeight

            for (laneX in listOf(lane1X, lane2X)) {
                var curY = h * 0.35f + (roadScrollOffset % period) - period
                while (curY < h) {
                    if (curY >= h * 0.35f) {
                        drawLine(
                            color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                            start = Offset(laneX, curY),
                            end = Offset(laneX, curY + dashHeight),
                            strokeWidth = 4f
                        )
                    }
                    curY += period
                }
            }

            // 4. Render Entities (Obstacles, Coins, Nitro)
            entities.forEach { e ->
                val entityX = e.x * w
                val entityY = e.y * h

                if (entityY > h * 0.30f && entityY < h + 50f) {
                    when (e.type) {
                        EntityType.COIN -> {
                            // Spinning glowing coin
                            drawCircle(
                                color = ElectricGold,
                                radius = 18f,
                                center = Offset(entityX, entityY)
                            )
                            drawCircle(
                                color = Color(0xFFFFF9C4),
                                radius = 12f,
                                center = Offset(entityX, entityY)
                            )
                            drawCircle(
                                color = Color(0xFFFF8F00),
                                radius = 18f,
                                center = Offset(entityX, entityY),
                                style = Stroke(width = 3f)
                            )
                        }
                        EntityType.NITRO -> {
                            // Nitro fuel cylinder
                            drawRoundRect(
                                color = CyberPink,
                                topLeft = Offset(entityX - 14f, entityY - 22f),
                                size = Size(28f, 44f),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(entityX - 8f, entityY - 14f),
                                size = Size(16f, 20f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                        EntityType.MAGNET -> {
                            // Magnet icon
                            drawCircle(
                                color = CyberPurple,
                                radius = 20f,
                                center = Offset(entityX, entityY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 10f,
                                center = Offset(entityX, entityY),
                                style = Stroke(width = 4f)
                            )
                        }
                        EntityType.CAR_RED -> {
                            drawVehicle(entityX, entityY, Color(0xFFE53935), Color(0xFFFFCDD2))
                        }
                        EntityType.CAR_YELLOW -> {
                            drawVehicle(entityX, entityY, Color(0xFFFDD835), Color(0xFFFFF9C4))
                        }
                        EntityType.TRUCK_BLUE -> {
                            // Larger truck
                            drawRoundRect(
                                color = Color(0xFF1E88E5),
                                topLeft = Offset(entityX - 22f, entityY - 38f),
                                size = Size(44f, 76f),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                            drawRoundRect(
                                color = Color(0xFF0D47A1),
                                topLeft = Offset(entityX - 18f, entityY - 14f),
                                size = Size(36f, 32f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                        EntityType.CONE -> {
                            drawCircle(
                                color = Color(0xFFFF6D00),
                                radius = 15f,
                                center = Offset(entityX, entityY)
                            )
                        }
                    }
                }
            }

            // 5. Draw Player Motorcycle
            val bikeCenter = Offset(playerX * w, h * 0.78f)
            val isNitro = nitroTimeRemainingMs > 0

            // Headlight beam
            val headlightPath = Path().apply {
                moveTo(bikeCenter.x - 12f, bikeCenter.y - 30f)
                lineTo(bikeCenter.x - 70f, bikeCenter.y - 240f)
                lineTo(bikeCenter.x + 70f, bikeCenter.y - 240f)
                lineTo(bikeCenter.x + 12f, bikeCenter.y - 30f)
                close()
            }
            drawPath(
                path = headlightPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    startY = bikeCenter.y - 30f,
                    endY = bikeCenter.y - 240f
                )
            )

            // Nitro fire trails
            if (isNitro || isAccelerating) {
                val flameColor = if (isNitro) CyberPink else ElectricGold
                drawRoundRect(
                    color = flameColor,
                    topLeft = Offset(bikeCenter.x - 6f, bikeCenter.y + 30f),
                    size = Size(12f, if (isNitro) 45f else 22f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Invulnerability shield glow
            if (isNitro) {
                drawCircle(
                    color = CyberPink.copy(alpha = 0.35f),
                    radius = 48f,
                    center = bikeCenter
                )
                drawCircle(
                    color = CyberPink,
                    radius = 48f,
                    center = bikeCenter,
                    style = Stroke(width = 3f)
                )
            }

            // Bike Chassis
            val bikeColor = Color(selectedBike.colorPrimaryHex)
            // Rear tire
            drawRoundRect(
                color = Color(0xFF111111),
                topLeft = Offset(bikeCenter.x - 8f, bikeCenter.y + 16f),
                size = Size(16f, 26f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Front tire
            drawRoundRect(
                color = Color(0xFF111111),
                topLeft = Offset(bikeCenter.x - 6f, bikeCenter.y - 34f),
                size = Size(12f, 20f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Bike body
            drawRoundRect(
                color = bikeColor,
                topLeft = Offset(bikeCenter.x - 14f, bikeCenter.y - 22f),
                size = Size(28f, 44f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Rider helmet
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = Offset(bikeCenter.x, bikeCenter.y - 6f)
            )
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 7f,
                center = Offset(bikeCenter.x, bikeCenter.y - 7f)
            )
            // Handlebars
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(bikeCenter.x - 22f, bikeCenter.y - 18f),
                end = Offset(bikeCenter.x + 22f, bikeCenter.y - 18f),
                strokeWidth = 4f
            )
            // Brake lights
            drawCircle(
                color = if (isBraking) Color(0xFFFF1744) else Color(0xFFD50000),
                radius = if (isBraking) 6f else 4f,
                center = Offset(bikeCenter.x, bikeCenter.y + 24f)
            )
        }

        // HUD OVERLAY (Top Bar)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF131A2E).copy(alpha = 0.85f), CircleShape)
                        .testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Exit Game",
                        tint = Color.White
                    )
                }

                // Coins collected badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF131A2E).copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = ElectricGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "%,d".format(coinsCollectedThisRun),
                            fontWeight = FontWeight.Bold,
                            color = ElectricGold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "($%.3f)".format(coinsCollectedThisRun / 1_000_000.0),
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                // Pause button
                IconButton(
                    onClick = { if (isPlaying) isPaused = !isPaused },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF131A2E).copy(alpha = 0.85f), CircleShape)
                        .testTag("game_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Speedometer & Distance Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${speedKmh.toInt()} KM/H",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = "🏁 ${distanceMeters.toInt()} m",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Nitro & Magnet Active Pill
            if (nitroTimeRemainingMs > 0 || magnetTimeRemainingMs > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (nitroTimeRemainingMs > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberPink.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "🔥 HYPER NITRO: ${(nitroTimeRemainingMs / 1000f + 0.1f).toInt()}s",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (magnetTimeRemainingMs > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberPurple.copy(alpha = 0.9f)
                        ) {
                            Text(
                                text = "🧲 COIN MAGNET: ${(magnetTimeRemainingMs / 1000f + 0.1f).toInt()}s",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // START SCREEN OVERLAY
        if (!isPlaying && !isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏍️ HIGHWAY RACER",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dodge traffic cars, collect coins, and trigger Nitro boosts to win real wallet earnings!",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected Bike Info
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1C253F),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(selectedBike.colorPrimaryHex), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = selectedBike.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Top Speed: ${selectedBike.topSpeedKmh} KM/H",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { resetGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("start_race_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF00363D)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "START RACE NOW",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00363D),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // GAME OVER / REWARD CLAIM OVERLAY
        if (isGameOver) {
            val speedBonusCoins = ((distanceMeters / 100).toLong() * 2000L).coerceAtLeast(10_000L)
            val totalCoins = coinsCollectedThisRun + speedBonusCoins

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, ElectricGold)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏁 RACE COMPLETED!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ElectricGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Great riding! Here are your race earnings:",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1C253F),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Distance Traveled", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                    Text("${distanceMeters.toInt()} meters", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Top Speed Reached", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                    Text("$maxSpeedReached KM/H", color = NeonCyan, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Road Coins Picked", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                    Text("%,d Coins".format(coinsCollectedThisRun), color = ElectricGold, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Speed & Distance Bonus", color = Color(0xFF94A3B8), fontSize = 13.sp)
                                    Text("+%,d Coins".format(speedBonusCoins), color = NeonGreen, fontWeight = FontWeight.Bold)
                                }
                                androidx.compose.material3.HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = Color(0xFF334155)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Earned", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "%,d Coins".format(totalCoins),
                                            color = ElectricGold,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            "≈ $%.3f USD".format(totalCoins / 1_000_000.0),
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Claim Reward Button
                        Button(
                            onClick = {
                                onClaimReward(
                                    distanceMeters.toInt(),
                                    coinsCollectedThisRun,
                                    speedBonusCoins,
                                    maxSpeedReached,
                                    selectedBike.name
                                )
                                isGameOver = false
                                isPlaying = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("claim_coins_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGold)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFF3E2723)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLAIM & ADD TO WALLET",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF3E2723),
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { resetGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("race_again_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RACE AGAIN", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ON-SCREEN GAME CONTROLS (Bottom Panel)
        if (isPlaying && !isGameOver) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left & Right Steering Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Steer Left
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .size(64.dp)
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            isSteeringLeft = event.changes.any { it.pressed }
                                        }
                                    }
                                }
                                .testTag("steer_left_btn"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Steer Left",
                                tint = NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Steer Right
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .size(64.dp)
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            isSteeringRight = event.changes.any { it.pressed }
                                        }
                                    }
                                }
                                .testTag("steer_right_btn"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Steer Right",
                                tint = NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Brake, Accel, and Nitro Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brake Button
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .size(56.dp)
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            isBraking = event.changes.any { it.pressed }
                                        }
                                    }
                                }
                                .testTag("brake_btn"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935).copy(alpha = 0.85f))
                        ) {
                            Text("BRAKE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Nitro Button
                        Button(
                            onClick = {
                                if (nitroCharges > 0 && nitroTimeRemainingMs <= 0) {
                                    nitroCharges--
                                    nitroTimeRemainingMs = 4000L
                                }
                            },
                            modifier = Modifier
                                .size(62.dp)
                                .testTag("nitro_btn"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (nitroCharges > 0) CyberPink else Color(0xFF334155)
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "Nitro",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("$nitroCharges", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Gas / Accelerate Button
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .size(68.dp)
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            isAccelerating = event.changes.any { it.pressed }
                                        }
                                    }
                                }
                                .testTag("accel_btn"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Gas",
                                    tint = Color(0xFF00381E),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text("GAS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF00381E))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawVehicle(x: Float, y: Float, bodyColor: Color, roofColor: Color) {
    // Car Body
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(x - 18f, y - 28f),
        size = Size(36f, 56f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Roof / Windshield
    drawRoundRect(
        color = roofColor,
        topLeft = Offset(x - 13f, y - 10f),
        size = Size(26f, 22f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Headlights
    drawCircle(
        color = Color(0xFFFFF9C4),
        radius = 4f,
        center = Offset(x - 12f, y - 24f)
    )
    drawCircle(
        color = Color(0xFFFFF9C4),
        radius = 4f,
        center = Offset(x + 12f, y - 24f)
    )
    // Taillights
    drawCircle(
        color = Color(0xFFFF1744),
        radius = 3f,
        center = Offset(x - 12f, y + 24f)
    )
    drawCircle(
        color = Color(0xFFFF1744),
        radius = 3f,
        center = Offset(x + 12f, y + 24f)
    )
}
