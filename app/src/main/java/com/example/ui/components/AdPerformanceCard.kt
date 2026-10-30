package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AdStats
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GlassCardBg
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun AdPerformanceCard(
    stats: AdStats,
    modifier: Modifier = Modifier
) {
    val animatedClicks by animateIntAsState(targetValue = stats.adClicks, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "clicks")
    val animatedRewarded by animateIntAsState(targetValue = stats.rewardedWatched, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "rewarded")
    val animatedInterstitials by animateIntAsState(targetValue = stats.interstitialsShown, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "interstitials")
    val animatedSkipped by animateIntAsState(targetValue = stats.adsSkipped, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "skipped")
    val animatedCoins by animateIntAsState(targetValue = stats.coinsEarned, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "coins")
    val animatedSuccess by animateIntAsState(targetValue = stats.loadSuccess, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "success")
    val animatedFailed by animateIntAsState(targetValue = stats.loadFailed, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "failed")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ad_performance_card")
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x35281A4E),
                        GlassCardBg
                    )
                )
            )
            .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Ad Telemetry",
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AD PERFORMANCE & TELEMETRY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2-Column Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PerformanceMetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Total Ad Clicks",
                    value = "$animatedClicks",
                    icon = Icons.Default.TouchApp,
                    accentColor = Color(0xFF64FFDA),
                    tileGradient = listOf(Color(0x2E004D40), Color(0x1A00251A))
                )
                PerformanceMetricTile(
                    modifier = Modifier.weight(1f),
                    title = "CTR %",
                    value = String.format(Locale.US, "%.1f%%", stats.ctrPercent),
                    icon = Icons.Default.Percent,
                    accentColor = Color(0xFFFFD54F),
                    tileGradient = listOf(Color(0x2EFF6F00), Color(0x1A3E2723))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PerformanceMetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Rewarded Watched",
                    value = "$animatedRewarded",
                    icon = Icons.Default.CardGiftcard,
                    accentColor = Color(0xFFB388FF),
                    tileGradient = listOf(Color(0x2E4A148C), Color(0x1A1A0933))
                )
                PerformanceMetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Interstitials Shown",
                    value = "$animatedInterstitials",
                    icon = Icons.Default.Visibility,
                    accentColor = Color(0xFF80D8FF),
                    tileGradient = listOf(Color(0x2E01579B), Color(0x1A001F3F))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PerformanceMetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Ads Skipped",
                    value = "$animatedSkipped",
                    icon = Icons.Default.SkipNext,
                    accentColor = Color(0xFFFFAB91),
                    tileGradient = listOf(Color(0x2EBF360C), Color(0x1A330F00))
                )
                PerformanceMetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Total Coins Earned",
                    value = "$animatedCoins",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = AccentGold,
                    tileGradient = listOf(Color(0x2EFF8F00), Color(0x1A281A00))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PerformanceMetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Load Success",
                    value = "$animatedSuccess",
                    icon = Icons.Default.CheckCircle,
                    accentColor = StatusSuccess,
                    tileGradient = listOf(Color(0x2E1B5E20), Color(0x1A09230B))
                )
                PerformanceMetricTile(
                    modifier = Modifier.weight(1f),
                    title = "Load Failed",
                    value = "$animatedFailed",
                    icon = Icons.Default.Cancel,
                    accentColor = StatusError,
                    tileGradient = listOf(Color(0x2EB71C1C), Color(0x1A330505))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Estimated Earnings Tile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0x352E7D32),
                                Color(0x251B5E20),
                                Color(0x1A0B2B0F)
                            )
                        )
                    )
                    .border(1.dp, Color(0x6600E676), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3300E676)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = "Estimated Earnings",
                                tint = StatusSuccess,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ESTIMATED EARNINGS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Estimated (check Unity Dashboard for real earnings)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = TextTertiary
                            )
                        }
                    }

                    Text(
                        text = String.format(Locale.US, "$%.4f USD", stats.estimatedEarningsUsd),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = StatusSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    tileGradient: List<Color>
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(tileGradient))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor
            )
        }
    }
}
