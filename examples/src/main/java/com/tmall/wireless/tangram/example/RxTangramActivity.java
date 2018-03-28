/*
 * MIT License
 *
 * Copyright (c) 2018 Alibaba Group
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tmall.wireless.tangram.example;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.libra.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.TangramEngine;
import com.tmall.wireless.tangram.dataparser.DataParser;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.PojoDataParser;
import com.tmall.wireless.tangram.example.data.RatioTextView;
import com.tmall.wireless.tangram.example.data.SimpleImgView;
import com.tmall.wireless.tangram.example.data.SingleImageView;
import com.tmall.wireless.tangram.example.data.TestView;
import com.tmall.wireless.tangram.example.data.TestViewHolder;
import com.tmall.wireless.tangram.example.data.TestViewHolderCell;
import com.tmall.wireless.tangram.example.data.VVTEST;
import com.tmall.wireless.tangram.example.support.SampleClickSupport;
import com.tmall.wireless.tangram.op.AppendGroupOp;
import com.tmall.wireless.tangram.op.LoadGroupOp;
import com.tmall.wireless.tangram.op.LoadMoreOp;
import com.tmall.wireless.tangram.op.UpdateCellOp;
import com.tmall.wireless.tangram.reactive.JSONArrayObservable;
import com.tmall.wireless.tangram.reactive.ViewClickObservable;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.viewcreator.ViewHolderCreator;
import com.tmall.wireless.tangram.support.BannerSupport;
import com.tmall.wireless.tangram.support.RxBannerScrolledListener.ScrollEvent;
import com.tmall.wireless.tangram.support.async.CardLoadSupport;
import com.tmall.wireless.tangram.util.IInnerImageSetter;
import com.tmall.wireless.vaf.framework.VafContext;
import com.tmall.wireless.vaf.virtualview.Helper.ImageLoader.IImageLoaderAdapter;
import com.tmall.wireless.vaf.virtualview.Helper.ImageLoader.Listener;
import com.tmall.wireless.vaf.virtualview.view.image.ImageBase;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by longerian on 2018/3/14.
 *
 * @author longerian
 * @date 2018/03/14
 */

public class RxTangramActivity extends Activity {

    private static final String TAG = TangramActivity.class.getSimpleName();

