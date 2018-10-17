package com.uliaovideo.videoline.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.uliaovideo.chat.model.CustomMessage;
import com.uliaovideo.chat.model.Message;
import com.uliaovideo.videoline.ApiConstantDefine;
import com.uliaovideo.videoline.LiveConstant;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.api.ApiUtils;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.business.CuckooVideoLineTimeBusiness;
import com.uliaovideo.videoline.dialog.GiftBottomDialog;
import com.uliaovideo.videoline.event.EImOnCloseVideoLine;
import com.uliaovideo.videoline.event.EImVideoCallEndMessages;
import com.uliaovideo.videoline.event.EImOnPrivateMessage;
import com.uliaovideo.videoline.inter.JsonCallback;
import com.uliaovideo.videoline.json.JsonRequest;
import com.uliaovideo.videoline.json.JsonRequestDoEndVideoCall;
import com.uliaovideo.videoline.json.JsonRequestDoPrivateSendGif;
import com.uliaovideo.videoline.json.JsonRequestDoVideoCallTimeCharging;
import com.uliaovideo.videoline.json.JsonRequestTarget;
import com.uliaovideo.videoline.json.jsonmodle.TargetUserData;
import com.uliaovideo.videoline.manage.RequestConfig;
import com.uliaovideo.videoline.modle.ConfigModel;
import com.uliaovideo.videoline.modle.GiftAnimationModel;
import com.uliaovideo.videoline.modle.UserChatData;
import com.uliaovideo.videoline.modle.custommsg.CustomMsg;
import com.uliaovideo.videoline.modle.custommsg.CustomMsgPrivateGift;
import com.uliaovideo.videoline.ui.common.Common;
import com.uliaovideo.videoline.utils.BGTimedTaskManage;
import com.uliaovideo.videoline.utils.DialogHelp;
import com.uliaovideo.videoline.utils.StringUtils;
import com.uliaovideo.videoline.utils.Utils;
import com.uliaovideo.videoline.utils.im.IMHelp;
import com.uliaovideo.videoline.widget.GiftAnimationContentView;
import com.lzy.okgo.callback.StringCallback;
import com.nineoldandroids.animation.ObjectAnimator;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMValueCallBack;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import okhttp3.Call;
import okhttp3.Response;
/**
 * 山东布谷鸟网络科技有限公司
 * 视频通话页面
 */

public class VideoLineActivity extends BaseActivity implements GiftBottomDialog.DoSendGiftListen, CuckooVideoLineTimeBusiness.VideoLineTimeBusinessCallback {

    @BindView(R.id.video_chat_small)
    FrameLayout localVideoView;//本地视图

    @BindView(R.id.video_chat_big)
    FrameLayout remoteVideoView;//目标视图

    @BindView(R.id.close_video_chat)
    ImageView closeVideo;//关闭按钮

    @BindView(R.id.videochat_switch)
    ImageView cutCamera;//切换摄像头

    @BindView(R.id.videochat_voice)
    ImageView isSoundOut;//是否关闭声音

    @BindView(R.id.videochat_gift)
    ImageView videoGift;//礼物按钮

    //动画布局
    @BindView(R.id.ll_gift_content)
    GiftAnimationContentView mGiftAnimationContentView;

    //信息
    @BindView(R.id.videochat_unit_price)
    TextView chatUnitPrice;//收费金额

    @BindView(R.id.videochat_timer)
    Chronometer videoChatTimer;//通话计时时长

    //用户信息
    @BindView(R.id.this_player_img)
    CircleImageView headImage;//头像

    @BindView(R.id.this_player_number)
    TextView thisPlayerNumber;//关注数

    @BindView(R.id.this_player_name)
    TextView nickName;//昵称

    @BindView(R.id.this_player_loveme)
    TextView thisPlayerLoveme;//关注按钮

    //标记
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;

    private UserChatData chatData;

    //创建 RtcEngine 对象
    private RtcEngine mRtcEngine;// Tutorial Step 1

    //是否是被呼叫用户
    private boolean isBeCall = false;

