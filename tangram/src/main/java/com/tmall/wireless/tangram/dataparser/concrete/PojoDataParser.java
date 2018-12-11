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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.text.TextUtils;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.dataparser.DataParser;
import com.tmall.wireless.tangram.op.ParseComponentsOp;
import com.tmall.wireless.tangram.op.ParseGroupsOp;
import com.tmall.wireless.tangram.op.ParseSingleGroupOp;
import com.tmall.wireless.tangram.op.ParseSingleComponentOp;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.SlideCard;
import com.tmall.wireless.tangram.structure.card.WrapCellCard;
import com.tmall.wireless.tangram.util.LogUtils;
import com.tmall.wireless.tangram.util.Preconditions;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * DataParser parse JSONArray into Card/Cell
 */
public class PojoDataParser extends DataParser<JSONObject, JSONArray, Card, BaseCell> {

    private static final String TAG = "PojoDataParser";

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public List<Card> parseGroup(@NonNull JSONArray data, @NonNull final ServiceManager serviceManager) {
        final CardResolver cardResolver = serviceManager.getService(CardResolver.class);
        Preconditions.checkState(cardResolver != null, "Must register CardResolver into ServiceManager first");
        final MVHelper cellResolver = serviceManager.getService(MVHelper.class);
        Preconditions.checkState(cellResolver != null, "Must register CellResolver into ServiceManager first");
        final int size = data.length();
        final List<Card> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            JSONObject cardData = data.optJSONObject(i);
            final Card card = parseSingleGroup(cardData, serviceManager);
            if (card != null) {
                if (card instanceof IDelegateCard) {
                    List<Card> cards = ((IDelegateCard) card).getCards(new CardResolver() {
                        @Override
                        public Card create(String type) {
                            Card c = cardResolver.create(type);
                            c.serviceManager = serviceManager;
                            c.id = card.id;
                            c.setStringType(type);
                            c.rowId = card.rowId;
                            return c;
                        }
                    });
                    for (Card c : cards) {
                        if (c.isValid()) {
                            result.add(c);
                        }
                    }
                } else {
                    result.add(card);
                }
            }
        }
        cellResolver.resolver().setCards(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public List<BaseCell> parseComponent(JSONArray data, ServiceManager serviceManager) {
        return parseComponent(data, null, serviceManager);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public List<BaseCell> parseComponent(@Nullable JSONArray data, Card parent, ServiceManager serviceManager) {
        if (data == null) {
            return new ArrayList<>();
        }
        final int size = data.length();
        final List<BaseCell> result = new ArrayList<>(size);
        //parse body
        JSONArray componentArray = data;
        if (componentArray != null) {
            final int cellLength = componentArray.length();
            for (int i = 0; i < cellLength; i++) {
                final JSONObject cellData = componentArray.optJSONObject(i);
                BaseCell cell = parseSingleComponent(cellData, parent, serviceManager);
                if (cell != null) {
                    result.add(cell);
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Card parseSingleGroup(@Nullable JSONObject data, final ServiceManager serviceManager) {
        if (data == null) {
            return Card.NaN;
        }
        final CardResolver cardResolver = serviceManager.getService(CardResolver.class);
        Preconditions.checkState(cardResolver != null, "Must register CardResolver into ServiceManager first");
        final MVHelper cellResolver = serviceManager.getService(MVHelper.class);
        Preconditions.checkState(cellResolver != null, "Must register CellResolver into ServiceManager first");
        final String cardType = data.optString(Card.KEY_TYPE);
        if (!TextUtils.isEmpty(cardType)) {
            final Card card = cardResolver.create(cardType);
            if (card != null) {
                card.serviceManager = serviceManager;
                card.parseWith(data, cellResolver);
                card.type = data.optInt(Card.KEY_TYPE, -1);
                card.stringType = cardType;
                if (card.isValid()) {
                    if (card.style.slidable) {
                        return new SlideCard(card);
                    } else {
                        return card;
                    }
                }
            } else {
                final Card cellCard = new WrapCellCard();
                if (cellCard != null) {
                    cellCard.serviceManager = serviceManager;
                    cellCard.parseWith(data, cellResolver);
                    cellCard.setStringType(TangramBuilder.TYPE_CONTAINER_1C_FLOW);
                    if (cellCard.isValid()) {
                        return cellCard;
                    }
                }
            }
        } else {
            LogUtils.w(TAG, "Invalid card type when parse JSON data");
        }
        return Card.NaN;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public BaseCell parseSingleComponent(@Nullable JSONObject data, Card parent, ServiceManager serviceManager) {
        if (data == null) {
            return BaseCell.NaN;
        }
        final CardResolver cardResolver = serviceManager.getService(CardResolver.class);
        Preconditions.checkState(cardResolver != null, "Must register CardResolver into ServiceManager first");
        final MVHelper cellResolver = serviceManager.getService(MVHelper.class);
        Preconditions.checkState(cellResolver != null, "Must register CellResolver into ServiceManager first");
        BaseCell cell = Card.createCell(parent, cellResolver, data, serviceManager, true);
        if (cellResolver.isValid(cell, serviceManager)) {
            return cell;
        } else {
            return BaseCell.NaN;
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ObservableTransformer<ParseGroupsOp, List<Card>> getGroupTransformer() {
        return new ObservableTransformer<ParseGroupsOp, List<Card>>() {
            @Override
            public ObservableSource<List<Card>> apply(Observable<ParseGroupsOp> upstream) {
                return upstream.map(new Function<ParseGroupsOp, List<Card>>() {
                    @Override
                    public List<Card> apply(ParseGroupsOp parseGroupsOp) throws Exception {
                        return parseGroup(parseGroupsOp.getArg1(), parseGroupsOp.getArg2());
                    }
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ObservableTransformer<ParseComponentsOp, List<BaseCell>> getComponentTransformer() {
        return new ObservableTransformer<ParseComponentsOp, List<BaseCell>>() {
            @Override
            public ObservableSource<List<BaseCell>> apply(Observable<ParseComponentsOp> upstream) {
                return upstream.map(new Function<ParseComponentsOp, List<BaseCell>>() {
                    @Override
                    public List<BaseCell> apply(ParseComponentsOp parseComponentsOp) throws Exception {
                        return parseComponent(parseComponentsOp.getArg1(), parseComponentsOp.getArg2(), parseComponentsOp.getArg3());
                    }
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ObservableTransformer<ParseSingleGroupOp, Card> getSingleGroupTransformer() {
        return new ObservableTransformer<ParseSingleGroupOp, Card>() {
            @Override
            public ObservableSource<Card> apply(Observable<ParseSingleGroupOp> upstream) {
                return upstream.map(new Function<ParseSingleGroupOp, Card>() {
                    @Override
                    public Card apply(ParseSingleGroupOp parseSingleGroupOp) throws Exception {
                        return parseSingleGroup(parseSingleGroupOp.getArg1(), parseSingleGroupOp.getArg2());
                    }
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ObservableTransformer<ParseSingleComponentOp, BaseCell> getSingleComponentTransformer() {
        return new ObservableTransformer<ParseSingleComponentOp, BaseCell>() {
            @Override
            public ObservableSource<BaseCell> apply(Observable<ParseSingleComponentOp> upstream) {
                return upstream.map(new Function<ParseSingleComponentOp, BaseCell>() {
                    @Override
                    public BaseCell apply(ParseSingleComponentOp parseSingleComponentOp) throws Exception {
                        return parseSingleComponent(parseSingleComponentOp.getArg1(), parseSingleComponentOp.getArg2(), parseSingleComponentOp

                            .getArg3());
                    }
                });
            }
        };
    }

}
