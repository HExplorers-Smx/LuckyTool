package com.hexplorers.splashguard

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.TextView
import com.highcapable.yukihookapi.hook.factory.preferences

class MainActivity : Activity() {
    private val files = "splashguard"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = preferences(files)
        val accent = if (Build.VERSION.SDK_INT >= 31) getColor(android.R.color.system_accent1_600)
        else Color.rgb(36, 119, 216)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(26), dp(20), dp(24))
            setBackgroundColor(Color.rgb(246, 248, 251))
        }
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
        root.addView(text("开屏净", 29f, Color.rgb(25, 38, 57), true))
        root.addView(text("独立的 API 102 开屏广告跳过模块", 14f, Color.DKGRAY, false))
        root.addView(text("作用域请在 LSPosed 中手动勾选需要处理的应用。", 13f, Color.GRAY, false))

        fun card(): LinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(16))
            val background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(18).toFloat()
            }
            setBackground(background)
            val params = LinearLayout.LayoutParams(-1, -2)
            params.topMargin = dp(20)
            root.addView(this, params)
        }
        val panel = card()
        fun toggle(title: String, sub: String, key: String, default: Boolean) {
            val item = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            val check = Switch(this).apply {
                text = title
                textSize = 16f
                isChecked = prefs.getBoolean(key, default)
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit { putBoolean(key, checked) }
                }
            }
            item.addView(check)
            item.addView(text(sub, 12f, Color.GRAY, false))
            val lp = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(13) }
            panel.addView(item, lp)
        }
        toggle("启用自动跳过", "仅在 LSPosed 选定的应用中运行", SkipSettings.ENABLED, true)
        toggle("谨慎模式（推荐）", "必须识别到倒计时、广告组件标识或开屏 Activity", SkipSettings.STRICT, true)
        toggle("仅点击屏幕右上角", "避免误点授权、登录与普通页面按钮", SkipSettings.TOP_RIGHT, true)

        val motionPanel = card()
        motionPanel.addView(text("摇一摇广告拦截", 17f, Color.rgb(25, 38, 57), true))
        fun motionToggle(title: String, sub: String, key: String, default: Boolean) {
            val switch = Switch(this).apply {
                text = title
                textSize = 16f
                isChecked = prefs.getBoolean(key, default)
                setOnCheckedChangeListener { _, checked ->
                    prefs.edit { putBoolean(key, checked) }
                }
            }
            motionPanel.addView(switch, LinearLayout.LayoutParams(-1, -2).apply { topMargin=dp(12) })
            motionPanel.addView(text(sub, 12f, Color.GRAY, false))
        }
        motionToggle("阻止摇一摇广告跳转", "默认开启，只拦截已识别广告 SDK 注册的加速度传感器监听",
            SkipSettings.BLOCK_SHAKE_ADS, true)
        motionToggle("同时拦截广告陀螺仪", "增强模式；部分交互广告会使用陀螺仪，默认关闭",
            SkipSettings.INCLUDE_GYROSCOPE, false)
        motionToggle("记录拦截日志", "只记录广告 SDK 类名和传感器类型，不记录动作数据",
            SkipSettings.DEBUG_LOG, false)

        val period = card()
        period.addView(text("启动检测时长", 17f, Color.rgb(25, 38, 57), true))
        period.addView(text("限制扫描发生在应用进入前台的最初几秒", 12f, Color.GRAY, false))
        val options = listOf(4, 6, 8, 10, 12)
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            options.map { "$it 秒" })
        val selected = prefs.getInt(SkipSettings.WINDOW_SECONDS, 8)
        spinner.setSelection(options.indexOf(selected).coerceAtLeast(0))
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, pos: Int, id: Long) {
                prefs.edit { putInt(SkipSettings.WINDOW_SECONDS, options[pos]) }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
        period.addView(spinner)

        val note = card()
        note.addView(text("检测策略", 17f, Color.rgb(25, 38, 57), true))
        note.addView(text("识别跳过按钮/倒计时，结合优量汇、穿山甲、快手、百度、Sigmob、华为、小米等 SDK 的视图标识。不会屏蔽网络请求，不读取账号或交易内容。", 13f, Color.DKGRAY, false))
        note.addView(text("摇一摇只拦广告 SDK 的传感器注册，不拦普通 App 监听；混淆或改包的 SDK 可能漏掉。", 12f, Color.DKGRAY, false))
        note.addView(text("设置修改后，强行停止目标应用再重新打开。当前为 1.0 测试版，不保证所有开屏/摇一摇广告均可识别。", 12f, accent, false))
        val foot = text("应用作用域由 Xposed 管理器控制 · 默认不注入系统框架", 12f, Color.GRAY, false)
        foot.gravity = Gravity.CENTER_HORIZONTAL
        root.addView(foot, LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(25) })
    }

    private fun text(value:String, sp:Float, color:Int, bold:Boolean): TextView = TextView(this).apply {
        text = value
        textSize = sp
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
        setPadding(0,dp(3),0,dp(5))
    }
    private fun dp(v:Int) = (v * resources.displayMetrics.density + 0.5f).toInt()
}
