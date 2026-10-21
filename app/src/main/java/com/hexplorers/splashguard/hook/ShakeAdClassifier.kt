package com.hexplorers.splashguard.hook

/**
 * Conservative fingerprinting of known advertising-SDK namespaces.
 * Do not match on generic words like "ad"/"splash" or on the app package.
 */
internal object ShakeAdClassifier {
    private val sdkNamespaces = arrayOf(
        "com.qq.e.",
        "com.bytedance.sdk.openadsdk.",
        "com.bytedance.sdk.component.adexpress.",
        "com.kwad.sdk.",
        "com.kuaishou.weapon.p0.",
        "com.baidu.mobads.",
        "com.sigmob.", "com.windmill.",
        "com.huawei.openalliance.ad.",
        "com.huawei.hms.ads.",
        "com.miui.zeus.mimo.",
        "com.mbridge.msdk.",
        "com.anythink.",
        "com.beizi.ad.",
        "com.mercury.sdk.",
        "com.san.ads."
    )

    fun isKnownAdSdkClass(name: String?): Boolean {
        if (name.isNullOrEmpty()) return false
        return sdkNamespaces.any { name.startsWith(it, ignoreCase = true) }
    }

    fun fromListenerOrStack(listenerClass: String, callerClasses: Iterable<String>): Boolean {
        if (isKnownAdSdkClass(listenerClass)) return true
        return callerClasses.any(::isKnownAdSdkClass)
    }
}
