package com.uliaovideo.videoline.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.FragAdapter;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.fragment.CuckooHomePageUserInfoFragment;
import com.uliaovideo.videoline.fragment.CuckooHomePageVideoFragment;
import com.uliaovideo.videoline.fragment.NewPeopleFragment;
import com.uliaovideo.videoline.helper.SelectResHelper;
import com.uliaovideo.videoline.inter.JsonCallback;
import com.uliaovideo.videoline.json.JsonRequest;
import com.uliaovideo.videoline.json.JsonRequestBase;
import com.uliaovideo.videoline.json.JsonRequestTarget;
import com.uliaovideo.videoline.json.jsonmodle.TargetUserData;
import com.uliaovideo.videoline.manage.RequestConfig;
import com.uliaovideo.videoline.manage.SaveData;
import com.uliaovideo.videoline.ui.common.Common;
import com.uliaovideo.videoline.utils.GlideImageLoader;
import com.uliaovideo.videoline.utils.StringUtils;
import com.uliaovideo.videoline.utils.Utils;
import com.uliaovideo.videoline.utils.im.IMHelp;
import com.uliaovideo.videoline.widget.BGLevelTextView;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.sns.TIMFriendResult;
import com.youth.banner.Banner;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Response;

public class CuckooHomePageActivity extends BaseActivity {

    @BindView(R.id.view_pager)
    QMUIViewPager viewPager;

    private FragAdapter mFragAdapter;
    private String targetUserId;//目标用户id
    private TargetUserData targetUserData;//当前目标用户信息

    //轮播图
    private ArrayList<String> rollPath = new ArrayList<>();
    private List fragmentList = new ArrayList();

    //数据
    private TextView maxLevelText;//当前最高等级
    private TextView userNickname; //当前player名称
    private TextView userTimeText; //通话x小时
    private TextView userGoodText;//好评
    private TextView userLocationText;//当前player位置
    private TextView userIsonLineText;//当前player是否在线
    private TextView fansNumber;//粉丝数量
    private TextView listBarGiftText;//礼物数量
    private TextView listBarVideoText;//小视频数量
    private TextView listBarPhotoText;//私照数量
    private TextView tvVideoMoney;
    private BGLevelTextView tv_level;
    private ImageView userIsonLine;//是否在线图标
    private ImageView userIsattestation;//是否认证
    private Banner homePageWallpaper;//轮播组件
    private TextView userLoveme;//关注这个player

