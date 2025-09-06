package io.github.tarifchakder.networkobserver

import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.w3c.dom.events.Event

internal class NetworkUtilWeb : NetworkHelper {

    override val networkState: Flow<NetworkStatus> = callbackFlow {
        val currentNetworkState = getCurrentNetworkState()
        trySend(currentNetworkState)

        val callback: (event: Event) -> Unit = { _ ->
            val networkState = getCurrentNetworkState()
            trySend(networkState)
        }

        window.addEventListener("online", callback)
        window.addEventListener("offline", callback)

        awaitClose {
            window.removeEventListener("offline", callback)
            window.removeEventListener("online", callback)
        }
    }

    override val networkType: Flow<NetworkType> = callbackFlow {
        trySend(NetworkType.UNKNOWN)
    }

    private fun getCurrentNetworkState() = when {
        window.navigator.onLine -> NetworkStatus.Reachable
        else -> NetworkStatus.Unreachable
    }
}