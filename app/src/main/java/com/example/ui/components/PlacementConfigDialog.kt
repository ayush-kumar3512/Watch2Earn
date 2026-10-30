package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.UnityAdConfig
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementConfigDialog(
    currentConfig: UnityAdConfig,
    onSave: (gameId: String, rewardedId: String, interstitialId: String, testMode: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var gameId by remember(currentConfig) { mutableStateOf(currentConfig.gameId) }
    var rewardedId by remember(currentConfig) { mutableStateOf(currentConfig.rewardedPlacementId) }
    var interstitialId by remember(currentConfig) { mutableStateOf(currentConfig.interstitialPlacementId) }
    var testMode by remember(currentConfig) { mutableStateOf(currentConfig.testMode) }

    val isRewardedNumeric = rewardedId.isNotBlank() && rewardedId.all { it.isDigit() }
    val isInterstitialNumeric = interstitialId.isNotBlank() && interstitialId.all { it.isDigit() }
    val hasNumericPlacementError = isRewardedNumeric || isInterstitialNumeric

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("placement_config_dialog")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF160E2E))
                .border(1.5.dp, Color(0x668C52FF), RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x338C52FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Config",
                            tint = PrimaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Unity Ads Settings",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Game ID vs Placement ID Guide",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Important Guidance Note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x253B82F6))
                        .border(1.dp, Color(0x553B82F6), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Game ID number hota hai (jaise $gameId). Placement ID text naam hota hai (jaise 'rewardedVideo' aur 'video'). Dono alag hote hain!",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                            color = Color(0xFFDBEAFE)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentGold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip(
                        title = "BP_Rewarded_Android",
                        subtitle = "Your Production Placement",
                        isSelected = (rewardedId == "BP_Rewarded_Android" && interstitialId == "BP_Interstitial_Android"),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            rewardedId = "BP_Rewarded_Android"
                            interstitialId = "BP_Interstitial_Android"
                        }
                    )
                    PresetChip(
                        title = "rewardedVideo",
                        subtitle = "Unity Standard Default",
                        isSelected = (rewardedId == "rewardedVideo" && interstitialId == "video"),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            rewardedId = "rewardedVideo"
                            interstitialId = "video"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = gameId,
                    onValueChange = { gameId = it },
                    label = { Text("Unity Game ID (Numbers Only)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryViolet,
                        unfocusedBorderColor = Color(0x448C52FF),
                        focusedLabelColor = PrimaryViolet,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rewardedId,
                    onValueChange = { rewardedId = it },
                    label = { Text("Rewarded Placement ID (Name)") },
                    singleLine = true,
                    isError = isRewardedNumeric,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryViolet,
                        unfocusedBorderColor = Color(0x448C52FF),
                        focusedLabelColor = PrimaryViolet,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                if (isRewardedNumeric) {
                    Text(
                        text = "⚠️ Placement ID number nahi ho sakta! 'rewardedVideo' use karein.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = StatusError,
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = interstitialId,
                    onValueChange = { interstitialId = it },
                    label = { Text("Interstitial Placement ID (Name)") },
                    singleLine = true,
                    isError = isInterstitialNumeric,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryViolet,
                        unfocusedBorderColor = Color(0x448C52FF),
                        focusedLabelColor = PrimaryViolet,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                if (isInterstitialNumeric) {
                    Text(
                        text = "⚠️ Placement ID number nahi ho sakta! 'video' use karein.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = StatusError,
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x228C52FF))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = if (testMode) "Safe test ads active" else "Live production ads active",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = testMode,
                        onCheckedChange = { testMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentGold,
                            checkedTrackColor = Color(0xFF6B2BEE)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            val finalRewarded = if (isRewardedNumeric) "rewardedVideo" else rewardedId
                            val finalInterstitial = if (isInterstitialNumeric) "video" else interstitialId
                            onSave(gameId, finalRewarded, finalInterstitial, testMode)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryViolet,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save & Reload", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0x448C52FF) else Color(0x188C52FF))
            .border(
                1.dp,
                if (isSelected) PrimaryViolet else Color(0x338C52FF),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = AccentGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) AccentGold else TextPrimary,
                    maxLines = 1
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextTertiary
            )
        }
    }
}
