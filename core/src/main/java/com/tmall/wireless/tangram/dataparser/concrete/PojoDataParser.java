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

import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.dataparser.DataParser;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.SlideCard;
import com.tmall.wireless.tangram.structure.entitycard.BannerEntityCard;
import com.tmall.wireless.tangram.structure.entitycard.EntityCard;
import com.tmall.wireless.tangram.structure.entitycard.GridEntityCard;
import com.tmall.wireless.tangram.util.LogUtils;
import com.tmall.wireless.tangram.util.Preconditions;
import com.tmall.wireless.tangram.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
                final int cardType = cardData.optInt(Card.KEY_TYPE, -1);
                if (cardType >= 0) {
                    final Card card = cardResolver.create(cardType);
                    if (card != null) {
                        card.rowId = i;
                        card.serviceManager = serviceManager;
                        card.parseWith(cardData, cellResolver);
                        card.type = cardType;
                        if (card.isValid()) {
                            if (card instanceof IDelegateCard) {
                                List<Card> cards = ((IDelegateCard) card).getCards(new CardResolver() {
                                    @Override
                                    public Card create(int type) {
                                        Card c = cardResolver.create(type);
                                        c.serviceManager = serviceManager;
                                        c.id = card.id;
                                        c.type = type;
                                        c.rowId = card.rowId;
                                        return c;
                                    }
                                });
                                for (Card c : cards) {
                                    if (c.isValid())
                                        result.add(c);
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
                        LogUtils.w(TAG, "Can not generate cardType: " + cardType);
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
        if (data == null) return new ArrayList<>();

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
            int cellType = cellData.optInt(Card.KEY_TYPE, -1);
            if (Utils.isCellizedCard(cellData)) {
                cellType = Utils.cellizeCard(cellType);
            }
            if (resolver != null && resolver.resolver().getViewClass(cellType) != null) {
                BaseCell cell;
                if (resolver.resolver().isCompatibleType(cellType)) {
                    cell = Utils.newInstance(resolver.resolver().getCellClass(cellType));

                    //do not display when newInstance failed
                    if (cell == null)
                        return null;

                    cell.serviceManager = serviceManager; // ensure service manager
                } else {
                    if (Utils.isCellizedCard(cellData)) {
                        switch (Utils.cardizeCell(cellType)) {
                            case TangramBuilder.TYPE_SINGLE_COLUMN:
                            case TangramBuilder.TYPE_DOUBLE_COLUMN:
                            case TangramBuilder.TYPE_TRIPLE_COLUMN:
                            case TangramBuilder.TYPE_FOUR_COLUMN:
                            case TangramBuilder.TYPE_FIVE_COLUMN:
                                cell = new GridEntityCard(cellType);
                                break;
                            case TangramBuilder.TYPE_CAROUSEL:
                                cell = new BannerEntityCard();
                                break;
                            default:
                                cell = new EntityCard(cellType);
                                break;
                        }
                        cell.serviceManager = serviceManager; // ensure service manager
                    } else {
                        cell = new BaseCell(cellType);
                        cell.serviceManager = serviceManager; // ensure service manager
                    }
                }
                resolver.parseCell(resolver, cell, cellData);
                cell.type = cellType; // ensure cell type
                return cell;
            }
            return null;
        }
        return null;
    }

}
