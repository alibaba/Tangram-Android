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

package com.tmall.wireless.tangram3.dataparser.concrete;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram3.MVHelper;
import com.tmall.wireless.tangram3.TangramBuilder;
import com.tmall.wireless.tangram3.core.service.ServiceManager;
import com.tmall.wireless.tangram3.dataparser.DataParser;
import com.tmall.wireless.tangram3.structure.BaseCell;
import com.tmall.wireless.tangram3.structure.card.BannerCard;
import com.tmall.wireless.tangram3.structure.card.FixCard;
import com.tmall.wireless.tangram3.structure.card.FixLinearScrollCard;
import com.tmall.wireless.tangram3.structure.card.GridCard;
import com.tmall.wireless.tangram3.structure.card.LinearScrollCard;
import com.tmall.wireless.tangram3.structure.card.OnePlusNCard;
import com.tmall.wireless.tangram3.structure.card.SlideCard;
import com.tmall.wireless.tangram3.structure.card.StaggeredCard;
import com.tmall.wireless.tangram3.structure.card.StickyCard;
import com.tmall.wireless.tangram3.structure.card.StickyEndCard;
import com.tmall.wireless.tangram3.structure.card.WrapCellCard;
import com.tmall.wireless.tangram3.structure.cell.BannerCell;
import com.tmall.wireless.tangram3.structure.cell.LinearScrollCell;
import com.tmall.wireless.tangram3.structure.style.ColumnStyle;
import com.tmall.wireless.tangram3.util.LogUtils;
import com.tmall.wireless.tangram3.util.Preconditions;
import com.tmall.wireless.tangram3.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.alibaba.android.vlayout.layout.FixLayoutHelper.BOTTOM_LEFT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.BOTTOM_RIGHT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.TOP_LEFT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.TOP_RIGHT;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ALWAYS;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ON_ENTER;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ON_LEAVE;

/**
 * DataParser parse JSONArray into Card/Cell
 */
public class PojoDataParser extends DataParser<JSONObject, JSONArray> {

    private static final String TAG = "PojoDataParser";

    public static final String KEY_TYPE = "type";

    public static final String KEY_STYLE = "style";

    public static final String KEY_ID = "id";

    public static final String KEY_ITEMS = "items";

    public static final String KEY_HEADER = "header";

    public static final String KEY_FOOTER = "footer";

    public static final String KEY_BIZ_ID = "bizId";

    public static final String KEY_TYPE_KEY = "typeKey";

    public static final String KEY_TYPE_REUSEID = "reuseId";

    public static final String KEY_POSITION = "position";

    /**
     * Use {@link #KEY_BACKGROUND_COLOR} instead
     */
    @Deprecated
    public static final String KEY_BG_COLOR = "bgColor";

    public static final String KEY_BACKGROUND_COLOR = "background-color";

    /**
     * Use {@link #KEY_BACKGROUND_IMAGE} instead
     */
    @Deprecated
    public static final String KEY_BG_IMAGE = "bgImage";

    /**
     * Use {@link #KEY_BACKGROUND_IMAGE} instead
     */
    @Deprecated
    public static final String KEY_STYLE_BG_IMAGE = "bgImgUrl";

    public static final String KEY_BACKGROUND_IMAGE = "background-image";

    public static final String KEY_MARGIN = "margin";

    public static final String KEY_PADDING = "padding";

    public static final String KEY_WIDTH = "width";

    public static final String KEY_HEIGHT = "height";

    public static final String KEY_ZINDEX = "zIndex";

    public static final String KEY_ASPECT_RATIO = "aspectRatio";

    public static final String KEY_RATIO = "ratio";

    public static final String KEY_SLIDABLE = "slidable";

    public static final String KEY_FOR_LABEL = "forLabel";

    public static final String KEY_COLS = "cols";

    public static final String KEY_COLUMN = "column";

    public static final String KEY_AUTO_EXPAND = "autoExpand";

    public static final String KEY_IGNORE_EXTRA = "ignoreExtra";

    public static final String KEY_H_GAP = "hGap";

