package com.uliaovideo.videoline.json;

import com.uliaovideo.videoline.modle.UserModel;

public class JsonRequestPerfectRegisterInfo extends JsonRequestBase{

    private UserModel data;

    public UserModel getData() {
        return data;
    }

    public void setData(UserModel data) {
        this.data = data;
    }
}
