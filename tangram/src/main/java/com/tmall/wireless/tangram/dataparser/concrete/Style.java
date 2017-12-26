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

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.tmall.wireless.tangram.util.TangramViewMetrics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;


public class Style {

    private static final LruCache<String, Integer> colorCache = new LruCache<>(100);

    public static final String KEY_BG_COLOR = "bgColor";

    public static final String KEY_BACKGROUND_COLOR = "background-color";

    public static final String KEY_BG_IMAGE = "bgImage";

    public static final String KEY_STYLE_BG_IMAGE = "bgImgUrl";

    public static final String KEY_BACKGROUND_IMAGE = "background-image";

    public static final String KEY_MARGIN = "margin";

    public static final String KEY_PADDING = "padding";

    public static final String KEY_WIDTH = "width";

    public static final String KEY_HEIGHT = "height";

    public static final String KEY_COLS = "cols";

    public static final String KEY_DISPLAY = "display";

    public static final String KEY_ZINDEX = "zIndex";

    public static final String KEY_ASPECT_RATIO = "aspectRatio";

    public static final String KEY_RATIO = "ratio";

    public static final String KEY_ANIMATION_DURATION = "animationDuration";

    public static final String DEFAULT_BG_COLOR = "#00000000";

    public static final String KEY_SLIDABLE = "slidable";

    public static final String KEY_FOR_LABEL = "forLabel";

    public static final int MARGIN_TOP_INDEX = 0;

    public static final int MARGIN_RIGHT_INDEX = 1;

    public static final int MARGIN_BOTTOM_INDEX = 2;

    public static final int MARGIN_LEFT_INDEX = 3;

    public static final String DISPLAY_INLINE = "inline-block";

    public static final String DISPLAY_BLOCK = "block";

    public static final int DISPLAY_INNER_INLINE = 0;

    public static final int DISPLAY_INNER_BLOCK = 1;

    private static final int[] DEFAULT_MARGIN = new int[]{0, 0, 0, 0};

    public int bgColor;

    /**
     * alias of bgImgUrl
     */
	@Deprecated
    public String bgImage;

    public String bgImgUrl;

    public String forLabel;

    @Nullable
    public JSONObject extras;

    public int overlapOffset;

    public int zIndex = 0;

    public boolean slidable;

    /**
     * margin in order of top, right, bottom, left
     */
    @NonNull
    public final int[] margin = new int[]{0, 0, 0, 0};

    /**
     * padding in order of top, right, bottom, left
     */
    @NonNull
    public final int[] padding = new int[]{0, 0, 0, 0};

    public int width = VirtualLayoutManager.LayoutParams.MATCH_PARENT;
    public int height = VirtualLayoutManager.LayoutParams.MATCH_PARENT;

    public float aspectRatio = Float.NaN;

    public Style() {
        this(DEFAULT_MARGIN);
    }

    public Style(float[] defaultMargin) {
        int size = Math.min(defaultMargin.length, this.margin.length);
        for (int i = 0; i < size; i++) {
            margin[i] = dp2px(defaultMargin[i]);
        }
        Arrays.fill(margin, size, margin.length, margin[size - 1]);

        this.bgColor = parseColor(DEFAULT_BG_COLOR);
    }

    public Style(int[] margin) {

        final int len = Math.min(margin.length, this.margin.length);
        System.arraycopy(margin, 0, this.margin, 0, len);
        if (len < this.margin.length) {
            Arrays.fill(this.margin, len, this.margin.length, this.margin[len - 1]);
        }

        this.bgColor = parseColor(DEFAULT_BG_COLOR);
    }

    public void setBgColor(String bgColor) {
        this.bgColor = parseColor(bgColor);
    }


    public void setMargin(@Nullable String marginString) {
        if (!TextUtils.isEmpty(marginString)) {
            // remove leading and ending '[' ']'
            try {
                marginString = marginString.trim().substring(1, marginString.length() - 1);
                String marginStringArray[] = marginString.split(",");
                int size = marginStringArray.length > 4 ? 4 : marginStringArray.length;
                for (int i = 0; i < size; i++) {
                    String marginStr = marginStringArray[i];
                    try {
                        if (!TextUtils.isEmpty(marginStr)) {
                            margin[i] = dp2px(Float.parseFloat(marginStr.trim().replace("\"", "")));
                        }
                    } catch (NumberFormatException ignored) {
                        margin[i] = 0;
                    }
                }
                Arrays.fill(margin, size, margin.length, margin[size - 1]);
            } catch (Exception e) {
                Arrays.fill(margin, 0);
            }
        }
    }

