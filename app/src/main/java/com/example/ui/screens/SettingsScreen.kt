package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.ui.components.AstralCanvasBackground
import com.example.ui.theme.AstralDarkBg
import com.example.ui.theme.AstralDarkCard
import com.example.ui.theme.AstralDarkSurfaceBorder
import com.example.ui.theme.AstralDarkSurfaceVariant
import com.example.ui.theme.AstralIndigo
import com.example.ui.theme.AstralTextMuted
import com.example.ui.theme.AstralTextPrimary
import com.example.ui.theme.AstralTextSecondary
import com.example.ui.theme.AuroraTeal
import com.example.ui.theme.CosmicViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.WarningRed
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    profile: UserProfile?,
    allProfiles: List<UserProfile>,
    currentUserId: String,
    onSwitchUser: (String) -> Unit,
    onUpdateSettings: (displayName: String, timezone: String, reducedMotion: Boolean) -> Unit,
    onDeleteAccount: () -> Unit,
    onExportData: suspend () -> String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var displayName by remember(profile) { mutableStateOf(profile?.displayName ?: "") }
    var timezone by remember(profile) { mutableStateOf(profile?.timezone ?: "") }
    var reducedMotion by remember(profile) { mutableStateOf(profile?.reducedMotion ?: false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var newAccountNameInput by remember { mutableStateOf("") }
    var showSwitchDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(AstralDarkBg)) {
        AstralCanvasBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "SETTINGS & SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuroraTeal,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Preferences & Identity",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AstralTextPrimary
                    )
                }
            }

            // Multi-user Account Switcher (Verifies Data Isolation!)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(CosmicViolet.copy(alpha = 0.5f), AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("account_isolation_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = null, tint = CosmicViolet, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(text = "Active Profile Isolation", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AstralTextPrimary)
                                    Text(text = "ID: $currentUserId", fontSize = 11.sp, color = AstralTextMuted)
                                }
                            }
                            Button(
                                onClick = { showSwitchDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AstralDarkSurfaceVariant, contentColor = CosmicViolet),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("switch_account_button")
                            ) {
                                Text("Switch / New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Kaizen guarantees zero cross-user leakage. Each profile maintains isolated quests, Momentum XP, and stats.",
                            fontSize = 11.sp,
                            color = AstralTextSecondary
                        )
                    }
                }
            }

            // Profile Fields
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(text = "Personal Parameters", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AstralTextPrimary)

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth().testTag("settings_display_name_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AstralTextPrimary,
                                unfocusedTextColor = AstralTextPrimary,
                                focusedBorderColor = AuroraTeal,
                                unfocusedBorderColor = AstralDarkSurfaceBorder
                            )
                        )

                        OutlinedTextField(
                            value = timezone,
                            onValueChange = { timezone = it },
                            label = { Text("Timezone") },
                            modifier = Modifier.fillMaxWidth().testTag("settings_timezone_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AstralTextPrimary,
                                unfocusedTextColor = AstralTextPrimary,
                                focusedBorderColor = AuroraTeal,
                                unfocusedBorderColor = AstralDarkSurfaceBorder
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Reduced Motion", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AstralTextPrimary)
                                Text(text = "Silences starlight pulses and background oscillations", fontSize = 11.sp, color = AstralTextMuted)
                            }
                            Switch(
                                checked = reducedMotion,
                                onCheckedChange = { reducedMotion = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AuroraTeal,
                                    checkedTrackColor = AuroraTeal.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("reduced_motion_switch")
                            )
                        }

                        Button(
                            onClick = { onUpdateSettings(displayName, timezone, reducedMotion) },
                            colors = ButtonDefaults.buttonColors(containerColor = AuroraTeal, contentColor = Color(0xFF042F2E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("save_settings_button")
                        ) {
                            Text("Save Preferences", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Export Data Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Export Personal Journal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AstralTextPrimary)
                            Text(text = "Export full database records in portable JSON format", fontSize = 11.sp, color = AstralTextSecondary)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    exportedJsonText = onExportData()
                                    showExportDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AstralDarkSurfaceVariant, contentColor = AstralIndigo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("export_data_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Planned Integrations (Explicitly Marked as Planned / Phase 2)
            item {
                Text(
                    text = "UPCOMING INTEGRATIONS (PLANNED)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AstralTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlannedIntegrationCard(
                        title = "Health Connect & Google Fit",
                        desc = "Automate physical workouts and movement telemetry sync.",
                        icon = Icons.Default.DirectionsRun
                    )
                    PlannedIntegrationCard(
                        title = "Strava Resonance",
                        desc = "Sync outdoor trail running and cycling into Vitality XP.",
                        icon = Icons.Default.DirectionsRun
                    )
                    PlannedIntegrationCard(
                        title = "Notion Astral Ledger",
                        desc = "Export daily habit entries and study notes directly to Notion.",
                        icon = Icons.Default.MenuBook
                    )
                    PlannedIntegrationCard(
                        title = "Friends & Astral Cohorts",
                        desc = "Shared accountability circles without toxic social comparison.",
                        icon = Icons.Default.Group
                    )
                    PlannedIntegrationCard(
                        title = "Global Wayfinder Leaderboard",
                        desc = "Opt-in seasonal rank standings and collective milestones.",
                        icon = Icons.Default.Leaderboard
                    )
                }
            }

            // Dangerous Area: Delete Account
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(WarningRed.copy(alpha = 0.4f), AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Reset Profile / Clean Slate", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WarningRed)
                            Text(text = "Irreversibly wipe all XP, stats, quests, and history", fontSize = 11.sp, color = AstralTextMuted)
                        }
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningRed.copy(alpha = 0.15f), contentColor = WarningRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("delete_account_button")
                        ) {
                            Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Account Switch Dialog
        if (showSwitchDialog) {
            Dialog(onDismissRequest = { showSwitchDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AstralDarkCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(CosmicViolet, AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Profiles & Accounts", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AstralTextPrimary)
                            IconButton(onClick = { showSwitchDialog = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AstralTextSecondary)
                            }
                        }

                        // Existing profiles
                        allProfiles.forEach { p ->
                            val isCurrent = p.id == currentUserId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) AstralIndigo.copy(alpha = 0.2f) else AstralDarkSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSwitchUser(p.id)
                                        showSwitchDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = p.displayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AstralTextPrimary)
                                        Text(text = "ID: ${p.id} • Lvl ${p.level} • ${p.totalXp} XP", fontSize = 11.sp, color = AstralTextMuted)
                                    }
                                    if (isCurrent) {
                                        Text(text = "ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AuroraTeal)
                                    }
                                }
                            }
                        }

                        // Create / switch to test account
                        OutlinedTextField(
                            value = newAccountNameInput,
                            onValueChange = { newAccountNameInput = it },
                            label = { Text("New User ID (e.g. user_alt)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AstralTextPrimary,
                                unfocusedTextColor = AstralTextPrimary,
                                focusedBorderColor = CosmicViolet,
                                unfocusedBorderColor = AstralDarkSurfaceBorder
                            )
                        )

                        Button(
                            onClick = {
                                val id = newAccountNameInput.trim().ifBlank { "user_alt" }
                                onSwitchUser(id)
                                showSwitchDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicViolet, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create / Switch Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Export Data JSON Viewer Dialog
        if (showExportDialog) {
            Dialog(onDismissRequest = { showExportDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AstralDarkCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AuroraTeal, AstralDarkSurfaceBorder))
                    ),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Exported JSON Data", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AstralTextPrimary)
                            IconButton(onClick = { showExportDialog = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AstralTextSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AstralDarkBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .border(1.dp, AstralDarkSurfaceBorder, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = exportedJsonText,
                                fontSize = 11.sp,
                                color = AuroraTeal,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Button(
                            onClick = { showExportDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = AuroraTeal, contentColor = Color(0xFF042F2E)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Delete Account Alert Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Reset Astral Account?", color = AstralTextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "This will permanently erase all accumulated Momentum XP, stats, quests, completions, and Vault rewards for this profile. You will restart at Level 1.",
                        color = AstralTextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteAccount()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_delete_account_button")
                    ) {
                        Text("Erase Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = AstralTextSecondary)
                    }
                },
                containerColor = AstralDarkCard,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun PlannedIntegrationCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AstralDarkCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(AstralDarkSurfaceBorder, AstralDarkSurfaceBorder))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AstralDarkSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = AstralTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
                Column {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AstralTextPrimary)
                    Text(text = desc, fontSize = 11.sp, color = AstralTextMuted, lineHeight = 15.sp)
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = AstralDarkSurfaceVariant,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(SolarAmber.copy(alpha = 0.4f), AstralDarkSurfaceBorder))
                )
            ) {
                Text(
                    text = "Planned",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SolarAmber,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