    private Handler mMainHandler;
    private TangramEngine engine;
    private TangramBuilder.InnerBuilder builder;
    private RecyclerView recyclerView;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private static class ImageTarget implements Target {

        ImageBase mImageBase;

        Listener mListener;

        public ImageTarget(ImageBase imageBase) {
            mImageBase = imageBase;
        }

        public ImageTarget(Listener listener) {
            mListener = listener;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
            mImageBase.setBitmap(bitmap, true);
            if (mListener != null) {
                mListener.onImageLoadSuccess(bitmap);
            }
            Log.d("TangramActivity", "onBitmapLoaded " + from);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (mListener != null) {
                mListener.onImageLoadFailed();
            }
            Log.d("TangramActivity", "onBitmapFailed ");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d("TangramActivity", "onPrepareLoad ");
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        recyclerView = (RecyclerView) findViewById(R.id.main_view);

        //Step 1: init tangram
        TangramBuilder.init(this, new IInnerImageSetter() {
            @Override
            public <IMAGE extends ImageView> void doLoadImageUrl(@NonNull IMAGE view,
                @Nullable String url) {
                Picasso.with(RxTangramActivity.this).load(url).into(view);
            }
        }, ImageView.class);

        //Tangram.switchLog(true);
        mMainHandler = new Handler(getMainLooper());

        //Step 2: register build=in cells and cards
        builder = TangramBuilder.newInnerBuilder(this);

        //Step 3: register business cells and cards
        builder.registerCell(1, TestView.class);
        builder.registerCell(10, SimpleImgView.class);
        builder.registerCell(2, SimpleImgView.class);
        builder.registerCell(4, RatioTextView.class);
        builder.registerCell(110,
            TestViewHolderCell.class,
            new ViewHolderCreator<>(R.layout.item_holder, TestViewHolder.class, TextView.class));
        builder.registerCell(199,SingleImageView.class);
        builder.registerVirtualView("vvtest");
        //Step 4: new engine
        engine = builder.build();
        engine.setVirtualViewTemplate(VVTEST.BIN);
        engine.getService(VafContext.class).setImageLoaderAdapter(new IImageLoaderAdapter() {

            private List<RxTangramActivity.ImageTarget> cache = new ArrayList<RxTangramActivity.ImageTarget>();

            @Override
            public void bindImage(String uri, final ImageBase imageBase, int reqWidth, int reqHeight) {
                RequestCreator requestCreator = Picasso.with(RxTangramActivity.this).load(uri);
                Log.d("TangramActivity", "bindImage request width height " + reqHeight + " " + reqWidth);
                if (reqHeight > 0 || reqWidth > 0) {
                    requestCreator.resize(reqWidth, reqHeight);
                }
                RxTangramActivity.ImageTarget imageTarget = new RxTangramActivity.ImageTarget(imageBase);
                cache.add(imageTarget);
                requestCreator.into(imageTarget);
            }

            @Override
            public void getBitmap(String uri, int reqWidth, int reqHeight, final Listener lis) {
                RequestCreator requestCreator = Picasso.with(RxTangramActivity.this).load(uri);
                Log.d("TangramActivity", "getBitmap request width height " + reqHeight + " " + reqWidth);
                if (reqHeight > 0 || reqWidth > 0) {
                    requestCreator.resize(reqWidth, reqHeight);
                }
                RxTangramActivity.ImageTarget imageTarget = new RxTangramActivity.ImageTarget(lis);
                cache.add(imageTarget);
                requestCreator.into(imageTarget);
            }
        });
        Utils.setUedScreenWidth(720);

        //Step 5: add card load support if you have card that loading cells async
        CardLoadSupport cardLoadSupport = new CardLoadSupport();
        engine.addCardLoadSupport(cardLoadSupport);
        Observable<Card> loadCardObservable = cardLoadSupport.observeCardLoading();
        Disposable dsp6 = loadCardObservable
            .observeOn(Schedulers.io())
            .map(new Function<Card, LoadGroupOp>() {

            @Override
            public LoadGroupOp apply(Card card) throws Exception {
                Log.d("TangramActivity", "loadCardObservable in map");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JSONArray cells = new JSONArray();
                for (int i = 0; i < 10; i++) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("type", 1);
                        obj.put("msg", "async loaded");
                        JSONObject style = new JSONObject();
                        style.put("bgColor", "#FF1111");
                        obj.put("style", style.toString());
                        cells.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                List<BaseCell> result = engine.parseComponent(cells);
                LoadGroupOp loadGroupOp = new LoadGroupOp(card, result);
                return loadGroupOp;
            }
        }).observeOn(AndroidSchedulers.mainThread())
            .subscribe(cardLoadSupport.asDoLoadFinishConsumer());
        mCompositeDisposable.add(dsp6);

        Observable<Card> loadMoreObservable = cardLoadSupport.observeCardLoadingMore();
        Disposable dsp7 = loadMoreObservable.observeOn(Schedulers.io())
            .map(new Function<Card, LoadMoreOp>() {
                @Override
                public LoadMoreOp apply(Card card) throws Exception {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.w("TangramActivity", "loadMoreObservable " + card.load + " page " + card.page);
                    JSONArray cells = new JSONArray();
                    for (int i = 0; i < 9; i++) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("type", 1);
                            obj.put("msg", "async page loaded, params: " + card.getParams().toString());
                            cells.put(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    List<BaseCell> cs = engine.parseComponent(cells);
                    //mock loading 6 pages
                    LoadMoreOp loadMoreOp = new LoadMoreOp(card, cs, card.page != 6);
                    return loadMoreOp;
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(cardLoadSupport.asDoLoadMoreFinishConsumer());
        mCompositeDisposable.add(dsp7);
        engine.addSimpleClickSupport(new SampleClickSupport());
        BannerSupport bannerSupport = new BannerSupport();
        engine.register(BannerSupport.class, bannerSupport);
        Disposable dsp1 = bannerSupport.observeSelected("banner1").subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d("TangramActivity", "1 selected " + integer);
            }
        });
        mCompositeDisposable.add(dsp1);
        Disposable dsp2 = bannerSupport.observeSelected("banner1").subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d("TangramActivity", "2 selected " + integer);
            }
        });
        mCompositeDisposable.add(dsp2);

