# 钱包 5.51.5 去广告 Hook 分析

目标 APK：用户提供的 钱包_5.51.5.apk，包名 com.finshell.wallet，versionCode 551053。

## 来自 DEX 的真实目标类

- 启动页：com.nearme.wallet.SplashActivity
- 广告启动流程：com.nearme.wallet.splash.SplashViewHelper：onImageReady(...)、onCountDown(boolean,int)、onSkipAds()。
- 另有 com.finshell.homepage.splash.SplashController：不屏蔽它的 startLoad() 等启动函数。
- 广告卡片：com.finshell.advertisement 下的 operate、adranking、commerce、synad ViewHolder。
- 广告接口：com.finshell.wallet.ad.IAdService、CMNAdServiceImpl；不修改它们的网络及业务回调。

## 设计

1. 开屏广告：在广告准备/倒计时后调用钱包自己的 onSkipAds()，不替换首页 Activity，不跳过隐私协议。
2. 推广卡片：只隐藏广告专用的 RecyclerView ViewHolder，不清除网络响应，不修改交易和账户数据。
3. 两个开关独立、默认关闭，作用域仅 com.finshell.wallet。

## 限制

- 仅针对 5.51.5 进行静态分析，未经过真机运行验证。
- 使用通用 QuickCard 渲染的广告可能仍显示。
- 需要测试启动、首页、银行卡、NFC 交通卡及支付流程，更新钱包版本后需要重新分析。
