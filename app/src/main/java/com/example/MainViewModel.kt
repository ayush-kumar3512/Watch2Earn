package com.example

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val statsRepository = AdStatsRepository(application)
    val adsManager = UnityAdsManager(application, statsRepository, viewModelScope)
    private val locationService = NetworkLocationService()

    val adStats: StateFlow<AdStats> = statsRepository.statsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdStats())

    val adConfig: StateFlow<UnityAdConfig> = adsManager.adConfig
    val sdkStatus: StateFlow<SdkStatus> = adsManager.sdkStatus
    val rewardedState: StateFlow<AdPlacementState> = adsManager.rewardedState
    val interstitialState: StateFlow<AdPlacementState> = adsManager.interstitialState
    val isPendingRewarded: StateFlow<Boolean> = adsManager.isPendingRewarded
    val isPendingInterstitial: StateFlow<Boolean> = adsManager.isPendingInterstitial
    val rawLastError: StateFlow<String?> = adsManager.rawLastError
    val isPlacementNotFound: StateFlow<Boolean> = adsManager.isPlacementNotFound
    val userMessages: SharedFlow<AdUserMessage> = adsManager.userMessages

    private val _locationState = MutableStateFlow<NetworkLocationState>(NetworkLocationState.Idle)
    val locationState: StateFlow<NetworkLocationState> = _locationState.asStateFlow()

    private val _sessionSeconds = MutableStateFlow(0L)
    val sessionSeconds: StateFlow<Long> = _sessionSeconds.asStateFlow()

    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    private val _showConfigDialog = MutableStateFlow(false)
    val showConfigDialog: StateFlow<Boolean> = _showConfigDialog.asStateFlow()

    private val _showCoinBurst = MutableStateFlow(false)
    val showCoinBurst: StateFlow<Boolean> = _showCoinBurst.asStateFlow()

    private val _hapticTrigger = MutableSharedFlow<HapticType>(extraBufferCapacity = 8)
    val hapticTrigger: SharedFlow<HapticType> = _hapticTrigger.asSharedFlow()

    enum class HapticType {
        LIGHT,
        SUCCESS,
        WARNING
    }

    private var timerJob: Job? = null

    init {
        startSessionTimer()
        refreshLocation()
        observeAdsMessages()
    }

    fun initAds(activity: Activity) {
        adsManager.initialize(activity)
    }

    private fun startSessionTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _sessionSeconds.value += 1
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _locationState.value = NetworkLocationState.Loading
            val result = locationService.fetchLocation()
            _locationState.value = result
        }
    }

    fun onWatchRewardedClicked(activity: Activity) {
        _hapticTrigger.tryEmit(HapticType.LIGHT)
        adsManager.onUserRequestRewardedAd(activity)
    }

    fun onShowInterstitialClicked(activity: Activity) {
        _hapticTrigger.tryEmit(HapticType.LIGHT)
        adsManager.onUserRequestInterstitialAd(activity)
    }

    fun onReloadAdsClicked() {
        _hapticTrigger.tryEmit(HapticType.LIGHT)
        adsManager.reloadAllAds()
    }

    fun openResetDialog() {
        _showResetDialog.value = true
    }

    fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    fun confirmResetStats() {
        _showResetDialog.value = false
        _hapticTrigger.tryEmit(HapticType.WARNING)
        viewModelScope.launch {
            statsRepository.resetAllStats()
        }
    }

    fun openConfigDialog() {
        _showConfigDialog.value = true
    }

    fun dismissConfigDialog() {
        _showConfigDialog.value = false
    }

    fun saveConfig(
        gameId: String,
        rewardedPlacementId: String,
        interstitialPlacementId: String,
        testMode: Boolean
    ) {
        _showConfigDialog.value = false
        _hapticTrigger.tryEmit(HapticType.LIGHT)
        viewModelScope.launch {
            statsRepository.updateAdConfig(
                gameId = gameId,
                rewardedPlacementId = rewardedPlacementId,
                interstitialPlacementId = interstitialPlacementId,
                testMode = testMode
            )
        }
    }

    fun switchPlacementPreset(rewardedId: String, interstitialId: String) {
        val current = adConfig.value
        saveConfig(
            gameId = current.gameId,
            rewardedPlacementId = rewardedId,
            interstitialPlacementId = interstitialId,
            testMode = current.testMode
        )
    }

    private fun observeAdsMessages() {
        viewModelScope.launch {
            userMessages.collect { message ->
                if (message is AdUserMessage.RewardSuccess) {
                    _hapticTrigger.tryEmit(HapticType.SUCCESS)
                    _showCoinBurst.value = true
                }
            }
        }
    }

    fun dismissCoinBurst() {
        _showCoinBurst.value = false
    }
}
