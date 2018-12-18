package com.tmall.wireless.tangram3.dataparser.concrete;

import org.json.JSONObject;

public class ComponentInfo {
    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String VERSION = "version";

    public static final String URL = "url";

    private String name;

    private String type;

    private long version;

    private String url;

    public ComponentInfo() {
    }

    public ComponentInfo(JSONObject json) {
        this.name = json.optString(NAME);
        this.type = json.optString(TYPE);
        this.version = json.optLong(VERSION);
        this.url = json.optString(URL);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
