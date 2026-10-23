package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveFocusSession
import com.example.data.model.FocusSessionRecord
import com.example.ui.components.AstralCanvasBackground
import com.example.ui.components.AstralCard
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
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningRed

@Composable
fun FocusRoomScreen(
    activeSession: ActiveFocusSession?,
    remainingSeconds: Int,
    history: List<FocusSessionRecord>,
    onStartWork: () -> Unit,
    onStartBreak: () -> Unit,
    onTogglePause: () -> Unit,
    onCancel: () -> Unit,
    onManualComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = activeSession != null
    val isBreak = activeSession?.isBreak ?: false
    val isPaused = activeSession?.isPaused ?: false

    val totalDuration = activeSession?.durationSeconds ?: (25 * 60)
    val progress = if (totalDuration > 0) (remainingSeconds.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 1f

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(modifier = modifier.fillMaxSize().background(AstralDarkBg)) {
        AstralCanvasBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "FOCUS ROOM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralIndigo,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = if (isBreak) "Restorative Astral Break" else "25-Minute Resonance Chamber",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextPrimary
                    )
                }
            }

            // Reward callout
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AstralDarkCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AstralIndigo.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Full 25m Completion:",
                            fontSize = 12.sp,
                            color = AstralTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "+25 XP", fontSize = 12.sp, color = AuroraTeal, fontWeight = FontWeight.Bold)
                            Text(text = "+2 Focus", fontSize = 12.sp, color = AstralIndigo, fontWeight = FontWeight.Bold)
                            Text(text = "+1 Spark", fontSize = 12.sp, color = SolarAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Circular Astral Countdown Timer
            item {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Inactive background track
                        drawArc(
                            color = AstralDarkSurfaceVariant,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Glowing active progress arc
                        val sweep = 360f * progress
                        drawArc(
                            brush = Brush.sweepGradient(
                                if (isBreak) listOf(AuroraTeal, CosmicViolet, AuroraTeal)
                                else listOf(AstralIndigo, AuroraTeal, AstralIndigo)
                            ),
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AstralTextPrimary,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = when {
                                !isRunning -> "Ready to ignite"
                                isPaused -> "Paused"
                                isBreak -> "Resting"
                                else -> "Deep Focus Active"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPaused) SolarAmber else if (isBreak) CosmicViolet else AuroraTeal
                        )
                    }
                }
            }

            // Controls
            item {
                if (!isRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onStartWork,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("start_focus_work_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuroraTeal,
                                contentColor = Color(0xFF042F2E)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Start 25m Focus", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = onStartBreak,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("start_focus_break_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AstralDarkSurfaceVariant,
                                contentColor = CosmicViolet
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Start 5m Break", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTogglePause,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("toggle_pause_focus_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPaused) AuroraTeal else AstralDarkSurfaceVariant,
                                contentColor = if (isPaused) Color(0xFF042F2E) else AstralTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isPaused) "Resume" else "Pause", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onCancel,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("cancel_focus_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AstralDarkSurfaceVariant,
                                contentColor = WarningRed
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Abandon", fontWeight = FontWeight.SemiBold)
                        }

                        // Complete early button for testing / manual session finish
                        Button(
                            onClick = onManualComplete,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("complete_focus_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AuroraTealDim,
                                contentColor = Color(0xFF042F2E)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Finish", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Note on Persistence
            item {
                Text(
                    text = "Timer state is safely stored in local database; switching tabs or reopening the app preserves your active session.",
                    fontSize = 11.sp,
                    color = AstralTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Recent Focus History
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHAMBER LOG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${history.count { it.status == "COMPLETED" }} Completed",
                        fontSize = 12.sp,
                        color = AuroraTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (history.isEmpty()) {
                item {
                    Text(
                        text = "No focus sessions logged yet. Complete a 25m sprint to record your first entry.",
                        fontSize = 12.sp,
                        color = AstralTextMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(history.take(5)) { sess ->
                    FocusHistoryCard(session = sess)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun FocusHistoryCard(session: FocusSessionRecord) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (session.status == "COMPLETED") listOf(AuroraTeal.copy(alpha = 0.4f), AstralDarkSurfaceBorder)
                else listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (session.status == "COMPLETED") AuroraTeal.copy(alpha = 0.2f) else AstralDarkSurfaceVariant
                    ) {
                        Text(
                            text = session.status,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (session.status == "COMPLETED") AuroraTeal else AstralTextMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "${session.plannedDurationSeconds / 60}m Session",
                        fontSize = 12.sp,
                        color = AstralTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = session.localDate,
                    fontSize = 10.sp,
                    color = AstralTextMuted
                )
            }

            if (session.status == "COMPLETED") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "+${session.xpAwarded} XP", fontSize = 11.sp, color = AuroraTeal, fontWeight = FontWeight.Bold)
                    Text(text = "+${session.sparksAwarded} Spark", fontSize = 11.sp, color = SolarAmber, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(text = "0 XP (Cancelled)", fontSize = 11.sp, color = AstralTextMuted)
            }
        }
    }
}
