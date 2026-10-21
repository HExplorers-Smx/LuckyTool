# 简净 SplashGuard 1.1

独立的 Android 13+ Xposed API 102 模块，不影响 LuckyTool。

## 新增
- 基于 Miuix 0.9.4 的 Compose 真实原生 UI，支持系统动态配色。
- 底部三标签使用 miuix-blur 的 layerBackdrop + textureBlur 实际背景模糊；浮动高亮、磨砂玻璃边框。
- 采用用户提供的彩虹少女线稿裁剪作为桌面启动图标。
- 本地 JSON 导入/导出，schema=1，最多 64 条，最大 128KB；可逐条启停。
- 自定义规则限制为应用包名及可见文字，作用于开屏前几秒；不执行外部脚本，不兼容完整 GKD JSON5。
- YukiHook 1.5.0 正式版 + libxposed API 102，保留开屏自动跳过和摇一摇广告 SDK 传感器拦截。
- 设置改动后需强制停止目标 App 才可生效。

## 构建
./gradlew :app:assembleDebug
