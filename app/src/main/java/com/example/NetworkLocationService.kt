package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class NetworkLocationData(
    val ip: String,
    val country: String,
    val flagEmoji: String,
    val region: String,
    val city: String,
    val isp: String,
    val timezone: String
)

sealed interface NetworkLocationState {
    data object Idle : NetworkLocationState
    data object Loading : NetworkLocationState
    data class Success(val data: NetworkLocationData) : NetworkLocationState
    data class Error(val message: String) : NetworkLocationState
}

class NetworkLocationService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLocation(): NetworkLocationState = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://ipwho.is/")
            .header("User-Agent", "Watch2Earn-Android/1.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkLocationState.Error("HTTP Error: ${response.code}")
                }

                val bodyString = response.body?.string()
                    ?: return@withContext NetworkLocationState.Error("Empty response from geolocation service")

                val json = JSONObject(bodyString)
                val success = json.optBoolean("success", false)

                if (!success) {
                    val message = json.optString("message", "Failed to retrieve location")
                    return@withContext NetworkLocationState.Error(message)
                }

                val ip = json.optString("ip", "Unknown")
                val country = json.optString("country", "Unknown")
                val flagObj = json.optJSONObject("flag")
                val flagEmoji = flagObj?.optString("emoji", "🌐") ?: "🌐"
                val region = json.optString("region", "Unknown")
                val city = json.optString("city", "Unknown")
                val connectionObj = json.optJSONObject("connection")
                val isp = connectionObj?.optString("isp", "Unknown") ?: "Unknown"
                val timezoneObj = json.optJSONObject("timezone")
                val timezone = timezoneObj?.optString("id", "Unknown") ?: "Unknown"

                NetworkLocationState.Success(
                    NetworkLocationData(
                        ip = ip,
                        country = country,
                        flagEmoji = flagEmoji,
                        region = region,
                        city = city,
                        isp = isp,
                        timezone = timezone
                    )
                )
            }
        } catch (e: IOException) {
            NetworkLocationState.Error(e.message ?: "Network connection failed")
        } catch (e: Exception) {
            NetworkLocationState.Error(e.message ?: "Unexpected error parsing location")
        }
    }
}
