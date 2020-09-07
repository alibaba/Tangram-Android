package com.tmall.wireless.tangram3.dataparser.concrete;

import com.alibaba.fastjson.JSONObject;

public class ComponentInfo {
    public static final String NAME = "name";

    public static final String ID = "id";

    public static final String TYPE = "type";

    public static final String VERSION = "version";

    public static final String URL = "url";

    private String name;

    private String id;

    private String type;

    private long version;

    private String url;

    public ComponentInfo() {
    }

    public ComponentInfo(JSONObject json) {
        this.name = json.getString(NAME);
        this.id = json.getString(ID);
        this.type = json.getString(TYPE);
        this.version = json.getLongValue(VERSION);
        this.url = json.getString(URL);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