    //是否需要扣费
    private boolean isNeedCharge = false;

    public static final String IS_BE_CALL = "IS_BE_CALL";
    public static final String IS_NEED_CHARGE = "IS_NEED_CHARGE";
    public static final String VIDEO_DEDUCTION = "VIDEO_DEDUCTION";
    private VideoCanvas smallVideoCanvas;
    private VideoCanvas bigVideoCanvas;

    private GiftBottomDialog giftBottomDialog;

    private int videoUid;
    private int videoViewStatus = 1;
    private TIMConversation conversation;

    //分钟扣费金额
    private String videoDeduction = "";


    private CuckooVideoLineTimeBusiness cuckooVideoLineTimeBusiness;

    @Override
    protected Context getNowContext() {
        return VideoLineActivity.this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_video_chat;
    }

    @Override
    protected void initView() {

        localVideoView.setOnClickListener(this);
        remoteVideoView.setOnClickListener(this);

        mGiftAnimationContentView.startHandel();

        //分钟扣费金额
        videoDeduction = getIntent().getStringExtra(VIDEO_DEDUCTION);
        chatUnitPrice.setText(videoDeduction + RequestConfig.getConfigObj().getCurrency() + "/分钟");

        videoChatTimer.setTextColor(getResources().getColor(R.color.white));
        String msgAlert = ConfigModel.getInitData().getVideo_call_msg_alert();
        if(!TextUtils.isEmpty(msgAlert)){
            ToastUtils.showLong(msgAlert);
        }
    }

    @Override
    protected void initSet() {
        setOnclickListener(isSoundOut,closeVideo,cutCamera,videoGift,headImage,thisPlayerLoveme);
        initAgoraEngineAndJoinChannel();//初始化本地操作
        joinChannel();//发起视频
    }

    @Override
    protected void initData() {

        chatData = getIntent().getParcelableExtra("obj");
        isBeCall = getIntent().getBooleanExtra(IS_BE_CALL,false);
        isNeedCharge = getIntent().getBooleanExtra(IS_NEED_CHARGE,false);

        cuckooVideoLineTimeBusiness = new CuckooVideoLineTimeBusiness(this,isNeedCharge,chatData.getUserModel().getId(),this);
        if(isBeCall){
            initBeCallView();
            initBeCallAction();
        }else{
            initCallView();
            initCallAction();
        }

        if(isNeedCharge){
            videoGift.setVisibility(View.VISIBLE);
            chatUnitPrice.setVisibility(View.VISIBLE);
        }else{
            chatUnitPrice.setVisibility(View.GONE);
        }

        conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C,chatData.getUserModel().getId());
        requestUserData();

