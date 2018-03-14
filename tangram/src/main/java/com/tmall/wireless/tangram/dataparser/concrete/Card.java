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

package com.tmall.wireless.tangram.dataparser.concrete;

import com.alibaba.android.vlayout.LayoutHelper;
import com.alibaba.android.vlayout.Range;
import com.alibaba.android.vlayout.layout.BaseLayoutHelper;
import com.alibaba.android.vlayout.layout.FixAreaLayoutHelper;
import com.alibaba.android.vlayout.layout.MarginLayoutHelper;

import android.support.v4.util.ArrayMap;
import com.tmall.wireless.tangram.Engine;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.entitycard.BannerEntityCard;
import com.tmall.wireless.tangram.structure.entitycard.LinearScrollEntityCard;
import com.tmall.wireless.tangram.support.CardSupport;
import com.tmall.wireless.tangram.support.ExposureSupport;
import com.tmall.wireless.tangram.util.ImageUtils;
import com.tmall.wireless.tangram.util.LogUtils;
import com.tmall.wireless.tangram.util.Preconditions;
import com.tmall.wireless.tangram.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Basic Card, which will represent LayoutHelpers
 * <p/>
 */
public abstract class Card extends ComponentLifecycle {

    private static final String TAG = "Card";

    public static final String KEY_TYPE = "type";

    public static final String KEY_STYLE = "style";

    public static final String KEY_ID = "id";

    public static final String KEY_ITEMS = "items";

    public static final String KEY_HEADER = "header";

    public static final String KEY_FOOTER = "footer";

    public static final String KEY_LOAD_TYPE = "loadType";

    public static final String KEY_LOADED = "loaded";

    public static final String KEY_API_LOAD = "load";

    public static final String KEY_HAS_MORE = "hasMore";

    public static final String KEY_API_LOAD_PARAMS = "loadParams";

    public static final String KEY_MAX_CHILDREN = "maxChildren";

    public static final int LOAD_TYPE_LOADMORE = 1;

    /**
     * card type, use {@link #stringType} instead
     */
    @Deprecated
    public int type;

    public String stringType;

    @Nullable
    public String id;

    protected
    @Nullable
    BaseCell mHeader;

    protected
    @Nullable
    BaseCell mFooter;

    @NonNull
    protected ArrayMap<Range<Integer>, Card> mChildren = new ArrayMap<>();

    @NonNull
    protected List<BaseCell> mCells = new ArrayList<>();

    @NonNull
    protected final List<BaseCell> mPendingCells = new ArrayList<>();

    @NonNull
    protected final List<BaseCell> mInQueueCells = new ArrayList<>();

    @Nullable
    public Style style;

    /**
     * if need load more
     */
    public boolean loadMore = false;

    /**
     * indicate if loading data now
     */
    public boolean loading = false;

    /**
     * page number used at loading more data
     */
    public int page;

    /**
     * api name of loading more data
     */
    public String load;

    /**
     * api params of loading more data
     */
    public JSONObject loadParams;

    /**
     * if data has been loaded
     */
    public boolean loaded = false;

    /**
     * if this card has more data
     */
    public boolean hasMore = false;

    public int rowId;

    /**
     * the max child count
     */
    protected int maxChildren = Integer.MAX_VALUE;

    /**
     * serviceManager
     */
    @Nullable
    public ServiceManager serviceManager;


    // TODO: back store refactor
    @Nullable
    private Map<String, Object> mParams;

    public JSONObject extras = new JSONObject();

    public void setParams(@Nullable Map<String, Object> params) {
        mParams = params;
    }

    @Nullable
    public Map<String, Object> getParams() {
        return mParams == null ? Collections.<String, Object>emptyMap() : mParams;
    }

    public Card() {

    }

    public void setStringType(String type) {
        this.stringType = type;
        try {
            this.type = Integer.parseInt(type);
        } catch (NumberFormatException e) {
        }
    }

    public void parseWith(@NonNull JSONObject data, @NonNull final MVHelper resolver) {
        parseWith(data, resolver, true);
    }

