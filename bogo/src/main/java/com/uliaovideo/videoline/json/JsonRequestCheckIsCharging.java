package com.uliaovideo.videoline.json;

import com.uliaovideo.videoline.alipay.PayResult;

/**
 * Created by 魏鹏 on 2018/3/26.
 * email:1403102936@qq.com
 * 山东布谷鸟网络科技有限公司著
 */

public class JsonRequestCheckIsCharging extends JsonRequestBase{
    private int is_need_charging;
    private String video_deduction;

    public String getVideo_deduction() {
        return video_deduction;
    }

    public void setVideo_deduction(String video_deduction) {
        this.video_deduction = video_deduction;
    }

    public int getIs_need_charging() {
        return is_need_charging;
    }

    public void setIs_need_charging(int is_need_charging) {
        this.is_need_charging = is_need_charging;
    }
}
