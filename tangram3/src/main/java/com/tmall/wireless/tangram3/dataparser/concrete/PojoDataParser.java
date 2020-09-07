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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.tmall.wireless.tangram3.support.InternalErrorSupport;
import com.tmall.wireless.tangram3.util.LogUtils;
import com.tmall.wireless.tangram3.util.Preconditions;
import com.tmall.wireless.tangram3.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.android.vlayout.layout.FixLayoutHelper.BOTTOM_LEFT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.BOTTOM_RIGHT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.TOP_LEFT;
import static com.alibaba.android.vlayout.layout.FixLayoutHelper.TOP_RIGHT;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ALWAYS;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ON_ENTER;
import static com.alibaba.android.vlayout.layout.ScrollFixLayoutHelper.SHOW_ON_LEAVE;
import static com.tmall.wireless.tangram3.TangramBuilder.TYPE_CAROUSEL_CELL_COMPACT;

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

    public static final String KEY_API_LOAD_PARAMS = "loadParams";

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

    public static final String COMPONENTINFO = "componentInfo";

    private CardResolver cardResolver;

    protected MVHelper mvHelper;

    private InternalErrorSupport errorSupport;

    @NonNull
    @Override
    public List<Card> parseGroup(@Nullable JSONArray data, @NonNull final ServiceManager serviceManager) {
        if (data == null) {
            return new ArrayList<>();
        }
        try {
            checkCardResolverAndMVHelper(serviceManager);
            final int size = data.size();
            final List<Card> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                JSONObject cardData = data.getJSONObject(i);
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
        } catch (Exception e) {
            if (errorSupport == null) {
                errorSupport = serviceManager.getService(InternalErrorSupport.class);
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("exception", Log.getStackTraceString(e));
            errorSupport.onError(InternalErrorSupport.ERROR_PARSE_CARDS_ERROR,
                    "Parse group error.", params);
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    @Override
    public List<BaseCell> parseComponent(@Nullable JSONArray data, Card parent, ServiceManager serviceManager, Map<String, ComponentInfo> componentInfoMap) {
        if (data == null) {
            return new ArrayList<>();
        }
        final int size = data.size();
        final List<BaseCell> result = new ArrayList<>(size);

        //parse body
        final int cellLength = data.size();
        for (int i = 0; i < cellLength; i++) {
            final JSONObject cellData = data.getJSONObject(i);
            BaseCell cell = parseSingleComponent(cellData, parent, serviceManager, componentInfoMap);
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

        String cardType = parseCardType(data);
        if (!TextUtils.isEmpty(cardType)) {
            Card card = cardResolver.create(cardType);
            if (card == null) {
                card = new WrapCellCard();
                cardType = TangramBuilder.TYPE_CONTAINER_1C_FLOW;

                JSONObject wrapCardJson = new JSONObject();
                wrapCardJson.put("type", TangramBuilder.TYPE_CONTAINER_1C_FLOW);
                JSONArray itemArray = new JSONArray();
                itemArray.add(data);
                wrapCardJson.put("items", itemArray);
                data = wrapCardJson;
            }

            card.dataParser = this;
            card.serviceManager = serviceManager;
            card.extras = data;
            card.stringType = cardType;
            card.loadParams = data.getJSONObject(KEY_API_LOAD_PARAMS);

            Map<String, ComponentInfo> infoMap = parseComponentInfo(data);

            parseCard(card, data, serviceManager, infoMap);

            JSONObject styleJson = data.getJSONObject(KEY_STYLE);

            // parse specific style
            if (card instanceof GridCard) {
                GridCard gridCard = (GridCard) card;
                GridCard.GridStyle style = new GridCard.GridStyle();
                if (styleJson != null) {
                    parseStyle(style, styleJson);

                    style.column = styleJson.getIntValue(KEY_COLUMN);

                    style.autoExpand = styleJson.getBooleanValue(KEY_AUTO_EXPAND);

                    JSONArray jsonCols = styleJson.getJSONArray(KEY_COLS);
                    if (jsonCols != null) {
                        style.cols = new float[jsonCols.size()];
                        for (int i = 0; i < style.cols.length; i++) {
                            style.cols[i] = (float) jsonCols.getDoubleValue(i);
                        }
                    } else {
                        style.cols = new float[0];
                    }

                    style.hGap = Style.parseSize(styleJson.getString(KEY_H_GAP), 0);
                    style.vGap = Style.parseSize(styleJson.getString(KEY_V_GAP), 0);

                    if (style.column > 0) {
                        gridCard.mColumn = style.column;
                    }

                    for (BaseCell cell : card.mCells) {
                        if (cell.style.extras != null) {
                            int colSpan = cell.style.extras.getIntValue("colspan");
                            if (colSpan == 0) {
                                colSpan = 1;
                            }
                            cell.colSpan = colSpan;
                        }
                    }
                }

                card = gridCard;
                card.style = style;
            } else if (card instanceof BannerCard) {
                BannerCard bannerCard = (BannerCard) card;
                if (bannerCard.cell == null) {
                    bannerCard.cell = new BannerCell();
                    bannerCard.cell.serviceManager = serviceManager;
                }

                try {
                    bannerCard.cell.stringType = TYPE_CAROUSEL_CELL_COMPACT;

                    if (!bannerCard.getCells().isEmpty()) {
                        bannerCard.cell.mCells.addAll(bannerCard.getCells());
                        bannerCard.setCells(bannerCard.getCells());
                    }
                } catch (Exception e) {
                    if (errorSupport == null) {
                        errorSupport = serviceManager.getService(InternalErrorSupport.class);
                    }
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("exception", Log.getStackTraceString(e));
                    errorSupport.onError(InternalErrorSupport.ERROR_PARSE_CARDS_ERROR,
                            "Parse banner card error.", params);
                    e.printStackTrace();
                    bannerCard.setCells(null);
                }

                Style style = new Style();
                if (styleJson != null) {
                    bannerCard.cell.setIndicatorRadius(Style.parseSize(styleJson.getString(ATTR_INDICATOR_RADIUS), 0));
                    bannerCard.cell.setIndicatorColor(Style.parseColor(styleJson.getString(ATTR_INDICATOR_COLOR), Color.TRANSPARENT));
                    bannerCard.cell.setIndicatorDefaultColor(Style.parseColor(styleJson.getString(ATTR_INDICATOR_DEFAULT_INDICATOR_COLOR), Color.TRANSPARENT));
                    bannerCard.cell.setAutoScrollInternal(styleJson.getIntValue(ATTR_AUTOSCROLL));
                    bannerCard.cell.setSpecialInterval(styleJson.getJSONObject(ATTR_SPECIAL_INTERVAL));
                    bannerCard.cell.setInfinite(styleJson.getBooleanValue(ATTR_INFINITE));
                    bannerCard.cell.setInfiniteMinCount(styleJson.getIntValue(ATTR_INFINITE_MIN_COUNT));
                    bannerCard.cell.setIndicatorFocus(styleJson.getString(ATTR_INDICATOR_FOCUS));
                    bannerCard.cell.setIndicatorNor(styleJson.getString(ATTR_INDICATOR_NORMAL));
                    bannerCard.cell.setIndicatorGravity(styleJson.getString(ATTR_INDICATOR_GRA));
                    bannerCard.cell.setIndicatorPos(styleJson.getString(ATTR_INDICATOR_POS));
                    bannerCard.cell.setIndicatorGap(Style.parseSize(styleJson.getString(ATTR_INDICATOR_GAP), Style.dp2px(6)));
                    bannerCard.cell.setIndicatorMargin(Style.parseSize(styleJson.getString(ATTR_INDICATOR_MARGIN), 0));
                    bannerCard.cell.setIndicatorHeight(Style.parseSize(styleJson.getString(ATTR_INDICATOR_HEIGHT), 0));
                    bannerCard.cell.setPageWidth(Utils.getJsonFloatValue(styleJson, ATTR_PAGE_WIDTH));
                    bannerCard.cell.sethGap(Style.parseSize(styleJson.getString(ATTR_HGAP), 0));
                    bannerCard.cell.itemRatio = Utils.getJsonDoubleValue(styleJson, ATTR_ITEM_RATIO);
                    bannerCard.cell.itemMargin[0] = Style.parseSize(styleJson.getString(ATTR_ITEM_MARGIN_LEFT), 0);
                    bannerCard.cell.itemMargin[1] = Style.parseSize(styleJson.getString(ATTR_ITEM_MARGIN_RIGHT), 0);
                    parseStyle(style, styleJson);
                }
                card.style = style;
                bannerCard.cell.setRatio(style.aspectRatio);
                bannerCard.cell.margin = style.margin;
                bannerCard.cell.height = style.height;
            } else if (card instanceof OnePlusNCard) {
                ColumnStyle style = new ColumnStyle();
                if (styleJson != null) {
                    parseStyle(style, styleJson);

                    JSONArray jsonCols = styleJson.getJSONArray(KEY_COLS);
                    if (jsonCols != null) {
                        style.cols = new float[jsonCols.size()];
                        for (int i = 0; i < style.cols.length; i++) {
                            style.cols[i] = (float) jsonCols.getDoubleValue(i);
                        }
                    } else {
                        style.cols = new float[0];
                    }

                    JSONArray jsonRows = styleJson.getJSONArray(KEY_ROWS);
                    if (jsonRows != null) {
                        style.rows = new float[jsonRows.size()];
                        for (int i = 0; i < style.rows.length; i++) {
                            style.rows[i] = (float) jsonRows.getDoubleValue(i);
                        }
                    } else {
                        style.rows = new float[0];
                    }
                }
                card.style = style;
            } else if (card instanceof FixLinearScrollCard) {
                FixLinearScrollCard fixLinearScrollCard = (FixLinearScrollCard) card;
                FixCard.FixStyle fixStyle = new FixCard.FixStyle();
                if (styleJson != null) {
                    parseStyle(fixStyle, styleJson);
                    String showTypeStr = styleJson.getString(KEY_SHOW_TYPE);
                    if (TextUtils.isEmpty(showTypeStr)) {
                        showTypeStr = "top_left";
                    } else {
                        showTypeStr = showTypeStr.toLowerCase();
                    }

                    String align = styleJson.getString(KEY_ALIGN);
                    if (TextUtils.isEmpty(align)) {
                        align = "always";
                    } else {
                        align = align.toLowerCase();
                    }

                    Boolean sketchMeasure = styleJson.getBoolean(KEY_SKETCH_MEASURE);
                    if (sketchMeasure == null) {
                        fixStyle.sketchMeasure = true;
                    } else {
                        fixStyle.sketchMeasure = sketchMeasure;
                    }

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

                    fixStyle.x = Style.parseSize(styleJson.getString(KEY_X), 0);
                    fixStyle.y = Style.parseSize(styleJson.getString(KEY_Y), 0);
                }
                fixLinearScrollCard.mFixStyle = fixStyle;
            } else if (card instanceof StickyEndCard) {
                StickyCard.StickyStyle stickyStyle = new StickyCard.StickyStyle(false);
                if (styleJson != null) {
                    stickyStyle.offset = Style.parseSize(styleJson.getString("offset"), 0);
                }
                card.style = stickyStyle;
            } else if (card instanceof StickyCard) {
                StickyCard.StickyStyle stickyStyle = new StickyCard.StickyStyle(true);
                if (styleJson != null) {
                    String sticky = styleJson.getString(KEY_STICKY);
                    if (TextUtils.isEmpty(sticky)) {
                        sticky = stickyStyle.stickyStart ? STICKY_START : STICKY_END;
                    }
                    stickyStyle.stickyStart = STICKY_START.equalsIgnoreCase(sticky);
                    stickyStyle.offset = Style.parseSize(styleJson.getString("offset"), 0);
                }
                card.style = stickyStyle;
            } else if (card instanceof FixCard) {
                FixCard.FixStyle fixStyle = new FixCard.FixStyle();
                if (styleJson != null) {
                    parseStyle(fixStyle, styleJson);
                    String showTypeStr = styleJson.getString(KEY_SHOW_TYPE);
                    if (TextUtils.isEmpty(showTypeStr)) {
                        showTypeStr = "top_left";
                    } else {
                        showTypeStr = showTypeStr.toLowerCase();
                    }

                    String align = styleJson.getString(KEY_ALIGN);
                    if (TextUtils.isEmpty(align)) {
                        align = "always";
                    } else {
                        align = align.toLowerCase();
                    }

                    Boolean sketchMeasure = styleJson.getBoolean(KEY_SKETCH_MEASURE);
                    if (sketchMeasure == null) {
                        fixStyle.sketchMeasure = true;
                    } else {
                        fixStyle.sketchMeasure = sketchMeasure;
                    }

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

                    fixStyle.x = Style.parseSize(styleJson.getString(KEY_X), 0);
                    fixStyle.y = Style.parseSize(styleJson.getString(KEY_Y), 0);
                }
                card.style = fixStyle;
            } else if (card instanceof LinearScrollCard) {
                LinearScrollCard linearScrollCard = (LinearScrollCard) card;
                try {
                    linearScrollCard.cell.stringType = TangramBuilder.TYPE_LINEAR_SCROLL_CELL_COMPACT;
                    linearScrollCard.cell.serviceManager = serviceManager;

                    if (!linearScrollCard.getCells().isEmpty()) {
                        linearScrollCard.cell.cells.addAll(linearScrollCard.getCells());
                        linearScrollCard.setCells(linearScrollCard.getCells());
                    }
                } catch (Exception e) {
                    if (errorSupport == null) {
                        errorSupport = serviceManager.getService(InternalErrorSupport.class);
                    }
                    HashMap<String, Object> params = new HashMap<>();
                    params.put("exception", Log.getStackTraceString(e));
                    errorSupport.onError(InternalErrorSupport.ERROR_PARSE_CARDS_ERROR,
                            "Parse linear scroll card error.", params);
                    e.printStackTrace();
                    linearScrollCard.setCells(null);
                }

                Style style = new Style();
                if (styleJson != null) {
                    linearScrollCard.cell.pageWidth = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_PAGE_WIDTH), 0);
                    linearScrollCard.cell.pageHeight = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_PAGE_HEIGHT), 0);
                    linearScrollCard.cell.defaultIndicatorColor = Style.parseColor(styleJson.getString(LinearScrollCell.KEY_DEFAULT_INDICATOR_COLOR),
                            LinearScrollCell.DEFAULT_DEFAULT_INDICATOR_COLOR);
                    linearScrollCard.cell.indicatorColor = Style.parseColor(styleJson.getString(LinearScrollCell.KEY_INDICATOR_COLOR),
                            LinearScrollCell.DEFAULT_INDICATOR_COLOR);
                    if (styleJson.containsKey(LinearScrollCell.KEY_HAS_INDICATOR)) {
                        linearScrollCard.cell.hasIndicator = styleJson.getBooleanValue(LinearScrollCell.KEY_HAS_INDICATOR);
                    }
                    linearScrollCard.cell.indicatorHeight = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_INDICATOR_HEIGHT), LinearScrollCell.DEFAULT_INDICATOR_HEIGHT);
                    linearScrollCard.cell.indicatorWidth = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_INDICATOR_WIDTH), LinearScrollCell.DEFAULT_INDICATOR_WIDTH);
                    linearScrollCard.cell.defaultIndicatorWidth = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_DEFAULT_INDICATOR_WIDTH), LinearScrollCell.DEFAULT_DEFAULT_INDICATOR_WIDTH);
                    linearScrollCard.cell.indicatorMargin = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_INDICATOR_MARGIN), LinearScrollCell.DEFAULT_INDICATOR_MARGIN);
                    linearScrollCard.cell.indicatorRadius = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_INDICATOR_RADIUS), LinearScrollCell.DEFAULT_INDICATOR_RADIUS);
                    if (styleJson.containsKey(LinearScrollCell.KEY_FOOTER_TYPE)) {
                        linearScrollCard.cell.footerType = styleJson.getString(LinearScrollCell.KEY_FOOTER_TYPE);
                    }
                    linearScrollCard.cell.bgColor = Style.parseColor(styleJson.getString(KEY_BG_COLOR), Color.TRANSPARENT);
                    Boolean retainScrollState = styleJson.getBoolean(LinearScrollCell.KEY_RETAIN_SCROLL_STATE);
                    if (retainScrollState == null) {
                        linearScrollCard.cell.retainScrollState = true;
                    } else {
                        linearScrollCard.cell.retainScrollState = retainScrollState;
                    }
                    linearScrollCard.cell.scrollMarginLeft = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_SCROLL_MARGIN_LEFT), 0);
                    linearScrollCard.cell.scrollMarginRight = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_SCROLL_MARGIN_RIGHT), 0);
                    linearScrollCard.cell.hGap = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_HGAP), 0);
                    linearScrollCard.cell.vGap = Style.parseSize(styleJson.getString(LinearScrollCell.KEY_VGAP), 0);
                    linearScrollCard.cell.nativeBackgroundImage = styleJson.getString(LinearScrollCell.KEY_NATIVE_BG_IMAGE);
                    Integer maxRows = styleJson.getInteger(LinearScrollCell.KEY_MAX_ROWS);
                    if (maxRows == null) {
                        maxRows = LinearScrollCell.DEFAULT_MAX_ROWS;
                    }
                    linearScrollCard.cell.maxRows = maxRows;
                    try {
                        linearScrollCard.cell.maxCols = (int) styleJson.getDoubleValue(LinearScrollCell.KEY_MAX_COLS);
                    } catch (Exception e) {
                        if (errorSupport == null) {
                            errorSupport = serviceManager.getService(InternalErrorSupport.class);
                        }
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("exception", Log.getStackTraceString(e));
                        errorSupport.onError(InternalErrorSupport.ERROR_PARSE_CARDS_ERROR,
                                "Parse linear scroll card max cols error.", params);
                        e.printStackTrace();
                    }
                    parseStyle(style, styleJson);
                }

                linearScrollCard.style = style;
                card = linearScrollCard;
            } else if (card instanceof StaggeredCard) {
                StaggeredCard staggeredCard = (StaggeredCard) card;
                StaggeredCard.StaggeredStyle style = new StaggeredCard.StaggeredStyle();
                if (styleJson != null) {
                    parseStyle(style, styleJson);
                    Integer column = styleJson.getInteger(KEY_COLUMN);
                    if (column == null) {
                        column = 2;
                    }
                    style.column = column;

                    style.hGap = Style.parseSize(styleJson.getString(KEY_H_GAP), 0);
                    style.vGap = Style.parseSize(styleJson.getString(KEY_V_GAP), 0);
                }
                staggeredCard.style = style;
            } else if (isCustomCard(card.stringType)) {
                parseCustomCard(card, data, serviceManager, infoMap);
            } else {
                Style style = new Style();
                parseStyle(style, styleJson);
                card.style = style;
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
    public BaseCell parseSingleComponent(@Nullable JSONObject data, Card parent, ServiceManager serviceManager, Map<String, ComponentInfo> componentInfoMap) {
        if (data == null) {
            return BaseCell.NaN;
        }

        checkCardResolverAndMVHelper(serviceManager);

        String cellType = parseCellType(data);
        if (componentInfoMap == null) {
            componentInfoMap = new HashMap<>();
        }
        ComponentInfo componentInfo = mvHelper.renderManager().supplementComponentInfo(cellType);
        if (componentInfo != null) {
            componentInfoMap.put(cellType, componentInfo);
        }

        BaseCell cell = createCell(parent, mvHelper, data, serviceManager, componentInfoMap);

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

        style.forLabel = data.getString(KEY_FOR_LABEL);

        String bgColor = data.getString(KEY_BG_COLOR);
        if (TextUtils.isEmpty(bgColor)) {
            style.setBgColor(Style.DEFAULT_BG_COLOR);
        } else {
            style.setBgColor(bgColor);
        }
        String backgroundColor = data.getString(KEY_BACKGROUND_COLOR);
        if (!TextUtils.isEmpty(backgroundColor)) {
            style.setBgColor(backgroundColor);
        }

        if (data.containsKey(KEY_WIDTH)) {
            String widthValue = data.getString(KEY_WIDTH);
            style.width = style.parseSize(widthValue, VirtualLayoutManager.LayoutParams.MATCH_PARENT);
        }
        if (data.containsKey(KEY_HEIGHT)) {
            String heightValue = data.getString(KEY_HEIGHT);
            style.height = style.parseSize(heightValue, VirtualLayoutManager.LayoutParams.WRAP_CONTENT);
        }

        style.bgImage = data.getString(KEY_BG_IMAGE);
        style.bgImgUrl = data.getString(KEY_STYLE_BG_IMAGE);

        String backgroundImage = data.getString(KEY_BACKGROUND_IMAGE);

        if (!TextUtils.isEmpty(backgroundImage)) {
            style.bgImage = backgroundImage;
            style.bgImgUrl = backgroundImage;
        }

        Float aspectRatio = data.getFloat(KEY_ASPECT_RATIO);
        if (aspectRatio == null) {
            style.aspectRatio = Float.NaN;
        } else {
            style.aspectRatio = aspectRatio;
        }

        Float ratio = data.getFloat(KEY_RATIO);
        if (ratio != null) {
            style.aspectRatio = ratio;
        } else {
            style.aspectRatio = Float.NaN;
        }

        style.zIndex = data.getIntValue(KEY_ZINDEX);

        style.slidable = data.getBooleanValue(KEY_SLIDABLE);

        Object marginObj = data.get(KEY_MARGIN);
        if (marginObj instanceof JSONArray) {
            JSONArray marginArray = (JSONArray) marginObj;
            int size = Math.min(style.margin.length, marginArray.size());
            for (int i = 0; i < size; i++) {
                style.margin[i] = style.parseSize(marginArray.getString(i), 0);
            }

            if (size > 0) {
                Arrays.fill(style.margin, size, style.margin.length, style.margin[size - 1]);
            }
        } else if (marginObj instanceof String) {
            String marginString = (String) marginObj;
            if (!TextUtils.isEmpty(marginString)) {
                style.setMargin(marginString);
            }
        }

        Object paddingObj = data.get(KEY_PADDING);
        if (paddingObj instanceof JSONArray) {
            JSONArray paddingArray = (JSONArray) paddingObj;
            int size = Math.min(style.padding.length, paddingArray.size());
            for (int i = 0; i < size; i++) {
                style.padding[i] = style.parseSize(paddingArray.getString(i), 0);
            }

            if (size > 0) {
                Arrays.fill(style.padding, size, style.padding.length, style.padding[size - 1]);
            }
        } else if (paddingObj instanceof String) {
            String paddingString = (String) paddingObj;
            if (!TextUtils.isEmpty(paddingString)) {
                style.setPadding(paddingString);
            }
        }
        return style;
    }

    protected BaseCell createCell(@Nullable Card parent, @NonNull MVHelper resolver, @NonNull JSONObject cellData,
                                  @NonNull ServiceManager serviceManager, Map<String, ComponentInfo> componentInfoMap) {
        BaseCell cell = null;
        String cellType = parseCellType(cellData);

        // cellType is null, maybe is card
        if (TextUtils.isEmpty(cellType)) {
            cellType = parseCardType(cellData);
        }

        if (TextUtils.isEmpty(cellType)) {
            return BaseCell.NaN;
        }

        if (Utils.isCard(cellType)) {
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

            parseCell(cell, cellData);
            if (parent != null) {
                boolean ret = parent.addCellInternal(cell, false);
                if (!ret && TangramBuilder.isPrintLog()) {
                    LogUtils.w(TAG, "Parse invalid cell with data: " + cellData.toString());
                }
            }
            return cell;
        } else if (resolver.resolver().getViewClass(cellType) != null) {
            cell = new BaseCell(cellType);
            cell.serviceManager = serviceManager;
            if (parent != null) {
                cell.parent = parent;
                cell.parentId = parent.id;
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
            if (TangramBuilder.isPrintLog()) {
                LogUtils.e("PojoDataParser", "createCell status: cellType=" + cellType + ", componentInfoMap=" + (componentInfoMap == null ? "null" : componentInfoMap.toString()));
            }
            if (!componentBinderResolver.has(cellType) && componentInfoMap != null && componentInfoMap.containsKey(cellType)) {
                componentBinderResolver.register(cellType, new BaseCellBinder<>(cellType, resolver));
            }
            if (componentBinderResolver.has(cellType)) {
                cell = new BaseCell(cellType);
                if (componentInfoMap != null) {
                    cell.componentInfo = componentInfoMap.get(cellType);
                }
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

    protected void parseCell(BaseCell cell, JSONObject json) {
        if (json != null) {
            cell.extras = json;
            cell.id = json.getString(KEY_BIZ_ID);
            if (TextUtils.isEmpty(cell.id) && json.containsKey(KEY_ID)) {
                cell.id = json.getString(KEY_ID);
            }
            cell.stringType = parseCellType(json);
            cell.typeKey = json.getString(KEY_TYPE_KEY);
            String reuseId = json.getString(KEY_TYPE_REUSEID);
            if (!TextUtils.isEmpty(reuseId)) {
                cell.typeKey = reuseId;
            }
            Integer position = json.getInteger(KEY_POSITION);
            if (position == null) {
                position = -1;
            }
            cell.position = position;
            JSONObject styleJson = json.getJSONObject(KEY_STYLE);
            Style style = new Style();
            cell.style = parseStyle(style, styleJson);
        } else {
            cell.extras = new JSONObject();
        }
    }

    protected void parseCard(Card card, JSONObject data, ServiceManager serviceManager, Map<String, ComponentInfo> componentInfoMap) {
        card.id = data.getString(KEY_ID);
        if (card.id == null) {
            card.id = "";
        }

        // parsing header
        JSONObject header = data.getJSONObject(KEY_HEADER);
        BaseCell headerCell = parseSingleComponent(header, card, serviceManager, componentInfoMap);
        parseHeaderCell(headerCell, card);

        // parsing body
        JSONArray componentArray = data.getJSONArray(KEY_ITEMS);
        if (componentArray != null) {
            final int cellLength = componentArray.size();
            for (int i = 0; i < cellLength; i++) {
                final JSONObject cellData = componentArray.getJSONObject(i);
                parseSingleComponent(cellData, card, card.serviceManager, componentInfoMap);
            }
        }
        // parsing footer
        JSONObject footer = data.getJSONObject(KEY_FOOTER);
        BaseCell footerCell = parseSingleComponent(footer, card, serviceManager, componentInfoMap);
        parseFooterCell(footerCell, card);
    }

    protected Map<String, ComponentInfo> parseComponentInfo(JSONObject cardJson) {
        if (cardJson == null || !cardJson.containsKey(COMPONENTINFO)) {
            return null;
        }

        JSONArray componentInfoArray = cardJson.getJSONArray(COMPONENTINFO);
        if (componentInfoArray == null) {
            return null;
        }

        Map<String, ComponentInfo> componentInfoMap = new HashMap<>(128);

        for (int i = 0; i < componentInfoArray.size(); i++) {
            JSONObject json = componentInfoArray.getJSONObject(i);
            ComponentInfo info = new ComponentInfo(json);
            mvHelper.renderManager().putComponentInfo(info);
            componentInfoMap.put(info.getId(), info);
        }

        return componentInfoMap;
    }

    protected String parseCardType(JSONObject json) {
        return json.getString(KEY_TYPE);
    }

    protected String parseCellType(JSONObject json) {
        return json.getString(KEY_TYPE);
    }

    protected boolean isCustomCard(String cardType) {
        return false;
    }

    protected void parseCustomCard(Card card,
                                   @NonNull JSONObject cellData,
                                   @NonNull ServiceManager serviceManager,
                                   Map<String, ComponentInfo> componentInfoMap) {
    }

    protected void parseHeaderCell(BaseCell headerCell, Card card) {
        card.mHeader = headerCell;
        if (card instanceof GridCard) {
            GridCard gridCard = (GridCard) card;
            gridCard.ensureBlock(card.mHeader);
        } else if (card instanceof OnePlusNCard) {
            OnePlusNCard onePlusNCard = (OnePlusNCard) card;
            onePlusNCard.ensureBlock(card.mHeader);
        }
    }

    protected void parseFooterCell(BaseCell footerCell, Card card) {
        card.mFooter = footerCell;
        if (card instanceof GridCard) {
            GridCard gridCard = (GridCard) card;
            gridCard.ensureBlock(card.mFooter);
        } else if (card instanceof OnePlusNCard) {
            OnePlusNCard onePlusNCard = (OnePlusNCard) card;
            onePlusNCard.ensureBlock(card.mFooter);
        }
    }

    protected void checkCardResolverAndMVHelper(ServiceManager serviceManager) {
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
