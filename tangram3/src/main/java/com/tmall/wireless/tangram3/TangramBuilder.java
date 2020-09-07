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

package com.tmall.wireless.tangram3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.android.vlayout.extend.PerformanceMonitor;
import com.tmall.wireless.tangram3.core.protocol.ElementRenderService;
import com.tmall.wireless.tangram3.dataparser.DataParser;
import com.tmall.wireless.tangram3.dataparser.IAdapterBuilder;
import com.tmall.wireless.tangram3.dataparser.concrete.BaseCardBinderResolver;
import com.tmall.wireless.tangram3.dataparser.concrete.BaseCellBinderResolver;
import com.tmall.wireless.tangram3.dataparser.concrete.Card;
import com.tmall.wireless.tangram3.dataparser.concrete.CardResolver;
import com.tmall.wireless.tangram3.dataparser.concrete.PojoAdapterBuilder;
import com.tmall.wireless.tangram3.dataparser.concrete.PojoDataParser;
import com.tmall.wireless.tangram3.dataparser.concrete.PojoGroupBasicAdapter;
import com.tmall.wireless.tangram3.eventbus.BusSupport;
import com.tmall.wireless.tangram3.structure.BaseCell;
import com.tmall.wireless.tangram3.structure.card.BannerCard;
import com.tmall.wireless.tangram3.structure.card.ColumnCard;
import com.tmall.wireless.tangram3.structure.card.DoubleColumnCard;
import com.tmall.wireless.tangram3.structure.card.FiveColumnCard;
import com.tmall.wireless.tangram3.structure.card.FixCard;
import com.tmall.wireless.tangram3.structure.card.FixLinearScrollCard;
import com.tmall.wireless.tangram3.structure.card.FloatCard;
import com.tmall.wireless.tangram3.structure.card.FlowCard;
import com.tmall.wireless.tangram3.structure.card.FourColumnCard;
import com.tmall.wireless.tangram3.structure.card.GridCard;
import com.tmall.wireless.tangram3.structure.card.LinearCard;
import com.tmall.wireless.tangram3.structure.card.LinearScrollCard;
import com.tmall.wireless.tangram3.structure.card.OnePlusNCard;
import com.tmall.wireless.tangram3.structure.card.PinBottomCard;
import com.tmall.wireless.tangram3.structure.card.PinTopCard;
import com.tmall.wireless.tangram3.structure.card.ScrollFixCard;
import com.tmall.wireless.tangram3.structure.card.SingleColumnCard;
import com.tmall.wireless.tangram3.structure.card.StaggeredCard;
import com.tmall.wireless.tangram3.structure.card.StickyCard;
import com.tmall.wireless.tangram3.structure.card.StickyEndCard;
import com.tmall.wireless.tangram3.structure.card.TripleColumnCard;
import com.tmall.wireless.tangram3.structure.view.SimpleEmptyView;
import com.tmall.wireless.tangram3.structure.viewcreator.ViewHolderCreator;
import com.tmall.wireless.tangram3.support.TimerSupport;
import com.tmall.wireless.tangram3.util.IInnerImageSetter;
import com.tmall.wireless.tangram3.util.ImageUtils;
import com.tmall.wireless.tangram3.util.Preconditions;
import com.tmall.wireless.tangram3.util.TangramViewMetrics;
import com.tmall.wireless.tangram3.view.BannerView;
import com.tmall.wireless.tangram3.view.LinearScrollView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by longerian on 17/1/16.
 */

public class TangramBuilder {

    private static boolean printLog = false;

    private static boolean sInitialized = false;

    /**
     * @param printLog true, turn on logging; false turn off logging.
     */
    public static void switchLog(boolean printLog) {
        TangramBuilder.printLog = printLog;
    }

    public static boolean isPrintLog() {
        return printLog;
    }

    public static boolean isInitialized() {
        return sInitialized;
    }


    public static final int TYPE_EXTENDED_VIEW = -1;

    public static final String TYPE_EXTENDED_VIEW_COMPACT = "-1";

    public static final int TYPE_EMPTY_VIEW = 0;

    public static final String TYPE_EMPTY_VIEW_COMPACT = "0";

