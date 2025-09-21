package io.github.tarifchakder.networkobserver

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Android implementation of NetworkHelper for monitoring network connectivity and type.
 *
 * This class provides real-time network connectivity monitoring using Android's
 * ConnectivityManager and NetworkCallback APIs. It offers precise network state
 * detection and detailed network type identification for Android devices.
 *
 * Features:
 * - Real-time network status monitoring with validated internet connectivity
 * - Detailed network type detection (WiFi, Cellular, Ethernet)
 * - Automatic network callback registration and cleanup
 * - Reactive streams with duplicate event filtering
 * - Android API level 21+ support
 *
 * Permissions Required:
 * - android.permission.ACCESS_NETWORK_STATE
 *
 * API Requirements:
 * - Minimum SDK: API 21 (Android 5.0)
 * - Target SDK: API 33+ recommended
 *
 * @param context Android context for accessing system services
 * @author Your Name
 * @since 1.0.0
 *
 * Example usage:
 * ```
 * val networkUtil = NetworkUtilAndroid(context)
 *
 * // Monitor network status
 * networkUtil.networkState.collect { status ->
 *     when (status) {
 *         NetworkStatus.Reachable -> enableOnlineFeatures()
 *         NetworkStatus.Unreachable -> showOfflineMode()
 *     }
 * }
 *
 * // Monitor network type
 * networkUtil.networkType.collect { type ->
 *     when (type) {
 *         NetworkType.WIFI -> enableHighQualityStreaming()
 *         NetworkType.CELLULAR -> enableDataSaverMode()
 *         NetworkType.ETHERNET -> enableMaximumQuality()
 *     }
 * }
 * ```
 */
internal class NetworkUtilAndroid(context: Context) : NetworkHelper {

