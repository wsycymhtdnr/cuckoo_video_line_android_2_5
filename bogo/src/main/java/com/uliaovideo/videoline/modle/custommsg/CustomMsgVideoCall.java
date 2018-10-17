package com.uliaovideo.videoline.modle.custommsg;


import com.uliaovideo.videoline.LiveConstant;

public class CustomMsgVideoCall extends CustomMsg
{
    private String channel;//视频通道
    private String is_free;

    public String getIs_free() {
        return is_free;
    }

    public void setIs_free(String is_free) {
        this.is_free = is_free;
    }

    public CustomMsgVideoCall()
    {
        super();
        setType(LiveConstant.CustomMsgType.MSG_VIDEO_LINE_CALL);
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
