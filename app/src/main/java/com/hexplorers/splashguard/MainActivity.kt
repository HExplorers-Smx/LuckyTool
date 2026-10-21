package com.hexplorers.splashguard

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highcapable.yukihookapi.hook.factory.preferences
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CrashRecovery.isSafe(this)) {
            showRecoveryScreen()
            return
        }
        try {
        val prefs = preferences("splashguard")
        setContent {
            MiuixTheme(controller = remember { ThemeController(ColorSchemeMode.MonetSystem) }) {
                var tab by rememberSaveable { mutableIntStateOf(0) }
                val backdrop = rememberLayerBackdrop()
                val primary = MiuixTheme.colorScheme.primary
                val ink = MiuixTheme.colorScheme.onSurface
                val muted = MiuixTheme.colorScheme.onSurfaceVariantSummary
                Box(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)) {
                    Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp).padding(top = 34.dp, bottom = 116.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(painterResource(R.drawable.app_icon_v2), "简净图标",
                                    Modifier.size(70.dp).clip(RoundedCornerShape(21.dp)))
                                Spacer(Modifier.width(15.dp))
                                Column {
                                    Label("简净", 28, ink, true)
                                    Label("让手机世界，更加纯净", 13, muted)
                                }
                            }
                            Spacer(Modifier.height(22.dp))
                            when (tab) {
                                0 -> HomePage(
                                    ink, muted,
                                    read = { key, fallback -> prefs.getBoolean(key, fallback) },
                                    write = { key, value -> prefs.edit { putBoolean(key, value) } },
                                    readWindow = { prefs.getInt(SkipSettings.WINDOW_SECONDS, 8) },
                                    writeWindow = { prefs.edit { putInt(SkipSettings.WINDOW_SECONDS, it) } })
                                1 -> RulesPage(
                                    ink, muted, raw = { prefs.getString(RuleCatalog.KEY, "") },
                                    save = { prefs.edit { putString(RuleCatalog.KEY, it) } },
                                    toast = { Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show() })
                                else -> AboutPage(ink, muted)
                            }
                        }
                    }
                    // A real blurred backdrop is captured by layerBackdrop above.
                    Box(Modifier.align(Alignment.BottomCenter).padding(horizontal = 18.dp, vertical = 17.dp)
                        .fillMaxWidth().height(77.dp)
                        .then(if (Build.VERSION.SDK_INT >= 33)
                            Modifier.textureBlur(backdrop = backdrop,
                                shape = RoundedCornerShape(30.dp), blurRadius = 24f)
                            else Modifier.background(MiuixTheme.colorScheme.surface.copy(alpha = .87f),
                                RoundedCornerShape(30.dp)))
                        .clip(RoundedCornerShape(30.dp))
                        .background(MiuixTheme.colorScheme.surface.copy(alpha = .53f))
                        .border(1.dp, Color.White.copy(alpha = .6f), RoundedCornerShape(30.dp))
                        .padding(7.dp)) {
                        Row(Modifier.fillMaxSize()) {
                            listOf("首页" to "⌂", "规则" to "▤", "我的" to "⚙")
                                .forEachIndexed { index, item ->
                                    val chosen = index == tab
                                    val bg by animateColorAsState(
                                        if (chosen) primary.copy(alpha = .17f) else Color.Transparent,
                                        label = "selectedGlassTab")
                                    Column(
                                        Modifier.weight(1f).fillMaxHeight()
                                            .clip(RoundedCornerShape(23.dp)).background(bg)
                                            .clickable { tab = index },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Label(item.second, 22, if (chosen) primary else ink.copy(alpha = .6f), chosen)
                                        Label(item.first, 11, if (chosen) primary else ink.copy(alpha = .6f), chosen)
                                    }
                                }
                        }
                    }
                }
            }
        }
        } catch (error: Throwable) {
            CrashRecovery.record(this, error)
            showRecoveryScreen()
        }
    }

    private fun showRecoveryScreen() {
        // Deliberately native Views: this page must not depend on Compose,
        // Miuix or the wallpaper color engine that may be crashing.
        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            val spacing = (20 * resources.displayMetrics.density).toInt()
            setPadding(spacing, spacing * 2, spacing, spacing)
            setBackgroundColor(android.graphics.Color.rgb(242, 247, 252))
        }
        fun line(value: String, large: Boolean = false): android.widget.TextView =
            android.widget.TextView(this).apply {
                text = value
                textSize = if (large) 24f else 14f
                setTextColor(android.graphics.Color.rgb(28, 44, 75))
                setPadding(0, 12, 0, 12)
            }
        root.addView(line("简净 · 安全模式", true))
        root.addView(line("检测到上次启动时界面异常。设置和规则没有被清除。"))
        root.addView(line("你可以复制崩溃日志，然后重新尝试打开现代化界面。"))
        val message = CrashRecovery.trace(this).ifBlank { "无已记录的崩溃堆栈" }
        root.addView(android.widget.Button(this).apply {
            text = "复制崩溃日志"
            setOnClickListener {
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("简净崩溃日志", message))
                android.widget.Toast.makeText(this@MainActivity, "日志已复制", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(android.widget.Button(this).apply {
            text = "重新尝试打开简净"
            setOnClickListener {
                CrashRecovery.retry(this@MainActivity)
                recreate()
            }
        })
        val scroll = android.widget.ScrollView(this).apply { addView(root) }
        setContentView(scroll)
    }
}

@Composable
private fun Label(text: String, size: Int, color: Color, bold: Boolean = false,
                  modifier: Modifier = Modifier) =
    BasicText(text, modifier, style = TextStyle(
        color = color, fontSize = size.sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal))

@Composable
private fun Header(text: String, ink: Color) {
    Label(text, 17, ink, true, Modifier.padding(start = 5.dp, bottom = 10.dp))
}

@Composable
private fun HomePage(
    ink: Color, muted: Color,
    read: (String, Boolean) -> Boolean, write: (String, Boolean) -> Unit,
    readWindow: () -> Int, writeWindow: (Int) -> Unit
) {
    var enabled by remember { mutableStateOf(read(SkipSettings.ENABLED, true)) }
    var shake by remember { mutableStateOf(read(SkipSettings.BLOCK_SHAKE_ADS, true)) }
    var strict by remember { mutableStateOf(read(SkipSettings.STRICT, true)) }
    var topRight by remember { mutableStateOf(read(SkipSettings.TOP_RIGHT, true)) }
    var gyro by remember { mutableStateOf(read(SkipSettings.INCLUDE_GYROSCOPE, false)) }
    var logs by remember { mutableStateOf(read(SkipSettings.DEBUG_LOG, false)) }
    var seconds by remember { mutableIntStateOf(readWindow().coerceIn(4,12)) }

    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 25.dp) {
        Column(Modifier.padding(20.dp)) {
            Label("✦  防护规则已就绪", 21, ink, true)
            Spacer(Modifier.height(9.dp))
            Label("需在 LSPosed 中启用模块并勾选作用域，才能实际生效。", 13, muted)
        }
    }
    Spacer(Modifier.height(22.dp))
    Header("广告防护", ink)
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
        SwitchPreference(title = "开屏广告跳过", summary = "识别并点击广告的跳过按钮",
            checked = enabled, onCheckedChange = { enabled = it; write(SkipSettings.ENABLED, it) })
        SwitchPreference(title = "摇一摇广告拦截", summary = "阻止已识别广告 SDK 注册运动传感器",
            checked = shake, onCheckedChange = { shake = it; write(SkipSettings.BLOCK_SHAKE_ADS, it) })
        SwitchPreference(title = "严格模式", summary = "优先匹配广告特征，降低误点击",
            checked = strict, onCheckedChange = { strict = it; write(SkipSettings.STRICT, it) })
    }
    Spacer(Modifier.height(22.dp))
    Header("高级设置", ink)
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
        SwitchPreference(title = "右上角优先", summary = "避免误点登录和隐私授权",
            checked = topRight, onCheckedChange = { topRight = it; write(SkipSettings.TOP_RIGHT, it) })
        SwitchPreference(title = "陀螺仪广告拦截", summary = "增强模式，默认关闭",
            checked = gyro, onCheckedChange = { gyro = it; write(SkipSettings.INCLUDE_GYROSCOPE, it) })
        SwitchPreference(title = "诊断日志", summary = "记录广告 SDK 的拦截信息",
            checked = logs, onCheckedChange = { logs = it; write(SkipSettings.DEBUG_LOG, it) })
    }
    Spacer(Modifier.height(22.dp))
    Header("开屏检测时长", ink)
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp) {
        Column(Modifier.padding(18.dp)) {
            Label(seconds.toString() + " 秒", 21, ink, true)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(4,6,8,10,12).forEach { value ->
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (seconds == value) MiuixTheme.colorScheme.primary.copy(alpha=.18f)
                            else muted.copy(alpha=.09f))
                        .clickable { seconds = value; writeWindow(value) }
                        .padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        Label(value.toString(), 14, if (seconds == value) MiuixTheme.colorScheme.primary else ink,
                            seconds == value)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(15.dp))
    Label("设置后强制停止并重启目标应用。不会关闭系统级传感器。", 12, muted)
}

