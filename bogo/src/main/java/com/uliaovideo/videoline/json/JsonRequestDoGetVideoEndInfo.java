package com.uliaovideo.videoline.json;

/**
 * Created by weipeng on 2018/2/28.
 */

public class JsonRequestDoGetVideoEndInfo extends JsonRequestBase {

    private String data;
    private String is_follow;

    public String getIs_follow() {
        return is_follow;
    }

    public void setIs_follow(String is_follow) {
        this.is_follow = is_follow;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
