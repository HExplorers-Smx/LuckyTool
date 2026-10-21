# 开屏净 SplashGuard 1.0

**独立 APK**，不是 LuckyTool 的模块补丁。

技术栈：YukiHook 1.5.0 正式版、libxposed API 102、Android SDK 37.2、JDK 21。

- **开屏广告跳过**：限定在前台启动最初几秒扫描 View 树，只点击可见的“跳过 / Skip”等按钮。对敏感页面的活动名做排除；支持可选严格模式、右上角限制、4–12 秒扫描时间。
- **摇一摇广告拦截**：只阻止已识别广告 SDK 使用 SensorManager 注册加速度传感器监听，默认启用；增强陀螺仪拦截默认关闭；允许开关诊断日志。未识别 SDK 和正常应用的动作传感器监听原样放行。
- 识别已知 SDK 命名空间：优量汇、穿山甲、快手、百度、Sigmob、华为、小米、Mintegral、TopOn 等。混淆/重打包后可能失效，**不承诺全部摇一摇广告有效**。
- 使用 LSPosed 等 API 102 兼容框架，手动选择要启用的应用作用域。不要勾选系统框架或敏感银行支付应用。
- 不拦截广告网络、不修改登录、支付和权限请求，也不会全局禁用传感器。
- 默认启用开屏跳过和摇一摇拦截；修改设置后需强行停止目标应用重新启动。功能效果需真机验证。

本独立工程暂存于 LuckyTool Fork 的 `standalone/splashguard-api102` 分支，Git 代码与 LuckyTool 原有工程分开。可自行新建仓库。

```bash
./gradlew :app:assembleDebug
```

源码打包交付时包含独立工程与 Gradle Wrapper。CI 验证包含 libxposed API 102 元数据校验；真实广告拦截效果不能通过 CI 保证。
