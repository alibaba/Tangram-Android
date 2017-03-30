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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.android.vlayout.LayoutViewFactory;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.extend.InnerRecycledViewPool;
import com.tmall.wireless.tangram.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.dataparser.DataParser;
import com.tmall.wireless.tangram.dataparser.IAdapterBuilder;
import com.tmall.wireless.tangram.eventbus.BusSupport;
import com.tmall.wireless.tangram.support.TimerSupport;
import com.tmall.wireless.tangram.util.ImageUtils;
import com.tmall.wireless.tangram.util.Preconditions;
import com.tmall.wireless.tangram.util.Predicate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link T} is the type of data, {@link C} is the class of Group, {@link L} is the class of Component
 *
 * @author kellen
 * @author villadora
 * @since 1.0.0
 */
public class BaseTangramEngine<T, C, L> implements ServiceManager {


    private Map<Class<?>, Object> mServices = new ArrayMap<>();

    @NonNull
    private final Context mContext;

    private RecyclerView mContentView;

    private final VirtualLayoutManager mLayoutManager;

    protected GroupBasicAdapter<C, ?> mGroupBasicAdapter;

    private final DataParser<T, C, L> mDataParser;

    private final IAdapterBuilder<C, ?> mAdapterBuilder;

    public BaseTangramEngine(@NonNull final Context context,
            @NonNull final DataParser<T, C, L> dataParser,
            @NonNull final IAdapterBuilder<C, ?> adapterBuilder) {
        //noinspection ConstantConditions
        Preconditions.checkArgument(context != null, "context is null");
        this.mContext = context;
        this.mLayoutManager = new VirtualLayoutManager(mContext);

        this.mLayoutManager.setLayoutViewFactory(new LayoutViewFactory() {
            @Override
            public View generateLayoutView(@NonNull Context context) {
                ImageView imageView = ImageUtils.createImageInstance(context);
                return imageView != null ? imageView : new View(context);
            }
        });

        this.mDataParser = Preconditions.checkNotNull(dataParser, "dataParser in constructor should not be null");
        this.mAdapterBuilder = Preconditions.checkNotNull(adapterBuilder, "adapterBuilder in constructor should not be null");
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    /**
     * @return Return the recyclerView binded to Tangram, do not call this method after {@link #destroy()}, since it
     * will recreate a recyclerView instance. Also it is suggested to call {@link #bindView(RecyclerView)} first
     * before call this method, since the recyclerView created in this method is a default one and may not meet up to
     * your case.
     */
    public RecyclerView getContentView() {
        if (mContentView == null) {
            RecyclerView recyclerView = new RecyclerView(mContext);
            bindView(recyclerView);
            Preconditions.checkState(mContentView != null, "mContentView is still null after call bindView()");
        }

        return mContentView;
    }

    /**
     * @return Adatepr binded to recyclerView.
     */
    public GroupBasicAdapter<C, ?> getGroupBasicAdapter() {
        return mGroupBasicAdapter;
    }

    /**
     * Be careful about doing operations in LayoutManger, it may break existing contract
     *
     * @return internal LayoutManager
     */
    public VirtualLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    /**
     * Bind a recyclerView to Tangram. After calling this, {@link GroupBasicAdapter}, {@link VirtualLayoutManager} are auto binded.
     * @param view A plain recyclerView with no adapter or layoutManager binded.
     */
    public void bindView(@NonNull final RecyclerView view) {
        //noinspection ConstantConditions
        Preconditions.checkArgument(view != null, "view must not be null");
        if (mContentView != null) {
            mContentView.setAdapter(null);
            mContentView.setLayoutManager(null);
        }

        this.mContentView = view;
        this.mContentView.setLayoutManager(mLayoutManager);
        if (mGroupBasicAdapter == null) {
            this.mGroupBasicAdapter = mAdapterBuilder.newAdapter(mContext, mLayoutManager, this);
        }

        if (mContentView.getRecycledViewPool() != null) {
            mContentView.setRecycledViewPool(new InnerRecycledViewPool(mContentView.getRecycledViewPool()));
        }

        register(GroupBasicAdapter.class, mGroupBasicAdapter);
        register(RecyclerView.RecycledViewPool.class, mContentView.getRecycledViewPool());

        this.mContentView.setAdapter(mGroupBasicAdapter);
    }

    /**
     * Unbind the adapter and layoutManger to recyclerView. And also set null to them.
     */
    public void unbindView() {
        if (mContentView != null) {
            this.mContentView.setAdapter(null);
            this.mContentView.setLayoutManager(null);
            this.mContentView = null;
        }
    }

    /**
     * Call this when your activity is ready to destory to clear inner resource.
     */
    public void destroy() {
        if (mContentView != null) {
            if (mGroupBasicAdapter != null) {
                mGroupBasicAdapter.destroy();
            }
            mContentView.setAdapter(null);
            mContentView = null;
        }
        TimerSupport timerSupport = getService(TimerSupport.class);
        if (timerSupport != null) {
            timerSupport.clear();
        }
        BusSupport busSupport = getService(BusSupport.class);
        if (busSupport != null) {
            busSupport.shutdown();
        }
    }

    /**
     * Set original data list with type {@link T} in Tangram.
     * @param data Original data with type {@link T}.
     */
    public void setData(@Nullable T data) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");

        List<C> cards = mDataParser.parseGroup(data, this);
        this.setData(cards);
    }

