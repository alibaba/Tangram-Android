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

package com.tmall.wireless.tangram.example.tangram3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.libra.Utils;
import com.squareup.picasso.Picasso;
import com.tmall.wireless.tangram.example.R;
import com.tmall.wireless.tangram3.TangramBuilder;
import com.tmall.wireless.tangram3.TangramEngine;
import com.tmall.wireless.tangram.example.support.SampleScrollSupport;
import com.tmall.wireless.tangram3.util.IInnerImageSetter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by villadora on 15/8/18.
 */
public class Tangram3Activity extends Activity {

    private static final String TAG = Tangram3Activity.class.getSimpleName();

    private Handler mMainHandler;
    TangramEngine engine;
    TangramBuilder.InnerBuilder builder;
    RecyclerView recyclerView;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        recyclerView = (RecyclerView) findViewById(R.id.main_view);

        //Step 1: init tangram
        TangramBuilder.init(this.getApplicationContext(), new IInnerImageSetter() {
            @Override
            public <IMAGE extends ImageView> void doLoadImageUrl(@NonNull IMAGE view,
                                                                 @Nullable String url) {
                Picasso.with(Tangram3Activity.this.getApplicationContext()).load(url).into(view);
            }
        }, ImageView.class);

        //Tangram.switchLog(true);
        mMainHandler = new Handler(getMainLooper());

        //Step 2: register build=in cells and cards
        builder = TangramBuilder.newInnerBuilder(this);

        //Step 3: register business cells and cards
        // recommend to use string type to register component
        builder.registerCell("testView", TestBlockView.class);
        builder.registerCell("Debug", TestBlockView.class);
        builder.registerCell("singleImgView", TestImageView.class);

        builder.registerRenderService(new VirtualViewRenderService3());
        //Step 4: new engine
        engine = builder.build();

        Utils.setUedScreenWidth(720);

//        engine.addSimpleClickSupport(new SampleClickSupport());

        //Step 6: enable auto load more if your page's data is lazy loaded
        engine.enableAutoLoadMore(true);
//        engine.register(InternalErrorSupport.class, new SampleErrorSupport());

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
        String json = new String(getAssertsFile(this, "data3.0.json"));
        JSONArray data = null;
        try {
            data = new JSONArray(json);
            engine.setData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Demo for component to listen container's event
        engine.register(SampleScrollSupport.class, new SampleScrollSupport(recyclerView));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (engine != null) {
            engine.destroy();
        }
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