    public static final int TYPE_SIMPLE_IMAGE = 1;

    protected static final String TYPE_SIMPLE_IMAGE_COMPACT = "1";

    public static final int TYPE_CAROUSEL_CELL = -2;

    public static final String TYPE_CAROUSEL_CELL_COMPACT = "-2";

    public static final int TYPE_LINEAR_SCROLL_CELL = -3;

    public static final String TYPE_LINEAR_SCROLL_CELL_COMPACT = "-3";

    public static final int TYPE_SINGLE_COLUMN = 1;

    public static final String TYPE_SINGLE_COLUMN_COMPACT = "1";

    public static final int TYPE_DOUBLE_COLUMN = 2;

    protected static final String TYPE_DOUBLE_COLUMN_COMPACT = "2";

    public static final int TYPE_TRIPLE_COLUMN = 3;

    protected static final String TYPE_TRIPLE_COLUMN_COMPACT = "3";

    public static final int TYPE_FOUR_COLUMN = 4;

    protected static final String TYPE_FOUR_COLUMN_COMPACT = "4";

    public static final int TYPE_ONE_PLUS_N = 5;

    protected static final String TYPE_ONE_PLUS_N_COMPACT = "5";

    public static final int TYPE_FLOAT = 7;

    protected static final String TYPE_FLOAT_COMPACT = "7";

    public static final int TYPE_PIN_BOTTOM = 8;

    protected static final String TYPE_PIN_BOTTOM_COMPACT = "8";

    public static final int TYPE_FIVE_COLUMN = 9;

    protected static final String TYPE_FIVE_COLUMN_COMPACT = "9";

    public static final int TYPE_CAROUSEL = 10;

    protected static final String TYPE_CAROUSEL_COMPACT = "10";

    public static final int TYPE_MIX = 11;

    protected static final String TYPE_MIX_COMPACT = "11";

    public static final int TYPE_STICKY = 20;

    protected static final String TYPE_STICKY_COMPACT = "20";

    public static final int TYPE_STICKY_START = 21;

    protected static final String TYPE_STICKY_START_COMPACT = "21";

    public static final int TYPE_STICKY_END = 22;

    protected static final String TYPE_STICKY_END_COMPACT = "22";

    public static final int TYPE_PIN_TOP = 23;

    protected static final String TYPE_PIN_TOP_COMPACT = "23";

    public static final int TYPE_FUSION_TABS = 24;

    protected static final String TYPE_FUSION_TABS_COMPACT = "24";

    public static final int TYPE_STAGGER = 25;

    protected static final String TYPE_STAGGER_COMPACT = "25";

    public static final int TYPE_FLOW = 27;

    protected static final String TYPE_FLOW_COMPACT = "27";

    public static final int TYPE_SCROLL_FIX = 28;

    protected static final String TYPE_SCROLL_FIX_COMPACT = "28";

    public static final int TYPE_LINEAR_SCROLL = 29;

    protected static final String TYPE_LINEAR_SCROLL_COMPACT = "29";

    public static final int TYPE_SCROLL_FIX_BANNER = 30;

    protected static final String TYPE_SCROLL_FIX_BANNER_COMPACT = "30";

    // type start point for extended built-in card
    public static final int START_EXTENDED_CARD_TYPE = 1024;

    public static final int TYPE_FIX = START_EXTENDED_CARD_TYPE + 1;

    protected static final String TYPE_FIX_COMPACT = "1025";

    public static final int TYPE_GRID = START_EXTENDED_CARD_TYPE + 2;

    protected static final String TYPE_GRID_COMPACT = "1026";

    public static final int TYPE_LINEAR = START_EXTENDED_CARD_TYPE + 3;

    protected static final String TYPE_LINEAR_COMPACT = "1027";

    public static final int TYPE_X_COLUMN = START_EXTENDED_CARD_TYPE + 9;

    protected static final String TYPE_X_COLUMN_COMPACT = "1033";

    public static final String TYPE_CONTAINER_FLOW = "container-flow";

    public static final String TYPE_CONTAINER_1C_FLOW = "container-oneColumn";

    public static final String TYPE_CONTAINER_2C_FLOW = "container-twoColumn";

