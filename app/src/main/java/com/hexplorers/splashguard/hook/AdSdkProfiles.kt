package com.hexplorers.splashguard.hook

/**
 * SDK-aware UI hints only, not direct network interception.
 * Obfuscation/repackaging can prevent identification.
 */
internal object AdSdkProfiles {
    private val markers = mapOf(
        "Tencent GDT" to listOf("gdt", "qq_e", "tencent_ad", "gdt_splash"),
        "Pangle" to listOf("tt_splash", "pangle", "bytedance", "csj_"),
        "Kuaishou" to listOf("ksad", "ks_splash", "kuaishou_ad"),
        "Baidu" to listOf("mobads", "bd_splash", "baidu_ad"),
        "Sigmob" to listOf("sigmob", "wind_splash"),
        "Huawei" to listOf("hms_ads", "huawei_ads", "hiad"),
        "Xiaomi" to listOf("mimo", "miad", "xiaomi_ad"),
        "Generic" to listOf("splash", "skip_ad", "ad_skip", "advertisement")
    )

    fun matches(viewId: String, contentDescription: String): Boolean {
        val source = (viewId + " " + contentDescription).lowercase()
        return markers.values.any { keys -> keys.any { source.contains(it) } }
    }
}