    public boolean isFullScreen() {
        mLayoutManager.findLastVisibleItemPosition();
        return false;
    }

    /**
     * Append original data with type {@link T} to Tangram.
     * @param data Original data with type {@link T}.
     */
    public void appendData(@Nullable T data) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");

        appendData(mDataParser.parseGroup(data, this));
    }

    /**
     * Insert original data to Tangram at target position.
     * @param position Target insert position.
     * @param data Original data with type {@link T}.
     */
    public void insertData(int position, @Nullable T data) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");

        insertData(position, mDataParser.parseGroup(data, this));
    }

    /**
     * Replace original data to Tangram at target position
     * @param position Target insert position.
     * @param data Original data with type {@link T}.
     */
    public void replaceData(int position, @Nullable T data) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");

        replaceData(position, mDataParser.parseGroup(data, this));
    }

    /**
     * Set parsed data list with type {@link C} in Tangram
     * @param data Parsed data list.
     */
    public void setData(@Nullable List<C> data) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");
        MVHelper MVHelper = (MVHelper) mServices.get(MVHelper.class);
        if (MVHelper != null)
            MVHelper.reset();
        this.mGroupBasicAdapter.setData(data);
    }

    /**
     * Append parsed data list with type {@link C} in Tangram
     * @param data Parsed data list.
     */
    public void appendData(@Nullable List<C> data) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");
        this.mGroupBasicAdapter.appendGroup(data);
    }

    /**
     * Insert parsed data to Tangram at target position
     * @param position Target insert position.
     * @param data Parsed data list.
     */
    public void insertData(int position, @Nullable List<C> data) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");
        this.mGroupBasicAdapter.insertGroup(position, data);
    }

    /**
     * Replace original data to Tangram at target position
     * @param position Target replace position.
     * @param data Parsed data list.
     */
    public void replaceData(int position, @Nullable List<C> data) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");
        this.mGroupBasicAdapter.replaceGroup(position, data);
    }

    /**
     * Parse original data with type {@link T} into model data list with type {@link C}
     * @param data Original data.
     * @return Parsed data list.
     */
    public List<C> parseData(@Nullable T data) {
        return mDataParser.parseGroup(data, this);
    }

    /**
     * Parse original data with type {@link T} into model data list with type {@link L}
     * @param data Original data.
     * @return Parsed data list.
     */
    public List<L> parseComponent(@Nullable T data) {
        return mDataParser.parseComponent(data, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> void register(@NonNull Class<S> type, @NonNull S service) {
        Preconditions.checkArgument(type != null, "type is null");

        mServices.put(type, type.cast(service));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> S getService(@NonNull Class<S> type) {
        Object service = mServices.get(type);
        if (service == null) {
            return null;
        }
        return type.cast(service);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public <C> List<C> findGroups(final Predicate<C> predicate) {
        Preconditions.checkState(mGroupBasicAdapter != null, "Must call bindView() first");

        List<C> groups = (List<C>) mGroupBasicAdapter.getGroups();
        if (predicate == null) {
            return groups;
        }

        List<C> rs = new LinkedList<C>();

        for (C g : groups) {
            if (predicate.isMatch(g)) {
                rs.add(g);
            }
        }

        return rs;
    }

}