    public static final String TYPE_CONTAINER_3C_FLOW = "container-threeColumn";

    public static final String TYPE_CONTAINER_4C_FLOW = "container-fourColumn";

    public static final String TYPE_CONTAINER_5C_FLOW = "container-fiveColumn";

    public static final String TYPE_CONTAINER_ON_PLUSN = "container-onePlusN";

    public static final String TYPE_CONTAINER_FLOAT = "container-float";

    public static final String TYPE_CONTAINER_BANNER = "container-banner";

    public static final String TYPE_CONTAINER_SCROLL = "container-scroll";

    public static final String TYPE_CONTAINER_STICKY = "container-sticky";

    public static final String TYPE_CONTAINER_WATERFALL = "container-waterfall";

    public static final String TYPE_CONTAINER_FIX = "container-fix";

    public static final String TYPE_CONTAINER_SCROLL_FIX = "container-scrollFix";

    public static final String TYPE_CONTAINER_SCROLL_FIX_BANNER = "container-scrollFixBanner";


    /**
     * init global Tangram environment.
     *
     * @param context          the app context
     * @param innerImageSetter an ImagerSetter to load image, see {@link ImageUtils}
     * @param imageClazz       a custom ImageView class, used to construct an imageView instance.
     */
    public static void init(@NonNull final Context context, IInnerImageSetter innerImageSetter,
                            Class<? extends ImageView> imageClazz) {
        if (sInitialized) {
            return;
        }
        //noinspection ConstantConditions
        Preconditions.checkArgument(context != null, "context should not be null");
        Preconditions
                .checkArgument(innerImageSetter != null, "innerImageSetter should not be null");
        Preconditions.checkArgument(imageClazz != null, "imageClazz should not be null");
        TangramViewMetrics.initWith(context.getApplicationContext());
        ImageUtils.sImageClass = imageClazz;
        ImageUtils.setImageSetter(innerImageSetter);
        sInitialized = true;
    }