    /**
     * Lazy-initialized ConnectivityManager instance for network operations.
     *
     * Uses unsafeLazy to defer initialization until first access, reducing
     * object creation overhead. The ConnectivityManager is obtained from
     * the system service and cached for subsequent operations.
     *
     * @see ConnectivityManager
     */
    private val connectivityManager by unsafeLazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    /**
     * Reactive stream that emits network connectivity status changes.
     *
     * This Flow monitors Android network callbacks and emits NetworkStatus values
     * when connectivity changes occur. It provides real-time updates about internet
     * connectivity with validation to ensure actual internet access.
     *
     * Implementation Details:
     * - Registers NetworkCallback with INTERNET capability requirement
     * - Emits initial connectivity state immediately upon collection
     * - Monitors onAvailable() and onLost() network events
     * - Validates internet connectivity using NET_CAPABILITY_VALIDATED
     * - Automatically unregisters callback when collection stops
     * - Filters duplicate consecutive emissions
     *
     * Network Request Configuration:
     * - Capability: NET_CAPABILITY_INTERNET (ensures internet access)
     *
     * @return Flow<NetworkStatus> Stream of connectivity status changes
     *         - NetworkStatus.Reachable: Device has validated internet access
     *         - NetworkStatus.Unreachable: Device has no internet connectivity
     *
     * Thread Safety: This Flow is thread-safe and can be collected from any thread
     *
     * Example:
     * ```
     * networkState.collect { status ->
     *     if (status == NetworkStatus.Reachable) {
     *         syncUserData()
     *         enableRealtimeFeatures()
     *     } else {
     *         showOfflineMessage()
     *         enableOfflineMode()
     *     }
     * }
     * ```
     */
    override val networkState: Flow<NetworkStatus> = callbackFlow {

        val callback = networkStatusCallBack { connectionState ->
            trySend(connectionState)
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        val currentState = getCurrentConnectivityStatus(connectivityManager)
        trySend(currentState)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * Reactive stream that emits network type changes.
     *
     * This Flow monitors Android network capabilities and emits NetworkType values
     * when the active network type changes. It provides detailed information about
     * the current network transport method (WiFi, Cellular, Ethernet).
     *
     * Implementation Details:
     * - Registers NetworkCallback with INTERNET capability requirement
     * - Emits initial network type immediately upon collection
     * - Monitors onAvailable(), onLost(), and onCapabilitiesChanged() events
     * - Analyzes NetworkCapabilities transport types for accurate detection
     * - Automatically unregisters callback when collection stops
     * - Filters duplicate consecutive emissions
     *
     * Supported Network Types:
     * - WIFI: Connected via WiFi network
     * - CELLULAR: Connected via mobile/cellular network
     * - ETHERNET: Connected via wired Ethernet connection
     * - UNKNOWN: No active network or unsupported transport type
     *
     * @return Flow<NetworkType> Stream of network type changes
     *
     * Thread Safety: This Flow is thread-safe and can be collected from any thread
     *
     * Example:
     * ```
     * networkType.collect { type ->
     *     when (type) {
     *         NetworkType.WIFI -> {
     *             enableAutoBackup()
     *             setVideoQuality(VideoQuality.HIGH)
     *         }
     *         NetworkType.CELLULAR -> {
     *             showDataUsageWarning()
     *             setVideoQuality(VideoQuality.MEDIUM)
     *         }
     *         NetworkType.ETHERNET -> {
     *             setVideoQuality(VideoQuality.ULTRA_HIGH)
     *         }
     *         NetworkType.UNKNOWN -> {
     *             pauseDataIntensiveTasks()
     *         }
     *     }
     * }
     * ```
     */
    override val networkType: Flow<NetworkType> = callbackFlow {

        val callback = networkTypeCallBack { networkType ->
            trySend(networkType)
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        trySend(currentNetworkType())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * Determines the current network connectivity status with internet validation.
     *
     * This method performs a comprehensive connectivity check by examining both
     * network availability and internet validation. It ensures that the device
     * not only has a network connection but also verified internet access.
     *
     * Validation Process:
     * 1. Checks for active network availability
     * 2. Retrieves network capabilities
     * 3. Verifies NET_CAPABILITY_INTERNET capability
     * 4. Confirms NET_CAPABILITY_VALIDATED status
     *
     * The NET_CAPABILITY_VALIDATED check is crucial as it ensures the network
     * has been validated by the system to have actual internet connectivity,
     * not just local network access.
     *
     * @param connectivityManager The ConnectivityManager instance to query
     * @return NetworkStatus Current connectivity status
     *         - NetworkStatus.Reachable: Device has validated internet access
     *         - NetworkStatus.Unreachable: No network or internet validation failed
     *
     * @see NetworkCapabilities.NET_CAPABILITY_INTERNET
     * @see NetworkCapabilities.NET_CAPABILITY_VALIDATED
     */
    private fun getCurrentConnectivityStatus(connectivityManager: ConnectivityManager): NetworkStatus {
        val network = connectivityManager.activeNetwork ?: return NetworkStatus.Unreachable
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.Unreachable

        return when {
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> {
                NetworkStatus.Reachable
            }

            else -> NetworkStatus.Unreachable
        }
    }

    /**
     * Determines the current network transport type.
     *
     * This method analyzes the active network's capabilities to determine the
     * specific transport mechanism being used for connectivity. It examines
     * NetworkCapabilities transport types to provide accurate network type detection.
     *
     * Detection Priority:
     * 1. WiFi: Checks for TRANSPORT_WIFI
     * 2. Cellular: Checks for TRANSPORT_CELLULAR
     * 3. Ethernet: Checks for TRANSPORT_ETHERNET
     * 4. Unknown: All other or unsupported transport types
     *
     * Transport Types Detected:
     * - TRANSPORT_WIFI: IEEE 802.11 wireless networks
     * - TRANSPORT_CELLULAR: Mobile/cellular data networks (2G/3G/4G/5G)
     * - TRANSPORT_ETHERNET: Wired Ethernet connections
     * - Other transports (Bluetooth, VPN, etc.) return UNKNOWN
     *
     * @return NetworkType The detected network transport type
     *         - NetworkType.WIFI: Connected via WiFi
     *         - NetworkType.CELLULAR: Connected via cellular network
     *         - NetworkType.ETHERNET: Connected via Ethernet cable
     *         - NetworkType.UNKNOWN: No network or unsupported transport
     *
     * @see NetworkCapabilities.TRANSPORT_WIFI
     * @see NetworkCapabilities.TRANSPORT_CELLULAR
     * @see NetworkCapabilities.TRANSPORT_ETHERNET
     */
    private fun currentNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.UNKNOWN
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.UNKNOWN

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }

    /**
     * Creates a NetworkCallback for monitoring network connectivity status changes.
     *
     * This factory method creates a specialized NetworkCallback that focuses on
     * connectivity state changes. It provides a simplified interface by converting
     * network events into NetworkStatus values through the provided callback function.
     *
     * Callback Events:
     * - onAvailable(): Network becomes available → NetworkStatus.Reachable
     * - onLost(): Network is lost → NetworkStatus.Unreachable
     *
     * Note: This callback does not handle onCapabilitiesChanged() events as
     * connectivity status is binary (available/unavailable). Network validation
     * is handled separately in getCurrentConnectivityStatus().
     *
     * @param callback Function to invoke when network status changes
     * @return ConnectivityManager.NetworkCallback Configured network callback
     *
     * Usage Pattern:
     * ```
     * val callback = networkStatusCallBack { status ->
     *     when (status) {
     *         NetworkStatus.Reachable -> handleOnline()
     *         NetworkStatus.Unreachable -> handleOffline()
     *     }
     * }
     * connectivityManager.registerNetworkCallback(networkRequest, callback)
     * ```
     */
    private fun networkStatusCallBack(callback: (NetworkStatus) -> Unit): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                callback(NetworkStatus.Reachable)
            }

            override fun onLost(network: Network) {
                callback(NetworkStatus.Unreachable)
            }
        }
    }

    /**
     * Creates a NetworkCallback for monitoring network type changes.
     *
     * This factory method creates a comprehensive NetworkCallback that monitors
     * all network-related events that could affect network type detection. It
     * provides detailed network type information by analyzing capabilities changes.
     *
     * Callback Events Handled:
     * - onAvailable(): New network available → Detect current type
     * - onLost(): Network lost → Return UNKNOWN type
     * - onCapabilitiesChanged(): Network capabilities modified → Re-detect type
     *
     * The onCapabilitiesChanged() callback is particularly important as network
     * capabilities can change without the network being lost/gained. For example,
     * a device might switch from cellular to WiFi while maintaining connectivity.
     *
     * Event Flow Examples:
     * ```
     * // WiFi to Cellular transition:
     * onLost(wifi_network) → NetworkType.UNKNOWN
     * onAvailable(cellular_network) → NetworkType.CELLULAR
     *
     * // Capabilities change within same network:
     * onCapabilitiesChanged(network, new_caps) → Re-evaluate type
     * ```
     *
     * @param callback Function to invoke when network type changes
     * @return ConnectivityManager.NetworkCallback Configured network callback
     *
     * Thread Safety: All callback methods are called on the ConnectivityManager's
     * internal thread, so the provided callback function should be thread-safe.
     */
    private fun networkTypeCallBack(callback: (NetworkType) -> Unit): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                callback(currentNetworkType())
            }

            override fun onLost(network: Network) {
                callback(NetworkType.UNKNOWN)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                callback(currentNetworkType())
            }
        }
    }

}