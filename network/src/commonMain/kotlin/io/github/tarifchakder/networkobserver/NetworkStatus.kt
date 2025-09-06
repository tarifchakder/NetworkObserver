package io.github.tarifchakder.networkobserver

sealed class NetworkStatus {
    data object Reachable : NetworkStatus()
    data object Unreachable : NetworkStatus()
}