    public static final String KEY_V_GAP = "vGap";

    public static final String KEY_ROWS = "rows";

    public static final String ATTR_AUTOSCROLL = "autoScroll";

    public static final String ATTR_SPECIAL_INTERVAL = "specialInterval";

    public static final String ATTR_INFINITE = "infinite";

    public static final String ATTR_INDICATOR_FOCUS = "indicatorImg1";

    public static final String ATTR_INDICATOR_NORMAL = "indicatorImg2";

    public static final String ATTR_INDICATOR_GRA = "indicatorGravity";

    public static final String ATTR_INDICATOR_POS = "indicatorPosition";

    public static final String ATTR_INDICATOR_GAP = "indicatorGap";

    public static final String ATTR_INDICATOR_HEIGHT = "indicatorHeight";

    public static final String ATTR_INDICATOR_MARGIN = "indicatorMargin";

    public static final String ATTR_INFINITE_MIN_COUNT = "infiniteMinCount";

    public static final String ATTR_PAGE_WIDTH = "pageRatio";

    public static final String ATTR_HGAP = "hGap";

    public static final String ATTR_ITEM_MARGIN_LEFT = "scrollMarginLeft";

    public static final String ATTR_ITEM_MARGIN_RIGHT = "scrollMarginRight";

    public static final String ATTR_ITEM_RATIO = "itemRatio";

    public static final String ATTR_INDICATOR_RADIUS = "indicatorRadius";

    public static final String ATTR_INDICATOR_COLOR = "indicatorColor";

    public static final String ATTR_INDICATOR_DEFAULT_INDICATOR_COLOR = "defaultIndicatorColor";

    public static final String KEY_INDEX = "index";

    public static final String KEY_STICKY = "sticky";

    public static final String STICKY_START = "start";

    public static final String STICKY_END = "end";

    public static final String KEY_ALIGN = "align";

    public static final String KEY_SHOW_TYPE = "showType";

    public static final String KEY_SKETCH_MEASURE = "sketchMeasure";

    public static final String KEY_X = "x";

    public static final String KEY_Y = "y";

    private CardResolver cardResolver;

    private MVHelper mvHelper;

    @NonNull
    @Override
    public List<Card> parseGroup(@Nullable JSONArray data, @NonNull final ServiceManager serviceManager) {
        if (data == null) {
            return new ArrayList<>();
        }

        checkCardResolverAndMVHelper(serviceManager);
        final int size = data.length();
        final List<Card> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            JSONObject cardData = data.optJSONObject(i);
            final Card card = parseSingleGroup(cardData, serviceManager);
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
        mvHelper.resolver().setCards(result);
        return result;
    }

    @NonNull
    @Override
    public List<BaseCell> parseComponent(@Nullable JSONArray data, Card parent, ServiceManager serviceManager) {
        if (data == null) {
            return new ArrayList<>();
        }
        final int size = data.length();
        final List<BaseCell> result = new ArrayList<>(size);

        //parse body
        final int cellLength = data.length();
        for (int i = 0; i < cellLength; i++) {
            final JSONObject cellData = data.optJSONObject(i);
            BaseCell cell = parseSingleComponent(cellData, parent, serviceManager);
            if (cell != null) {
                result.add(cell);
            }
        }
        return result;
    }

