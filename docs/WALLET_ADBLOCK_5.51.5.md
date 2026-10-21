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

## 2026-10-21 追加：管钱页“大家还在用”

用户截图中，钱包“管钱”页依然显示“大家还在用”一栏（京东金融、东方证券期货等应用推广）。原有 `HideAdCards` 仅覆盖 operate/adranking/commerce/synad ViewHolder，**漏掉了 activation 这一类应用下载推广**。

重新从 APK 5.51.5（classes2.dex）验证到：

- `com.finshell.advertisement.activation.QcpAppVerticalViewHolder`：extends `com.chad.library.adapter.base.temp.viewholder.QuickViewHolder`，有 `Z(int, jf.a)`、`Y(int)`。
- `com.finshell.advertisement.activation.QcpAppHorizonViewHolder`：同样继承 `QuickViewHolder`，有 `Z(int, jf.a)`。
- `QuickViewHolder` 是 `RecyclerView.ViewHolder` 的子类，适用于仅隐藏对应 `itemView` 的现有实现。

在既有“隐藏钱包推广与应用推荐卡片（含管钱页）”开关下，新增两个专用 ViewHolder 的构造以及数据绑定后隐藏操作（View.GONE、height=0、margin=0）。不拦截支付、贷款计算器、银行卡或网络请求。**静态分析只能确认这些专用广告类存在；它们与截图卡片的运行时对应关系仍待真机复测**。若还有广告，需要用运行时视图树/日志进一步定位，不应直接清空整个管钱页。