@Composable
private fun ActionButton(text: String, modifier: Modifier = Modifier, action: () -> Unit) {
    Box(modifier.clip(RoundedCornerShape(15.dp))
        .background(MiuixTheme.colorScheme.primary.copy(alpha = .14f))
        .clickable(onClick = action).padding(horizontal = 15.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center) {
        Label(text, 14, MiuixTheme.colorScheme.primary, true)
    }
}

@Composable
private fun RulesPage(
    ink: Color, muted: Color,
    raw: () -> String, save: (String) -> Unit, toast: (String) -> Unit
) {
    var rules by remember { mutableStateOf(runCatching { RuleCatalog.parse(raw()) }.getOrDefault(emptyList())) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readNBytes(131073) }
                ?: error("无法读取文件")
            require(bytes.size <= 131072) { "最多 128KB" }
            val parsed = RuleCatalog.parse(bytes.toString(Charsets.UTF_8))
            save(RuleCatalog.encode(parsed))
            rules = parsed
            toast("导入成功：" + parsed.size + " 条规则")
        }.onFailure { toast("导入失败：" + it.message) }
    }
    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(RuleCatalog.encode(rules).toByteArray())
            } ?: error("无法写入")
            toast("导出成功")
        }.onFailure { toast("导出失败：" + it.message) }
    }
    Header("本地规则订阅", ink)
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 25.dp) {
        Column(Modifier.padding(19.dp)) {
            Label("受限制的安全规则格式", 19, ink, true)
            Spacer(Modifier.height(8.dp))
            Label("借鉴 GKD 的规则订阅管理，使用简净 schema=1 JSON。拒绝脚本、任意坐标和危险点击。", 13, muted)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                ActionButton("导入 JSON", Modifier.weight(1f)) {
                    importer.launch(arrayOf("application/json", "text/plain"))
                }
                ActionButton("导出规则", Modifier.weight(1f)) {
                    exporter.launch("jianjing-rules.json")
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    Header("规则管理 · " + rules.size + "/64", ink)
    if (rules.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
            Column(Modifier.padding(18.dp)) {
                Label("暂无自定义规则", 17, ink, true)
                Spacer(Modifier.height(9.dp))
                Label("内置开屏与摇一摇规则不受影响。可添加示例规则并导出作为模板。", 13, muted)
                Spacer(Modifier.height(12.dp))
                ActionButton("添加示例") {
                    rules = RuleCatalog.parse(RuleCatalog.EXAMPLE)
                    save(RuleCatalog.encode(rules))
                }
            }
        }
    } else {
        rules.forEach { rule ->
            Card(modifier = Modifier.fillMaxWidth().padding(bottom=10.dp), cornerRadius=22.dp) {
                SwitchPreference(
                    title = rule.name, summary = rule.packageName + " · " + rule.text,
                    checked = rule.enabled, onCheckedChange = { on ->
                        rules = rules.map { if (it.id == rule.id) it.copy(enabled=on) else it }
                        save(RuleCatalog.encode(rules))
                    }
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    Label("规则仅匹配指定包名、文字和开屏时间窗口；不执行任意脚本。", 12, muted)
}

@Composable
private fun AboutPage(ink: Color, muted: Color) {
    Header("关于简净", ink)
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius=24.dp) {
        Column(Modifier.padding(20.dp)) {
            Label("简净 1.1", 21, ink, true)
            Spacer(Modifier.height(10.dp))
            Label("YukiHook 1.5.0 · libxposed API 102", 14, muted)
            Spacer(Modifier.height(10.dp))
            Label("使用真正的 MIUIX Compose 组件，Android 13+ 采用 MIUIX Blur 浮动玻璃底栏。", 13, muted)
            Spacer(Modifier.height(10.dp))
            Label("规则格式受限，不支持直接导入 GKD 的全部 JSON5 订阅；内置识别覆盖常见广告 SDK。", 13, muted)
            Spacer(Modifier.height(10.dp))
            Label("请在 Xposed 管理器中设置作用域，勿启用系统服务和敏感金融应用。", 13, muted)
        }
    }
}
