# 歪理研究所 · 安卓网页水平仪

面向手机浏览器的静态娱乐网页。没有后端或运行依赖，使用 GitHub Pages HTTPS 托管。

## 使用

在安卓手机浏览器打开网页，点击「开始测量」，允许动作感应。手机屏幕相对水平面的倾斜 0–10° 显示 0°，大于 10° 后显示实际角度减 10°。包括前后、左右及斜向；正面朝上和朝下的平放状态均为水平。

手机姿态采用 `DeviceOrientationEvent` 的 beta/gamma，由 W3C Z-X-Y 旋转矩阵求得重力方向。方向按屏幕旋转修正。未启动、权限拒绝、无效数据、超时均不会显示伪造的零读数；后台暂停。真实传感器表现仍需安卓设备验证。

「先试玩一下」使用同一角度函数，明确标注模拟数据。界面数字、表情、状态和水平指示同步使用经过处理的角度。非零值最小显示 0.1°，避免越过 10° 后被四舍五入回零。

图标沿用用户指定的 Apple Measure 图标，来源：https://apps.apple.com/us/app/measure/id1383426740 。本页面是独立娱乐实验，不是苹果官方产品。

传感器信息只在浏览器内处理，无数据上传、分析脚本或外部字体。浏览器菜单可添加到主屏幕；当前需要联网加载，不承诺离线使用。

## 开发

- `npm ci`
- `npm start`，打开 http://127.0.0.1:4173
- `npm test`：计算、阈值、方向、无效输入测试
- `npm run test:browser`：Chromium 界面和模拟传感器测试，配置默认使用本机 Google Chrome
- 部署目录：`docs/`，静态相对路径，可部署在 GitHub Pages 项目子路径

## 发布

仓库使用 `main` 分支的 `/docs` 目录发布；页面不需要构建。

## 参考

- [W3C Device Orientation and Motion](https://www.w3.org/TR/orientation-event/)
- [GitHub Pages 发布文档](https://docs.github.com/en/pages/getting-started-with-github-pages/creating-a-github-pages-site)

## 手机安装版

网页底部提供 [Android APK 下载](https://calibg.github.io/tilt-prank-web/downloads/Measure-Level-1.0.1.apk)。当前是已签名的 1.0.1 Release 版本，Android 8.0 及以上，安装后离线运行；原生源码在 `android/`。

iOS 不上架 App Store，网页明确提示联系作者本人，由作者协助安装到 iPhone。

安卓发布密钥不包含在仓库或公开下载中。APK 校验信息位于 `docs/downloads/SHA256SUMS.txt`。
