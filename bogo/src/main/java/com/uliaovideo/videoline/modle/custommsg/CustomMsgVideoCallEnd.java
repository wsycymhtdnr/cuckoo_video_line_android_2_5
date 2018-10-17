package com.uliaovideo.videoline.modle.custommsg;


import com.uliaovideo.videoline.LiveConstant;

public class CustomMsgVideoCallEnd extends CustomMsg
{
    public CustomMsgVideoCallEnd()
    {
        super();
        setType(LiveConstant.CustomMsgType.MSG_VIDEO_LINE_CALL_END);
    }

}