    /**
     * regiser the framework default cell and card.
     *
     * @param registry
     */
    public static void installDefaultRegistry(@NonNull final DefaultResolverRegistry registry) {
        /*
         * register built-in cards & mCells
         */
        MVHelper mvHelper = new MVHelper(new MVResolver());
        registry.setMVHelper(mvHelper);

        // built-in mCells
        registry.registerCell(TYPE_EXTENDED_VIEW_COMPACT, SimpleEmptyView.class);
        registry.registerCell(TYPE_EMPTY_VIEW_COMPACT, SimpleEmptyView.class);
        //registry.registerCell(TYPE_SIMPLE_IMAGE_COMPACT, Cell.class, SimpleImgView.class);
        registry.registerCell(TYPE_CAROUSEL_CELL_COMPACT, BannerView.class);
        registry.registerCell(TYPE_CONTAINER_BANNER, BannerView.class);
        registry.registerCell(TYPE_LINEAR_SCROLL_CELL_COMPACT, LinearScrollView.class);
        registry.registerCell(TYPE_CONTAINER_SCROLL, LinearScrollView.class);

        // built-in cards
        registry.registerCard(TYPE_CAROUSEL_COMPACT, BannerCard.class);
        registry.registerCard(TYPE_CONTAINER_BANNER, BannerCard.class);
        registry.registerCard(TYPE_SINGLE_COLUMN_COMPACT, SingleColumnCard.class);
        registry.registerCard(TYPE_CONTAINER_1C_FLOW, SingleColumnCard.class);
        registry.registerCard(TYPE_DOUBLE_COLUMN_COMPACT, DoubleColumnCard.class);
        registry.registerCard(TYPE_CONTAINER_2C_FLOW, DoubleColumnCard.class);
        registry.registerCard(TYPE_TRIPLE_COLUMN_COMPACT, TripleColumnCard.class);
        registry.registerCard(TYPE_CONTAINER_3C_FLOW, TripleColumnCard.class);
        registry.registerCard(TYPE_FOUR_COLUMN_COMPACT, FourColumnCard.class);
        registry.registerCard(TYPE_CONTAINER_4C_FLOW, FourColumnCard.class);
        registry.registerCard(TYPE_ONE_PLUS_N_COMPACT, OnePlusNCard.class);
        registry.registerCard(TYPE_CONTAINER_ON_PLUSN, OnePlusNCard.class);
        registry.registerCard(TYPE_FLOAT_COMPACT, FloatCard.class);
        registry.registerCard(TYPE_CONTAINER_FLOAT, FloatCard.class);
        registry.registerCard(TYPE_PIN_BOTTOM_COMPACT, PinBottomCard.class);
        registry.registerCard(TYPE_FIVE_COLUMN_COMPACT, FiveColumnCard.class);
        registry.registerCard(TYPE_CONTAINER_5C_FLOW, FiveColumnCard.class);
        registry.registerCard(TYPE_STICKY_COMPACT, StickyCard.class);
        registry.registerCard(TYPE_CONTAINER_STICKY, StickyCard.class);
        registry.registerCard(TYPE_STICKY_START_COMPACT, StickyCard.class);
        registry.registerCard(TYPE_STICKY_END_COMPACT, StickyEndCard.class);
        registry.registerCard(TYPE_PIN_TOP_COMPACT, PinTopCard.class);
        registry.registerCard(TYPE_CONTAINER_FIX, FixCard.class);
        registry.registerCard(TYPE_STAGGER_COMPACT, StaggeredCard.class);
        registry.registerCard(TYPE_CONTAINER_WATERFALL, StaggeredCard.class);

        registry.registerCard(TYPE_FLOW_COMPACT, FlowCard.class);
        registry.registerCard(TYPE_CONTAINER_FLOW, FlowCard.class);
        registry.registerCard(TYPE_SCROLL_FIX_COMPACT, ScrollFixCard.class);
        registry.registerCard(TYPE_CONTAINER_SCROLL_FIX, ScrollFixCard.class);

        registry.registerCard(TYPE_LINEAR_SCROLL_COMPACT, LinearScrollCard.class);
        registry.registerCard(TYPE_CONTAINER_SCROLL, LinearScrollCard.class);
        registry.registerCard(TYPE_SCROLL_FIX_BANNER_COMPACT, FixLinearScrollCard.class);
        registry.registerCard(TYPE_CONTAINER_SCROLL_FIX_BANNER, FixLinearScrollCard.class);

        // extend cards
        registry.registerCard(TYPE_FIX_COMPACT, FixCard.class);
        registry.registerCard(TYPE_GRID_COMPACT, GridCard.class);
        registry.registerCard(TYPE_LINEAR_COMPACT, LinearCard.class);
        registry.registerCard(TYPE_X_COLUMN_COMPACT, ColumnCard.class);
    }

    /**
     * init a {@link TangramEngine} builder with build-in resource inited, such as registering build-in card and cell. Users use this builder to regiser custom card and cell, then call {@link InnerBuilder#build()} to create a {@link TangramEngine} instance.
     *
     * @param context activity context
     * @return a {@link TangramEngine} builder
     */
    @NonNull
    public static InnerBuilder newInnerBuilder(@NonNull final Context context) {
        if (!TangramBuilder.isInitialized()) {
            throw new IllegalStateException("Tangram must be init first");
        }

        DefaultResolverRegistry registry = new DefaultResolverRegistry();

        // install default cards & mCells
        installDefaultRegistry(registry);

        return new InnerBuilder(context, registry);
    }

    public static final class InnerBuilder {

        @NonNull
        private Context mContext;

        private DefaultResolverRegistry mDefaultResolverRegistry;

        private MVHelper mMVHelper;

        private MVResolver mMVResolver;

        private IAdapterBuilder mPojoAdapterBuilder;

        private PerformanceMonitor mPerformanceMonitor;

        private DataParser mDataParser;

        private Map<String, ElementRenderService> renderServiceMap;

        protected InnerBuilder(@NonNull final Context context, DefaultResolverRegistry registry) {
            this.mContext = context;
            this.mDefaultResolverRegistry = registry;
            mMVHelper = registry.getMVHelper();
            mMVResolver = mMVHelper.resolver();
            mPojoAdapterBuilder = new PojoAdapterBuilder();
            mDataParser = new PojoDataParser();
        }

