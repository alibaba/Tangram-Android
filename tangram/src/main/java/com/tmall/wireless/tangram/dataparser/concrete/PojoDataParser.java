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

package com.tmall.wireless.tangram.dataparser.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.text.TextUtils;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.dataparser.DataParser;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.SlideCard;
import com.tmall.wireless.tangram.structure.card.WrapCellCard;
import com.tmall.wireless.tangram.structure.entitycard.BannerEntityCard;
import com.tmall.wireless.tangram.structure.entitycard.LinearScrollEntityCard;
import com.tmall.wireless.tangram.util.LogUtils;
import com.tmall.wireless.tangram.util.Preconditions;
import com.tmall.wireless.tangram.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * DataParser parse JSONArray into Card/Cell
 */
public final class PojoDataParser extends DataParser<JSONArray, Card, BaseCell> {

    private static final String TAG = "PojoDataParser";

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
            if (cardData != null) {
                final String cardType = cardData.optString(Card.KEY_TYPE);
                if (!TextUtils.isEmpty(cardType)) {
                    final Card card = cardResolver.create(cardType);
                    if (card != null) {
                        card.rowId = i;
                        card.serviceManager = serviceManager;
                        card.parseWith(cardData, cellResolver);
                        card.type = cardData.optInt(Card.KEY_TYPE, -1);
                        card.stringType = cardType;
                        if (card.isValid()) {
                            if (card instanceof IDelegateCard) {
                                List<Card> cards = ((IDelegateCard) card).getCards(new CardResolver() {
                                    @Override
                                    public Card create(String type) {
                                        Card c = cardResolver.create(type);
                                        c.serviceManager = serviceManager;
                                        c.id = card.id;
                                        c.setStringType(cardType);
                                        c.stringType = cardType;
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
                                if (card.style.slidable) {
                                    result.add(new SlideCard(card));
                                } else {
                                    result.add(card);
                                }
                            }
                        }
                    } else {
                        final Card cellCard = new WrapCellCard();
                        if (cellCard != null) {
                            cellCard.rowId = i;
                            cellCard.serviceManager = serviceManager;
                            cellCard.parseWith(cardData, cellResolver);
                            cellCard.setStringType(TangramBuilder.TYPE_CONTAINER_1C_FLOW);
                            if (cellCard.isValid()) {
                                result.add(cellCard);
                            }
                        }
                    }
                } else {
                    LogUtils.w(TAG, "Invalid card type when parse JSON data");
                }
            }
        }

        cellResolver.resolver().setCards(result);
        return result;
    }

    @Nullable
    @Override
    public List<BaseCell> parseComponent(JSONArray data, ServiceManager serviceManager) {
        return parseComponent(data, serviceManager, null);
    }

    @Nullable
    public List<BaseCell> parseComponent(JSONArray data, ServiceManager serviceManager, Card card) {
        if (data == null) {
            return new ArrayList<>();
        }

        final CardResolver cardResolver = serviceManager.getService(CardResolver.class);
        Preconditions.checkState(cardResolver != null, "Must register CardResolver into ServiceManager first");
        final MVHelper cellResolver = serviceManager.getService(MVHelper.class);
        Preconditions.checkState(cellResolver != null, "Must register CellResolver into ServiceManager first");

        final int size = data.length();
        final List<BaseCell> result = new ArrayList<>(size);
        //解析 body 组件
        JSONArray componentArray = data;
        if (componentArray != null) {
            final int cellLength = componentArray.length();
            for (int i = 0; i < cellLength; i++) {
                final JSONObject cellData = componentArray.optJSONObject(i);
                BaseCell cell = createCell(cellResolver, cellData, serviceManager);
                if (cell != null && cellResolver.isValid(cell, serviceManager)) {
                    result.add(cell);
                }
            }
        }
        return result;
    }

    protected BaseCell createCell(@NonNull MVHelper resolver, @Nullable JSONObject cellData, ServiceManager serviceManager) {
        if (cellData != null) {
            BaseCell cell = null;
            String cellType = cellData.optString(Card.KEY_TYPE);
            if ((resolver != null && resolver.resolver().getViewClass(cellType) != null) || Utils.isCard(cellData)) {
                if (resolver.resolver().isCompatibleType(cellType)) {
                    cell = Utils.newInstance(resolver.resolver().getCellClass(cellType));

                    //do not display when newInstance failed
                    if (cell == null) {
                        return null;
                    }

                    cell.serviceManager = serviceManager;
                } else {
                    if (Utils.isCard(cellData)) {
                        switch (cellType) {
                            //TODO support parse inline flow card
                            case TangramBuilder.TYPE_CONTAINER_BANNER:
                                cell = new BannerEntityCard();
                                break;
                            case TangramBuilder.TYPE_CONTAINER_SCROLL:
                                cell = new LinearScrollEntityCard();
                                break;
                        }
                        if (cell != null) {
                            cell.serviceManager = serviceManager;
                        }
                    } else {
                        cell = new BaseCell(cellType);
                        cell.serviceManager = serviceManager;
                    }
                }
                if (cell != null) {
                    resolver.parseCell(resolver, cell, cellData);
                    cell.setStringType(cellType);
                }
                return cell;
            } else {
                //support virtual view at layout
                BaseCellBinderResolver componentBinderResolver = serviceManager.getService(BaseCellBinderResolver.class);
                if (componentBinderResolver.has(cellType)) {
                    cell = new BaseCell(cellType);
                    cell.serviceManager = serviceManager;
                    resolver.parseCell(resolver, cell, cellData);
                    cell.setStringType(cellType);
                    return cell;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

}