    public void parseWith(@NonNull JSONObject data, @NonNull final MVHelper resolver, boolean isParseCell) {
        if (TangramBuilder.isPrintLog()) {
            if (serviceManager == null) {
                throw new RuntimeException("serviceManager is null when parsing card");
            }
        }

        this.extras = data;
        this.type = data.optInt(KEY_TYPE, type);
        this.stringType = data.optString(KEY_TYPE);
        id = data.optString(KEY_ID, id == null ? "" : id);

        loadMore = data.optInt(KEY_LOAD_TYPE, 0) == LOAD_TYPE_LOADMORE;
        //you should alway assign hasMore to indicate there's more data explicitly
        if (data.has(KEY_HAS_MORE)) {
            hasMore = data.optBoolean(KEY_HAS_MORE);
        } else {
            if (data.has(KEY_LOAD_TYPE)) {
                hasMore = data.optInt(KEY_LOAD_TYPE) == LOAD_TYPE_LOADMORE;
            }
        }
        load = data.optString(KEY_API_LOAD, null);
        loadParams = data.optJSONObject(KEY_API_LOAD_PARAMS);
        loaded = data.optBoolean(KEY_LOADED, false);

        maxChildren = data.optInt(KEY_MAX_CHILDREN, maxChildren);
        // parsing header
        if (isParseCell) {
            JSONObject header = data.optJSONObject(KEY_HEADER);
            parseHeaderCell(resolver, header);
        }

        // parsing body
        JSONArray componentArray = data.optJSONArray(KEY_ITEMS);
        if (componentArray != null && isParseCell) {
            final int cellLength = Math.min(componentArray.length(), maxChildren);
            for (int i = 0; i < cellLength; i++) {
                final JSONObject cellData = componentArray.optJSONObject(i);
                createCell(resolver, cellData, true);
            }
        }
        // parsing footer
        if (isParseCell) {
            JSONObject footer = data.optJSONObject(KEY_FOOTER);
            parseFooterCell(resolver, footer);
        }

        JSONObject styleJson = data.optJSONObject(KEY_STYLE);

        parseStyle(styleJson);

    }

