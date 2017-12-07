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

package com.tmall.wireless.tangram.util;

import com.tmall.wireless.tangram.TangramBuilder;

import org.json.JSONObject;

import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kellen on 8/3/15.
 */
public class Utils {


    private static final Pattern REGEX_1 = Pattern.compile("(\\d+)x(\\d+)(_?q\\d+)?(\\.[jpg|png|gif])");
    private static final Pattern REGEX_2 = Pattern.compile("(\\d+)-(\\d+)(_?q\\d+)?(\\.[jpg|png|gif])");

    public static <T> List<T> newArrayList() {
        return new ArrayList<T>();
    }

    public static <K, V> Map<K, V> newMap(K k1, V v1, K k2, V v2) {
        ArrayMap<K, V> map = new ArrayMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <K, V> Map<K, V> newMap(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = newMap(k1, v1, k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static <K, V> Map<K, V> newMap(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> map = newMap(k1, v1, k2, v2, k3, v3);
        map.put(k4, v4);
        return map;
    }

    public static float getImageRatio(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl))
            return Float.NaN;

        try {
            Matcher matcher = REGEX_1.matcher(imageUrl);
            String widthStr;
            String heightStr;
            if (matcher.find()) {
                if (matcher.groupCount() >= 2) {
                    widthStr = matcher.group(1);
                    heightStr = matcher.group(2);
                    if (widthStr.length() < 5 && heightStr.length() < 5) {
                        int urlWidth = Integer.parseInt(widthStr);
                        int urlHeight = Integer.parseInt(heightStr);

                        if (urlWidth == 0 || urlHeight == 0) {
                            return 1;
                        }
                        return (float) urlWidth / urlHeight;
                    }
                }
            } else {
                matcher = REGEX_2.matcher(imageUrl);
                if (matcher.find()) {
                    if (matcher.groupCount() >= 2) {
                        widthStr = matcher.group(1);
                        heightStr = matcher.group(2);
                        if (widthStr.length() < 5 && heightStr.length() < 5) {
                            int urlWidth = Integer.parseInt(widthStr);
                            int urlHeight = Integer.parseInt(heightStr);

                            if (urlWidth == 0 || urlHeight == 0) {
                                return 1;
                            }
                            return (float) urlWidth / urlHeight;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Float.NaN;
    }

    private static ArrayMap<String, Pair<Integer, Integer>> imageSizeMap = new ArrayMap<>();

    /**
     * <pre>
     * parse image ratio from imageurl with regex as follows:
     * (\d+)-(\d+)(_?q\d+)?(\.[jpg|png|gif])
     * (\d+)x(\d+)(_?q\d+)?(\.[jpg|png|gif])
     *
     * samples urls:
     * http://img.alicdn.com/tps/i1/TB1x623LVXXXXXZXFXXzo_ZPXXX-372-441.png --> return 372/441
     * http://img.alicdn.com/tps/i1/TB1P9AdLVXXXXa_XXXXzo_ZPXXX-372-441.png --> return 372/441
     * http://img.alicdn.com/tps/i1/TB1NZxRLFXXXXbwXFXXzo_ZPXXX-372-441.png --> return 372/441
     * http://img07.taobaocdn.com/tfscom/T10DjXXn4oXXbSV1s__105829.jpg_100x100.jpg --> return 100/100
     * http://img07.taobaocdn.com/tfscom/T10DjXXn4oXXbSV1s__105829.jpg_100x100q90.jpg --> return 100/100
     * http://img07.taobaocdn.com/tfscom/T10DjXXn4oXXbSV1s__105829.jpg_100x100q90.jpg_.webp --> return 100/100
     * http://img03.taobaocdn.com/tps/i3/T1JYROXuRhXXajR_DD-1680-446.jpg_q50.jpg --> return 1680/446
     * </pre>
     * @param imageUrl image url
     * @return ratio of with to height parsed from url
     */
    public static Pair<Integer, Integer> getImageSize(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }

        if (imageSizeMap.get(imageUrl) != null) {
            return imageSizeMap.get(imageUrl);
        }

        try {
            Matcher matcher = REGEX_1.matcher(imageUrl);
            String widthStr;
            String heightStr;
            if (matcher.find()) {
                if (matcher.groupCount() >= 2) {
                    widthStr = matcher.group(1);
                    heightStr = matcher.group(2);
                    if (widthStr.length() < 5 && heightStr.length() < 5) {
                        int urlWidth = Integer.parseInt(widthStr);
                        int urlHeight = Integer.parseInt(heightStr);
                        Pair<Integer, Integer> result = new Pair<>(urlWidth, urlHeight);
                        imageSizeMap.put(imageUrl, result);
                        return result;
                    }
                }
            } else {
                matcher = REGEX_2.matcher(imageUrl);
                if (matcher.find()) {
                    if (matcher.groupCount() >= 2) {
                        widthStr = matcher.group(1);
                        heightStr = matcher.group(2);
                        if (widthStr.length() < 5 && heightStr.length() < 5) {
                            int urlWidth = Integer.parseInt(widthStr);
                            int urlHeight = Integer.parseInt(heightStr);
                            Pair<Integer, Integer> result = new Pair<>(urlWidth, urlHeight);
                            imageSizeMap.put(imageUrl, result);
                            return result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T newInstance(Class<T> clz) {
        if (clz != null) {
            try {
                return clz.newInstance();
            } catch (InstantiationException e) {
                if (TangramBuilder.isPrintLog())
                    Log.e("ClassResolver", e.getMessage(), e);
            } catch (IllegalAccessException e) {
                if (TangramBuilder.isPrintLog())
                    Log.e("ClassResolver", e.getMessage(), e);
            }
        }
        return null;
    }

    public static boolean isCard(JSONObject cellData) {
        if (cellData != null) {
            String type = cellData.optString("type");
            switch (type) {
                case TangramBuilder.TYPE_CONTAINER_BANNER:
                case TangramBuilder.TYPE_CONTAINER_FLOW:
                case TangramBuilder.TYPE_CONTAINER_1C_FLOW:
                case TangramBuilder.TYPE_CONTAINER_2C_FLOW:
                case TangramBuilder.TYPE_CONTAINER_3C_FLOW:
                case TangramBuilder.TYPE_CONTAINER_4C_FLOW:
                case TangramBuilder.TYPE_CONTAINER_5C_FLOW:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private static ArrayMap<String, Boolean> supportHeaderFooterTable = new ArrayMap<>();

    static {
        supportHeaderFooterTable.put(TangramBuilder.TYPE_CONTAINER_FLOW, Boolean.TRUE);
        supportHeaderFooterTable.put(TangramBuilder.TYPE_CONTAINER_1C_FLOW, Boolean.TRUE);
        supportHeaderFooterTable.put(TangramBuilder.TYPE_CONTAINER_2C_FLOW, Boolean.TRUE);
        supportHeaderFooterTable.put(TangramBuilder.TYPE_CONTAINER_3C_FLOW, Boolean.TRUE);
        supportHeaderFooterTable.put(TangramBuilder.TYPE_CONTAINER_4C_FLOW, Boolean.TRUE);
        supportHeaderFooterTable.put(TangramBuilder.TYPE_CONTAINER_5C_FLOW, Boolean.TRUE);
        supportHeaderFooterTable.put(TangramBuilder.TYPE_CONTAINER_ON_PLUSN, Boolean.TRUE);
        supportHeaderFooterTable.put(TangramBuilder.TYPE_CONTAINER_BANNER, Boolean.TRUE);
    }

    /**
     * 只有流式布局(1,2,3,4,9)，和滚动布局(10) 支持header和 footer
     * @param type
     * @return
     */
    public static boolean isSupportHeaderFooter(String type) {
        Boolean isSupport = supportHeaderFooterTable.get(type);
        if (isSupport != null) {
            return isSupport.booleanValue();
        } else {
            return false;
        }
    }

}