        Disposable dsp3 = bannerSupport.observeScrollStateChanged("banner2").subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.d("TangramActivity", "state changed " + integer);
            }
        });
        mCompositeDisposable.add(dsp3);
        Disposable dsp4 = bannerSupport.observeScrolled("banner2").subscribe(new Consumer<ScrollEvent>() {
            @Override
            public void accept(ScrollEvent scrollEvent) throws Exception {
                Log.d("TangramActivity", "scrolled " + scrollEvent.toString());
            }
        });
        mCompositeDisposable.add(dsp4);
        //Step 6: enable auto load more if your page's data is lazy loaded
        engine.enableAutoLoadMore(true);

        //Step 7: bind recyclerView to engine
        engine.bindView(recyclerView);

        //Step 8: listener recyclerView onScroll event to trigger auto load more
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                engine.onScrolled();
            }
        });

        //Step 9: set an offset to fix card
        engine.getLayoutManager().setFixOffset(0, 40, 0, 0);

        //Step 10: get tangram data and pass it to engine
        // method 1, use simple consumer api
        //Observable.fromCallable(new Callable<byte[]>() {
        //    @Override
        //    public byte[] call() throws Exception {
        //        Log.d("TangramActivity", "call asset in thread " + Thread.currentThread().getName());
        //
        //        return getAssertsFile(getApplicationContext(), "data.json");
        //    }
        //}).subscribeOn(Schedulers.io())
        //    .observeOn(Schedulers.computation())
        //    .map(new Function<byte[], String>() {
        //    @Override
        //    public String apply(byte[] bytes) throws Exception {
        //        Log.d("TangramActivity", "to string in thread " + Thread.currentThread().getName());
        //
        //        return new String(bytes);
        //    }
        //}).map(new Function<String, JSONArray>() {
        //    @Override
        //    public JSONArray apply(String s) throws Exception {
        //        Log.d("TangramActivity", "to json in thread " + Thread.currentThread().getName());
        //        return new JSONArray(s);
        //    }
        //}).observeOn(AndroidSchedulers.mainThread())
        //    .doOnSubscribe(new Consumer<Disposable>() {
        //        @Override
        //        public void accept(Disposable disposable) throws Exception {
        //            Log.d("TangramActivity", "do subscribe in thread " + Thread.currentThread().getName());
        //        }
        //    })
        //    .subscribe(engine.asOriginalDataConsume());

        // method 2, use transformer api
        //Observable.fromCallable(new Callable<byte[]>() {
        //    @Override
        //    public byte[] call() throws Exception {
        //        Log.d("TangramActivity", "call asset in thread " + Thread.currentThread().getName());
        //
        //        return getAssertsFile(getApplicationContext(), "data.json");
        //    }
        //}).map(new Function<byte[], String>() {
        //    @Override
        //    public String apply(byte[] bytes) throws Exception {
        //        Log.d("TangramActivity", "to string in thread " + Thread.currentThread().getName());
        //
        //        return new String(bytes);
        //    }
        //}).map(new Function<String, JSONArray>() {
        //    @Override
        //    public JSONArray apply(String s) throws Exception {
        //        Log.d("TangramActivity", "to json in thread " + Thread.currentThread().getName());
        //        return new JSONArray(s);
        //    }
        //}).subscribeOn(Schedulers.io())
        //    .compose(engine.getDataTransformer())
        //    .doOnSubscribe(new Consumer<Disposable>() {
        //        @Override
        //        public void accept(Disposable disposable) throws Exception {
        //            Log.d("TangramActivity", "do subscribe in thread " + Thread.currentThread().getName());
        //        }
        //    })
        //    .subscribe(engine.asParsedDataConsume());
        // method 3, emit json array and flat map to jsonobject
        Disposable dsp8 = Observable.create(new ObservableOnSubscribe<JSONArray>() {
            @Override
            public void subscribe(ObservableEmitter<JSONArray> emitter) throws Exception {
                String json = new String(getAssertsFile(getApplicationContext(), "data.json"));
                JSONArray data = null;
                try {
                    data = new JSONArray(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                emitter.onNext(data);
                emitter.onComplete();
            }
        }).flatMap(new Function<JSONArray, ObservableSource<JSONObject>>() {
            @Override
            public ObservableSource<JSONObject> apply(JSONArray jsonArray) throws Exception {
                return JSONArrayObservable.fromJsonArray(jsonArray);
            }
        }).compose(((PojoDataParser) engine.getService(DataParser.class)).getSingleGroupTransformer(engine)) //using map of transformer
        .filter(new Predicate<Card>() {
            @Override
            public boolean test(Card card) throws Exception {
                return card.isValid();
            }
        }).map(new Function<Card, AppendGroupOp>() {
            @Override
            public AppendGroupOp apply(Card card) throws Exception {
                Thread.sleep(300);
                return new AppendGroupOp(card);
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete(new Action() {
                @Override
                public void run() throws Exception {
                    final BaseCell cell = (BaseCell)engine.getGroupBasicAdapter().getComponents().get(1);
                    mCompositeDisposable.add(ViewClickObservable.from(findViewById(R.id.last)).map(
                        new Function<Object, JSONObject>() {
                            @Override
                            public JSONObject apply(Object o) throws Exception {
                                cell.extras.put("title", "Rx标题2");
                                JSONObject jsonObject = cell.extras;
                                return jsonObject;
                            }
                        }).subscribe(cell.asUpdateConsumer()));

                }
            })
        .subscribe(engine.asAppendGroupConsumer(), new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });
        mCompositeDisposable.add(dsp8);
        //method 4, create from json
        //Disposable dsp5 = Observable.create(new ObservableOnSubscribe<AppendGroupOp>() {
        //    @Override
        //    public void subscribe(ObservableEmitter<AppendGroupOp> emitter) throws Exception {
        //        Log.d("TangramActivity", "subscribe in thread " + Thread.currentThread().getName());
        //        String json = new String(getAssertsFile(getApplicationContext(), "data.json"));
        //        JSONArray data = null;
        //        try {
        //            data = new JSONArray(json);
        //        } catch (JSONException e) {
        //            e.printStackTrace();
        //        }
        //        List<Card> cards = engine.parseData(data);
        //        for (int i = 0, size = cards.size(); i < size; i++) {
        //            emitter.onNext(new AppendGroupOp(cards.get(i)));
        //            Log.d("TangramActivity", "emitter " + i);
        //            try {
        //                Thread.sleep(500);
        //            } catch (Exception e) {
        //                e.printStackTrace();
        //            }
        //        }
        //        emitter.onComplete();
        //    }
        //}).subscribeOn(Schedulers.io())
        //.observeOn(AndroidSchedulers.mainThread())
        //.doOnComplete(new Action() {
        //    @Override
        //    public void run() throws Exception {
        //        final BaseCell cell = (BaseCell) engine.getGroupBasicAdapter().getComponents().get(1);
        //        mCompositeDisposable.add(ViewClickObservable.from(findViewById(R.id.last)).map(
        //            new Function<Object, JSONObject>() {
        //                @Override
        //                public JSONObject apply(Object o) throws Exception {
        //                    cell.extras.put("title", "Rx标题2");
        //                    JSONObject jsonObject = cell.extras;
        //                    return jsonObject;
        //                }
        //            }).subscribe(cell.asUpdateConsumer()));
        //    }
        //})
        //.subscribe(engine.asAppendGroupConsumer());
        //mCompositeDisposable.add(dsp5);

        mCompositeDisposable.add(ViewClickObservable.from(findViewById(R.id.first)).map(new Function<Object, UpdateCellOp>() {
            @Override
            public UpdateCellOp apply(Object o) throws Exception {
                BaseCell cell = (BaseCell)engine.getGroupBasicAdapter().getComponents().get(0);
                cell.extras.put("title", "Rx标题1");
                return new UpdateCellOp(cell);
            }
        }).subscribe(engine.asUpdateCellConsumer()));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (engine != null) {
            engine.destroy();
        }
        mCompositeDisposable.dispose();
    }

    public static byte[] getAssertsFile(Context context, String fileName) {
        InputStream inputStream = null;
        AssetManager assetManager = context.getAssets();
        try {
            inputStream = assetManager.open(fileName);
            if (inputStream == null) {
                return null;
            }

            BufferedInputStream bis = null;
            int length;
            try {
                bis = new BufferedInputStream(inputStream);
                length = bis.available();
                byte[] data = new byte[length];
                bis.read(data);

                return data;
            } catch (IOException e) {

            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (Exception e) {

                    }
                }
            }

            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
