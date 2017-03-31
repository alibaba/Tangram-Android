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

package com.tmall.wireless.tangram.structure.card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.tmall.wireless.tangram.Engine;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.CardResolver;
import com.tmall.wireless.tangram.dataparser.concrete.Cell;
import com.tmall.wireless.tangram.dataparser.concrete.IDelegateCard;
import com.tmall.wireless.tangram.dataparser.concrete.WrapperCard;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.style.DelegateStyle;
import com.tmall.wireless.tangram.support.async.CardLoadSupport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by villadora on 15/12/3.
 */
public class FusionCard extends Card implements IDelegateCard {

    @Override
    public void parseWith(@NonNull JSONObject data, @NonNull MVHelper resolver) {
        super.parseWith(data, resolver);
    }

    @Override
    protected void parseStyle(JSONObject data) {
        style = new DelegateStyle();
        if (data != null) {
            style.parseWith(data);
            for (DelegateStyle.CardInfo info : ((DelegateStyle) style).cardInfos) {
                try {
                    info.data.put("load", load);
                    info.data.put("loadMore", loadMore);
                    info.data.put("hasMore", hasMore);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && mCells.size() > 0 && (style instanceof DelegateStyle && ((DelegateStyle) style).cardInfos.size() > 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Card> getCards(final CardResolver cardResolver) {
        if (serviceManager == null) return Collections.emptyList();

        final CardLoadSupport loadSupport = serviceManager.getService(CardLoadSupport.class);
        final MVHelper cellResolver = serviceManager.getService(MVHelper.class);

        final ViewFactory viewFactory = serviceManager.getService(ViewFactory.class);

        if (loadSupport == null || cellResolver == null || cardResolver == null)
            return Collections.emptyList();


        if (style instanceof DelegateStyle) {
            DelegateStyle dStyle = (DelegateStyle) this.style;

            final Card anchorCard = cardResolver.create(TangramBuilder.TYPE_SINGLE_COLUMN);
            final BaseCell emptyCell = new BaseCell(TangramBuilder.TYPE_EMPTY_VIEW);
            if (emptyCell.style != null)
                emptyCell.style.height = 0;

            anchorCard.addCell(emptyCell);


            final Card headerCard = cardResolver.create(TangramBuilder.TYPE_STICKY_START);
            final BaseCell tabCell = mCells.get(0);
            mCells.remove(0);
            // asign header id
            headerCard.id = id + "-tabheader";
            headerCard.addCell(tabCell);

            final DelegateStyle.CardInfo info = dStyle.cardInfos.get(0);
            final int cardType = info.type;
            final Card originalCard = cardResolver.create(info.type);

            // no cell is needed
            originalCard.type = info.type;
            // assign id;
            originalCard.id = id;
            originalCard.parseWith(info.data, cellResolver);

            final Card contentCard = new FusionContentCard(originalCard,
                    (tabCell instanceof SwitchTabHeaderCell) ? (SwitchTabHeaderCell) tabCell : null, 0);

            if (viewFactory != null) {
                View view = viewFactory.create();
                int defaultHeight = viewFactory.getDefaultHeight();
                if (view != null && defaultHeight > 0)
                    contentCard.enablePlaceholderView(view, defaultHeight);
            }

            contentCard.loadMore = true;
            contentCard.hasMore = true;

            if (TextUtils.isEmpty(contentCard.load))
                contentCard.load = this.load;

            if (TextUtils.isEmpty(contentCard.load))
                return Collections.emptyList();

            if (mCells.size() > 0) {
                contentCard.addCells(mCells);
            }

            if (mPendingCells.size() > 0) {
                contentCard.addCells(mPendingCells);
            }

            if ((tabCell instanceof SwitchTabHeaderCell)) {
                final SwitchTabHeaderCell switchHeader = (SwitchTabHeaderCell) tabCell;
                switchHeader.setSwitchTabTrigger(new SwitchTabTrigger() {
                    private Map<Integer, IndexCache> mCacheMap = new HashMap<>();

                    private int currentIndex = 0;

                    private Card currentCard = contentCard;

                    @Override
                    public void invalidate(int index) {
                        mCacheMap.remove(index);
                        if (currentIndex == index) {
                            currentIndex = -1;
                        }
                    }

                    @Override
                    public void switchTo(int index, @NonNull Cell cell, Map<String, Object> params) {
                        switchTo(index, FusionCard.this.id, cell, params);
                    }

                    @Override
                    public void switchTo(int index, String id, @NonNull Cell cell, @Nullable Map<String, Object> params) {
                        if (currentIndex == index) {
                            // reassign id
                            currentCard.id = id;
                            currentCard.setParams(params);
                            return;
                        }

                        if (currentIndex >= 0)
                            mCacheMap.put(currentIndex, new IndexCache(currentIndex, currentCard));
                        IndexCache indexCache = mCacheMap.get(index);
                        if (indexCache == null) {
                            Card newCard = cardResolver.create(cardType);
                            // no cell is needed
                            newCard.type = cardType;
                            // assign id
                            newCard.id = id;
                            newCard.parseWith(info.data, cellResolver);
                            newCard = new FusionContentCard(newCard, switchHeader, index);
                            newCard.loadMore = true;
                            newCard.hasMore = true;

                            if (viewFactory != null) {
                                View view = viewFactory.create();
                                int defaultHeight = viewFactory.getDefaultHeight();
                                if (view != null && defaultHeight > 0)
                                    newCard.enablePlaceholderView(view, defaultHeight);
                            }

                            indexCache = new IndexCache(index, newCard);
                        }

                        indexCache.card.setParams(params);

                        // TODO: not generic!!!
                        Engine engine = (Engine) FusionCard.this.serviceManager;
                        if (engine != null) {
                            // first stopScrolling!!!
                            engine.scrollToPosition(headerCard);
                            engine.replaceCard(currentCard, indexCache.card);
                            currentCard = indexCache.card;
                            if (!currentCard.loaded)
                                loadSupport.loadMore(currentCard);
                        }

                        currentIndex = index;
                    }
                });
            } else {
                return Collections.emptyList();
            }


            return Arrays.asList(anchorCard, headerCard, contentCard);
        }

        return Collections.emptyList();
    }

    public interface SwitchTabHeaderCell {
        void setSwitchTabTrigger(SwitchTabTrigger trigger);

        int getTotalPage();

        void switchTo(int index);
    }


    public interface SwitchTabTrigger {
        void switchTo(int index, @NonNull Cell cell, @Nullable Map<String, Object> params);

        void switchTo(int index, String id, @NonNull Cell cell, @Nullable Map<String, Object> params);

        void invalidate(int index);
    }


    /**
     * ContentCard used in FusionCard, which wrap another card
     */
    static class FusionContentCard extends WrapperCard implements SwipeCard {

        @Nullable
        private SwitchTabHeaderCell mHeaderCell;

        private final int mIndex;

        public FusionContentCard(@NonNull Card card, @Nullable SwitchTabHeaderCell header, int index) {
            super(card);
            this.mHeaderCell = header;
            this.mIndex = index;
        }

        @Override
        public int getCurrentIndex() {
            return mIndex;
        }

        @Override
        public int getTotalPage() {
            if (mHeaderCell != null)
                return mHeaderCell.getTotalPage();
            return 0;
        }

        public void switchTo(int index) {
            if (mHeaderCell != null)
                mHeaderCell.switchTo(index);
        }
    }

    static final class IndexCache {
        int index = -1;
        Card card;

        IndexCache(int index, Card card) {
            this.index = index;
            this.card = card;
        }
    }


    public interface ViewFactory {
        View create();

        int getDefaultHeight();
    }

}
