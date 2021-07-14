# 注意，该项目停止维护！！！

# Tangram for Android

[English Document](README.md)

## Tangram 相关开源库

### Android

+ [Tangram-Android](https://github.com/alibaba/Tangram-Android)
+ [Virtualview-Android](https://github.com/alibaba/Virtualview-Android)
+ [vlayout](https://github.com/alibaba/vlayout)
+ [UltraViewPager](https://github.com/alibaba/UltraViewPager)

### iOS

+ [Tangram-iOS](https://github.com/alibaba/Tangram-iOS)
+ [Virtualview-iOS](https://github.com/alibaba/VirtualView-iOS)
+ [LazyScrollView](https://github.com/alibaba/lazyscrollview)

Tangram是一套动态化构建 Native 页面的框架，它包含 Tangram Android、Tangram iOS，管理后台等一些列基础设施。本工程是 Tangram Android 的sdk 项目地址，底层依赖于[vlayout](https://github.com/alibaba/vlayout) 和 [UltraViewPager](https://github.com/alibaba/UltraViewPager)。

# 特点
清注意勿使用tangram3 package下的类！Tangram3.0 正在开发中，为保持兼容，保留了原tangram的package，并新增了tangram3的package，tangram3正在密集开发中，可能会有频繁的接口调整，不推荐用于线上正式App开发！

- Android iOS 双平台支持，iOS 版本参考开源库 [Tangram-iOS](https://github.com/alibaba/Tangram-iOS)。
- 通过 json 创建页面视图，并提供了默认的解析器。
- 可轻松实现页面视图的回收与复用。
- 框架提供多种默认的布局方式。
- 通过 json 数据或代码支持自定义布局样式。
- 高性能，基于[vlayout](https://github.com/alibaba/vlayout)
- 支持扩展功能模块

# 示例

![](docs/images/tangramdemo.gif)

# 基本概念
+ 卡片：同一区块的组件集合，负责对组件进行布局。
+ 组件：最小业务单元，好比 RecyclelrView 的 Item。

# 默认卡片

* 通用流式布局卡片（网格）
* 线性布局卡片
* 固定位置布局卡片
* 滑动固定布局卡片
* 滑动吸顶/吸底布局卡片
* 一拖N布局卡片
* 轮播布局卡片
* 瀑布流布局卡片
* 悬浮拖动布局卡片

# 接入教程
详情见[上手教程](docs/Tutorial-ch.md)。

# Tangram 文档

详细的介绍文档参考[这里](http://tangram.pingguohe.net/)。

# 贡献代码

在提 Issue 或者 PR 之前，建议先阅读[Contributing Guide](CONTRIBUTING.md)。按照规范提建议。

# 开源许可证
Tangram 遵循MIT开源许可证协议。

# 微信群

![](https://img.alicdn.com/tfs/TB11_2_kbSYBuNjSspiXXXNzpXa-167-167.png)

搜索 `tangram_` 或者扫描以上二维码添加 Tangram 为好友，以便我们邀请你入群。
