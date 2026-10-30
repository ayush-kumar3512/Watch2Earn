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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AdPlacementState
import com.example.SdkStatus
import com.example.UnityAdConfig
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusNeutral
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AdsEngineStatusCard(
    sdkStatus: SdkStatus,
    rewardedState: AdPlacementState,
    interstitialState: AdPlacementState,
    adConfig: UnityAdConfig,
    rawLastError: String?,
    isPlacementNotFound: Boolean,
    onReloadAds: () -> Unit,
    onOpenConfig: () -> Unit,
    onSwitchToDefaultPlacements: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ads_engine_status_card")
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x35211545),
                        GlassCardBg
                    )
                )
            )
            .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (sdkStatus == SdkStatus.INITIALIZED) StatusSuccess else StatusWarning
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADS ENGINE STATUS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = TextPrimary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x228C52FF))
                            .border(1.dp, Color(0x558C52FF), RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenConfig)
                            .testTag("open_placement_settings_button")
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Placement Settings",
                                tint = Color(0xFFD4B2FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Config",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFD4B2FF)
                            )
                        }
                    }

                    // Reload Ads Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x338C52FF))
                            .border(1.dp, Color(0x668C52FF), RoundedCornerShape(10.dp))
                            .clickable(onClick = onReloadAds)
                            .testTag("reload_ads_button")
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Ads",
                                tint = Color(0xFFD4B2FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reload",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFD4B2FF)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Channel Status Rows
            StatusIndicatorRow(
                label = "Unity SDK Core (ID: ${adConfig.gameId})",
                statusText = when (sdkStatus) {
                    SdkStatus.INITIALIZED -> "INITIALIZED"
                    SdkStatus.INITIALIZING -> "INITIALIZING..."
                    SdkStatus.INITIALIZATION_FAILED -> "INIT FAILED"
                    SdkStatus.NOT_INITIALIZED -> "NOT INITIALIZED"
                },
                dotColor = when (sdkStatus) {
                    SdkStatus.INITIALIZED -> StatusSuccess
                    SdkStatus.INITIALIZING -> StatusWarning
                    SdkStatus.INITIALIZATION_FAILED -> StatusError
                    SdkStatus.NOT_INITIALIZED -> StatusNeutral
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusIndicatorRow(
                label = "Rewarded (${adConfig.rewardedPlacementId})",
                statusText = when (rewardedState) {
                    AdPlacementState.READY -> "READY"
                    AdPlacementState.LOADING -> "LOADING..."
                    AdPlacementState.SHOWING -> "PLAYING"
                    AdPlacementState.FAILED -> "FAILED (RETRYING)"
                    AdPlacementState.IDLE -> "IDLE"
                },
                dotColor = when (rewardedState) {
                    AdPlacementState.READY -> StatusSuccess
                    AdPlacementState.LOADING -> StatusWarning
                    AdPlacementState.SHOWING -> PrimaryViolet
                    AdPlacementState.FAILED -> StatusError
                    AdPlacementState.IDLE -> StatusNeutral
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusIndicatorRow(
                label = "Interstitial (${adConfig.interstitialPlacementId})",
                statusText = when (interstitialState) {
                    AdPlacementState.READY -> "READY"
                    AdPlacementState.LOADING -> "LOADING..."
                    AdPlacementState.SHOWING -> "PLAYING"
                    AdPlacementState.FAILED -> "FAILED (RETRYING)"
                    AdPlacementState.IDLE -> "IDLE"
                },
                dotColor = when (interstitialState) {
                    AdPlacementState.READY -> StatusSuccess
                    AdPlacementState.LOADING -> StatusWarning
                    AdPlacementState.SHOWING -> PrimaryViolet
                    AdPlacementState.FAILED -> StatusError
                    AdPlacementState.IDLE -> StatusNeutral
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Raw Error Console Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0A1F))
                    .border(
                        1.dp,
                        if (rawLastError != null) Color(0x66FF5252) else Color(0x228C52FF),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (rawLastError != null) Icons.Default.Warning else Icons.Default.Terminal,
                        contentDescription = "Diagnostics",
                        tint = if (rawLastError != null) StatusError else StatusSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (rawLastError != null) "RAW SDK ERROR TELEMETRY" else "SDK TELEMETRY CONSOLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp
                        ),
                        color = if (rawLastError != null) StatusError else StatusSuccess
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = rawLastError ?: "✓ No SDK error returned. Channels operating normally.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    color = if (rawLastError != null) Color(0xFFFFB4AB) else TextTertiary
                )

                // Solution Banner if Placement is not found
                if (isPlacementNotFound || (rawLastError != null && rawLastError.contains("Placement not found", ignoreCase = true))) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFAB00))
                            .border(1.dp, Color(0x66FFAB00), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Fix",
                                tint = AccentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "QUICK FIX FOR 'Placement not found'",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = AccentGold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Placement name mismatch! Aapke Unity Dashboard ke exact placement names 'BP_Rewarded_Android' aur 'BP_Interstitial_Android' set karne ke liye button dabayein:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                            color = Color(0xFFFFF8E1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onSwitchToDefaultPlacements,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF8F00),
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("fix_placement_preset_button")
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Set 'BP_Rewarded_Android'",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenConfig,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("custom_placement_button")
                            ) {
                                Text(
                                    text = "Edit IDs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIndicatorRow(
    label: String,
    statusText: String,
    dotColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = dotColor
            )
        }
    }
}
