package com.tmall.wireless.tangram.example.dataparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import com.tmall.wireless.tangram.MVHelper;
import com.tmall.wireless.tangram.core.service.ServiceManager;
import com.tmall.wireless.tangram.dataparser.DataParser;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.dataparser.concrete.CardResolver;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.card.BannerCard;
import com.tmall.wireless.tangram.structure.card.GridCard;
import com.tmall.wireless.tangram.util.Preconditions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liupeng on 13/12/2017.
 */

public class SampleDataParser extends DataParser<JSONArray, Card, BaseCell> {

    @Nullable
    @Override
    public List<Card> parseGroup(@Nullable JSONArray data, ServiceManager serviceManager) {
        final CardResolver cardResolver = serviceManager.getService(CardResolver.class);
        Preconditions.checkState(cardResolver != null, "Must register CardResolver into ServiceManager first");
        final MVHelper cellResolver = serviceManager.getService(MVHelper.class);
        Preconditions.checkState(cellResolver != null, "Must register CellResolver into ServiceManager first");
        final int size = data.length();
        final List<Card> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            JSONObject cardData = data.optJSONObject(i);
            if (cardData != null) {
                //maybe custom card
                final String cardType = cardData.optString(Card.KEY_TYPE);
                if (!TextUtils.isEmpty(cardType)) {
                    final Card card = cardResolver.create(cardType);
                    if (card != null) {
                        card.rowId = i;
                        card.serviceManager = serviceManager;
                        //add card style
                        addCardStyle(cardData, card);
                        //custom items element key if need.
                        transformCardCellData(cardData);
                        //parser card
                        card.parseWith(cardData, cellResolver);
                        card.type = cardData.optInt(Card.KEY_TYPE, -1);
                        card.stringType = cardType;
                        result.add(card);
                    }
                }
            }
        }
        cellResolver.resolver().setCards(result);
        return result;
    }

    private void addCardStyle(JSONObject cardData, Card card) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Float.class, new JsonSerializer<Float>() {
                @Override
                public JsonElement serialize(final Float src, final Type typeOfSrc, final JsonSerializationContext context) {
                    try {
                        if (src.isInfinite() || src.isNaN()) {
                            return new JsonPrimitive(0f);
                        }
                        BigDecimal value = BigDecimal.valueOf(src);
                        return new JsonPrimitive(value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new JsonPrimitive(0f);
                }
            });

            Gson gson = gsonBuilder.create();
            GridCard.GridStyle gridStyle = new GridCard.GridStyle();
            if (card instanceof BannerCard) {
                gridStyle.aspectRatio = 3.223f;
            }
            cardData.put(Card.KEY_STYLE, new JSONObject(gson.toJson(gridStyle)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void transformCardCellData(JSONObject cardData) {
        try {
            if (cardData.has("iconList")) {
                cardData.put(Card.KEY_ITEMS, cardData.getJSONArray("iconList"));
            } else if (cardData.has("centerBannerList")) {
                cardData.put(Card.KEY_ITEMS, cardData.getJSONArray("centerBannerList"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public List<BaseCell> parseComponent(@Nullable JSONArray data, ServiceManager serviceManager) {
        return null;
    }
}
