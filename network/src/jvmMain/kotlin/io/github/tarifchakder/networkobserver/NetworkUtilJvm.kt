package io.github.tarifchakder.networkobserver

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.net.*
import java.io.IOException
import javax.net.ssl.HttpsURLConnection

internal class NetworkUtilJvm : NetworkHelper {

    private val connectionCache = mutableMapOf<String, Boolean>()
    private var lastCheckTime = 0L
    private val cacheValidityMs = 5000L


    override val networkState: Flow<NetworkStatus> = callbackFlow {
        val scope = this
        val job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val isReachable = isInternetAvailable()
                trySend(if (isReachable) NetworkStatus.Reachable else NetworkStatus.Unreachable)
                delay(3000)
            }
        }

        awaitClose {
            job.cancel()
        }
    }.distinctUntilChanged()

    override val networkType: Flow<NetworkType> = callbackFlow {
        val scope = this
        val job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val type = detectNetworkType()
                trySend(type)
                delay(3000)
            }
        }

        awaitClose {
            job.cancel()
        }
    }.distinctUntilChanged()

    private suspend fun isInternetAvailable(): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastCheckTime < cacheValidityMs) {
            connectionCache["internet"]?.let { return it }
        }

        return withContext(Dispatchers.IO) {
            try {
                val endpoints = listOf(
                    "https://www.google.com",
                    "https://www.cloudflare.com",
                    "https://1.1.1.1"
                )

                for (endpoint in endpoints) {
                    if (checkConnection(endpoint)) {
                        lastCheckTime = currentTime
                        connectionCache["internet"] = true
                        return@withContext true
                    }
                }

                lastCheckTime = currentTime
                connectionCache["internet"] = false
                false
            } catch (e: Exception) {
                lastCheckTime = currentTime
                connectionCache["internet"] = false
                false
            }
        }
    }

    private fun checkConnection(urlString: String): Boolean {
        return try {
            val url = URL(urlString)
            val connection = when (url.protocol) {
                "https" -> url.openConnection() as HttpsURLConnection
                "http" -> url.openConnection() as HttpURLConnection
                else -> url.openConnection() as HttpURLConnection
            }

            connection.apply {
                requestMethod = "HEAD"
                connectTimeout = 2000
                readTimeout = 2000
                setRequestProperty("User-Agent", "NetworkCheck/1.0")
                useCaches = false
                instanceFollowRedirects = false
            }

            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()

            responseCode in 200..299
        } catch (_: IOException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun detectNetworkType(): NetworkType {
        return withContext(Dispatchers.IO) {
            if (!isInternetAvailable()) {
                return@withContext NetworkType.UNKNOWN
            }

            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()

                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    val name = networkInterface.name.lowercase()
                    val displayName = networkInterface.displayName?.lowercase() ?: ""

                    when {
                        displayName.contains("wi-fi") || displayName.contains("wifi") || name.contains("wlan") || name.contains("wlp") -> {
                            return@withContext NetworkType.WIFI
                        }

                        name.contains("eth") || name.contains("enp") || name.contains("eno") || (name.startsWith("en") && displayName.contains("ethernet")) -> {
                            return@withContext NetworkType.ETHERNET
                        }

                        name.matches(Regex("en\\d+")) && displayName.isEmpty() -> {
                            return@withContext NetworkType.WIFI
                        }

                        name.contains("mobile") || name.contains("cellular") || name.contains("ppp") || name.contains("rmnet") -> {
                            return@withContext NetworkType.CELLULAR
                        }
                    }
                }
            }
            NetworkType.UNKNOWN
        }
    }
}