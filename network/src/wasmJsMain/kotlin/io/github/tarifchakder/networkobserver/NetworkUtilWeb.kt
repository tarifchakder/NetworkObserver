package io.github.tarifchakder.networkobserver

import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.w3c.dom.events.Event

/**
 * Web implementation of NetworkHelper for browser-based Kotlin/JS applications.
 *
 * This class provides real-time network connectivity monitoring using browser APIs.
 * It listens to browser online/offline events and provides reactive streams for
 * network status and type detection.
 *
 * Features:
 * - Real-time network status monitoring (online/offline)
 * - Simple network type detection (WiFi vs Cellular)
 * - Duplicate event filtering to prevent unnecessary emissions
 * - Automatic event listener cleanup
 *
 * Browser Compatibility:
 * - Chrome: Full support
 * - Firefox: Full support
 * - Safari: Full support
 * - Edge: Full support
 *
 * @author Your Name
 * @since 1.0.0
 */
internal class NetworkUtilWeb : NetworkHelper {

    /**
     * Reactive stream that emits network connectivity status changes.
     *
     * This Flow monitors browser online/offline events and emits NetworkStatus
     * values when the connection state changes. The stream automatically filters
     * out duplicate consecutive emissions to prevent unnecessary UI updates.
     *
     * Behavior:
     * - Emits initial status immediately upon collection
     * - Listens to 'online' and 'offline' browser events
     * - Includes 50ms delay to ensure browser state synchronization
     * - Uses distinctUntilChanged() to filter duplicate emissions
     * - Automatically cleans up event listeners when collection stops
     *
     * @return Flow<NetworkStatus> Stream of network connectivity status
     *         - NetworkStatus.Reachable: Device has internet connectivity
     *         - NetworkStatus.Unreachable: Device has no internet connectivity
     *
     * Example usage:
     * ```
     * networkState.collect { status ->
     *     when (status) {
     *         NetworkStatus.Reachable -> showOnlineUI()
     *         NetworkStatus.Unreachable -> showOfflineUI()
     *     }
     * }
     * ```
     */
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

    /**
     * Reactive stream that emits network type changes.
     *
     * This Flow provides basic network type detection based on user agent analysis.
     * It distinguishes between mobile devices (assumed cellular) and desktop devices
     * (assumed WiFi). The detection is simplified due to web platform limitations.
     *
     * Detection Logic:
     * - Mobile devices (Android, iPhone, iPad): Assumed CELLULAR
     * - Desktop devices: Assumed WIFI
     * - Offline state: Returns UNKNOWN
     *
     * Limitations:
     * - Cannot distinguish between WiFi and cellular on mobile devices
     * - Cannot detect Ethernet vs WiFi on desktop
     * - Cannot detect specific cellular types (2G, 3G, 4G, 5G)
     *
     * @return Flow<NetworkType> Stream of network type changes
     *         - NetworkType.CELLULAR: Mobile device (likely using cellular)
     *         - NetworkType.WIFI: Desktop device (likely using WiFi)
     *         - NetworkType.UNKNOWN: Device is offline or type unknown
     *
     * Example usage:
     * ```
     * networkType.collect { type ->
     *     when (type) {
     *         NetworkType.CELLULAR -> enableDataSaverMode()
     *         NetworkType.WIFI -> enableHighQualityContent()
     *         NetworkType.UNKNOWN -> showOfflineMessage()
     *     }
     * }
     * ```
     */
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

    /**
     * Determines the current network connectivity status.
     *
     * Uses the browser's navigator.onLine API to check if the device
     * currently has internet connectivity. This is a synchronous operation
     * that returns the current state immediately.
     *
     * Implementation Notes:
     * - Uses navigator.onLine browser API
     * - Returns immediately without network requests
     * - May have slight delays in some browsers when network state changes
     *
     * @return NetworkStatus Current connectivity status
     *         - NetworkStatus.Reachable: Device is online
     *         - NetworkStatus.Unreachable: Device is offline
     */
    private fun getCurrentNetworkState(): NetworkStatus {
        return if (window.navigator.onLine) {
            NetworkStatus.Reachable
        } else {
            NetworkStatus.Unreachable
        }
    }

    /**
     * Determines the current network type using simple device detection.
     *
     * Performs basic network type detection by analyzing the browser's user agent
     * string to distinguish between mobile and desktop devices. This provides
     * a reasonable approximation of network type in web environments where
     * detailed network APIs may not be available.
     *
     * Detection Algorithm:
     * 1. Check if device is online using navigator.onLine
     * 2. If offline, return UNKNOWN
     * 3. If online, analyze user agent string:
     *    - Mobile keywords (mobile, android, iphone, ipad) → CELLULAR
     *    - All other devices → WIFI
     *
     * User Agent Keywords Checked:
     * - "mobile": Generic mobile devices
     * - "android": Android devices
     * - "iphone": iPhone devices
     * - "ipad": iPad devices
     *
     * Limitations:
     * - Assumes all mobile devices use cellular (may be on WiFi)
     * - Assumes all desktop devices use WiFi (may be on Ethernet)
     * - Cannot detect actual connection type, only device type
     * - User agent detection may be unreliable with some browsers
     *
     * @return NetworkType Detected network type based on device analysis
     *         - NetworkType.CELLULAR: Mobile device detected
     *         - NetworkType.WIFI: Desktop device detected
     *         - NetworkType.UNKNOWN: Device is offline
     */
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