package com.luckyzyx.luckytool.hook.scopes.otherapp

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.extension.kavaref.toClassOrNull
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate
import java.util.Collections
import java.util.WeakHashMap

/**
 * OPPO / OnePlus Wallet 5.51.5 (com.finshell.wallet).
 *
 * Targets identified from the uploaded APK:
 * - SplashViewHelper.onImageReady / onCountDown / onSkipAds
 * - dedicated com.finshell.advertisement view holders
 *
 * The payment, account, NFC and transit business flows are left untouched.
 */
@Obfuscate
object HookWalletAds : YukiBaseHooker() {

    override fun onHook() {
        if (preferences(ModulePrefs).getBoolean("wallet_skip_splash_ads", false)) {
            loadHooker(SkipSplashAds)
        }
        if (preferences(ModulePrefs).getBoolean("wallet_hide_ad_cards", false)) {
            loadHooker(HideAdCards)
        }
    }

    @Obfuscate
    object SkipSplashAds : YukiBaseHooker() {
        private val skipped = Collections.newSetFromMap(WeakHashMap<Any, Boolean>())

        private fun skipOnce(helper: Any?) {
            if (helper == null) return
            synchronized(skipped) {
                if (!skipped.add(helper)) return
            }
            // Use the application's own ad skip flow, not a replacement
            // launcher Activity or a bypass of the privacy consent screen.
            runCatching {
                helper.javaClass.getMethod("onSkipAds").invoke(helper)
            }.onFailure {
                synchronized(skipped) { skipped.remove(helper) }
            }
        }

        override fun onHook() {
            "com.nearme.wallet.splash.SplashViewHelper".toClassOrNull()
                ?.resolve()?.apply {
                    firstMethodOrNull { name = "onImageReady" }?.self?.hook {
                        after { skipOnce(instanceOrNull) }
                    }
                    firstMethodOrNull { name = "onCountDown" }?.self?.hook {
                        after { skipOnce(instanceOrNull) }
                    }
                }
        }
    }

    @Obfuscate
    object HideAdCards : YukiBaseHooker() {
        private val adHolders = arrayOf(
            "com.finshell.advertisement.operate.QcpBannerOperateViewHolder",
            "com.finshell.advertisement.adranking.QcpAdRankingViewHolder",
            "com.finshell.advertisement.adranking.QcpBigPicRankingViewHolder",
            "com.finshell.advertisement.adranking.QcpPicRankingViewHolder",
            "com.finshell.advertisement.adranking.QcpTextRankingViewHolder",
            "com.finshell.advertisement.commerce.QcpOnlineMallHorizonViewHolder",
            "com.finshell.advertisement.commerce.QcpOnlineMallVerticalViewHolder",
            "com.finshell.advertisement.synad.QcpSynBigTextDownViewHolder",
            "com.finshell.advertisement.synad.QcpSynBigTextUpViewHolder",
            "com.finshell.advertisement.synad.QcpSynSmallViewHolder"
        )

        private fun collapse(holder: Any?) {
            val view = (holder as? RecyclerView.ViewHolder)?.itemView ?: return
            view.visibility = View.GONE
            view.layoutParams?.let { params ->
                if (params.height != 0) {
                    params.height = 0
                    view.layoutParams = params
                }
            }
        }

        override fun onHook() {
            adHolders.forEach { className ->
                className.toClassOrNull()?.resolve()?.apply {
                    // Each target has exactly one constructor in v5.51.5.
                    firstConstructorOrNull { }?.self?.hook {
                        after { collapse(instanceOrNull) }
                    }
                }
            }
        }
    }
}