    public void setPadding(@Nullable String paddingString) {
        if (!TextUtils.isEmpty(paddingString)) {
            // remove leading and ending '[' ']'
            try {
                paddingString = paddingString.trim().substring(1, paddingString.length() - 1);
                String paddingStringArray[] = paddingString.split(",");
                int size = paddingStringArray.length > 4 ? 4 : paddingStringArray.length;
                for (int i = 0; i < size; i++) {
                    String paddingStr = paddingStringArray[i];
                    try {
                        if (!TextUtils.isEmpty(paddingStr)) {
                            padding[i] = dp2px(Float.parseFloat(paddingStr.trim().replace("\"", "")));
                        }
                    } catch (NumberFormatException ignored) {
                        padding[i] = 0;
                    }
                }
                Arrays.fill(padding, size, padding.length, padding[size - 1]);
            } catch (Exception e) {
                Arrays.fill(padding, 0);
            }
        }
    }


    public void parseWith(@Nullable JSONObject data) {
        if (data != null) {

            extras = data;

            forLabel = data.optString(KEY_FOR_LABEL, "");

            setBgColor(data.optString(KEY_BG_COLOR, DEFAULT_BG_COLOR));
            String backgroundColor = data.optString(KEY_BACKGROUND_COLOR);
            if (!TextUtils.isEmpty(backgroundColor)) {
                setBgColor(backgroundColor);
            }

            double width = data.optDouble(KEY_WIDTH, VirtualLayoutManager.LayoutParams.MATCH_PARENT);
            if (width >= 0) {
                this.width = dp2px(width);
            }

            double height = data.optDouble(KEY_HEIGHT, VirtualLayoutManager.LayoutParams.WRAP_CONTENT);
            if (height >= 0) {
                this.height = dp2px(height);
            }

            bgImage = data.optString(KEY_BG_IMAGE, "");
            bgImgUrl = data.optString(KEY_STYLE_BG_IMAGE, "");

            String backgroundImage = data.optString(KEY_BACKGROUND_IMAGE, "");

            if (!TextUtils.isEmpty(backgroundImage)) {
                bgImage = backgroundImage;
                bgImgUrl = backgroundImage;
            }

            aspectRatio = (float) data.optDouble(KEY_ASPECT_RATIO);

            double ratio = data.optDouble(KEY_RATIO);
            if (!Double.isNaN(ratio)) {
                aspectRatio = (float)ratio;
            }

            zIndex = data.optInt(KEY_ZINDEX, 0);

            slidable = data.optBoolean(KEY_SLIDABLE);

            JSONArray marginArray = data.optJSONArray(KEY_MARGIN);
            if (marginArray != null) {
                int size = Math.min(margin.length, marginArray.length());
                for (int i = 0; i < size; i++) {
                    try {
                        margin[i] = dp2px(marginArray.optDouble(i));
                    } catch (Exception e) {
                    }
                }

                if (size > 0) {
                    Arrays.fill(margin, size, margin.length, margin[size - 1]);
                }
            } else {
                String marginString = data.optString(KEY_MARGIN);
                if (!TextUtils.isEmpty(marginString)) {
                    setMargin(marginString);
                }
            }

            JSONArray paddingArray = data.optJSONArray(KEY_PADDING);
            if (paddingArray != null) {
                int size = Math.min(padding.length, paddingArray.length());
                for (int i = 0; i < size; i++) {
                    try {
                        padding[i] = dp2px(paddingArray.optDouble(i));
                    } catch (Exception e) {
                    }
                }

                if (size > 0) {
                    Arrays.fill(padding, size, padding.length, padding[size - 1]);
                }
            } else {
                String paddingString = data.optString(KEY_PADDING);
                if (!TextUtils.isEmpty(paddingString)) {
                    setPadding(paddingString);
                }
            }
        }

    }

    public static int parseColor(String colorString) {
        return parseColor(colorString, Color.WHITE);
    }

    public static int parseColor(String colorString, int defaultColor) {
        try {
            Integer integer = colorCache.get(colorString);
            if (integer != null) {
                return integer.intValue();
            } else {
                integer = Color.parseColor(colorString);
                colorCache.put(colorString, integer);
                return integer.intValue();
            }
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public static int dp2px(double dpValue) {
        float scaleRatio = TangramViewMetrics.screenDensity();
        final float scale = scaleRatio < 0 ? 1.0f : scaleRatio;

        int finalValue;
        if (dpValue >= 0) {
            finalValue = (int) (dpValue * scale + 0.5f);
        } else {
            finalValue = -((int) (-dpValue * scale + 0.5f));
        }
        return finalValue;
    }




}
