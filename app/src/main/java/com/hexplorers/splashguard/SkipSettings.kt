package com.hexplorers.splashguard

/** Module settings are synchronized via libxposed RemotePreferences. */
data class SkipSettings(
    val enabled: Boolean = true,
    val strict: Boolean = true,
    val topRight: Boolean = true,
    val windowSeconds: Int = 8,
    val blockShakeAds: Boolean = true,
    val includeGyroscope: Boolean = false,
    val debugLog: Boolean = false
) {
    companion object {
        const val ENABLED = "enabled"
        const val STRICT = "strict"
        const val TOP_RIGHT = "top_right"
        const val WINDOW_SECONDS = "window_seconds"
        const val BLOCK_SHAKE_ADS = "block_shake_ads"
        const val INCLUDE_GYROSCOPE = "include_gyroscope"
        const val DEBUG_LOG = "debug_log"

        fun fromHost(
            enabled: Boolean, strict: Boolean, topRight: Boolean, windowSeconds: Int,
            blockShakeAds: Boolean, includeGyroscope: Boolean, debugLog: Boolean
        ) = SkipSettings(
            enabled, strict, topRight, windowSeconds.coerceIn(4, 12),
            blockShakeAds, includeGyroscope, debugLog
        )
    }
}
