package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.engine.KaizenProgressionEngine
import com.example.data.model.Rank
import com.example.data.model.StatType
import com.example.ui.theme.AstralDarkBg
import com.example.ui.theme.AstralDarkCard
import com.example.ui.theme.AstralDarkSurfaceBorder
import com.example.ui.theme.AstralDarkSurfaceVariant
import com.example.ui.theme.AstralIndigo
import com.example.ui.theme.AstralIndigoDeep
import com.example.ui.theme.AstralTextMuted
import com.example.ui.theme.AstralTextPrimary
import com.example.ui.theme.AstralTextSecondary
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.AuroraTealDim
import com.example.ui.theme.CosmicViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SolarAmberDeep
import com.example.ui.theme.StarlightCyan
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AstralCanvasBackground(
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "starlight")
    val pulse by if (!reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        rememberInfiniteTransition(label = "static").animateFloat(
            initialValue = 0.6f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "static"
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Radial glow at top-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AuroraTeal.copy(alpha = 0.08f * pulse), Color.Transparent),
                center = Offset(w * 0.85f, h * 0.15f),
                radius = w * 0.6f
            ),
            radius = w * 0.6f,
            center = Offset(w * 0.85f, h * 0.15f)
        )

        // Radial glow at bottom-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CosmicViolet.copy(alpha = 0.07f * pulse), Color.Transparent),
                center = Offset(w * 0.15f, h * 0.85f),
                radius = w * 0.7f
            ),
            radius = w * 0.7f,
            center = Offset(w * 0.15f, h * 0.85f)
        )

        // Starlight field nodes
        val starPositions = listOf(
            Offset(w * 0.12f, h * 0.08f),
            Offset(w * 0.35f, h * 0.04f),
            Offset(w * 0.88f, h * 0.12f),
            Offset(w * 0.74f, h * 0.22f),
            Offset(w * 0.22f, h * 0.32f),
            Offset(w * 0.82f, h * 0.48f),
            Offset(w * 0.15f, h * 0.62f),
            Offset(w * 0.65f, h * 0.74f),
            Offset(w * 0.90f, h * 0.88f),
            Offset(w * 0.38f, h * 0.92f)
        )

        for (i in starPositions.indices) {
            val pt = starPositions[i]
            val alphaFactor = if (i % 2 == 0) pulse else (1.3f - pulse)
            drawCircle(
                color = StarlightCyan.copy(alpha = (0.25f * alphaFactor).coerceIn(0.1f, 0.8f)),
                radius = if (i % 3 == 0) 2.2f else 1.4f,
                center = pt
            )
        }

        // Faint constellation lines between adjacent stars
        drawLine(
            color = AstralIndigo.copy(alpha = 0.12f * pulse),
            start = starPositions[0],
            end = starPositions[1],
            strokeWidth = 1f
        )
        drawLine(
            color = AuroraTeal.copy(alpha = 0.10f * pulse),
            start = starPositions[2],
            end = starPositions[3],
            strokeWidth = 1f
        )
        drawLine(
            color = CosmicViolet.copy(alpha = 0.10f * pulse),
            start = starPositions[6],
            end = starPositions[7],
            strokeWidth = 1f
        )
    }
}

@Composable
fun AstralCard(
    modifier: Modifier = Modifier,
    borderColor: Color = AstralDarkSurfaceBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(borderColor, AstralDarkSurfaceBorder)))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun AstralDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, AstralDarkSurfaceBorder)))
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(6.dp)
                .background(AuroraTeal.copy(alpha = 0.7f), RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(AstralDarkSurfaceBorder, Color.Transparent)))
        )
    }
}

@Composable
fun MomentumXpBar(
    totalXp: Int,
    level: Int,
    xpInLevel: Int,
    modifier: Modifier = Modifier
) {
    val needed = KaizenProgressionEngine.xpNeededForLevel(level)
    val progress = (xpInLevel.toFloat() / needed.toFloat()).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Level $level Progress",
                fontSize = 13.sp,
                color = AstralTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$xpInLevel / $needed XP",
                fontSize = 13.sp,
                color = AuroraTeal,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AstralDarkSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AstralIndigo, AuroraTeal)
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Momentum: $totalXp XP",
                fontSize = 11.sp,
                color = AstralTextMuted
            )
            Text(
                text = "${(progress * 100).toInt()}% to Level ${level + 1}",
                fontSize = 11.sp,
                color = AstralTextMuted
            )
        }
    }
}

