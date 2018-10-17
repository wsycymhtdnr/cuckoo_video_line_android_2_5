package com.uliaovideo.videoline.json;

import com.uliaovideo.videoline.modle.UserModel;

/**
 * Created by 魏鹏 on 2018/3/3.
 * email:1403102936@qq.com
 * 山东布谷鸟网络科技有限公司著
 */

public class JsonRequestDoPrivateChat extends JsonRequestBase {
    private UserModel user_info;
    private int is_pay;
    private String pay_coin;

    public String getPay_coin() {
        return pay_coin;
    }

    public void setPay_coin(String pay_coin) {
        this.pay_coin = pay_coin;
    }

    public int getIs_pay() {
        return is_pay;
    }

    public void setIs_pay(int is_pay) {
        this.is_pay = is_pay;
    }

    public UserModel getUser_info() {
        return user_info;
    }

    public void setUser_info(UserModel user_info) {
        this.user_info = user_info;
    }
}
