## 1.Import dependency

```
// gradle
compile 'com.tmall.android:tangram:1.0.0@aar'
```

or

```
// maven
<dependency>
  <groupId>com.tmall.android</groupId>
  <artifactId>tangram</artifactId>
  <version>1.0.0</version>
  <type>aar</type>
</dependency>
```

## 2.Initialize Tangram

Initialize Tangram globally, provide a universal image loader, and a custom ImageView class(Usually an app has a custom ImageView class extended from system ImageView, provide system ImageView by default).

```
TangramBuilder.init(context, new IInnerImageSetter() {
	@Override
	public <IMAGE extends ImageView> void doLoadImageUrl(@NonNull IMAGE view,
                    @Nullable String url) {
		//here assume you use Picasso to load image                		Picasso.with(context).load(url).into(view);
	}
}, ImageView.class);
```

## 3.Initialize ```TangramBuilder```

Initialize ```TangramBuilder``` at the ```onCreate()``` of your Activity, assume your Activity is ```TangramActivity```.

```
TangramBuilder.InnerBuilder builder = TangramBuilder.newInnerBuilder(TangramActivity.this);
```

When the builder object created, default cards and cells supported by Tangram is already registered. An default ```IAdapterBuilder``` is also created to create an ```Adapter``` instance used by ```RecyclerView```.

## 4.Register custom card and cell

The default cards provided by framework meets the most UI situation, while cells need be provided by Tangram users.  There's three ways to register a custom:

+ bind a cell's type to a custom view class using ```builder.registerCell(1, TestView.class);```, which means a cell's data with type 1 will be binded to an instance of type ```TestView```. Registering in this way, the cell use general model class ```BaseCell```.
+ bind a cell's type to a custom model class and custom view class using ```builder.registerCell(1, TestCell.class, TestView.class);```, which means a cell with type 1 will use custom model class ```TestCell``` which should be extended from ```BaseCell``` and its data will be binded to an instance of ```TestView``` during rendering.
+ bind a cell's type to a custom class and custom viewholder class using ```builder.registerCell(1, TestCell.class, new ViewHolderCreator<>(R.layout.item_holder, TestViewHolder.class, TestView.class));```, which means a cell with type 1 will use custom model class ```TestCell``` which should be extended from ```BaseCell``` and its data will be binded to a view instance created by ```ViewHolderCreator ```. The view creator inflates the view from ```R.layout.item_holder``` and binds it to the ```TestViewHolder``` instance.

The most common way to regiser custom cell is the former two. As to developping a custom cell, please read [document]() here.

## 5.Create ```TangramEngine``` instance

Call:

```
TangramEngine engine = builder.build();
```

## 6.Register support module to engine

Tangram provides some common support module to assist business development. Users could also register your custom support module. Here are three build-in support modules, for more details see [document]().

```
engine.register(SimpleClickSupport.class, new XXClickSupport());
engine.register(CardLoadSupport.class, new XXCardLoadSupport());
engine.register(ExposureSupport.class, new XXExposureSuport());
```

## 7.Bind to recyclerView

```
setContentView(R.layout.main_activity);
RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_view);
...
engine.bindView(recyclerView);
```

## 8.Listen recyclerView's onScroll

```
recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);
		//trigger engine's onScroll to preload data if needed
		engine.onScrolled();
	}
});
```

## 9.Setting fix offset to float views (Option)

If your recycelrView has other view coverd on top of it, such as a tabbar at bottom or a actionbar at top. In order to provent float view in Tangram overiding with them, here is a way to set an offset.

```
engine.getLayoutManager().setFixOffset(0, 40, 0, 0);
```

## 10.Setting preload number(Option)

As mentioned before, ```engine.onScrolled()``` would be trigged during the scroll of recyclerView, then engine will look for card that need load its data async. By default engine look for next 5 cards at most, users can change the number.

```
engine.setPreLoadNumber(3)
```

# 11.Load page data and bind to engine

Business data always loaded from server, here we just read from asset for simplity.

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

A complete data structure of a page could be found at [Demo]()'s asset。
