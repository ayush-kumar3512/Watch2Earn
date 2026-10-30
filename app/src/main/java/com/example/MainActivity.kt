package com.example

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ActionButtonsSection
import com.example.ui.components.AdPerformanceCard
import com.example.ui.components.AdsEngineStatusCard
import com.example.ui.components.CoinBalanceCard
import com.example.ui.components.CoinBurstOverlay
import com.example.ui.components.DeviceSessionCard
import com.example.ui.components.HeaderSection
import com.example.ui.components.NetworkLocationCard
import com.example.ui.components.ResetStatsDialog
import com.example.ui.theme.BgCosmicDark
import com.example.ui.theme.BgDeepPurple
import com.example.ui.theme.BgMidnightNavy
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.Watch2EarnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Watch2EarnTheme {
                MainAppScreen(activity = this)
            }
        }
    }
}

@Composable
fun MainAppScreen(
    activity: Activity,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    val adStats by viewModel.adStats.collectAsStateWithLifecycle()
    val sdkStatus by viewModel.sdkStatus.collectAsStateWithLifecycle()
    val rewardedState by viewModel.rewardedState.collectAsStateWithLifecycle()
    val interstitialState by viewModel.interstitialState.collectAsStateWithLifecycle()
    val isPendingRewarded by viewModel.isPendingRewarded.collectAsStateWithLifecycle()
    val isPendingInterstitial by viewModel.isPendingInterstitial.collectAsStateWithLifecycle()
    val rawLastError by viewModel.rawLastError.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val sessionSeconds by viewModel.sessionSeconds.collectAsStateWithLifecycle()
    val showResetDialog by viewModel.showResetDialog.collectAsStateWithLifecycle()
    val showCoinBurst by viewModel.showCoinBurst.collectAsStateWithLifecycle()

    // Initialize Unity Ads with Activity reference on launch
    LaunchedEffect(Unit) {
        viewModel.initAds(activity)
    }

    // Collect user notifications / snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { message ->
            when (message) {
                is AdUserMessage.RewardSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = message.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is AdUserMessage.Info -> {
                    snackbarHostState.showSnackbar(
                        message = message.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is AdUserMessage.Warning -> {
                    snackbarHostState.showSnackbar(
                        message = message.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Tactile haptic feedback handler
    LaunchedEffect(Unit) {
        viewModel.hapticTrigger.collect { hapticType ->
            try {
                when (hapticType) {
                    MainViewModel.HapticType.LIGHT -> {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    MainViewModel.HapticType.SUCCESS -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(
                                VibrationEffect.createWaveform(
                                    longArrayOf(0, 60, 80, 140),
                                    -1
                                )
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(120)
                        }
                    }
                    MainViewModel.HapticType.WARNING -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(
                                VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(80)
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore devices without vibrator hardware
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BgCosmicDark,
                            BgDeepPurple,
                            BgMidnightNavy
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderSection()

                CoinBalanceCard(coinsEarned = adStats.coinsEarned)

                ActionButtonsSection(
                    isPendingRewarded = isPendingRewarded,
                    isPendingInterstitial = isPendingInterstitial,
                    onWatchRewarded = { viewModel.onWatchRewardedClicked(activity) },
                    onShowInterstitial = { viewModel.onShowInterstitialClicked(activity) }
                )

                AdsEngineStatusCard(
                    sdkStatus = sdkStatus,
                    rewardedState = rewardedState,
                    interstitialState = interstitialState,
                    rawLastError = rawLastError,
                    onReloadAds = { viewModel.onReloadAdsClicked() }
                )

                AdPerformanceCard(stats = adStats)

                NetworkLocationCard(
                    locationState = locationState,
                    onRefresh = { viewModel.refreshLocation() }
                )

                DeviceSessionCard(
                    sessionSeconds = sessionSeconds,
                    sdkStatus = sdkStatus,
                    onResetStatsClicked = { viewModel.openResetDialog() }
                )

                // Footer section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "UNITY ADS SUITE  •  PACKAGE: ${Constants.PACKAGE_NAME}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time Production Environment  •  v1.0",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextTertiary.copy(alpha = 0.7f)
                    )
                }
            }

            // Coin burst overlay on reward
            CoinBurstOverlay(
                isVisible = showCoinBurst,
                onAnimationEnd = { viewModel.dismissCoinBurst() }
            )

            // Reset Confirmation Modal
            if (showResetDialog) {
                ResetStatsDialog(
                    onConfirm = { viewModel.confirmResetStats() },
                    onDismiss = { viewModel.dismissResetDialog() }
                )
            }
        }
    }
}
