package io.github.tarifchakder.networkobserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.flow.Flow

@Composable
fun networkObserverAsState(): State<NetworkStatus> {
    return produceState(initialValue = NetworkStatus.Unreachable as NetworkStatus) {
        networkHelper().networkState.collect { status ->
            value = status
        }
    }
}

fun networkObserver(): Flow<NetworkStatus> = networkHelper().networkState

@Composable
fun networkTypeAsState(): State<NetworkType> {
    return produceState(initialValue = NetworkType.UNKNOWN) {
        networkHelper().networkType.collect {
            value = it
        }
    }
}

fun networkType(): Flow<NetworkType> = networkHelper().networkType