@Composable
fun RankBadge(rank: Rank, modifier: Modifier = Modifier) {
    val (badgeColor, borderColor) = when (rank) {
        Rank.SEED -> Pair(AuroraTealDim, AuroraTeal.copy(alpha = 0.4f))
        Rank.WAYFINDER -> Pair(AuroraTeal, AuroraTeal)
        Rank.VANGUARD -> Pair(AstralIndigo, AstralIndigoDeep)
        Rank.LUMINARY -> Pair(CosmicViolet, CosmicViolet)
        Rank.PARAGON -> Pair(SolarAmber, SolarAmberDeep)
        Rank.APEX -> Pair(StarlightCyan, AuroraTeal)
    }

    Surface(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        color = AstralDarkSurfaceVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = "Rank Icon",
                tint = badgeColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = rank.title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SparkBadge(sparks: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.border(1.dp, SolarAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        color = AstralDarkSurfaceVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Sparks Currency",
                tint = SolarAmber,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$sparks Sparks",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SolarAmber
            )
        }
    }
}

@Composable
fun RhythmBadge(rhythmDays: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.border(1.dp, AuroraTeal.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        color = AstralDarkSurfaceVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Rhythm Streak",
                tint = AuroraTeal,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$rhythmDays d Rhythm",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AuroraTeal
            )
        }
    }
}

@Composable
fun CompactStatCard(
    statType: StatType,
    value: Int,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (statType) {
        StatType.VITALITY -> Pair(Icons.Default.FitnessCenter, AuroraTeal)
        StatType.FOCUS -> Pair(Icons.Default.Psychology, AstralIndigo)
        StatType.KNOWLEDGE -> Pair(Icons.Default.MenuBook, CosmicViolet)
        StatType.RESOLVE -> Pair(Icons.Default.Shield, SolarAmber)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkSurfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(color.copy(alpha = 0.4f), AstralDarkSurfaceBorder)))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = statType.label,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = statType.label,
                    fontSize = 12.sp,
                    color = AstralTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$value",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AstralTextPrimary
            )
        }
    }
}

@Composable
fun LevelUpCelebrationModal(
    level: Int,
    rank: Rank,
    isRankPromotion: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AstralDarkCard,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AuroraTeal, CosmicViolet, SolarAmber))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("level_up_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Radiance Ring & Particles Canvas
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - 8.dp.toPx()

                        // Outer glowing radiance ring
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(AuroraTeal, AstralIndigo, CosmicViolet, SolarAmber, AuroraTeal)
                            ),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // 8 geometric starlight particles
                        for (i in 0 until 8) {
                            val angle = (i * Math.PI / 4).toFloat()
                            val pRadius = radius + 6.dp.toPx()
                            val px = center.x + pRadius * cos(angle)
                            val py = center.y + pRadius * sin(angle)
                            drawCircle(
                                color = if (i % 2 == 0) AuroraTeal else SolarAmber,
                                radius = 2.5.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Ascension",
                        tint = AuroraTeal,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = if (isRankPromotion) "RANK ELEVATED" else "MOMENTUM EXPANDED",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuroraTeal,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "Level $level Reached",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AstralTextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AstralDarkSurfaceVariant,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(CosmicViolet.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ASTRAL RANK: ${rank.title.uppercase()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicViolet,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rank.description,
                            fontSize = 12.sp,
                            color = AstralTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Text(
                    text = "Every threshold crossed is earned discipline compounding into quiet mastery.",
                    fontSize = 12.sp,
                    color = AstralTextMuted,
                    lineHeight = 16.sp
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_level_up_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuroraTeal,
                        contentColor = Color(0xFF042F2E)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continue Journey", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