    private Dialog menuDialog;

    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_cuckoo_home_page;
    }

    @Override
    protected void initView() {

        QMUIStatusBarHelper.translucent(this); // 沉浸式状态栏

        userIsonLine = findViewById(R.id.userinfo_bar_isonLine);
        userIsattestation = findViewById(R.id.userinfo_bar_isattestation);
        userNickname = findViewById(R.id.userinfo_bar_userid);
        userTimeText = findViewById(R.id.userinfo_bar_time_text);
        userGoodText = findViewById(R.id.userinfo_bar_good_text);
        userLocationText = findViewById(R.id.userinfo_bar_location_text);
        userIsonLineText = findViewById(R.id.userinfo_bar_isonLine_text);
        fansNumber = findViewById(R.id.fans_number);
        //listBarGiftList = findViewById(R.id.list_bar_gift_list);
        //listBarVideoList = findViewById(R.id.list_bar_video_list);
        //listBarPhotoList = findViewById(R.id.list_bar_photo_list);
        listBarGiftText = findViewById(R.id.list_bar_gift_text);
        listBarVideoText = findViewById(R.id.list_bar_video_text);
        listBarPhotoText = findViewById(R.id.list_bar_photo_text);
        userLoveme = findViewById(R.id.userinfo_bar_loveme);
        //homePageWallpaper = findViewById(R.id.home_page_wallpaper);
        tv_level = findViewById(R.id.tv_level);
        maxLevelText = findViewById(R.id.userinfo_bar_max_number);
        tvVideoMoney = findViewById(R.id.tv_video_money);
        homePageWallpaper = findViewById(R.id.home_page_wallpaper);
    }

    @Override
    protected void initSet() {

        //轮播参数设置
        //设置图片加载器
        homePageWallpaper.setImageLoader(new GlideImageLoader());
        //设置图片集合
        homePageWallpaper.setImages(rollPath);
        //banner设置方法全部调用完毕时最后调用
        homePageWallpaper.start();

        fragmentList.add(CuckooHomePageUserInfoFragment.getInstance(targetUserId));
        fragmentList.add(CuckooHomePageVideoFragment.getInstance(targetUserId));

        viewPager.setOffscreenPageLimit(1);
        mFragAdapter = new FragAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(mFragAdapter);
    }

    @Override
    protected void initData() {

        targetUserId = getIntent().getStringExtra("str");
        requestTargetUserData();
    }

    @Override
    protected void initPlayerDisplayData() {

    }

    @OnClick({R.id.tv_btn_info, R.id.tv_btn_video, R.id.contribution_btn, R.id.float_back, R.id.float_meun, R.id.ll_chat, R.id.ll_gift, R.id.rl_call, R.id.userinfo_bar_loveme})
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.userinfo_bar_loveme:
                loveThisPlayer();
                break;
            case R.id.tv_btn_info:
                viewPager.setCurrentItem(0);
                break;
            case R.id.tv_btn_video:
                viewPager.setCurrentItem(1);
                break;

            //贡献榜按钮
            case R.id.contribution_btn:
                showContribution();
                break;
            case R.id.float_back:
                finish();
                break;
            case R.id.float_meun:
                showFloatMeun();
                break;
            //加入黑名单操作
            case R.id.join_black_list:
                clickBlack();
                menuDialog.dismiss();
                break;
            //举报该用户操作
            case R.id.report_this_user:
                clickReport();
                menuDialog.dismiss();
                break;
            //取消操作
            case R.id.dialog_dis:
                menuDialog.dismiss();
                break;
            case R.id.ll_chat:

                showChatPage(false);
                break;
            case R.id.ll_gift:
                showChatPage(true);
                break;
            case R.id.rl_call:

                callThisPlayer();
                break;
        }
    }

    //关注这个player
    private void loveThisPlayer() {
        Api.doLoveTheUser(
                targetUserId,
                uId,
                uToken,
                new JsonCallback() {
                    @Override
                    public Context getContextToJson() {
                        return getNowContext();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        super.onSuccess(s, call, response);
                        JsonRequest requestObj = JsonRequest.getJsonObj(s);
                        if (requestObj.getCode() == 1) {
                            concealView(userLoveme);//隐藏关注按钮
                            showToastMsg(getString(R.string.action_success));
                        } else {
                            LogUtils.i("关注当前player:" + requestObj.getMsg());
                        }
                    }
                }
        );
    }

    //给这个player打电话
    private void callThisPlayer() {
        showToastMsg(getString(R.string.now_call));
        Common.callVideo(this, targetUserId);
    }

    //显示聊天页面
    private void showChatPage(boolean isShowGift) {

        Common.startPrivatePage(this, targetUserId);

    }

    //显示菜单
    private void showFloatMeun() {
        int[] a = {R.id.join_black_list, R.id.report_this_user, R.id.dialog_dis};
        menuDialog = showButtomDialog(R.layout.dialog_float_meun, a, 20);
        TextView tv = menuDialog.findViewById(R.id.join_black_list);
        if (targetUserData.getIs_black() == 1) {
            tv.setText(R.string.relieve_black);
        }
        menuDialog.show();
    }


    //显示贡献榜
    private void showContribution() {
        Intent intent = new Intent(this, UserContribuionRankActivity.class);
        intent.putExtra(UserContribuionRankActivity.TO_USER_ID, targetUserId);
        startActivity(intent);
    }

    //举报
    private void clickReport() {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra(ReportActivity.REPORT_USER_ID, targetUserId);
        startActivity(intent);
    }

    /**
     * 初始化显示页面数据
     */
    private void initDisplayData() {

        //初始化显示页面数据
        tv_level.setLevelInfo(targetUserData.getSex(), targetUserData.getLevel());
        //设置是否显示关注
        userLoveme.setVisibility(StringUtils.toInt(targetUserData.getAttention()) == 1 ? View.INVISIBLE : View.VISIBLE);

        userNickname.setText(targetUserData.getUser_nickname());
        userTimeText.setText(getString(R.string.call) + targetUserData.getCall());
        userGoodText.setText(getString(R.string.praise) + targetUserData.getEvaluation());
        userLocationText.setText(targetUserData.getAddress());
        tvVideoMoney.setText(targetUserData.getVideo_deduction() + RequestConfig.getConfigObj().getCurrency() + getString(R.string.minute));
        userIsattestation.setImageResource(SelectResHelper.getAttestationRes(StringUtils.toInt(targetUserData.getUser_status())));
        fansNumber.setText(targetUserData.getAttention_fans() + getString(R.string.fans));

        //是否在线
        userIsonLineText.setText(StringUtils.toInt(targetUserData.getIs_online()) == 1 ? getString(R.string.on_line) : getString(R.string.off_line));
        userIsonLine.setBackgroundResource(SelectResHelper.getOnLineRes(StringUtils.toInt(targetUserData.getIs_online())));

        //礼物--视频--私照--统计个数
        //listBarVideoText.setText("小视频("+objToString(targetUserData.getVideo_count())+")");
        //listBarPhotoText.setText("私照("+objToString(targetUserData.getPictures_count())+")");

//        if (targetUserId.equals(SaveData.getInstance().getId())){
//            //查询自己
//            userLoveme.setVisibility(View.GONE);
//            listBarGiftText.setText("送出的礼物("+objToString(targetUserData.getGift_count())+")");
//        }else{
//            listBarGiftText.setText("收到的礼物("+objToString(targetUserData.getGift_count())+")");
//        }

        if (targetUserData.getImg() != null) {
            for (TargetUserData.ImgBean img : targetUserData.getImg()) {
                rollPath.add(Utils.getCompleteImgUrl(img.getImg()));
            }
            if (targetUserData.getImg().size() == 0) {
                rollPath.add(Utils.getCompleteImgUrl(targetUserData.getAvatar()));
            }
        }

        //填充收到的礼物列表
//        if(targetUserData.getGift() != null){
//            giftList.addAll(targetUserData.getGift());
//            recycleUserHomeGiftAdapter.notifyDataSetChanged();
//        }
//
//        //填充发布的视频列表
//        if(targetUserData.getVideo() != null){
//            videoList.addAll(targetUserData.getVideo());
//            recycleUserHomePhotoAdapter.notifyDataSetChanged();
//        }
//        //填充发布的私照列表
//        if(targetUserData.getPictures() != null){
//            photoList.addAll(targetUserData.getPictures());
//            recycleUserHomeVideoAdapter.notifyDataSetChanged();
//        }

        homePageWallpaper.setImages(rollPath);
        homePageWallpaper.start();

    }

    /**
     * 获取目标用户基础信息
     */
    private void requestTargetUserData() {

        Api.getUserHomePageInfo(
                targetUserId,
                uId,
                uToken,
                new JsonCallback() {
                    @Override
                    public Context getContextToJson() {
                        return getNowContext();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        super.onSuccess(s, call, response);
                        JsonRequestTarget jsonTargetUser = JsonRequestTarget.getJsonObj(s);
                        if (jsonTargetUser.getCode() == 1) {
                            targetUserData = jsonTargetUser.getData();
                            initDisplayData();
                        } else {
                            //请求失败
                            showToastMsg(jsonTargetUser.getMsg());
                        }
                    }
                }
        );
    }


    //拉黑
    private void clickBlack() {

        showLoadingDialog(getString(R.string.loading_action));
        Api.doRequestBlackUser(uId, uToken, targetUserId, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                hideLoadingDialog();
                JsonRequestBase jsonObj = JsonRequestBase.getJsonObj(s, JsonRequestBase.class);
                if (jsonObj.getCode() == 1) {
                    showToastMsg(getResources().getString(R.string.action_success));
                    if (targetUserData.getIs_black() == 1) {
                        targetUserData.setIs_black(0);
                        IMHelp.deleteBlackUser(targetUserId, new TIMValueCallBack<List<TIMFriendResult>>() {
                            @Override
                            public void onError(int i, String s) {
                                LogUtils.i("解除拉黑用户失败:" + s);
                            }

                            @Override
                            public void onSuccess(List<TIMFriendResult> timFriendResults) {
                                LogUtils.i("解除拉黑用户成功");
                            }
                        });
                    } else {
                        targetUserData.setIs_black(1);
                        IMHelp.addBlackUser(targetUserId, new TIMValueCallBack<List<TIMFriendResult>>() {
                            @Override
                            public void onError(int i, String s) {
                                LogUtils.i("拉黑用户失败:" + s);
                            }

                            @Override
                            public void onSuccess(List<TIMFriendResult> timFriendResults) {
                                LogUtils.i("拉黑用户成功");
                            }
                        });
                    }
                } else {
                    showToastMsg(jsonObj.getMsg());
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                hideLoadingDialog();
            }
        });
    }


}
