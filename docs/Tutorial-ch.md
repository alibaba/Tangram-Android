## 1.引入依赖

最新版本号从 [release 说明](https://github.com/alibaba/Tangram-Android/releases)里获取，比如：

```
// gradle
compile 'com.alibaba.android:tangram:2.0.5@aar'

// 最新版本引入了rxjava，需要自行添加rx依赖
compile 'io.reactivex.rxjava2:rxjava:2.1.12'
compile 'io.reactivex.rxjava2:rxandroid:2.0.2'
```

或者

```
// maven
<dependency>
  <groupId>com.alibaba.android</groupId>
  <artifactId>tangram</artifactId>
  <version>2.0.5</version>
  <type>aar</type>
</dependency>
<dependency>
  <groupId>io.reactivex.rxjava2</groupId>
  <artifactId>rxjava</artifactId>
  <version>2.1.12</version>
  <type>aar</type>
</dependency>
<dependency>
  <groupId>io.reactivex.rxjava2</groupId>
  <artifactId>rxandroid</artifactId>
  <version>2.0.2</version>
  <type>aar</type>
</dependency>
```

## 2.初始化 Tangram 环境

应用全局只需要初始化一次，提供一个通用的图片加载器，一个应用内通用的ImageView类型（通常情况下每个应用都有自定义的 ImageView，如果没有的话就提供系统的 ImageView 类）。

```
TangramBuilder.init(context, new IInnerImageSetter() {
	@Override
	public <IMAGE extends ImageView> void doLoadImageUrl(@NonNull IMAGE view,
                    @Nullable String url) {
		//假设你使用 Picasso 加载图片                		Picasso.with(context).load(url).into(view);
	}
}, ImageView.class);
```

## 3.初始化 ```TangramBuilder```

在 Activity 中初始化```TangramBuilder```，假设你的 Activity 是```TangramActivity```。

```
TangramBuilder.InnerBuilder builder = TangramBuilder.newInnerBuilder(TangramActivity.this);
```

这一步 builder 对象生成的时候，内部已经注册了框架所支持的所有组件和卡片，以及默认的```IAdapterBuilder```（它被用来创建 绑定到 RecyclerView 的Adapter）。

## 4.注册自定义的卡片和组件

一般情况下，内置卡片的类型已经满足大部分场景了，业务方主要是注册一下自定义组件。注册组件有3种方式：

+ 注册绑定组件类型和自定义```View```，比如```builder.registerCell(1, TestView.class);```。意思是类型为1的组件渲染时会被绑定到```TestView```的实例上，这种方式注册的组件使用通用的组件模型```BaseCell```。
+ 注册绑定组件类型、自定义 model、自定义```View```，比如```builder.registerCell(1, TestCell.class, TestView.class);```。意思是类型为1的组件使用自定义的组件模型```TestCell```，它应当继承于```BaseCell```，在渲染时会被绑定到```TestView```的实例上。
+ 注册绑定组件类型、自定义model、自定义```ViewHolder```，比如```builder.registerCell(1, TestCell.class, new ViewHolderCreator<>(R.layout.item_holder, TestViewHolder.class, TestView.class));```。意思是类型为1的组件使用自定义的组件模型```TestCell```，它应当继承于```BaseCell```，在渲染时以```R.layout.item_holder```为布局创建类型为```TestView ```的 view，并绑定到类型为```TestViewHolder```的 viewHolder 上，组件数据被绑定到定到```TestView```的实例上。

一般情况下，使用前两种方式注册组件即可。至于组件开发规范，请参考[组件文档]()。

## 5.生成```TangramEngine```实例

在上述基础上调用：

```
TangramEngine engine = builder.build();
```

## 6.绑定业务 support 类到 engine

Tangram 内部提供了一些常用的 support 类辅助业务开发，业务方也可以自定义所需要的功能模块注册进去。以下常用三个常用的support，分别处理点击、卡片数据加载、曝光逻辑，详情请参考[文档]()。

```
engine.register(SimpleClickSupport.class, new XXClickSupport());
engine.register(CardLoadSupport.class, new XXCardLoadSupport());
engine.register(ExposureSupport.class, new XXExposureSuport());
```

## 7.绑定 recyclerView

```
setContentView(R.layout.main_activity);
RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_view);
...
engine.bindView(recyclerView);
```

## 8.监听 recyclerView 的滚动事件

```
recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);
		//在 scroll 事件中触发 engine 的 onScroll，内部会触发需要异步加载的卡片去提前加载数据
		engine.onScrolled();
	}
});
```

## 9.设置悬浮类型布局的偏移（可选）

如果你的 recyclerView 上方还覆盖有其他 view，比如底部的 tabbar 或者顶部的 actionbar，为了防止悬浮类 view 和这些外部 view 重叠，可以设置一个偏移量。

```
engine.getLayoutManager().setFixOffset(0, 40, 0, 0);
```

## 10.设置卡片预加载的偏移量（可选）

在页面滚动过程中触发```engine.onScrolled()```方法，会去寻找屏幕外需要异步加载数据的卡片，默认往下寻找5个，让数据预加载出来，可以修改这个偏移量。

```
engine.setPreLoadNumber(3)
```

# 11.加载数据并传递给 engine

数据一般是调用接口加载远程数据，这里演示的是 mock 加载本地的数据：

```
String json = new String(getAssertsFile(this, "data.json"));
        JSONArray data = null;
        try {
            data = new JSONArray(json);
            engine.setData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
```

完整页面的数据结构可参考 [Demo](https://github.com/alibaba/Tangram-Android/tree/master/examples) 里。
