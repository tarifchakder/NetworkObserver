package io.github.tarifchakder.networkobserver

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.nw_interface_type_cellular
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t

internal class NetworkUtilIOS : NetworkHelper {

    private var monitor: nw_path_monitor_t? = null
    private var queue: dispatch_queue_t? = null
    private val updateFlows = mutableListOf<CallbackFlowWrapper<*>>()

    override val networkState: Flow<NetworkStatus> = callbackFlow {
        val wrapper = CallbackFlowWrapper(this) { path: nw_path_t ->
            val reachable = nw_path_get_status(path) == nw_path_status_satisfied
            if (reachable) NetworkStatus.Reachable else NetworkStatus.Unreachable
        }
        register(wrapper)
        awaitClose { unregister(wrapper) }
    }.distinctUntilChanged()

    override val networkType: Flow<NetworkType> = callbackFlow {
        val wrapper = CallbackFlowWrapper(this) { path: nw_path_t ->
            val reachable = nw_path_get_status(path) == nw_path_status_satisfied
            when {
                !reachable -> NetworkType.UNKNOWN
                nw_path_uses_interface_type(path, nw_interface_type_wifi) -> NetworkType.WIFI
                nw_path_uses_interface_type(path, nw_interface_type_cellular) -> NetworkType.CELLULAR
                else -> NetworkType.UNKNOWN
            }
        }
        register(wrapper)
        awaitClose { unregister(wrapper) }
    }.distinctUntilChanged()

    private fun <T> register(wrapper: CallbackFlowWrapper<T>) {
        updateFlows.add(wrapper)
        if (monitor == null) startMonitor()
    }

    private fun <T> unregister(wrapper: CallbackFlowWrapper<T>) {
        updateFlows.remove(wrapper)
        if (updateFlows.isEmpty()) stopMonitor()
    }

    private fun startMonitor() {
        queue = dispatch_get_main_queue()
        monitor = nw_path_monitor_create()

        nw_path_monitor_set_update_handler(monitor) { path ->
            updateFlows.forEach { it.send(path) }
        }

        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }

    private fun stopMonitor() {
        monitor?.let { nw_path_monitor_cancel(it) }
        monitor = null
    }

    private class CallbackFlowWrapper<T>(
        private val flow: kotlinx.coroutines.channels.ProducerScope<T>,
        private val mapper: (nw_path_t) -> T
    ) {
        fun send(path: nw_path_t) {
            flow.trySend(mapper(path))
        }
    }
}