        //开始计时
        videoChatTimer.start();
    }

    /**
     * 呼叫用户业务
     * */
    private void initCallAction() {

    }

    /**
     * 余额不足操作
     * */
    private void doBalance() {

        hangUpVideo();
        ToastUtils.showShort(R.string.money_insufficient);
    }

    /**
     * 被呼叫用户业务
     * */
    private void initBeCallAction() {
    }

    /**
     * 呼叫视频通话用户View初始化
     * */
    private void initCallView() {

    }

    /**
     * 被呼叫视频通话用户View初始化
     * */
    private void initBeCallView() {
        videoGift.setVisibility(View.GONE);
    }

    @Override
    protected void initPlayerDisplayData() {
    }

    /////////////////////////////////////////////监听事件处理/////////////////////////////////////////
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.videochat_gift:
                clickOpenGiftDialog();
                break;
            case R.id.videochat_screen:
                //TODO:截屏
                break;
            case R.id.this_player_loveme:
                doLoveHer();
                break;
            case R.id.close_video_chat:
                logoutChat();
                break;
            case R.id.videochat_switch:
                doCutCamera();
                break;
            case R.id.videochat_voice:
                onLocalAudioMuteClicked();
                break;
            case R.id.this_player_img:

                Common.jumpUserPage(VideoLineActivity.this,chatData.getUserModel().getId());
                break;
            case R.id.video_chat_big:

                switchVideoView();
                break;
            case R.id.video_chat_small:

                switchVideoView();
                break;
        }
    }

    //礼物弹窗
    private void clickOpenGiftDialog() {
        if(giftBottomDialog == null){

            giftBottomDialog = new GiftBottomDialog(this,chatData.getUserModel().getId());
            giftBottomDialog.setType(1);
            giftBottomDialog.setChanelId(chatData.getChannelName());
            giftBottomDialog.setDoSendGiftListen(this);
        }

        giftBottomDialog.show();
    }

    //切换视图
    private void switchVideoView() {

        localVideoView.removeAllViews();
        remoteVideoView.removeAllViews();
        mRtcEngine.setupLocalVideo(null);
        mRtcEngine.setupRemoteVideo(null);


        if(videoViewStatus == 1){
            //创建视频渲染视图, 设置本地视频视图##初始化本地视图
            SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
            surfaceView.setTag(videoUid);
            //surfaceView.setZOrderMediaOverlay(true);
            localVideoView.addView(surfaceView);
            smallVideoCanvas = new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, videoUid);
            mRtcEngine.setupRemoteVideo(smallVideoCanvas);


            SurfaceView surfaceView2 = RtcEngine.CreateRendererView(getBaseContext());
            remoteVideoView.addView(surfaceView2);
            bigVideoCanvas = new VideoCanvas(surfaceView2, VideoCanvas.RENDER_MODE_HIDDEN, 0);
            mRtcEngine.setupLocalVideo(bigVideoCanvas);

            videoViewStatus = 2;
        }else{

            //创建视频渲染视图, 设置本地视频视图##初始化本地视图
            SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
            surfaceView.setZOrderMediaOverlay(true);
            localVideoView.addView(surfaceView);

            smallVideoCanvas = new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
            mRtcEngine.setupLocalVideo(smallVideoCanvas);

            SurfaceView surfaceView2 = RtcEngine.CreateRendererView(getBaseContext());
            surfaceView2.setTag(videoUid);
            remoteVideoView.addView(surfaceView2);

            bigVideoCanvas = new VideoCanvas(surfaceView2, VideoCanvas.RENDER_MODE_HIDDEN, videoUid);
            mRtcEngine.setupRemoteVideo(bigVideoCanvas);

            videoViewStatus = 1;
        }

    }

    //发起会话通道名额外的可选的数据--uid(uid为空时自动赋予)
    private void joinChannel() {
        mRtcEngine.joinChannel(null, chatData.getChannelName(), null, 0);
    }

    //本地音频静音
    public void onLocalAudioMuteClicked() {
        if (isSoundOut.isSelected()) {
            isSoundOut.setSelected(false);
            isSoundOut.setImageResource(R.drawable.icon_call_unmute);
        } else {
            isSoundOut.setSelected(true);
            isSoundOut.setImageResource(R.drawable.icon_call_muted);
        }
        mRtcEngine.muteLocalAudioStream(isSoundOut.isSelected());
    }

    /**
     * 执行切换前后相机
     */
    private void doCutCamera() {
        mRtcEngine.switchCamera();
    }

    /**
     * 退出聊天
     */
    private void logoutChat() {

        DialogHelp.getConfirmDialog(this, getString(R.string.is_huang_call), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                hangUpVideo();
            }
        }).show();

    }

    /**
     * 关注目标主播
     */
    private void doLoveHer() {
        Api.doLoveTheUser(
                chatData.getUserModel().getId(),
                uId,
                uToken,
                new JsonCallback() {
                    @Override
                    public Context getContextToJson() {
                        return getNowContext();
                    }
                    @Override
                    public void onSuccess(String s, Call call, Response response) {

                        JsonRequest requestObj = JsonRequest.getJsonObj(s);
                        if (requestObj.getCode() == 1){
                            concealView(thisPlayerLoveme);//隐藏关注按钮
                            showToastMsg("关注成功!");
                        }
                    }
                }
        );
    }

    /**
     * 获取当前视频主播信息
     */
    private void requestUserData() {

        Api.getUserData(
                chatData.getUserModel().getId(),
                uId,
                uToken,
                new JsonCallback() {
                    @Override
                    public Context getContextToJson() {
                        return getNowContext();
                    }
                    @Override
                    public void onSuccess(String s, Call call, Response response) {

                        JsonRequestTarget requestObj = JsonRequestTarget.getJsonObj(s);
                        if (requestObj.getCode() == 1){
                            TargetUserData targetUserData = requestObj.getData();
                            if (ApiUtils.isTrueUrl(targetUserData.getAvatar())){
                                Utils.loadHttpImg(VideoLineActivity.this,Utils.getCompleteImgUrl(targetUserData.getAvatar()),headImage);
                            }
                            nickName.setText(targetUserData.getUser_nickname());
                            thisPlayerNumber.setText(getString(R.string.follow) + ": " + targetUserData.getAttention_all());
                            thisPlayerLoveme.setVisibility("0".equals(targetUserData.getAttention()) ? View.VISIBLE : View.GONE);
                        }else{
                            showToastMsg("获取当前视频主播信息:"+requestObj.getMsg());
                        }
                    }
                }
        );
    }

    private void operationVideoAndAudio(boolean muted){

        mRtcEngine.muteLocalAudioStream(muted);
        mRtcEngine.muteLocalVideoStream(muted);
        mRtcEngine.muteAllRemoteVideoStreams(muted);
        mRtcEngine.muteAllRemoteAudioStreams(muted);

    }

    //销毁操作
    private void leaveChannel() {
        if(mRtcEngine != null){
            mRtcEngine.leaveChannel();
        }
    }

    /**
     * 初始化设置视图
     */
    private void initAgoraEngineAndJoinChannel() {
        //初始化RtcEngine对象
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), RequestConfig.getConfigObj().getApp_qgorq_key(), mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }

        //初始化设置信息
        mRtcEngine.enableVideo();//打开视频模式
        mRtcEngine.enableAudio();
        mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_480P, false);//设置视频分辨率


        //创建视频渲染视图, 设置本地视频视图##初始化本地视图
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        localVideoView.addView(surfaceView);

        smallVideoCanvas = new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(smallVideoCanvas);
        mRtcEngine.startPreview();
    }

    /**
     * 创建视频渲染视图, 设置远端视频视图
     * @param uid 用户uid
     */
    private void setupRemoteVideo(int uid) {
        //创建视频渲染视图, 设置远端视频视图
        if (remoteVideoView.getChildCount() >= 1) {
            return;
        }
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setTag(uid);
        remoteVideoView.addView(surfaceView);
        videoUid = uid;

        bigVideoCanvas = new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        mRtcEngine.setupRemoteVideo(bigVideoCanvas);
    }


    //移除目标组件所有视图文件
    private void onRemoteUserLeft() {
        remoteVideoView.removeAllViews();
        //hangUpVideo();

    }

    //初始化目标视图
    private void onRemoteUserVideoMuted(int uid, boolean muted) {
        SurfaceView surfaceView = (SurfaceView) remoteVideoView.getChildAt(0);
        if(surfaceView == null){
            return;
        }
        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }

    //关闭通话
    private void hangUpVideo() {

        operationVideoAndAudio(true);
        showLoadingDialog(getString(R.string.loading_huang_up));
        cuckooVideoLineTimeBusiness.doHangUpVideo();
    }

    //添加礼物消息
    private void pushGiftMsg(CustomMsgPrivateGift giftCustom){

        GiftAnimationModel giftAnimationModel = new GiftAnimationModel();

        giftAnimationModel.setUserAvatar(giftCustom.getSender().getAvatar());
        giftAnimationModel.setUserNickname(giftCustom.getSender().getUser_nickname());
        giftAnimationModel.setMsg(giftCustom.getFrom_msg());
        giftAnimationModel.setGiftIcon(giftCustom.getProp_icon());
        if(mGiftAnimationContentView != null){
            mGiftAnimationContentView.addGift(giftAnimationModel);
        }
    }


    /**
     * 回调监听
     */
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            // 在第一个远程的
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            //用户不在线
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(uid != 1){
                        onRemoteUserLeft();
                    }
                }
            });
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            // 在用户静音视频
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRemoteUserVideoMuted(uid, muted);
                }
            });
        }
    };

    @Override
    protected void doLogout() {
        super.doLogout();
        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventVideoCallEndThread(EImVideoCallEndMessages var1) {

        LogUtils.i("收到消息一对一视频结束请求消息:" + var1.msg.getCustomMsg().getSender().getUser_nickname());

        try {
            CustomMsg customMsg = var1.msg.getCustomMsg();
            showLiveLineEnd(1);

        }catch (Exception e){
            LogUtils.i("收到消息一对一视频结束请求消息错误error" + e.getMessage());
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPrivateGiftEvent(EImOnPrivateMessage var1) {

        pushGiftMsg(var1.customMsgPrivateGift);
        LogUtils.i("收到消息发送礼物消息:" + var1.customMsgPrivateGift.getFrom_msg());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloseVideoEvent(EImOnCloseVideoLine var1) {

        //后台结束通话警示信息
        DialogHelp.getMessageDialog(this, var1.customMsgCloseVideo.getMsg_content()).show();
        hangUpVideo();

        LogUtils.i("收到后台关闭视频消息:" + var1.customMsgCloseVideo.getMsg_content());
    }

    //赠送礼物
    @Override
    public void onSuccess(JsonRequestDoPrivateSendGif sendGif) {

        final CustomMsgPrivateGift gift = new CustomMsgPrivateGift();
        gift.fillData(sendGif.getSend());
        Message message = new CustomMessage(gift, LiveConstant.CustomMsgType.MSG_PRIVATE_GIFT);
        conversation.sendMessage(message.getMessage(), new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                LogUtils.i("一对一视频礼物消息发送失败");
            }

            @Override
            public void onSuccess(TIMMessage timMessage) {

                pushGiftMsg(gift);
                LogUtils.i("一对一视频礼物消息发送SUCCESS");
            }
        });
    }

    @Override
    public void onBackPressed() {
        logoutChat();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(mGiftAnimationContentView != null){
            mGiftAnimationContentView.stopHandel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(cuckooVideoLineTimeBusiness != null){
            cuckooVideoLineTimeBusiness.stop();
        }

        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    /**
     * 跳转视频结束页面
     * */
    private void showLiveLineEnd(int isFabulous) {

        Intent intent = new Intent(this,VideoLineEndActivity.class);
        intent.putExtra(VideoLineEndActivity.USER_HEAD,chatData.getUserModel().getAvatar());
        intent.putExtra(VideoLineEndActivity.USER_NICKNAME,chatData.getUserModel().getUser_nickname());
        intent.putExtra(VideoLineEndActivity.LIVE_LINE_TIME,videoChatTimer.getText());
        intent.putExtra(VideoLineEndActivity.LIVE_CHANNEL_ID,chatData.getChannelName());
        intent.putExtra(VideoLineEndActivity.IS_CALL_BE_USER,!isNeedCharge);
        intent.putExtra(VideoLineEndActivity.USER_ID,chatData.getUserModel().getId());
        intent.putExtra(VideoLineEndActivity.IS_FABULOUS,isFabulous);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCallbackChargingSuccess() {

    }

    @Override
    public void onCallbackNotBalance() {

        doBalance();
    }

    @Override
    public void onCallbackCallRecordNotFount() {

        showToastMsg("通话记录不存在");
        finishNow();
    }

    @Override
    public void onCallbackCallNotMuch(String msg) {
        DialogHelp.getConfirmDialog(VideoLineActivity.this, msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                RechargeActivity.startRechargeActivity(VideoLineActivity.this);
            }
        }).show();
    }

    @Override
    public void onCallbackEndVideo(String msg) {

        showToastMsg(msg);
        cuckooVideoLineTimeBusiness.doHangUpVideo();
    }

    @Override
    public void onHangUpVideoSuccess(int isFabulous) {

        hideLoadingDialog();
        showLiveLineEnd(isFabulous);
    }
}
