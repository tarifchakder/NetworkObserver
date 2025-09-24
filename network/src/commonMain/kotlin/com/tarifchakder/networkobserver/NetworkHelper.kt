package com.tarifchakder.networkobserver

import kotlinx.coroutines.flow.Flow

interface NetworkHelper {
    val networkState: Flow<NetworkStatus>
    val networkType: Flow<NetworkType>
}

expect fun networkHelper(): NetworkHelper