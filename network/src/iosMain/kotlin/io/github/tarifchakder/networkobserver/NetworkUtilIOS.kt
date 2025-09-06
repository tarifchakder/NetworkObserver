package io.github.tarifchakder.networkobserver

import cocoapods.Reachability.Reachability
import cocoapods.Reachability.ReachableViaWWAN
import cocoapods.Reachability.ReachableViaWiFi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(ExperimentalForeignApi::class)
internal class NetworkUtilIOS : NetworkHelper {

    private val reachability: Reachability? by unsafeLazy {
        Reachability.reachabilityForInternetConnection()
    }

    override val networkState: Flow<NetworkStatus> = callbackFlow {
        val currentNetworkState = reachability?.currentReachabilityStatus().toNetworkState()
        trySend(currentNetworkState)

        reachability?.reachableBlock = { r ->
            val networkState = r?.currentReachabilityStatus().toNetworkState()
            trySend(networkState)
        }

        reachability?.unreachableBlock = { _ ->
            trySend(NetworkStatus.Unreachable)
        }

        reachability?.startNotifier()
        awaitClose {
            reachability?.stopNotifier()
        }
    }

    override val networkType: Flow<NetworkType> = callbackFlow {
        val currentNetworkState = reachability?.currentReachabilityStatus().toNetworkType()
        trySend(currentNetworkState)

        reachability?.reachableBlock = { r ->
            val networkState = r?.currentReachabilityStatus().toNetworkType()
            trySend(networkState)
        }

        reachability?.unreachableBlock = { _ ->
            trySend(NetworkType.UNKNOWN)
        }

        reachability?.startNotifier()
        awaitClose {
            reachability?.stopNotifier()
        }
    }

    private fun cocoapods.Reachability.NetworkStatus?.toNetworkType() = when (this) {
        ReachableViaWWAN -> NetworkType.CELLULAR
        ReachableViaWiFi -> NetworkType.WIFI
        else -> NetworkType.UNKNOWN
    }

    private fun cocoapods.Reachability.NetworkStatus?.toNetworkState() = when (this) {
            ReachableViaWWAN, ReachableViaWiFi -> NetworkStatus.Reachable
            else -> NetworkStatus.Unreachable
        }
}