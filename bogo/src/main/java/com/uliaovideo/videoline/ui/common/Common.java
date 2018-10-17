package com.uliaovideo.videoline.ui.common;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.uliaovideo.chat.ui.ChatActivity;
import com.uliaovideo.videoline.ApiConstantDefine;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.dialog.ShowPayPhotoDialog;
import com.uliaovideo.videoline.helper.TxLogin;
import com.uliaovideo.videoline.json.JsonRequestBase;
import com.uliaovideo.videoline.json.JsonRequestDoPrivateChat;
import com.uliaovideo.videoline.json.JsonRequestSelectPic;
import com.uliaovideo.videoline.json.JsonRequestsDoCall;
import com.uliaovideo.videoline.manage.AppManager;
import com.uliaovideo.videoline.manage.SaveData;
import com.uliaovideo.videoline.modle.ConfigModel;
import com.uliaovideo.videoline.modle.UserModel;
import com.uliaovideo.videoline.ui.CuckooHomePageActivity;
import com.uliaovideo.videoline.ui.HomePageActivity;
import com.uliaovideo.videoline.ui.PerViewImgActivity;
import com.uliaovideo.videoline.ui.PlayerCallActivity;
import com.uliaovideo.videoline.ui.PrivatePhotoActivity;
import com.uliaovideo.videoline.ui.RegisterSelectActivity;
import com.uliaovideo.videoline.utils.DialogHelp;
import com.uliaovideo.videoline.utils.im.IMHelp;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMValueCallBack;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by weipeng on 2018/2/28.
 */

public class Common {

    //跳转用户主页
    public static void jumpUserPage(Context context,String uid){
        Intent intent = new Intent(context, HomePageActivity.class);
        intent.putExtra("str",uid);
        context.startActivity(intent);
    }

    //拨打视频通话
    public static void callVideo(final Context context,final String toUserId){

        UserModel userModel = SaveData.getInstance().getUserInfo();

        final QMUITipDialog tipD = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在接通...")
                .create();
        tipD.show();

        Api.doCallToUser(
                userModel.getId(),
                userModel.getToken(),
                toUserId,
                new StringCallback(){

                    @Override
                    public void onSuccess(String s, Call call, Response response) {


                        final JsonRequestsDoCall requestObj = JsonRequestsDoCall.getJsonObj(s);
                        if (requestObj.getCode() ==1){

                            IMHelp.sendVideoCallMsg(requestObj.getData().getChannel_id(), toUserId, new TIMValueCallBack<TIMMessage>() {
                                @Override
                                public void onError(int i, String s) {
                                    tipD.dismiss();
                                    LogUtils.i("IM 一对一消息推送失败！");
                                    ToastUtils.showLong("拨打通话失败！");
                                }

                                @Override
                                public void onSuccess(TIMMessage timMessage) {
                                    tipD.dismiss();
                                    LogUtils.i("IM 一对一消息推送成功！");
                                    UserModel callUserInfo = new UserModel();
                                    callUserInfo.setId(requestObj.getData().getTo_user_base_info().getId());
                                    callUserInfo.setUser_nickname(requestObj.getData().getTo_user_base_info().getUser_nickname());
                                    callUserInfo.setAvatar(requestObj.getData().getTo_user_base_info().getAvatar());
                                    callUserInfo.setSex(requestObj.getData().getTo_user_base_info().getSex());

                                    Intent intent = new Intent(context,PlayerCallActivity.class);
                                    intent.putExtra(PlayerCallActivity.CALL_USER_INFO,callUserInfo);
                                    intent.putExtra(PlayerCallActivity.CALL_CHANNEL_ID,requestObj.getData().getChannel_id());
                                    context.startActivity(intent);
                                }
                            });

                            //goActivity(PlayerCallActivity.class,new UserChatData(targetUserId,requestObj.getData().getChannel_id(),""));
                        }else{
                            tipD.dismiss();
                            LogUtils.i("拨打电话error:"+requestObj.getMsg());
                            ToastUtils.showLong(requestObj.getMsg());
                        }
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        tipD.dismiss();
                    }
                }

        );

        //DoCallVideoWaitDialog doCallVideoWaitDialog = new DoCallVideoWaitDialog(HomePageActivity.this);
        //doCallVideoWaitDialog.show();

        //跳转私信页面

    }

    //跳转私信页面
    public static void startPrivatePage(final Context context,String toUserId){

        String uid = SaveData.getInstance().getId();
        String token = SaveData.getInstance().getToken();
        if(uid.equals(toUserId)){
            ToastUtils.showLong("不能给自己发送私信消息!");
            return;
        }
        final QMUITipDialog tipD = new QMUITipDialog.Builder(context)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在获取信息...")
                .create();
        tipD.show();


        Api.doPrivateChat(uid,token,toUserId,new StringCallback(){

            @Override
            public void onSuccess(String s, Call call, Response response) {

                tipD.dismiss();
                JsonRequestDoPrivateChat jsonObj =
                        (JsonRequestDoPrivateChat) JsonRequestBase.getJsonObj(s,JsonRequestDoPrivateChat.class);
                if(jsonObj.getCode() == 1){

                    if(jsonObj.getUser_info() == null){
                        return;
                    }
                    ChatActivity.navToChat(context,jsonObj.getUser_info().getId(),jsonObj.getUser_info().getUser_nickname()
                            ,jsonObj.getUser_info().getAvatar(),jsonObj.getIs_pay(),jsonObj.getPay_coin(), TIMConversationType.C2C);
                }else{
                    ToastUtils.showLong(jsonObj.getMsg());
                }

            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                tipD.dismiss();
            }
        });

    }

    //查看私照
    public static void requestSelectPic(final Context context,final String id) {

        Api.doRequestSelectPic(SaveData.getInstance().getId(),SaveData.getInstance().getToken(),id,new StringCallback(){

            @Override
            public void onSuccess(String s, Call call, Response response) {

                JsonRequestSelectPic jsonObj = (JsonRequestSelectPic) JsonRequestSelectPic.getJsonObj(s,JsonRequestSelectPic.class);
                if(jsonObj.getCode() == 1){
                    PerViewImgActivity.startPerViewImg(context,jsonObj.getImg());

                }else if(jsonObj.getCode() == ApiConstantDefine.ApiCode.PHOTO_NOT_PAY){

                    ShowPayPhotoDialog showPayPhotoDialog = new ShowPayPhotoDialog(context,id);
                    showPayPhotoDialog.show();
                }else{
                    ToastUtils.showShort(jsonObj.getMsg());
                }
            }
        });

    }

    //QQ客服
    public static void openCustomServiceQQ(Context context){
        if(ConfigModel.getInitData().getCustom_service_qq() == null){
            return;
        }
//        String url1 ="mqqwpa://im/chat?chat_type=wpa&uin=" + ConfigModel.getInitData().getCustom_service_qq();
//
//        Intent i1 = new Intent(Intent.ACTION_VIEW, Uri.parse(url1));
//
//        i1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        i1.setAction(Intent.ACTION_VIEW);
//
//        context.startActivity(i1);

        DialogHelp.getConfirmDialog(context, "客服QQ：" + ConfigModel.getInitData().getCustom_service_qq() + context.getString(R.string.click_copy), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                ClipboardUtils.copyText(ConfigModel.getInitData().getCustom_service_qq());
                ToastUtils.showLong(R.string.copy_success);
            }
        }).show();
    }


}
