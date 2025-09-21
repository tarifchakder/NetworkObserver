package io.github.tarifchakder.networkobserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.flow.Flow

/**
 * Observes the network status and provides it as a Composable State.
 *
 * This function uses `produceState` to create a State that emits the current network status.
 * It initializes with `NetworkStatus.Unreachable` and then collects updates from the
 * `networkHelper().networkState` flow.
 *
 * @return A [State] object that emits [NetworkStatus] updates.
 */
@Composable
fun networkObserverAsState(): State<NetworkStatus> {
    return produceState(initialValue = NetworkStatus.Unreachable as NetworkStatus) {
        networkHelper().networkState.collect { status ->
            value = status
        }
    }
}

/**
 * Observes network status changes and emits them as a Flow.
 *
 * This function provides a cold Flow that emits [NetworkStatus] updates
 * whenever the network connectivity state changes.
 *
 * It utilizes the underlying `networkHelper()` to get the `networkState` Flow.
 *
 * @return A [Flow] of [NetworkStatus] that emits the current network status and subsequent changes.
 */
fun networkObserver(): Flow<NetworkStatus> = networkHelper().networkState

/**
 * Observes the network type and provides it as a Composable State.
 *
 * This function uses [produceState] to create a State object that automatically
 * updates its value whenever the underlying network type changes.
 *
 * The initial value of the state is [NetworkType.UNKNOWN].
 * It then collects emissions from `networkHelper().networkType` Flow
 * and updates the state's value accordingly.
 *
 * @return A [State] object holding the current [NetworkType].
 *         Recomposition will be triggered when the network type changes.
 */
@Composable
fun networkTypeAsState(): State<NetworkType> {
    return produceState(initialValue = NetworkType.UNKNOWN) {
        networkHelper().networkType.collect {
            value = it
        }
    }
}

/**
 * Observes the network type and emits the current network type.
 *
 * This function returns a Flow that emits the current network type (e.g., WIFI, MOBILE)
 * whenever it changes. This is useful for scenarios where you need to react to network
 * type changes in a non-Compose context or when you prefer to work with Flows directly.
 *
 * @return A Flow that emits the current [NetworkType].
 */
fun networkType(): Flow<NetworkType> = networkHelper().networkType