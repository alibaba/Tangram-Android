# Tangram for Android

[中文文档](README-ch.md)

Tangram is a modular UI solution for building native page dynamically including Tangram for Android, Tangram for iOS and even backend CMS. This project provides the sdk on Android which is based on [vlayout](https://github.com/alibaba/vlayout) and [UltraViewPager](https://github.com/alibaba/UltraViewPager).

# Features

- Two platform support (iOS & Android, See [Tangram-iOS](https://github.com/alibaba/Tangram-iOS) in Github for iOS Version)
- Fast Generate View by JSON Data , provide default parser.
- Easily control the reuseability of views
- Provide multiple Built-in layouts
- Custom layout style (by JSON Data or code)
- High performance (Base on [vlayout](https://github.com/alibaba/vlayout))
- Extendable API

# demo

![](docs/images/tangramdemo.gif)

# Basic Concepts
+ Card, a group of cells, is responsible for layouting child cells.
+ Cell, smallest business UI element, like an item in RecyclerView.

# Default cards
* Flow Card（like grid）
* Linear Card
* Fix Card
* Scroll Fix Card
* Sticky Card
* One drag N Card
* Page Scroll Card
* Water Flow Card
* Dragable Card

# Get started
See details at [Tutorial](docs/Tutorial.md).

# Tangram Documents

See complete [documents](http://tangram.pingguohe.net/) here.

# Contributing

Before you open an issue or create a pull request, please read [Contributing Guide](CONTRIBUTING.md) first.

# LICENSE
Tangram is available under the MIT license.
