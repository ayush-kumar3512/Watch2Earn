package com.example

import android.app.Activity
import android.content.Context
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

enum class SdkStatus {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED,
    INITIALIZATION_FAILED
}

enum class AdPlacementState {
    IDLE,
    LOADING,
    READY,
    SHOWING,
    FAILED
}

sealed interface AdUserMessage {
    data class RewardSuccess(val coins: Int, val message: String) : AdUserMessage
    data class Info(val message: String) : AdUserMessage
    data class Warning(val message: String) : AdUserMessage
}

class UnityAdsManager(
    private val context: Context,
    private val statsRepository: AdStatsRepository,
    private val scope: CoroutineScope
) {
    private val _adConfig = MutableStateFlow(UnityAdConfig())
    val adConfig: StateFlow<UnityAdConfig> = _adConfig.asStateFlow()

    private val _sdkStatus = MutableStateFlow(SdkStatus.NOT_INITIALIZED)
    val sdkStatus: StateFlow<SdkStatus> = _sdkStatus.asStateFlow()

    private val _rewardedState = MutableStateFlow(AdPlacementState.IDLE)
    val rewardedState: StateFlow<AdPlacementState> = _rewardedState.asStateFlow()

    private val _interstitialState = MutableStateFlow(AdPlacementState.IDLE)
    val interstitialState: StateFlow<AdPlacementState> = _interstitialState.asStateFlow()

    private val _rawLastError = MutableStateFlow<String?>(null)
    val rawLastError: StateFlow<String?> = _rawLastError.asStateFlow()

    private val _isPlacementNotFound = MutableStateFlow(false)
    val isPlacementNotFound: StateFlow<Boolean> = _isPlacementNotFound.asStateFlow()

    private val _userMessages = MutableSharedFlow<AdUserMessage>(extraBufferCapacity = 16)
    val userMessages: SharedFlow<AdUserMessage> = _userMessages.asSharedFlow()

    private val _isPendingRewarded = MutableStateFlow(false)
    val isPendingRewarded: StateFlow<Boolean> = _isPendingRewarded.asStateFlow()

    private val _isPendingInterstitial = MutableStateFlow(false)
    val isPendingInterstitial: StateFlow<Boolean> = _isPendingInterstitial.asStateFlow()

    private var pendingRewardedJob: Job? = null
    private var pendingInterstitialJob: Job? = null

    private var retryRewardedJob: Job? = null
    private var retryInterstitialJob: Job? = null

    private var currentActivityRef: WeakReference<Activity>? = null

    init {
        scope.launch {
            statsRepository.configFlow.collect { config ->
                val prev = _adConfig.value
                _adConfig.value = config
                if (prev != config && _sdkStatus.value == SdkStatus.INITIALIZED) {
                    if (prev.gameId != config.gameId || prev.testMode != config.testMode) {
                        initialize(currentActivityRef?.get(), force = true)
                    } else {
                        reloadAllAds()
                    }
                }
            }
        }
    }

    fun initialize(activity: Activity? = null, force: Boolean = false) {
        if (activity != null) {
            currentActivityRef = WeakReference(activity)
        }
        if (!force && (_sdkStatus.value == SdkStatus.INITIALIZING || _sdkStatus.value == SdkStatus.INITIALIZED)) {
            return
        }

        _sdkStatus.value = SdkStatus.INITIALIZING
        _rawLastError.value = null
        _isPlacementNotFound.value = false

        val config = _adConfig.value
        UnityAds.initialize(
            context.applicationContext,
            config.gameId,
            config.testMode,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    scope.launch(Dispatchers.Main) {
                        _sdkStatus.value = SdkStatus.INITIALIZED
                        loadRewardedAd()
                        loadInterstitialAd()
                    }
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError,
                    message: String
                ) {
                    scope.launch(Dispatchers.Main) {
                        _sdkStatus.value = SdkStatus.INITIALIZATION_FAILED
                        val rawError = "InitializationError: ${error.name} - $message"
                        _rawLastError.value = rawError
                        _userMessages.emit(AdUserMessage.Warning("Init failed: $message"))
                    }
                }
            }
        )
    }

    fun loadRewardedAd() {
        if (_rewardedState.value == AdPlacementState.LOADING || _rewardedState.value == AdPlacementState.SHOWING) {
            return
        }
        retryRewardedJob?.cancel()
        _rewardedState.value = AdPlacementState.LOADING

        val placementId = _adConfig.value.rewardedPlacementId
        UnityAds.load(
            placementId,
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placement: String) {
                    scope.launch(Dispatchers.Main) {
                        _rewardedState.value = AdPlacementState.READY
                        statsRepository.recordLoadSuccess()
                        _isPlacementNotFound.value = false

                        if (_isPendingRewarded.value) {
                            pendingRewardedJob?.cancel()
                            _isPendingRewarded.value = false
                            val activity = currentActivityRef?.get()
                            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                                showRewardedAdInternal(activity)
                            }
                        }
                    }
                }

                override fun onUnityAdsFailedToLoad(
                    placement: String,
                    error: UnityAds.UnityAdsLoadError,
                    message: String
                ) {
                    scope.launch(Dispatchers.Main) {
                        _rewardedState.value = AdPlacementState.FAILED
                        statsRepository.recordLoadFailed()
                        _rawLastError.value = "LoadError [$placement]: ${error.name} - $message"
                        if (message.contains("Placement not found", ignoreCase = true)) {
                            _isPlacementNotFound.value = true
                        }
                        scheduleRetry(placement, isRewarded = true)
                    }
                }
            }
        )
    }

    fun loadInterstitialAd() {
        if (_interstitialState.value == AdPlacementState.LOADING || _interstitialState.value == AdPlacementState.SHOWING) {
            return
        }
        retryInterstitialJob?.cancel()
        _interstitialState.value = AdPlacementState.LOADING

        val placementId = _adConfig.value.interstitialPlacementId
        UnityAds.load(
            placementId,
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placement: String) {
                    scope.launch(Dispatchers.Main) {
                        _interstitialState.value = AdPlacementState.READY
                        statsRepository.recordLoadSuccess()
                        _isPlacementNotFound.value = false

                        if (_isPendingInterstitial.value) {
                            pendingInterstitialJob?.cancel()
                            _isPendingInterstitial.value = false
                            val activity = currentActivityRef?.get()
                            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                                showInterstitialAdInternal(activity)
                            }
                        }
                    }
                }

                override fun onUnityAdsFailedToLoad(
                    placement: String,
                    error: UnityAds.UnityAdsLoadError,
                    message: String
                ) {
                    scope.launch(Dispatchers.Main) {
                        _interstitialState.value = AdPlacementState.FAILED
                        statsRepository.recordLoadFailed()
                        _rawLastError.value = "LoadError [$placement]: ${error.name} - $message"
                        if (message.contains("Placement not found", ignoreCase = true)) {
                            _isPlacementNotFound.value = true
                        }
                        scheduleRetry(placement, isRewarded = false)
                    }
                }
            }
        )
    }

    fun onUserRequestRewardedAd(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        if (_isPendingRewarded.value) return

        if (_rewardedState.value == AdPlacementState.READY) {
            showRewardedAdInternal(activity)
        } else {
            _isPendingRewarded.value = true
            if (_rewardedState.value != AdPlacementState.LOADING) {
                loadRewardedAd()
            }
            pendingRewardedJob?.cancel()
            pendingRewardedJob = scope.launch(Dispatchers.Main) {
                delay(30_000)
                if (_isPendingRewarded.value) {
                    _isPendingRewarded.value = false
                    _userMessages.emit(AdUserMessage.Info("No ad available right now, please try again"))
                }
            }
        }
    }

    fun onUserRequestInterstitialAd(activity: Activity) {
        currentActivityRef = WeakReference(activity)
        if (_isPendingInterstitial.value) return

        if (_interstitialState.value == AdPlacementState.READY) {
            showInterstitialAdInternal(activity)
        } else {
            _isPendingInterstitial.value = true
            if (_interstitialState.value != AdPlacementState.LOADING) {
                loadInterstitialAd()
            }
            pendingInterstitialJob?.cancel()
            pendingInterstitialJob = scope.launch(Dispatchers.Main) {
                delay(30_000)
                if (_isPendingInterstitial.value) {
                    _isPendingInterstitial.value = false
                    _userMessages.emit(AdUserMessage.Info("No ad available right now, please try again"))
                }
            }
        }
    }

    private fun showRewardedAdInternal(activity: Activity) {
        _rewardedState.value = AdPlacementState.SHOWING

        val placementId = _adConfig.value.rewardedPlacementId
        UnityAds.show(
            activity,
            placementId,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowClick(placement: String) {
                    scope.launch(Dispatchers.Main) {
                        statsRepository.recordAdClick()
                    }
                }

                override fun onUnityAdsShowComplete(
                    placement: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    scope.launch(Dispatchers.Main) {
                        _rewardedState.value = AdPlacementState.IDLE
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            statsRepository.recordRewardedWatched()
                            statsRepository.addCoins(Constants.REWARD_COINS_PER_AD)
                            _userMessages.emit(
                                AdUserMessage.RewardSuccess(
                                    coins = Constants.REWARD_COINS_PER_AD,
                                    message = "Congratulations! You earned +${Constants.REWARD_COINS_PER_AD} coins!"
                                )
                            )
                        } else {
                            statsRepository.recordAdSkipped()
                            _userMessages.emit(AdUserMessage.Info("Ad skipped — watch till end to earn coins"))
                        }
                        loadRewardedAd()
                    }
                }

                override fun onUnityAdsShowFailure(
                    placement: String,
                    error: UnityAds.UnityAdsShowError,
                    message: String
                ) {
                    scope.launch(Dispatchers.Main) {
                        _rewardedState.value = AdPlacementState.IDLE
                        _rawLastError.value = "ShowError [$placement]: ${error.name} - $message"
                        _userMessages.emit(AdUserMessage.Warning("Ad display failed: $message"))
                        loadRewardedAd()
                    }
                }

                override fun onUnityAdsShowStart(placement: String) {}
            }
        )
    }

    private fun showInterstitialAdInternal(activity: Activity) {
        _interstitialState.value = AdPlacementState.SHOWING

        val placementId = _adConfig.value.interstitialPlacementId
        UnityAds.show(
            activity,
            placementId,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowClick(placement: String) {
                    scope.launch(Dispatchers.Main) {
                        statsRepository.recordAdClick()
                    }
                }

                override fun onUnityAdsShowComplete(
                    placement: String,
                    state: UnityAds.UnityAdsShowCompletionState
                ) {
                    scope.launch(Dispatchers.Main) {
                        _interstitialState.value = AdPlacementState.IDLE
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            statsRepository.recordInterstitialShown()
                        } else {
                            statsRepository.recordAdSkipped()
                        }
                        loadInterstitialAd()
                    }
                }

                override fun onUnityAdsShowFailure(
                    placement: String,
                    error: UnityAds.UnityAdsShowError,
                    message: String
                ) {
                    scope.launch(Dispatchers.Main) {
                        _interstitialState.value = AdPlacementState.IDLE
                        _rawLastError.value = "ShowError [$placement]: ${error.name} - $message"
                        _userMessages.emit(AdUserMessage.Warning("Interstitial failed: $message"))
                        loadInterstitialAd()
                    }
                }

                override fun onUnityAdsShowStart(placement: String) {}
            }
        )
    }

    private fun scheduleRetry(placementId: String, isRewarded: Boolean) {
        if (isRewarded) {
            retryRewardedJob?.cancel()
            retryRewardedJob = scope.launch(Dispatchers.Main) {
                delay(5000)
                loadRewardedAd()
            }
        } else {
            retryInterstitialJob?.cancel()
            retryInterstitialJob = scope.launch(Dispatchers.Main) {
                delay(5000)
                loadInterstitialAd()
            }
        }
    }

    fun reloadAllAds() {
        _rawLastError.value = null
        _isPlacementNotFound.value = false
        loadRewardedAd()
        loadInterstitialAd()
    }
}