    protected BaseCell createCell(@NonNull MVHelper resolver, @Nullable JSONObject cellData, boolean appended) {
        if (cellData != null) {
            BaseCell cell = null;
            String cellType = cellData.optString(Card.KEY_TYPE);
            if ((getMVHelper() != null && getMVHelper().resolver().getViewClass(cellType) != null) || Utils.isCard(cellData)) {
                if (resolver.resolver().isCompatibleType(cellType)) {
                    cell = Utils.newInstance(resolver.resolver().getCellClass(cellType));

                    //do not display when newInstance failed
                    if (cell == null)
                        return null;

                    cell.serviceManager = serviceManager; // ensure service manager
                } else {
                    if (Utils.isCard(cellData)) {
                        switch (cellType) {
                            case TangramBuilder.TYPE_CONTAINER_FLOW:
                            case TangramBuilder.TYPE_CONTAINER_1C_FLOW:
                            case TangramBuilder.TYPE_CONTAINER_2C_FLOW:
                            case TangramBuilder.TYPE_CONTAINER_3C_FLOW:
                            case TangramBuilder.TYPE_CONTAINER_4C_FLOW:
                            case TangramBuilder.TYPE_CONTAINER_5C_FLOW:
                                CardResolver cardResolver = serviceManager.getService(CardResolver.class);
                                Card gridCard = cardResolver.create(cellType);
                                gridCard.serviceManager = serviceManager;
                                gridCard.parseWith(cellData, resolver);
                                addChildCard(gridCard);
                                break;
                            case TangramBuilder.TYPE_CONTAINER_BANNER:
                                cell = new BannerEntityCard();
                                break;
                            case TangramBuilder.TYPE_CONTAINER_SCROLL:
                                cell = new LinearScrollEntityCard();
                                break;
                        }
                        if (cell != null) {
                            cell.serviceManager = serviceManager;
                            cell.parent = this;
                            cell.parentId = id;
                        } else {
                            return null;
                        }
                    } else {
                        cell = new BaseCell(cellType);
                        cell.serviceManager = serviceManager;
                        cell.parent = this;
                        cell.parentId = id;
                    }
                }
                parseCell(resolver, cellData, cell, appended);
                cell.setStringType(cellType);
                return cell;
            } else {
                //support virtual view at layout
                BaseCellBinderResolver componentBinderResolver = serviceManager.getService(BaseCellBinderResolver.class);
                if (componentBinderResolver.has(cellType)) {
                    cell = new BaseCell(cellType);
                    cell.serviceManager = serviceManager;
                    cell.parent = this;
                    cell.parentId = id;
                    parseCell(resolver, cellData, cell, appended);
                    cell.setStringType(cellType);
                    return cell;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    protected void parseCell(@NonNull MVHelper resolver, @NonNull JSONObject data, @NonNull final BaseCell cell, boolean appended) {
        resolver.parseCell(resolver, cell, data);
        //noinspection unchecked
        if (appended && !addCellInternal(cell, false)) {
            if (TangramBuilder.isPrintLog())
                LogUtils.w(TAG, "Parse invalid cell with data: " + data.toString());
        }

    }

    protected void parseStyle(@Nullable JSONObject data) {
        style = new Style();
        style.parseWith(data);
    }


    /**
     * LayoutHelper for this card, it won't get update for now!!!, as Card can be replaced, or data can be changed
     * If you want to reset the LayoutHelper? Think why you want to do it, make the LayoutHelper unmodified is easy
     * Please replace the card instead of LayoutHelper.
     */
    private LayoutHelper mLayoutHelper = null;

    public LayoutHelper getExistLayoutHelper(){
        return mLayoutHelper;
    }

    /**
     * whether retain LayoutHelper once it created,
     */
    protected boolean mRetainLayout = true;

    @Nullable
    public final LayoutHelper getLayoutHelper() {

        LayoutHelper helper = convertLayoutHelper(mLayoutHelper);

        // bind style to helper
        if (style != null && helper != null) {
            helper.setZIndex(style.zIndex);

            if (helper instanceof BaseLayoutHelper) {
                BaseLayoutHelper baseHelper = (BaseLayoutHelper) helper;
                baseHelper.setBgColor(style.bgColor);
                if (!TextUtils.isEmpty(style.bgImgUrl)) {
                    if (serviceManager != null && serviceManager.getService(CardSupport.class) != null) {
                        final CardSupport support = serviceManager.getService(CardSupport.class);
                        baseHelper.setLayoutViewBindListener(new BindListener(style) {
                            @Override
                            public void onBind(View layoutView, BaseLayoutHelper baseLayoutHelper) {
                                support.onBindBackgroundView(layoutView, Card.this);
                            }
                        });
                        baseHelper.setLayoutViewUnBindListener(new UnbindListener(style) {
                            @Override
                            public void onUnbind(View layoutView, BaseLayoutHelper baseLayoutHelper) {
                                support.onUnbindBackgroundView(layoutView, Card.this);
                            }
                        });
                    } else {
                        baseHelper.setLayoutViewBindListener(new BindListener(style));
                        baseHelper.setLayoutViewUnBindListener(new UnbindListener(style));
                    }
                } else {
                    baseHelper.setLayoutViewBindListener(null);
                    baseHelper.setLayoutViewUnBindListener(null);
                }

                if (!Float.isNaN(style.aspectRatio)) {
                    // ((BaseLayoutHelper) helper).setAspectRatio(style.aspectRatio);
                }

            }

            if (helper instanceof FixAreaLayoutHelper) {
                FixAreaLayoutHelper fixHelper = (FixAreaLayoutHelper) helper;
                boolean hasCustomAnimatorHelper = false;
                if (serviceManager != null && serviceManager.getService(CardSupport.class) != null) {
                    CardSupport support = serviceManager.getService(CardSupport.class);
                    FixAreaLayoutHelper.FixViewAnimatorHelper viewAnimatorHelper = support.onGetFixViewAppearAnimator(Card.this);
                    if (viewAnimatorHelper != null) {
                        hasCustomAnimatorHelper = true;
                        fixHelper.setFixViewAnimatorHelper(viewAnimatorHelper);
                    }
                }
                if (!hasCustomAnimatorHelper) {
                    final int duration = style.extras != null ? style.extras.optInt(Style.KEY_ANIMATION_DURATION) : 0;
                    if (duration > 0) {
                        fixHelper.setFixViewAnimatorHelper(new FixAreaLayoutHelper.FixViewAnimatorHelper() {
                            @Override
                            public ViewPropertyAnimator onGetFixViewAppearAnimator(View fixView) {
                                int height = fixView.getMeasuredHeight();
                                fixView.setTranslationY(-height);
                                return fixView.animate().translationYBy(height).setDuration(duration);
                            }

                            @Override
                            public ViewPropertyAnimator onGetFixViewDisappearAnimator(View fixView) {
                                int height = fixView.getMeasuredHeight();
                                return fixView.animate().translationYBy(-height).setDuration(duration);
                            }
                        });
                    }
                }
            }

            if (helper instanceof MarginLayoutHelper) {
                ((MarginLayoutHelper) helper).setMargin(style.margin[Style.MARGIN_LEFT_INDEX], style.margin[Style.MARGIN_TOP_INDEX],
                    style.margin[Style.MARGIN_RIGHT_INDEX], style.margin[Style.MARGIN_BOTTOM_INDEX]);
                ((MarginLayoutHelper) helper).setPadding(style.padding[Style.MARGIN_LEFT_INDEX], style.padding[Style.MARGIN_TOP_INDEX],
                    style.padding[Style.MARGIN_RIGHT_INDEX], style.padding[Style.MARGIN_BOTTOM_INDEX]);
            }
        }

        if (mRetainLayout) {
            mLayoutHelper = helper;
        }

        return helper;
    }

    public static class BindListener implements BaseLayoutHelper.LayoutViewBindListener {
        private Style mStyle;

        public BindListener(Style style) {
            this.mStyle = style;
        }

        @Override
        public void onBind(View layoutView, BaseLayoutHelper baseLayoutHelper) {
            if (mStyle != null && !TextUtils.isEmpty(mStyle.bgImgUrl)) {
                if (layoutView instanceof ImageView) {
                    ImageUtils.doLoadImageUrl((ImageView) layoutView, mStyle.bgImgUrl);
                }
            }
        }
    }

    public static class UnbindListener implements BaseLayoutHelper.LayoutViewUnBindListener {
        private Style mStyle;

        public UnbindListener(Style style) {
            this.mStyle = style;
        }

        @Override
        public void onUnbind(View layoutView, BaseLayoutHelper baseLayoutHelper) {
        }
    }


    @Nullable
    public LayoutHelper convertLayoutHelper(@Nullable LayoutHelper oldHelper) {
        return null;
    }


    private boolean mIsExposed = false;

    /**
     * Don't change card's mCells in binding process!
     * <p/>
     */
    public void onBindCell(int offset, int position, boolean showFromEnd) {
        if (!mIsExposed && serviceManager != null) {
            ExposureSupport exposureSupport = serviceManager.getService(ExposureSupport.class);
            if (exposureSupport != null) {
                mIsExposed = true;
                exposureSupport.onExposure(this, offset, position);
            }
        }
    }


    public List<BaseCell> getCells() {
        return Collections.unmodifiableList(mCells);
    }

    @NonNull
    public ArrayMap<Range<Integer>, Card> getChildren() {
        return mChildren;
    }

    private final SparseBooleanArray pendingDeleteMap = new SparseBooleanArray();
    private final SparseArray<BaseCell> oldMap = new SparseArray<>();
    private final SparseArray<BaseCell> newMap = new SparseArray<>();

    public void setCells(@Nullable List<BaseCell> cells) {
        if (mPlaceholderCell != null)
            this.mCells.remove(mPlaceholderCell);

        oldMap.clear();
        pendingDeleteMap.clear();
        for (BaseCell cell : this.mCells) {
            oldMap.put(System.identityHashCode(cell), cell);
        }

        this.mCells.clear();
        if (cells != null) {
            for (BaseCell c : cells) {
                //noinspection unchecked
                this.addCellInternal(c, true);
            }
        }

        adjustPendingCells(true);

        newMap.clear();

        for (BaseCell cell : this.mCells) {
            newMap.put(System.identityHashCode(cell), cell);
        }

        for (int i = 0, size = oldMap.size(); i < size; i++) {
            int key = oldMap.keyAt(i);
            if (newMap.get(key) != null) {
                newMap.remove(key);
                pendingDeleteMap.put(key, true);
            }
        }

        for (int i = 0, size = pendingDeleteMap.size(); i < size; i++) {
            oldMap.remove(pendingDeleteMap.keyAt(i));
        }

        diffCells(newMap, oldMap);

        newMap.clear();
        oldMap.clear();
        pendingDeleteMap.clear();


        if (requirePlaceholderCell()) {
            this.mCells.add(mPlaceholderCell);
        }
    }


    public void addCell(@Nullable BaseCell cell) {
        addCellInternal(cell, false);

        adjustPendingCells(false);

        if (mPlaceholderCell != null && this.mCells.contains(mPlaceholderCell))
            this.mCells.remove(mPlaceholderCell);

        if (requirePlaceholderCell()) {
            this.mCells.add(mPlaceholderCell);
        }
    }

    public void addCells(@Nullable List<BaseCell> cells) {
        if (cells != null) {
            for (BaseCell cell : cells) {
                addCellInternal(cell, false);
            }
        }


        adjustPendingCells(false);

        if (mPlaceholderCell != null && this.mCells.contains(mPlaceholderCell))
            this.mCells.remove(mPlaceholderCell);

        if (requirePlaceholderCell()) {
            this.mCells.add(mPlaceholderCell);
        }
    }

    public void addCells(Card parent, int index, @Nullable List<BaseCell> cells) {
        if (cells != null) {
            int i = 0;
            for (BaseCell cell : cells) {
                addCellInternal(parent, index + i, cell, false);
                i++;
            }
        }


        adjustPendingCells(false);

        if (mPlaceholderCell != null && this.mCells.contains(mPlaceholderCell))
            this.mCells.remove(mPlaceholderCell);

        if (requirePlaceholderCell()) {
            this.mCells.add(mPlaceholderCell);
        }
    }

    public void removeAllCells() {
        for (int i = 0, size = mCells.size(); i < size; i++) {
            mCells.get(i).onRemoved();
        }
        mCells.clear();
    }

    public boolean removeCell(@Nullable BaseCell cell) {
        if (cell == null) {
            return false;
        }
        boolean removed = mCells.remove(cell);
        if (removed) {
            cell.onRemoved();
        }

        notifyDataChange();

        return removed;
    }

    public boolean removeCellSilently(@Nullable BaseCell cell) {
        if (cell == null) {
            return false;
        }
        boolean removed = mCells.remove(cell);
        if (removed) {
            cell.onRemoved();
        }
        return removed;
    }

    public boolean replaceCell(@Nullable BaseCell oldCell, @Nullable BaseCell newCell) {
        if (oldCell == null || newCell == null) {
            return false;
        }
        int index = mCells.indexOf(oldCell);
        if (index >= 0) {
            mCells.set(index, newCell);
            newCell.onAdded();
            oldCell.onRemoved();
            return true;
        } else {
            return false;
        }
    }

    private boolean addCellInternal(@Nullable BaseCell cell, boolean silent) {
        if (cell != null) {
            cell.parentId = id;
            cell.parent = this;
            cell.serviceManager = serviceManager;
            MVHelper mvHelper = getMVHelper();
            if (mvHelper != null) {
                if (mvHelper.isValid(cell, serviceManager)) {
                    if (cell.position >= 0 && !TextUtils.isEmpty(load)) {
                        cell.pos = cell.position;
                        mPendingCells.add(cell);
                        return true;
                    } else {
                        cell.pos = mHeader != null ? this.mCells.size() + 1 : this.mCells.size();
                    }

                    if (!silent && mIsActivated) {
                        // do cell added
                        cell.added();
                    }

                    this.mCells.add(cell);
                    if (mFooter != null) {
                        mFooter.pos = cell.pos + 1;
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private boolean addCellInternal(Card parent, int index, @Nullable BaseCell cell, boolean silent) {
        if (cell != null) {
            cell.parentId = parent.id;
            cell.parent = parent;
            cell.serviceManager = serviceManager;
            MVHelper mvHelper = getMVHelper();
            if (mvHelper != null) {
                if (mvHelper.isValid(cell, serviceManager)) {
                    if (cell.position >= 0 && !TextUtils.isEmpty(load)) {
                        cell.pos = cell.position;
                        mPendingCells.add(cell);
                        return true;
                    } else {
                        //FixMe pos not correct when insert cell
                        cell.pos = mHeader != null ? this.mCells.size() + 1 : this.mCells.size();
                    }

                    if (!silent && mIsActivated) {
                        // do cell added
                        cell.added();
                    }

                    this.mCells.add(index, cell);
                    if (mFooter != null) {
                        mFooter.pos = cell.pos + 1;
                    }
                    return true;
                }
            }
        }

        return false;
    }


    private void adjustPendingCells(boolean silent) {
        if (mPendingCells.size() > 0) {
            Collections.sort(mPendingCells, CellPositionComparator.COMPARATOR);

            for (Iterator<BaseCell> iter = mPendingCells.iterator(); iter.hasNext(); ) {
                BaseCell next = iter.next();
                if (next.position < 0) continue;
                if (next.position < mCells.size()) {
                    mCells.add(next.position, next);
                    mInQueueCells.add(next);
                    iter.remove();

                    if (!silent)
                        next.added();
                } else {
                    break;
                }
            }
        }

        if (mInQueueCells.size() > 0) {
            // when do clean, the cell is already removed
            Collections.sort(mInQueueCells, CellPositionComparator.REVERSE_COMPARATOR);

            for (Iterator<BaseCell> iter = mInQueueCells.iterator(); iter.hasNext(); ) {
                BaseCell next = iter.next();
                if (next.position < 0) continue;
                if (next.position > mCells.size()) {
                    mPendingCells.add(next);
                    iter.remove();
                } else {
                    break;
                }
            }

        }

        if (TangramBuilder.isPrintLog()) {
            if (mPendingCells.size() > 0 && mInQueueCells.size() > 0) {
                Preconditions.checkState(mPendingCells.get(0).position >= mInQueueCells.get(mInQueueCells.size() - 1).position
                    , "Items in pendingQueue must have large position than Items in queue");
            }
        }
    }

    public void addChildCard(Card card) {

    }

    public void offsetChildCard(Card anchorCard, int offset) {

    }

    public void clearChildMap() {

    }

    public boolean isValid() {
        return (!TextUtils.isEmpty(stringType) || type >= 0) && serviceManager != null;
    }

    public final void notifyDataChange() {
        if (serviceManager instanceof Engine) {
            ((Engine) serviceManager).refresh();
        }
    }

    private void diffCells(@NonNull SparseArray<BaseCell> added, @NonNull SparseArray<BaseCell> removed) {
        if (!mIsActivated) return;

        for (int i = 0, size = added.size(); i < size; i++) {
            int key = added.keyAt(i);
            BaseCell cell = added.get(key);
            if (cell != null) {
                cell.added();
            }
        }


        for (int i = 0, size = removed.size(); i < size; i++) {
            int key = removed.keyAt(i);
            BaseCell cell = removed.get(key);
            if (cell != null) {
                cell.removed();
            }
        }
    }


    /*==========================================
     * Manage card lifecycle
     *==========================================*/
    protected void onAdded() {
        for (BaseCell cell : mCells) {
            cell.added();
        }
    }

    protected void onRemoved() {
        for (BaseCell cell : mCells) {
            cell.removed();
        }
    }

    protected void parseHeaderCell(@NonNull MVHelper resolver, @Nullable JSONObject header) {

    }

    protected void parseFooterCell(@NonNull MVHelper resolver, @Nullable JSONObject footer) {

    }


    /*==========================================
     * Place Holder
     *==========================================*/
    private BaseCell mPlaceholderCell;
    private float mTmpAspectRatio = Float.NaN;

    public void storeAspectRatio() {
        if (style != null && !Float.isNaN(style.aspectRatio)) {
            mTmpAspectRatio = style.aspectRatio;
            style.aspectRatio = Float.NaN;
        }
    }

    public void restoreAspectRatio() {
        if (style != null && !Float.isNaN(mTmpAspectRatio)) {
            style.aspectRatio = mTmpAspectRatio;
        }
    }

    public void enablePlaceholderView(View placeholderView, int placeholderHeight) {
        if (!TextUtils.isEmpty(load) && placeholderView != null) {
            storeAspectRatio();
            this.mPlaceholderCell = new PlaceholderCell(placeholderHeight, placeholderView);
            if (this.mCells.size() == 0) {
                mCells.add(mPlaceholderCell);
            }
        } else {
            this.mCells.remove(mPlaceholderCell);
            this.mPlaceholderCell = null;
        }
    }


    private boolean mPlaceholderRequired = true;

    public void showPlaceholderView(boolean shown) {
        this.mPlaceholderRequired = shown;
        if (!shown) {
            restoreAspectRatio();
        } else {
            storeAspectRatio();
        }
        if (!this.mCells.contains(mPlaceholderCell)) {
            if (requirePlaceholderCell()) {
                this.mCells.add(mPlaceholderCell);
                notifyDataChange();
            }
        } else {
            if (!requirePlaceholderCell() && this.mCells.remove(mPlaceholderCell))
                notifyDataChange();
        }


    }

    public BaseCell getPlaceholderCell() {
        return mPlaceholderCell;
    }

    public boolean requirePlaceholderCell() {
        return mPlaceholderRequired && mPlaceholderCell != null && !TextUtils.isEmpty(load)
            && (mCells.size() == 0 || (mCells.size() == 1 && mCells.contains(mPlaceholderCell)));
    }

    private MVHelper getMVHelper() {
        if (serviceManager != null) {
            return serviceManager.getService(MVHelper.class);
        }
        return null;
    }

    public Object optParam(String key) {
        if (extras.has(key))
            return extras.opt(key);
        if (style != null && style.extras != null)
            return style.extras.opt(key);
        return null;
    }

    public long optLongParam(String key) {
        if (extras.has(key))
            return extras.optLong(key);
        if (style != null && style.extras != null)
            return style.extras.optLong(key);
        return 0;
    }

    public int optIntParam(String key) {
        if (extras.has(key))
            return extras.optInt(key);
        if (style != null && style.extras != null)
            return style.extras.optInt(key);
        return 0;
    }

    public String optStringParam(String key) {
        if (extras.has(key))
            return extras.optString(key);
        if (style != null && style.extras != null)
            return style.extras.optString(key);
        return "";
    }

    public double optDoubleParam(String key) {
        if (extras.has(key))
            return extras.optDouble(key);
        if (style != null && style.extras != null)
            return style.extras.optDouble(key);
        return Double.NaN;
    }

    public boolean optBoolParam(String key) {
        if (extras.has(key))
            return extras.optBoolean(key);
        return style != null && style.extras != null && style.extras.optBoolean(key);
    }

    public JSONObject optJsonObjectParam(String key) {
        if (extras.has(key))
            return extras.optJSONObject(key);
        if (style != null && style.extras != null)
            return style.extras.optJSONObject(key);
        return null;
    }

    public JSONArray optJsonArrayParam(String key) {
        if (extras.has(key))
            return extras.optJSONArray(key);
        if (style != null && style.extras != null)
            return style.extras.optJSONArray(key);
        return null;
    }

    public Card findChildCardById(String id) {
        if (!mChildren.isEmpty()) {
            for (int i = 0, size = mChildren.size(); i < size; i++) {
                Card card = mChildren.valueAt(i);
                if (card != null && card.id.equals(id)) {
                    return card;
                }
            }
        }
        return null;
    }

    public ArrayMap<Range<Integer>, Card> getChildrenCards(){
        return mChildren;
    }

    public static final class PlaceholderCell extends BaseCell {

        private int mHeight = 0;

        private View mPlaceholderView;

        private int mBgColor;

        public PlaceholderCell(int height, int bgColor) {
            this(height, null, bgColor);
        }

        public PlaceholderCell(int height, View placeholderView) {
            this(height, placeholderView, 0x0);
        }

        public PlaceholderCell(int height, View loadingView, int bgColor) {
            this.mHeight = height;
            this.mPlaceholderView = loadingView;
            this.mBgColor = bgColor;
            this.style = new Style();
            this.style.height = mHeight;
            this.style.bgColor = mBgColor;
            this.style.extras = new JSONObject();
            try {
                this.style.extras.put(Style.KEY_DISPLAY, Style.DISPLAY_BLOCK);
            } catch (JSONException e) {
                Log.w(TAG, Log.getStackTraceString(e), e);
            }

            this.type = TangramBuilder.TYPE_EXTENDED_VIEW;
            this.stringType = String.valueOf(TangramBuilder.TYPE_EXTENDED_VIEW);
        }

        public void bindView(@NonNull View view) {
            if (mPlaceholderView != null && view instanceof FrameLayout) {
                if (mPlaceholderView.getParent() instanceof FrameLayout) {
                    ((FrameLayout) mPlaceholderView.getParent()).removeView(mPlaceholderView);
                }

                ((FrameLayout) view).addView(mPlaceholderView);
            }
        }
    }

    private static class CellPositionComparator implements Comparator<BaseCell> {

        public static final CellPositionComparator COMPARATOR = new CellPositionComparator(false);
        public static final CellPositionComparator REVERSE_COMPARATOR = new CellPositionComparator(true);

        private int mLarge;
        private int mSmall;

        CellPositionComparator(boolean reverse) {
            mLarge = reverse ? -1 : 1;
            mSmall = -mLarge;
        }

        @Override
        public int compare(BaseCell lhs, BaseCell rhs) {


            if (lhs == null && rhs == null) return 0;
            if (lhs == null) return mSmall;
            if (rhs == null) return mLarge;

            return lhs.position < rhs.position ? mSmall : (lhs.position == rhs.position ? 0 : mLarge);

        }
    }

}
