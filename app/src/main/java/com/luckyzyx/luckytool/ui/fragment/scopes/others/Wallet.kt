package com.luckyzyx.luckytool.ui.fragment.scopes.others

import android.content.Context
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.ui.fragment.base.BaseScopePreferenceFeagment
import com.luckyzyx.luckytool.utils.AppUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.arraySummaryDot
import com.luckyzyx.luckytool.utils.checkPackName
import com.luckyzyx.luckytool.utils.openApp
import com.luckyzyx.luckytool.utils.setPrefsIconRes
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class Wallet : BaseScopePreferenceFeagment() {

    override val scopes = arrayOf("com.finshell.wallet")
    override val isEnableRestartMenu = true
    override val isEnableOpenMenu = true
    override val currentPrefsName = ModulePrefs
    override val navigateFragmentId = R.id.wallet

    override fun Context.loadRootPreference(): Preference = Preference(this).apply {
        key = scopes.first()
        setPrefsIconRes(key) { resource, show ->
            icon = resource
            isIconSpaceReserved = show
        }
        title = AppUtils(context).getAppLabel(key)
        summary = arraySummaryDot(
            getString(R.string.wallet_skip_splash_ads),
            getString(R.string.wallet_hide_ad_cards)
        )
        isVisible = checkPackName(key)
    }

    override fun Context.loadPreferences(): ArrayList<Preference> = arrayListOf(
        SwitchPreference(this).apply {
            key = "wallet_skip_splash_ads"
            title = getString(R.string.wallet_skip_splash_ads)
            summary = getString(R.string.wallet_ads_scope_notice)
            setDefaultValue(false)
            isIconSpaceReserved = false
        },
        SwitchPreference(this).apply {
            key = "wallet_hide_ad_cards"
            title = getString(R.string.wallet_hide_ad_cards)
            setDefaultValue(false)
            isIconSpaceReserved = false
        }
    )

    override fun callOpenMenu() = requireActivity().openApp(scopes)
}
