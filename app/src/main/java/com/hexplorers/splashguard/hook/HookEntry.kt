package com.hexplorers.splashguard.hook

import com.hexplorers.splashguard.SkipSettings
import com.hexplorers.splashguard.BuildConfig
import com.highcapable.yukihookapi.annotation.xposed.YukiHookLibXposedEntry
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.YukiHookXposedModule

@YukiHookLibXposedEntry(
    entryClassName = "SplashGuardEntry",
    minApiVersion = 102,
    targetApiVersion = 102,
    scope = [
        "com.ss.android.ugc.aweme",
        "com.smile.gifmaker",
        "com.taobao.taobao",
        "com.jingdong.app.mall",
        "com.xunmeng.pinduoduo",
        "com.sina.weibo",
        "com.zhihu.android",
        "com.baidu.searchbox",
        "com.finshell.wallet"
    ],
    staticScope = false
)
object HookEntry : YukiHookXposedModule {
    override fun onHook() = encase {
        loadApp() {
            if (packageName == BuildConfig.APPLICATION_ID || processName != mainProcessName) {
                return@loadApp
            }
            val rules = SkipSettings.fromHost(
                enabled = preferences("splashguard").getBoolean(SkipSettings.ENABLED, true),
                strict = preferences("splashguard").getBoolean(SkipSettings.STRICT, true),
                topRight = preferences("splashguard").getBoolean(SkipSettings.TOP_RIGHT, true),
                windowSeconds = preferences("splashguard").getInt(SkipSettings.WINDOW_SECONDS, 8),
                blockShakeAds = preferences("splashguard").getBoolean(SkipSettings.BLOCK_SHAKE_ADS, true),
                includeGyroscope = preferences("splashguard").getBoolean(SkipSettings.INCLUDE_GYROSCOPE, false),
                debugLog = preferences("splashguard").getBoolean(SkipSettings.DEBUG_LOG, false)
            )
            if (rules.blockShakeAds) loadHooker(ShakeSensorHook(rules))
            if (rules.enabled) {
                withApplicationReady { app ->
                    SplashLifecycle.attach(app, rules)
                }
            }
        }
    }
}
