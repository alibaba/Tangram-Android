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

package com.tmall.wireless.tangram.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.tmall.wireless.tangram.example.tangram3.Tangram3Activity;

/**
 * Created by longerian on 2017/7/24.
 *
 * @author longerian
 * @date 2017/07/24
 */

public class DemoListActivity extends ListActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.list_content);
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        HashMap<String, String> parse = new HashMap<String, String>();
        parse.put("name", "基本Tangram");
        parse.put("class", TangramActivity.class.getName());
        list.add(parse);
        HashMap<String, String> api = new HashMap<String, String>();
        api.put("name", "响应式Tangram");
        api.put("class", RxTangramActivity.class.getName());
        list.add(api);
        HashMap<String, String> bizItems = new HashMap<String, String>();
        bizItems.put("name", "自定义数据解析");
        bizItems.put("class", TangramDataParserActivity.class.getName());
        list.add(bizItems);
        HashMap<String, String> tangram3Items = new HashMap<String, String>();
        tangram3Items.put("name", "Tangram 3.0 (beta)");
        tangram3Items.put("class", Tangram3Activity.class.getName());
        list.add(tangram3Items);
        ListAdapter listAdapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        setListAdapter(listAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Map<String, String> item = (Map<String, String>)l.getItemAtPosition(position);
        String className = item.get("class");
        if (className != null) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(this, className));
            startActivity(intent);
        }
    }
}