    @NonNull
    @Override
    public Card parseSingleGroup(@Nullable JSONObject data, @NonNull final ServiceManager serviceManager) {
        if (TangramBuilder.isPrintLog() && serviceManager == null) {
            throw new RuntimeException("serviceManager is null when parsing card!");
        }

        if (data == null) {
            return Card.NaN;
        }

        checkCardResolverAndMVHelper(serviceManager);

        final String cardType = data.optString(KEY_TYPE);
        if (!TextUtils.isEmpty(cardType)) {
            Card card = cardResolver.create(cardType);
            if (card == null) {
                card = new WrapCellCard();
                card.setStringType(TangramBuilder.TYPE_CONTAINER_1C_FLOW);
            }

            card.dataParser = this;
            card.serviceManager = serviceManager;
            card.extras = data;
            card.stringType = data.optString(KEY_TYPE);
            card.id = data.optString(KEY_ID, card.id == null ? "" : card.id);

            mvHelper.renderManager().parseComponentInfo(data);

            // parsing header
            JSONObject header = data.optJSONObject(KEY_HEADER);
            BaseCell headerCell = parseSingleComponent(header, card, serviceManager);
            parseHeaderCell(headerCell, card);

            // parsing body
            JSONArray componentArray = data.optJSONArray(KEY_ITEMS);
            if (componentArray != null) {
                final int cellLength = componentArray.length();
                for (int i = 0; i < cellLength; i++) {
                    final JSONObject cellData = componentArray.optJSONObject(i);
                    parseSingleComponent(cellData, card, card.serviceManager);
                }
            }
            // parsing footer
            JSONObject footer = data.optJSONObject(KEY_FOOTER);
            BaseCell footerCell = parseSingleComponent(footer, card, serviceManager);
            parseFooterCell(footerCell, card);

            JSONObject styleJson = data.optJSONObject(KEY_STYLE);

            // parse specific style
            if (styleJson != null) {
                if (card instanceof GridCard) {
                    GridCard gridCard = (GridCard) card;
                    GridCard.GridStyle style = new GridCard.GridStyle();
                    parseStyle(style, styleJson);

                    style.column = styleJson.optInt(KEY_COLUMN, 0);

                    style.autoExpand = styleJson.optBoolean(KEY_AUTO_EXPAND, false);

                    JSONArray jsonCols = styleJson.optJSONArray(KEY_COLS);
                    if (jsonCols != null) {
                        style.cols = new float[jsonCols.length()];
                        for (int i = 0; i < style.cols.length; i++) {
                            style.cols[i] = (float) jsonCols.optDouble(i, 0);
                        }
                    } else {
                        style.cols = new float[0];
                    }

                    style.hGap = Style.parseSize(styleJson.optString(KEY_H_GAP), 0);
                    style.vGap = Style.parseSize(styleJson.optString(KEY_V_GAP), 0);

                    if (style.column > 0) {
                        gridCard.mColumn = style.column;
                    }
                    card = gridCard;
                } else if (card instanceof BannerCard) {
                    BannerCard bannerCard = (BannerCard) card;
                    if (bannerCard.cell == null)
                        bannerCard.cell = new BannerCell();

                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("type", TangramBuilder.TYPE_CAROUSEL_CELL);
                        obj.put("bizId", bannerCard.id);

                        parseSingleComponent(obj, bannerCard, serviceManager);

                        if (!bannerCard.getCells().isEmpty()) {
                            bannerCard.cell.mCells.addAll(bannerCard.getCells());
                            bannerCard.setCells(Collections.<BaseCell>singletonList(bannerCard.cell));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        bannerCard.setCells(null);
                    }

                    bannerCard.cell.setIndicatorRadius(Style.parseSize(styleJson.optString(ATTR_INDICATOR_RADIUS), 0));
                    bannerCard.cell.setIndicatorColor(Style.parseColor(styleJson.optString(ATTR_INDICATOR_COLOR, "#00000000")));
                    bannerCard.cell.setIndicatorDefaultColor(Style.parseColor(styleJson.optString(ATTR_INDICATOR_DEFAULT_INDICATOR_COLOR, "#00000000")));
                    bannerCard.cell.setAutoScrollInternal(styleJson.optInt(ATTR_AUTOSCROLL));
                    bannerCard.cell.setSpecialInterval(styleJson.optJSONObject(ATTR_SPECIAL_INTERVAL));
                    bannerCard.cell.setInfinite(styleJson.optBoolean(ATTR_INFINITE));
                    bannerCard.cell.setInfiniteMinCount(styleJson.optInt(ATTR_INFINITE_MIN_COUNT));
                    bannerCard.cell.setIndicatorFocus(styleJson.optString(ATTR_INDICATOR_FOCUS));
                    bannerCard.cell.setIndicatorNor(styleJson.optString(ATTR_INDICATOR_NORMAL));
                    bannerCard.cell.setIndicatorGravity(styleJson.optString(ATTR_INDICATOR_GRA));
                    bannerCard.cell.setIndicatorPos(styleJson.optString(ATTR_INDICATOR_POS));
                    bannerCard.cell.setIndicatorGap(Style.parseSize(styleJson.optString(ATTR_INDICATOR_GAP), 0));
                    bannerCard.cell.setIndicatorMargin(Style.parseSize(styleJson.optString(ATTR_INDICATOR_MARGIN), 0));
                    bannerCard.cell.setIndicatorHeight(Style.parseSize(styleJson.optString(ATTR_INDICATOR_HEIGHT), 0));
                    bannerCard.cell.setPageWidth(styleJson.optDouble(ATTR_PAGE_WIDTH));
                    bannerCard.cell.sethGap(Style.parseSize(styleJson.optString(ATTR_HGAP), 0));
                    bannerCard.cell.itemRatio = styleJson.optDouble(ATTR_ITEM_RATIO, Double.NaN);
                    bannerCard.cell.itemMargin[0] = Style.parseSize(styleJson.optString(ATTR_ITEM_MARGIN_LEFT), 0);
                    bannerCard.cell.itemMargin[1] = Style.parseSize(styleJson.optString(ATTR_ITEM_MARGIN_RIGHT), 0);
                    Style style = new Style();
                    parseStyle(style, styleJson);
                    card.style = style;
                    bannerCard.cell.setRatio(style.aspectRatio);
                    bannerCard.cell.margin = style.margin;
                    bannerCard.cell.height = style.height;
                } else if (card instanceof OnePlusNCard) {
                    ColumnStyle style = new ColumnStyle();
                    parseStyle(style, styleJson);

                    JSONArray jsonCols = styleJson.optJSONArray(KEY_COLS);
                    if (jsonCols != null) {
                        style.cols = new float[jsonCols.length()];
                        for (int i = 0; i < style.cols.length; i++) {
                            style.cols[i] = (float) jsonCols.optDouble(i, 0);
                        }
                    } else {
                        style.cols = new float[0];
                    }

                    JSONArray jsonRows = styleJson.optJSONArray(KEY_ROWS);
                    if (jsonRows != null) {
                        style.rows = new float[jsonRows.length()];
                        for (int i = 0; i < style.rows.length; i++) {
                            style.rows[i] = (float) jsonRows.optDouble(i, 0);
                        }
                    } else {
                        style.rows = new float[0];
                    }
                    card.style = style;
                } else if (card instanceof FixLinearScrollCard) {
                    FixLinearScrollCard fixLinearScrollCard = (FixLinearScrollCard) card;
                    FixCard.FixStyle fixStyle = new FixCard.FixStyle();
                    parseStyle(fixStyle, styleJson);
                    String showTypeStr = styleJson.optString(KEY_SHOW_TYPE, "top_left").toLowerCase();

                    String align = styleJson.optString(KEY_ALIGN, "always").toLowerCase();

                    fixStyle.sketchMeasure = styleJson.optBoolean(KEY_SKETCH_MEASURE, true);

                    if ("showonenter".equals(showTypeStr)) {
                        fixStyle.showType = SHOW_ON_ENTER;
                    } else if ("showonleave".equals(showTypeStr)) {
                        fixStyle.showType = SHOW_ON_LEAVE;
                    } else if ("always".equals(showTypeStr)) {
                        fixStyle.showType = SHOW_ALWAYS;
                    }

                    if ("top_left".equals(align)) {
                        fixStyle.alignType = TOP_LEFT;
                    } else if ("top_right".equals(align)) {
                        fixStyle.alignType = TOP_RIGHT;
                    } else if ("bottom_left".equals(align)) {
                        fixStyle.alignType = BOTTOM_LEFT;
                    } else if ("bottom_right".equals(align)) {
                        fixStyle.alignType = BOTTOM_RIGHT;
                    }

                    fixStyle.x = Style.parseSize(styleJson.optString(KEY_X), 0);
                    fixStyle.y = Style.parseSize(styleJson.optString(KEY_Y), 0);
                    fixLinearScrollCard.mFixStyle = fixStyle;
                } else if (card instanceof StickyEndCard) {
                    StickyCard.StickyStyle stickyStyle = new StickyCard.StickyStyle(false);
                    stickyStyle.offset = Style.parseSize(styleJson.optString("offset"), 0);
                    card.style = stickyStyle;
                } else if (card instanceof StickyCard) {
                    StickyCard.StickyStyle stickyStyle = new StickyCard.StickyStyle(true);
                    String sticky = styleJson.optString(KEY_STICKY, stickyStyle.stickyStart ? STICKY_START : STICKY_END);
                    stickyStyle.stickyStart = STICKY_START.equalsIgnoreCase(sticky);
                    stickyStyle.offset = Style.parseSize(styleJson.optString("offset"), 0);
                    card.style = stickyStyle;
                } else if (card instanceof FixCard) {
                    FixCard.FixStyle fixStyle = new FixCard.FixStyle();
                    parseStyle(fixStyle, styleJson);
                    String showTypeStr = styleJson.optString(KEY_SHOW_TYPE, "top_left").toLowerCase();

                    String align = styleJson.optString(KEY_ALIGN, "always").toLowerCase();

                    fixStyle.sketchMeasure = styleJson.optBoolean(KEY_SKETCH_MEASURE, true);

                    if ("showonenter".equals(showTypeStr)) {
                        fixStyle.showType = SHOW_ON_ENTER;
                    } else if ("showonleave".equals(showTypeStr)) {
                        fixStyle.showType = SHOW_ON_LEAVE;
                    } else if ("always".equals(showTypeStr)) {
                        fixStyle.showType = SHOW_ALWAYS;
                    }

                    if ("top_left".equals(align)) {
                        fixStyle.alignType = TOP_LEFT;
                    } else if ("top_right".equals(align)) {
                        fixStyle.alignType = TOP_RIGHT;
                    } else if ("bottom_left".equals(align)) {
                        fixStyle.alignType = BOTTOM_LEFT;
                    } else if ("bottom_right".equals(align)) {
                        fixStyle.alignType = BOTTOM_RIGHT;
                    }

                    fixStyle.x = Style.parseSize(styleJson.optString(KEY_X), 0);
                    fixStyle.y = Style.parseSize(styleJson.optString(KEY_Y), 0);
                    card.style = fixStyle;
                } else if (card instanceof LinearScrollCard) {
                    LinearScrollCard linearScrollCard = (LinearScrollCard) card;
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("type", TangramBuilder.TYPE_LINEAR_SCROLL_CELL);
                        obj.put("bizId", linearScrollCard.id);

                        parseSingleComponent(obj, linearScrollCard, serviceManager);

                        if (!linearScrollCard.getCells().isEmpty()) {
                            linearScrollCard.cell.cells.addAll(linearScrollCard.getCells());
                            linearScrollCard.setCells(Collections.<BaseCell>singletonList(linearScrollCard.cell));
                        }
                    } catch (Exception e) {
                        linearScrollCard.setCells(null);
                    }

                    linearScrollCard.cell.pageWidth = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_PAGE_WIDTH), 0);
                    linearScrollCard.cell.pageHeight = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_PAGE_HEIGHT), 0);
                    linearScrollCard.cell.defaultIndicatorColor = Style.parseColor(styleJson.optString(LinearScrollCell.KEY_DEFAULT_INDICATOR_COLOR),
                            LinearScrollCell.DEFAULT_DEFAULT_INDICATOR_COLOR);
                    linearScrollCard.cell.indicatorColor = Style.parseColor(styleJson.optString(LinearScrollCell.KEY_INDICATOR_COLOR),
                            LinearScrollCell.DEFAULT_INDICATOR_COLOR);
                    if (styleJson.has(LinearScrollCell.KEY_HAS_INDICATOR)) {
                        linearScrollCard.cell.hasIndicator = styleJson.optBoolean(LinearScrollCell.KEY_HAS_INDICATOR);
                    }
                    linearScrollCard.cell.indicatorHeight = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_INDICATOR_HEIGHT), LinearScrollCell.DEFAULT_INDICATOR_HEIGHT);
                    linearScrollCard.cell.indicatorWidth = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_INDICATOR_WIDTH), LinearScrollCell.DEFAULT_INDICATOR_WIDTH);
                    linearScrollCard.cell.defaultIndicatorWidth = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_DEFAULT_INDICATOR_WIDTH), LinearScrollCell.DEFAULT_DEFAULT_INDICATOR_WIDTH);
                    linearScrollCard.cell.indicatorMargin = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_INDICATOR_MARGIN), LinearScrollCell.DEFAULT_INDICATOR_MARGIN);
                    if (styleJson.has(LinearScrollCell.KEY_FOOTER_TYPE)) {
                        linearScrollCard.cell.footerType = styleJson.optString(LinearScrollCell.KEY_FOOTER_TYPE);
                    }
                    linearScrollCard.cell.bgColor = Style.parseColor(styleJson.optString(KEY_BG_COLOR), Color.TRANSPARENT);
                    linearScrollCard.cell.retainScrollState = styleJson.optBoolean(LinearScrollCell.KEY_RETAIN_SCROLL_STATE, true);
                    linearScrollCard.cell.scrollMarginLeft = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_SCROLL_MARGIN_LEFT), 0);
                    linearScrollCard.cell.scrollMarginRight = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_SCROLL_MARGIN_RIGHT), 0);
                    linearScrollCard.cell.hGap = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_HGAP), 0);
                    linearScrollCard.cell.vGap = Style.parseSize(styleJson.optString(LinearScrollCell.KEY_VGAP), 0);
                    linearScrollCard.cell.maxRows = styleJson.optInt(LinearScrollCell.KEY_MAX_ROWS, LinearScrollCell.DEFAULT_MAX_ROWS);
                    linearScrollCard.cell.maxCols = styleJson.optInt(LinearScrollCell.KEY_MAX_COLS, 0);

                    Style style = new Style();
                    parseStyle(style, styleJson);
                    linearScrollCard.style = style;
                    card = linearScrollCard;
                } else if (card instanceof StaggeredCard) {
                    StaggeredCard staggeredCard = (StaggeredCard) card;
                    StaggeredCard.StaggeredStyle style = new StaggeredCard.StaggeredStyle();
                    parseStyle(style, styleJson);
                    style.column = styleJson.optInt(KEY_COLUMN, 2);

                    style.hGap = Style.parseSize(styleJson.optString(KEY_H_GAP), 0);
                    style.vGap = Style.parseSize(styleJson.optString(KEY_V_GAP), 0);

                    staggeredCard.style = style;
                } else {
                    Style style = new Style();
                    parseStyle(style, styleJson);
                    card.style = style;
                }
            }

            if (card.isValid()) {
                if (card.style != null && card.style.slidable) {
                    return new SlideCard(card);
                } else {
                    return card;
                }
            }
        } else {
            LogUtils.w(TAG, "Invalid card type when parse JSON data");
        }
        return Card.NaN;
    }

    @NonNull
    @Override
    public BaseCell parseSingleComponent(@Nullable JSONObject data, Card parent, ServiceManager serviceManager) {
        if (data == null) {
            return BaseCell.NaN;
        }

        checkCardResolverAndMVHelper(serviceManager);

        BaseCell cell = createCell(parent, mvHelper, data, serviceManager);

        if (mvHelper.isValid(cell, serviceManager)) {
            return cell;
        } else {
            return BaseCell.NaN;
        }
    }

    @NonNull
    @Override
    public <T extends Style> T parseStyle(@NonNull T style, @Nullable JSONObject data) {
        if (data == null) {
            return style;
        }

        style.extras = data;

        style.forLabel = data.optString(KEY_FOR_LABEL, "");

        style.setBgColor(data.optString(KEY_BG_COLOR, Style.DEFAULT_BG_COLOR));
        String backgroundColor = data.optString(KEY_BACKGROUND_COLOR);
        if (!TextUtils.isEmpty(backgroundColor)) {
            style.setBgColor(backgroundColor);
        }

        if (data.has(KEY_WIDTH)) {
            String widthValue = data.optString(KEY_WIDTH);
            style.width = style.parseSize(widthValue, VirtualLayoutManager.LayoutParams.MATCH_PARENT);
        }
        if (data.has(KEY_HEIGHT)) {
            String heightValue = data.optString(KEY_HEIGHT);
            style.height = style.parseSize(heightValue, VirtualLayoutManager.LayoutParams.WRAP_CONTENT);
        }

        style.bgImage = data.optString(KEY_BG_IMAGE, "");
        style.bgImgUrl = data.optString(KEY_STYLE_BG_IMAGE, "");

        String backgroundImage = data.optString(KEY_BACKGROUND_IMAGE, "");

        if (!TextUtils.isEmpty(backgroundImage)) {
            style.bgImage = backgroundImage;
            style.bgImgUrl = backgroundImage;
        }

        style.aspectRatio = (float) data.optDouble(KEY_ASPECT_RATIO);

        double ratio = data.optDouble(KEY_RATIO);
        if (!Double.isNaN(ratio)) {
            style.aspectRatio = (float) ratio;
        }

        style.zIndex = data.optInt(KEY_ZINDEX, 0);

        style.slidable = data.optBoolean(KEY_SLIDABLE);

        JSONArray marginArray = data.optJSONArray(KEY_MARGIN);
        if (marginArray != null) {
            int size = Math.min(style.margin.length, marginArray.length());
            for (int i = 0; i < size; i++) {
                style.margin[i] = style.parseSize(marginArray.optString(i), 0);
            }

            if (size > 0) {
                Arrays.fill(style.margin, size, style.margin.length, style.margin[size - 1]);
            }
        } else {
            String marginString = data.optString(KEY_MARGIN);
            if (!TextUtils.isEmpty(marginString)) {
                style.setMargin(marginString);
            }
        }

        JSONArray paddingArray = data.optJSONArray(KEY_PADDING);
        if (paddingArray != null) {
            int size = Math.min(style.padding.length, paddingArray.length());
            for (int i = 0; i < size; i++) {
                style.padding[i] = style.parseSize(paddingArray.optString(i), 0);
            }

            if (size > 0) {
                Arrays.fill(style.padding, size, style.padding.length, style.padding[size - 1]);
            }
        } else {
            String paddingString = data.optString(KEY_PADDING);
            if (!TextUtils.isEmpty(paddingString)) {
                style.setPadding(paddingString);
            }
        }
        return style;
    }

    private BaseCell createCell(@Nullable Card parent, @NonNull MVHelper resolver, @NonNull JSONObject cellData,
                                @NonNull ServiceManager serviceManager) {
        BaseCell cell = null;
        String cellType = cellData.optString(KEY_TYPE);
        if ((resolver.resolver().getViewClass(cellType) != null) || Utils.isCard(cellData)) {

            if (Utils.isCard(cellData)) {
                switch (cellType) {
                    case TangramBuilder.TYPE_CONTAINER_FLOW:
                    case TangramBuilder.TYPE_CONTAINER_1C_FLOW:
                    case TangramBuilder.TYPE_CONTAINER_2C_FLOW:
                    case TangramBuilder.TYPE_CONTAINER_3C_FLOW:
                    case TangramBuilder.TYPE_CONTAINER_4C_FLOW:
                    case TangramBuilder.TYPE_CONTAINER_5C_FLOW: {
                        Card card = parseSingleGroup(cellData, serviceManager);
                        parent.addChildCard(card);
                        break;
                    }
                    case TangramBuilder.TYPE_CONTAINER_BANNER:
                    case TangramBuilder.TYPE_CONTAINER_SCROLL: {
                        Card card = parseSingleGroup(cellData, serviceManager);
                        List<BaseCell> children = card.getCells();
                        if (children.size() > 0) {
                            cell = card.getCells().get(0);
                        }
                        break;
                    }
                }
                if (cell != null) {
                    cell.serviceManager = serviceManager;
                    if (parent != null) {
                        cell.parent = parent;
                        cell.parentId = parent.id;
                    }
                } else {
                    return BaseCell.NaN;
                }
            } else {
                cell = new BaseCell(cellType);
                cell.serviceManager = serviceManager;
                if (parent != null) {
                    cell.parent = parent;
                    cell.parentId = parent.id;
                }
            }

            parseCell(cell, cellData);
            if (parent != null) {
                boolean ret = parent.addCellInternal(cell, false);
                if (!ret && TangramBuilder.isPrintLog()) {
                    LogUtils.w(TAG, "Parse invalid cell with data: " + cellData.toString());
                }
            }
            return cell;
        } else {
            BaseCellBinderResolver componentBinderResolver = serviceManager.getService(BaseCellBinderResolver.class);
            // if cell type not register and has component info, register it!
            if (!componentBinderResolver.has(cellType) && resolver.renderManager().getComponentInfo(cellType) != null) {
                componentBinderResolver.register(cellType, new BaseCellBinder<>(cellType, resolver));
            }
            if (componentBinderResolver.has(cellType)) {
                cell = new BaseCell(cellType);
                cell.componentInfo = resolver.renderManager().getComponentInfo(cellType);
                cell.serviceManager = serviceManager;
                if (parent != null) {
                    cell.parent = parent;
                    cell.parentId = parent.id;
                    parseCell(cell, cellData);
                    boolean ret = parent.addCellInternal(cell, false);
                    if (!ret && TangramBuilder.isPrintLog()) {
                        LogUtils.w(TAG, "Parse invalid cell with data: " + cellData.toString());
                    }
                } else {
                    parseCell(cell, cellData);
                }
                cell.setStringType(cellType);
                return cell;
            } else {
                return BaseCell.NaN;
            }
        }
    }

    private void parseCell(BaseCell cell, JSONObject json) {
        if (json != null) {
            cell.extras = json;
            cell.id = json.optString(KEY_BIZ_ID);
            if (TextUtils.isEmpty(cell.id) && json.has(KEY_ID)) {
                cell.id = json.optString(KEY_ID);
            }
            cell.stringType = json.optString(KEY_TYPE);
            cell.typeKey = json.optString(KEY_TYPE_KEY);
            String reuseId = json.optString(KEY_TYPE_REUSEID);
            if (!TextUtils.isEmpty(reuseId)) {
                cell.typeKey = reuseId;
            }
            cell.position = json.optInt(KEY_POSITION, -1);
            JSONObject styleJson = json.optJSONObject(KEY_STYLE);
            Style style = new Style();
            cell.style = parseStyle(style, styleJson);
        } else {
            cell.extras = new JSONObject();
        }
    }

    private void parseHeaderCell(BaseCell headerCell, Card card) {
        card.mHeader = headerCell;
        if (card instanceof GridCard) {
            GridCard gridCard = (GridCard) card;
            gridCard.ensureBlock(card.mHeader);
        } else if (card instanceof OnePlusNCard) {
            OnePlusNCard onePlusNCard = (OnePlusNCard) card;
            onePlusNCard.ensureBlock(card.mHeader);
        }
    }

    private void parseFooterCell(BaseCell footerCell, Card card) {
        card.mFooter = footerCell;
        if (card instanceof GridCard) {
            GridCard gridCard = (GridCard) card;
            gridCard.ensureBlock(card.mFooter);
        } else if (card instanceof OnePlusNCard) {
            OnePlusNCard onePlusNCard = (OnePlusNCard) card;
            onePlusNCard.ensureBlock(card.mFooter);
        }
    }

    private void checkCardResolverAndMVHelper(ServiceManager serviceManager) {
        if (cardResolver == null) {
            cardResolver = serviceManager.getService(CardResolver.class);
            Preconditions.checkState(cardResolver != null, "Must register CardResolver into ServiceManager first");
        }
        if (mvHelper == null) {
            mvHelper = serviceManager.getService(MVHelper.class);
            Preconditions.checkState(mvHelper != null, "Must register CellResolver into ServiceManager first");
        }
    }
}
