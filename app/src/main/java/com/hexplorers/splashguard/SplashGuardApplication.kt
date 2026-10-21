package com.hexplorers.splashguard

import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication

class SplashGuardApplication : ModuleApplication() {
    override fun onCreate() {
        CrashRecovery.install(this)
        super.onCreate()
    }
}