        /**
         * register cell with custom view class, the model of cell is provided with default type
         *
         * @param type
         * @param viewClz
         * @param <V>
         */
        @Deprecated
        public <V extends View> void registerCell(int type, @NonNull Class<V> viewClz) {
            mDefaultResolverRegistry.registerCell(String.valueOf(type), viewClz);
        }

        /**
         * register card with type and card class
         *
         * @param type
         * @param cardClz
         */
        @Deprecated
        public void registerCard(int type, Class<? extends Card> cardClz) {
            mDefaultResolverRegistry.registerCard(String.valueOf(type), cardClz);
        }

        public <V extends View> void registerCell(String type, @NonNull Class<V> viewClz) {
            mDefaultResolverRegistry.registerCell(type, viewClz);
        }

        public void registerCard(String type, Class<? extends Card> cardClz) {
            mDefaultResolverRegistry.registerCard(type, cardClz);
        }

        /**
         * set a custom {@link IAdapterBuilder} to build adapter, the default is {@link PojoAdapterBuilder} which
         * would create a {@link PojoGroupBasicAdapter}
         *
         * @param builder custom IadapterBuilder
         */
        public void setAdapterBuilder(@NonNull IAdapterBuilder builder) {
            Preconditions.checkNotNull(builder, "newInnerBuilder should not be null");
            this.mPojoAdapterBuilder = builder;
        }

        /**
         * set a custom {@link PerformanceMonitor} to record performance of critical phase, such as creating view, binding view, unbind view, measuring view, layout view.
         *
         * @param performanceMonitor
         */
        public void setPerformanceMonitor(@Nullable PerformanceMonitor performanceMonitor) {
            this.mPerformanceMonitor = performanceMonitor;
        }

        /**
         * set an custom {@link DataParser}, the default is {@link PojoDataParser}
         *
         * @param dataParser
         */
        public void setDataParser(@NonNull DataParser dataParser) {
            Preconditions.checkNotNull(dataParser, "newDataParser should not be null");
            this.mDataParser = dataParser;
        }

        public int getCellTypeCount() {
            if (mDefaultResolverRegistry != null) {
                return mDefaultResolverRegistry.mDefaultCellBinderResolver.size();
            } else {
                return 0;
            }
        }

        public void registerRenderService(ElementRenderService renderService) {
            if (renderServiceMap == null) {
                renderServiceMap = new HashMap<>(5);
            } else if (renderServiceMap.containsKey(renderService.getSDKBizName())) {
                throw new IllegalArgumentException("Can not register duplicated render service.");
            }
            renderServiceMap.put(renderService.getSDKBizName(), renderService);
        }

        /**
         * @return a {@link TangramBuilder} instance to bind {@link android.support.v7.widget.RecyclerView}, data.
         */
        public TangramEngine build() {

            TangramEngine tangramEngine =
                    new TangramEngine(mContext, mDataParser, mPojoAdapterBuilder);

            tangramEngine.setPerformanceMonitor(mPerformanceMonitor);
            // register service with default services
            tangramEngine.register(MVHelper.class, mMVHelper);
            tangramEngine
                    .register(CardResolver.class, mDefaultResolverRegistry.mDefaultCardResolver);
            tangramEngine.register(BaseCellBinderResolver.class,
                    mDefaultResolverRegistry.mDefaultCellBinderResolver);
            tangramEngine.register(BaseCardBinderResolver.class,
                    mDefaultResolverRegistry.mDefaultCardBinderResolver);

            // add other features service
            tangramEngine.register(TimerSupport.class, new TimerSupport());
            tangramEngine.register(BusSupport.class, new BusSupport());

            // add renderService
            if (renderServiceMap != null) {
                for (ElementRenderService renderService : renderServiceMap.values()) {
                    mMVHelper.renderManager().addRenderService(renderService);
                    renderService.init(tangramEngine);
                }
            }

            if (callback != null) {
                callback.onBuild(tangramEngine);
            }
            return tangramEngine;
        }

        BuildCallback callback = null;

        public void setBuildCallback(BuildCallback callback) {
            this.callback = callback;
        }

    }

    public interface BuildCallback {
        void onBuild(TangramEngine tangramEngine);
    }

}
