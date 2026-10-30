package com.example

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ad_telemetry_stats")

data class UnityAdConfig(
    val gameId: String = Constants.UNITY_GAME_ID,
    val rewardedPlacementId: String = Constants.REWARDED_PLACEMENT_ID,
    val interstitialPlacementId: String = Constants.INTERSTITIAL_PLACEMENT_ID,
    val testMode: Boolean = Constants.TEST_MODE
)

data class AdStats(
    val coinsEarned: Int = 0,
    val rewardedWatched: Int = 0,
    val interstitialsShown: Int = 0,
    val adClicks: Int = 0,
    val adsSkipped: Int = 0,
    val loadSuccess: Int = 0,
    val loadFailed: Int = 0
) {
    val totalImpressions: Int
        get() = rewardedWatched + interstitialsShown

    val ctrPercent: Double
        get() = if (totalImpressions > 0) {
            (adClicks.toDouble() / totalImpressions.toDouble()) * 100.0
        } else {
            0.0
        }

    val estimatedEarningsUsd: Double
        get() = (totalImpressions.toDouble() / 1000.0) * Constants.ESTIMATED_ECPM_USD
}

class AdStatsRepository(private val context: Context) {

    private object PreferencesKeys {
        val COINS_EARNED = intPreferencesKey("coins_earned")
        val REWARDED_WATCHED = intPreferencesKey("rewarded_watched")
        val INTERSTITIALS_SHOWN = intPreferencesKey("interstitials_shown")
        val AD_CLICKS = intPreferencesKey("ad_clicks")
        val ADS_SKIPPED = intPreferencesKey("ads_skipped")
        val LOAD_SUCCESS = intPreferencesKey("load_success")
        val LOAD_FAILED = intPreferencesKey("load_failed")

        val GAME_ID = stringPreferencesKey("unity_game_id")
        val REWARDED_PLACEMENT = stringPreferencesKey("rewarded_placement_id")
        val INTERSTITIAL_PLACEMENT = stringPreferencesKey("interstitial_placement_id")
        val TEST_MODE = booleanPreferencesKey("test_mode")
    }

    val statsFlow: Flow<AdStats> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AdStats(
                coinsEarned = preferences[PreferencesKeys.COINS_EARNED] ?: 0,
                rewardedWatched = preferences[PreferencesKeys.REWARDED_WATCHED] ?: 0,
                interstitialsShown = preferences[PreferencesKeys.INTERSTITIALS_SHOWN] ?: 0,
                adClicks = preferences[PreferencesKeys.AD_CLICKS] ?: 0,
                adsSkipped = preferences[PreferencesKeys.ADS_SKIPPED] ?: 0,
                loadSuccess = preferences[PreferencesKeys.LOAD_SUCCESS] ?: 0,
                loadFailed = preferences[PreferencesKeys.LOAD_FAILED] ?: 0
            )
        }

    val configFlow: Flow<UnityAdConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val savedGameId = preferences[PreferencesKeys.GAME_ID]
            val activeGameId = if (savedGameId.isNullOrBlank() || savedGameId == "800407539") {
                Constants.UNITY_GAME_ID
            } else {
                savedGameId
            }

            val savedRewarded = preferences[PreferencesKeys.REWARDED_PLACEMENT]
            val savedInterstitial = preferences[PreferencesKeys.INTERSTITIAL_PLACEMENT]

            // Migrate any old or numeric placement to user's exact BP_ placements
            val validRewarded = if (savedRewarded.isNullOrBlank() || savedRewarded.all { it.isDigit() } || savedRewarded == "800408970" || savedRewarded == "800407539" || savedRewarded == "Rewarded_Android" || savedRewarded == "rewardedVideo") {
                Constants.REWARDED_PLACEMENT_ID
            } else {
                savedRewarded
            }

            val validInterstitial = if (savedInterstitial.isNullOrBlank() || savedInterstitial.all { it.isDigit() } || savedInterstitial == "800408970" || savedInterstitial == "800407539" || savedInterstitial == "Interstitial_Android" || savedInterstitial == "video") {
                Constants.INTERSTITIAL_PLACEMENT_ID
            } else {
                savedInterstitial
            }

            UnityAdConfig(
                gameId = activeGameId,
                rewardedPlacementId = validRewarded,
                interstitialPlacementId = validInterstitial,
                testMode = preferences[PreferencesKeys.TEST_MODE] ?: Constants.TEST_MODE
            )
        }

    suspend fun updateAdConfig(
        gameId: String,
        rewardedPlacementId: String,
        interstitialPlacementId: String,
        testMode: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GAME_ID] = gameId.trim()
            preferences[PreferencesKeys.REWARDED_PLACEMENT] = rewardedPlacementId.trim()
            preferences[PreferencesKeys.INTERSTITIAL_PLACEMENT] = interstitialPlacementId.trim()
            preferences[PreferencesKeys.TEST_MODE] = testMode
        }
    }

    suspend fun addCoins(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.COINS_EARNED] ?: 0
            preferences[PreferencesKeys.COINS_EARNED] = current + amount
        }
    }

    suspend fun recordRewardedWatched() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.REWARDED_WATCHED] ?: 0
            preferences[PreferencesKeys.REWARDED_WATCHED] = current + 1
        }
    }

    suspend fun recordInterstitialShown() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.INTERSTITIALS_SHOWN] ?: 0
            preferences[PreferencesKeys.INTERSTITIALS_SHOWN] = current + 1
        }
    }

    suspend fun recordAdClick() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.AD_CLICKS] ?: 0
            preferences[PreferencesKeys.AD_CLICKS] = current + 1
        }
    }

    suspend fun recordAdSkipped() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.ADS_SKIPPED] ?: 0
            preferences[PreferencesKeys.ADS_SKIPPED] = current + 1
        }
    }

    suspend fun recordLoadSuccess() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.LOAD_SUCCESS] ?: 0
            preferences[PreferencesKeys.LOAD_SUCCESS] = current + 1
        }
    }

    suspend fun recordLoadFailed() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.LOAD_FAILED] ?: 0
            preferences[PreferencesKeys.LOAD_FAILED] = current + 1
        }
    }

    suspend fun resetAllStats() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COINS_EARNED] = 0
            preferences[PreferencesKeys.REWARDED_WATCHED] = 0
            preferences[PreferencesKeys.INTERSTITIALS_SHOWN] = 0
            preferences[PreferencesKeys.AD_CLICKS] = 0
            preferences[PreferencesKeys.ADS_SKIPPED] = 0
            preferences[PreferencesKeys.LOAD_SUCCESS] = 0
            preferences[PreferencesKeys.LOAD_FAILED] = 0
        }
    }
}
