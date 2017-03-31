/*
 * MIT License
 *
 * Copyright (c) 2017 Alibaba Group
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

package com.tmall.wireless.tangram;

import com.tmall.wireless.tangram.dataparser.IAdapterBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.BaseCardBinderResolver;
import com.tmall.wireless.tangram.dataparser.concrete.BaseCellBinderResolver;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.CardResolver;
import com.tmall.wireless.tangram.dataparser.concrete.PojoAdapterBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.PojoDataParser;
import com.tmall.wireless.tangram.eventbus.BusSupport;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.BannerCard;
import com.tmall.wireless.tangram.structure.card.ColumnCard;
import com.tmall.wireless.tangram.structure.card.DoubleColumnCard;
import com.tmall.wireless.tangram.structure.card.FiveColumnCard;
import com.tmall.wireless.tangram.structure.card.FixCard;
import com.tmall.wireless.tangram.structure.card.FixLinearScrollCard;
import com.tmall.wireless.tangram.structure.card.FloatCard;
import com.tmall.wireless.tangram.structure.card.FlowCard;
import com.tmall.wireless.tangram.structure.card.FourColumnCard;
import com.tmall.wireless.tangram.structure.card.FusionCard;
import com.tmall.wireless.tangram.structure.card.GridCard;
import com.tmall.wireless.tangram.structure.card.LinearCard;
import com.tmall.wireless.tangram.structure.card.LinearScrollCard;
import com.tmall.wireless.tangram.structure.card.OnePlusNCard;
import com.tmall.wireless.tangram.structure.card.PinBottomCard;
import com.tmall.wireless.tangram.structure.card.PinTopCard;
import com.tmall.wireless.tangram.structure.card.ScrollFixCard;
import com.tmall.wireless.tangram.structure.card.SingleColumnCard;
import com.tmall.wireless.tangram.structure.card.StaggeredCard;
import com.tmall.wireless.tangram.structure.card.StickyCard;
import com.tmall.wireless.tangram.structure.card.StickyEndCard;
import com.tmall.wireless.tangram.structure.card.TripleColumnCard;
import com.tmall.wireless.tangram.structure.view.GridEntityCardView;
import com.tmall.wireless.tangram.structure.view.SimpleEmptyView;
import com.tmall.wireless.tangram.structure.viewcreator.ViewHolderCreator;
import com.tmall.wireless.tangram.support.TimerSupport;
import com.tmall.wireless.tangram.util.IInnerImageSetter;
import com.tmall.wireless.tangram.util.ImageUtils;
import com.tmall.wireless.tangram.util.Preconditions;
import com.tmall.wireless.tangram.util.TangramViewMetrics;
import com.tmall.wireless.tangram.util.Utils;
import com.tmall.wireless.tangram.view.BannerView;
import com.tmall.wireless.tangram.view.LinearScrollView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

/**
 *
 *
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

    // type start point for extended build-in cellInited
    public static final int START_EXTENDED_CELL_TYPE = 1000;

    public static final int TYPE_EXTENDED_VIEW = -1;

    public static final int TYPE_EMPTY_VIEW = 0;

    public static final int TYPE_SIMPLE_IMAGE = 1;

    public static final int TYPE_CAROUSEL_CELL = -2;

    public static final int TYPE_LINEAR_SCROLL_CELL = -3;

    // cards already in home page
    public static final int TYPE_SINGLE_COLUMN = 1;

    public static final int TYPE_DOUBLE_COLUMN = 2;

    public static final int TYPE_TRIPLE_COLUMN = 3;

    public static final int TYPE_FOUR_COLUMN = 4;

    public static final int TYPE_ONE_PLUS_N = 5;

    public static final int TYPE_FLOAT = 7;

    public static final int TYPE_PIN_BOTTOM = 8;

    public static final int TYPE_FIVE_COLUMN = 9;

    public static final int TYPE_CAROUSEL = 10;

    public static final int TYPE_STICKY = 20;

    public static final int TYPE_STICKY_START = 21;

    public static final int TYPE_STICKY_END = 22;

    public static final int TYPE_PIN_TOP = 23;

    public static final int TYPE_FUSION_TABS = 24;

    public static final int TYPE_STAGGER = 25;

    public static final int TYPE_FLOW = 27;

    public static final int TYPE_SCROLL_FIX = 28;

    public static final int TYPE_LINEAR_SCROLL = 29;

    public static final int TYPE_SCROLL_FIX_BANNER = 30;

    // type start point for extended built-in card
    public static final int START_EXTENDED_CARD_TYPE = 1024;

    public static final int TYPE_FIX = START_EXTENDED_CARD_TYPE + 1;

    public static final int TYPE_GRID = START_EXTENDED_CARD_TYPE + 2;

    public static final int TYPE_LINEAR = START_EXTENDED_CARD_TYPE + 3;

    public static final int TYPE_X_COLUMN = START_EXTENDED_CARD_TYPE + 9;

    // cellize card type
    public static final int TYPE_SINGLE_COLUMN_ENTITY = Utils.cellizeCard(TYPE_SINGLE_COLUMN);

    public static final int TYPE_DOUBLE_COLUMN_ENTITY = Utils.cellizeCard(TYPE_DOUBLE_COLUMN);

    public static final int TYPE_TRIPLE_COLUMN_ENTITY = Utils.cellizeCard(TYPE_TRIPLE_COLUMN);

    public static final int TYPE_FOUR_COLUMN_ENTITY = Utils.cellizeCard(TYPE_FOUR_COLUMN);

    public static final int TYPE_FIVE_COLUMN_ENTITY = Utils.cellizeCard(TYPE_FIVE_COLUMN);

    public static final int TYPE_CAROUSEL_ENTITY = Utils.cellizeCard(TYPE_CAROUSEL);

    /**
     * init global Tangram environment.
     * @param context the app context
     * @param innerImageSetter an ImagerSetter to load image, see {@link ImageUtils}
     * @param imageClazz a custom ImageView class, used to construct an imageView instance.
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
     * @param registry
     */
    public static void installDefaultRegistry(@NonNull final DefaultResolverRegistry registry) {
        /*
         * register built-in cards & mCells
         */
        MVHelper MVHelper = new MVHelper(new MVResolver());
        registry.setMVHelper(MVHelper);

        // built-in mCells
        registry.registerCell(TYPE_EXTENDED_VIEW, Card.PlaceholderCell.class,
                SimpleEmptyView.class);
        registry.registerCell(TYPE_EMPTY_VIEW, BaseCell.class, SimpleEmptyView.class);
        registry.registerCell(TYPE_CAROUSEL_CELL, BannerView.class);
        registry.registerCell(TYPE_SINGLE_COLUMN_ENTITY, GridEntityCardView.class);
        registry.registerCell(TYPE_DOUBLE_COLUMN_ENTITY, GridEntityCardView.class);
        registry.registerCell(TYPE_TRIPLE_COLUMN_ENTITY, GridEntityCardView.class);
        registry.registerCell(TYPE_FOUR_COLUMN_ENTITY, GridEntityCardView.class);
        registry.registerCell(TYPE_FIVE_COLUMN_ENTITY, GridEntityCardView.class);
        registry.registerCell(TYPE_CAROUSEL_ENTITY, BannerView.class);

        registry.registerCell(TYPE_LINEAR_SCROLL_CELL, LinearScrollView.class);

        // built-in cards
        registry.registerCard(TYPE_CAROUSEL, BannerCard.class);
        registry.registerCard(TYPE_SINGLE_COLUMN, SingleColumnCard.class);
        registry.registerCard(TYPE_DOUBLE_COLUMN, DoubleColumnCard.class);
        registry.registerCard(TYPE_TRIPLE_COLUMN, TripleColumnCard.class);
        registry.registerCard(TYPE_FOUR_COLUMN, FourColumnCard.class);
        registry.registerCard(TYPE_ONE_PLUS_N, OnePlusNCard.class);
        registry.registerCard(TYPE_FLOAT, FloatCard.class);
        registry.registerCard(TYPE_PIN_BOTTOM, PinBottomCard.class);
        registry.registerCard(TYPE_FIVE_COLUMN, FiveColumnCard.class);
        registry.registerCard(TYPE_STICKY, StickyCard.class);
        registry.registerCard(TYPE_STICKY_START, StickyCard.class);
        registry.registerCard(TYPE_STICKY_END, StickyEndCard.class);
        registry.registerCard(TYPE_PIN_TOP, PinTopCard.class);
        registry.registerCard(TYPE_STAGGER, StaggeredCard.class);

        registry.registerCard(TYPE_FUSION_TABS, FusionCard.class);
        registry.registerCard(TYPE_FLOW, FlowCard.class);
        registry.registerCard(TYPE_SCROLL_FIX, ScrollFixCard.class);

        registry.registerCard(TYPE_LINEAR_SCROLL, LinearScrollCard.class);
        registry.registerCard(TYPE_SCROLL_FIX_BANNER, FixLinearScrollCard.class);

        // extend cards
        registry.registerCard(TYPE_FIX, FixCard.class);
        registry.registerCard(TYPE_GRID, GridCard.class);
        registry.registerCard(TYPE_LINEAR, LinearCard.class);
        registry.registerCard(TYPE_X_COLUMN, ColumnCard.class);
    }

    /**
     * init a {@link TangramEngine} builder with build-in resource inited, such as registering build-in card and cell. Users use this builder to regiser custom card and cell, then call {@link InnerBuilder#build()} to create a {@link TangramEngine} instance.
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

        protected InnerBuilder(@NonNull final Context context, DefaultResolverRegistry registry) {
            this.mContext = context;
            this.mDefaultResolverRegistry = registry;
            mMVHelper = registry.getMVHelper();
            mPojoAdapterBuilder = new PojoAdapterBuilder();
        }

        /**
         * register cell with custom model class and view class
         * @param type
         * @param cellClz
         * @param viewClz
         * @param <V>
         */
        public <V extends View> void registerCell(int type,
                @NonNull Class<? extends BaseCell> cellClz, @NonNull Class<V> viewClz) {
            mDefaultResolverRegistry.registerCell(type, cellClz, viewClz);
        }

        /**
         * register cell with custom model class and view creator
         * @param type
         * @param cellClz
         * @param viewHolderCreator
         * @param <V>
         */
        public <V extends View> void registerCell(int type,
                @NonNull Class<? extends BaseCell> cellClz,
                @NonNull ViewHolderCreator viewHolderCreator) {
            mDefaultResolverRegistry.registerCell(type, cellClz, viewHolderCreator);
        }

        /**
         * register cell with custom view class, the model of cell is provided with default type
         * @param type
         * @param viewClz
         * @param <V>
         */
        public <V extends View> void registerCell(int type, @NonNull Class<V> viewClz) {
            mDefaultResolverRegistry.registerCell(type, viewClz);
        }

        /**
         * register card with type and card class
         * @param type
         * @param cardClz
         */
        public void registerCard(int type, Class<? extends Card> cardClz) {
            mDefaultResolverRegistry.registerCard(type, cardClz);
        }

        /**
         * set an custom {@link IAdapterBuilder} to build adapter, the default is {@link PojoAdapterBuilder} which would create a {@link com.tmall.wireless.tangram.dataparser.concrete.PojoGroupBasicAdapter}
         * @param builder custom IadapterBuilder
         */
        public void setAdapterBuilder(@NonNull IAdapterBuilder builder) {
            Preconditions.checkNotNull(builder, "newInnerBuilder should not be null");
            this.mPojoAdapterBuilder = builder;
        }

        public int getCellTypeCount() {
            if (mDefaultResolverRegistry != null) {
                return mDefaultResolverRegistry.mDefaultCellBinderResolver.size();
            } else {
                return 0;
            }
        }

        /**
         * @return a {@link TangramBuilder} instance to bind {@link android.support.v7.widget.RecyclerView}, data.
         */
        public TangramEngine build() {

            TangramEngine tangramEngine =
                    new TangramEngine(mContext, new PojoDataParser(), mPojoAdapterBuilder);

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

            return tangramEngine;
        }

    }

}
