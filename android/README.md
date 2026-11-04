# 歪理研究所 Android 安装版

原生 Java / Android SDK 应用，包名 `com.calibg.waililevel`，显示名称「水平仪」。Android 8.0（API 26）及以上，版本 1.0.4。

- 使用重力传感器，依次回退到游戏旋转向量和经过低通滤波的加速度计。
- 手机屏幕相对水平面 0–10° 显示 0°；15° 显示 5°，20° 显示 10°。双向和斜向一起计算。
- 前台才读取传感器；切到后台停止采集，返回后自动恢复。缺少传感器或初始读取超时显示错误。
- 界面采用黑底、双圆和大号角度，水平时变绿；打开自动测量，不显示品牌标语、试玩入口或恶搞文案。
- 完全离线，不声明网络、位置、相机等权限，不上传传感器数据。
- 按用户要求使用苹果指南针图标，包含传统启动器 PNG 和 Android 自适应图标；App 名称仍为“水平仪”。

## 构建

安装 JDK 17、Android SDK 35，设置 `JAVA_HOME` 和 `ANDROID_HOME`（也可设置本地 `local.properties` 的 `sdk.dir`），执行：

```sh
./gradlew testReleaseUnitTest assembleRelease
```

产物 `app/build/outputs/apk/release/app-release-unsigned.apk` 需要使用本项目专用发布密钥，经 Android SDK 的 `apksigner` 签名后才可分发。已签名版本位于网站的 `docs/downloads/Level-1.0.4.apk`，校验值在同目录 `SHA256SUMS.txt`。

发布密钥单独保存在构建者电脑的 Android 私有密钥目录，不进入 Git 或网站。后续更新必须保留同一包名、同一签名密钥，并增加 versionCode，否则无法覆盖安装。

参考：[Android 动作传感器](https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion)、[APK 签名工具](https://developer.android.com/tools/apksigner)。

## 已完成验证

- Release 构建、9 项原生计算测试、APK v2/v3 签名校验通过。
- Android 15 模拟器安装成功；通过模拟器传感器输入验证 0°→0°、15°→5°、20°→10°以及后台停止采集及返回自动恢复的行为。
- 网站浏览器测试验证了实际 APK 下载与 iOS 联系本人安装提示。
- 尚未在真实安卓手机上测试传感器。

1.0.1 保持原包名和签名，versionCode 增为 2，已在模拟器验证从 1.0.0 覆盖安装。

1.0.2 将 App 名称改为“水平仪”，替换水平仪图标；包名和签名保持一致，versionCode 为 3。

1.0.3 替换为苹果指南针图标，名称保持“水平仪”；versionCode 为 4，沿用原签名。图标来源：https://apps.apple.com/us/app/compass/id1067456176 。

1.0.4 支持在 App 内容区域任意位置双击，在真实角度与原有显示规则间切换；数字、指示圆和水平状态同步切换，无弹窗、模式文字、声音或震动。切换即时使用最近一帧传感器数据；后台返回保留当前模式，重新启动默认原有显示规则。versionCode 为 5。
