package io.github.tarifchakder.networkobserver

import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.w3c.dom.events.Event
internal class NetworkUtilWeb : NetworkHelper {

    override val networkState: Flow<NetworkStatus> = callbackFlow {
        trySend(getCurrentNetworkState())

        val callback: (Event) -> Unit = {
            launch {
                delay(50)
                trySend(getCurrentNetworkState())
            }
        }

        window.addEventListener("online", callback)
        window.addEventListener("offline", callback)

        awaitClose {
            window.removeEventListener("online", callback)
            window.removeEventListener("offline", callback)
        }
    }.distinctUntilChanged()

    override val networkType: Flow<NetworkType> = callbackFlow {
        trySend(getSimpleNetworkType())

        val callback: (Event) -> Unit = {
            launch {
                delay(50)
                trySend(getSimpleNetworkType())
            }
        }

        window.addEventListener("online", callback)
        window.addEventListener("offline", callback)

        awaitClose {
            window.removeEventListener("online", callback)
            window.removeEventListener("offline", callback)
        }

    }.distinctUntilChanged()

    private fun getCurrentNetworkState(): NetworkStatus {
        return if (window.navigator.onLine) {
            NetworkStatus.Reachable
        } else {
            NetworkStatus.Unreachable
        }
    }

    private fun getSimpleNetworkType(): NetworkType {
        return if (window.navigator.onLine) {
            val userAgent = window.navigator.userAgent.lowercase()
            when {
                userAgent.contains("mobile")
                        || userAgent.contains("android")
                        || userAgent.contains("iphone")
                        || userAgent.contains("ipad") -> NetworkType.CELLULAR

                else -> NetworkType.WIFI
            }
        } else {
            NetworkType.UNKNOWN
        }
    }

}