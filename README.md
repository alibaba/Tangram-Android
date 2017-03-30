# Tangram for Android

[中文文档](README-ch.md)

Tangram is a framework to build native page dynamically based on ```RecyclerView```. It treats a page as structured by a series of card and cell, renders and updates the page's UI by card and cell's data description. 

# Features

- Two platform support (iOS & Android, See [Tangram-iOS]() in Github for iOS Version)
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
See details at [Tutorial](docs/TUTORIAL.md).

# Tangram Documents

See complete [documents]() here.

# LICENSE
Tangram is available under the MIT license.