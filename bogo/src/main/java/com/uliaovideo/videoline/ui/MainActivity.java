package com.uliaovideo.videoline.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.FragAdapter;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.dialog.AppUpdateDialog;
import com.uliaovideo.videoline.dialog.DoCallVideoWaitDialog;
import com.uliaovideo.videoline.dialog.InviteCodeDialog;
import com.uliaovideo.videoline.event.CuckooOnLoginTimSuccessEvent;
import com.uliaovideo.videoline.event.CuckooPushVideoCallDialogEvent;
import com.uliaovideo.videoline.event.EImOnNewMessages;
import com.uliaovideo.videoline.event.LocalEvent;
import com.uliaovideo.videoline.fragment.IndexOrderFragment;
import com.uliaovideo.videoline.fragment.MsgFragment;
import com.uliaovideo.videoline.fragment.IndexPageFragment;
import com.uliaovideo.videoline.fragment.UserPageFragment;
import com.uliaovideo.videoline.fragment.VideoPlayFragment;
import com.uliaovideo.videoline.fragment.VideoPushFragment;
import com.uliaovideo.videoline.fragment.VideoSmallFragment;
import com.uliaovideo.videoline.manage.AppManager;
import com.uliaovideo.videoline.MyApplication;
import com.uliaovideo.videoline.manage.RequestConfig;
import com.uliaovideo.videoline.manage.SaveData;
import com.uliaovideo.videoline.modle.ConfigModel;
import com.uliaovideo.videoline.modle.custommsg.CustomMsgVideoCall;
import com.uliaovideo.videoline.ui.common.Common;
import com.uliaovideo.videoline.utils.CuckooSharedPreUtil;
import com.uliaovideo.videoline.utils.DialogHelp;
import com.uliaovideo.videoline.utils.IMUtils;
import com.uliaovideo.videoline.utils.StringUtils;
import com.uliaovideo.videoline.utils.UserOnlineHeartUtils;
import com.uliaovideo.videoline.widget.NoScrollViewPager;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMManager;
import com.tencent.open.utils.ThreadManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 主页
 */

public class MainActivity extends BaseActivity implements PermissionUtils.OnPermissionListener {

    @BindView(R.id.rl_bottom)
    RelativeLayout rl_bottom;
    //功能
    private NoScrollViewPager mainViewPage;
    private QMUITabSegment mainTabSegment;

    //数据
    private List<Fragment> fragmentList;

    private static final int REQUEST_PERMISSION = 0;
    private AppUpdateDialog appUpdateDialog;
    private TextView unReadMsg;

    @Override
    protected Context getNowContext() {
        return MainActivity.this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

        QMUIStatusBarHelper.translucent(this); // 沉浸式状态栏
        mainViewPage = findViewById(R.id.main_view_page);
        mainTabSegment = findViewById(R.id.main_tab_segment);

        //未读消息View
        int itemWidth = ScreenUtils.getScreenWidth() / 4;
        int padding = ConvertUtils.dp2px(1) * 2;
        unReadMsg = new TextView(this);
        unReadMsg.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ConvertUtils.dp2px(15), ConvertUtils.dp2px(15));
        params.setMargins(itemWidth * 2 + itemWidth/4 * 3,0,0,0);
        unReadMsg.setPadding(padding,padding,padding,padding);
        unReadMsg.setGravity(Gravity.CENTER);
        unReadMsg.setTextSize(ConvertUtils.dp2px(3));
        unReadMsg.setBackgroundResource(R.drawable.bg_un_read_msg);
        unReadMsg.setTextColor(getResources().getColor(R.color.white));
        unReadMsg.setLayoutParams(params);
        rl_bottom.addView(unReadMsg);

        addTabAndViewPage();

    }

    @Override
    protected void initData() {

        //检查邀请码
        InviteCodeDialog.checkInviteCode(this);

        if (ConfigModel.getInitData().getIs_force_upgrade() != 1) {

            showUpdateDialog();
        }
        //系统公告消息
        showSystemMsg();

        //初始化心跳
        UserOnlineHeartUtils.getInstance().startHeartTime();

        String pushData = getIntent().getStringExtra("push_data");
        if (pushData != null) {

            CustomMsgVideoCall videoCall = JSON.parseObject(pushData, CustomMsgVideoCall.class);
            DoCallVideoWaitDialog dialog = new DoCallVideoWaitDialog(this, videoCall.getSender(), videoCall.getChannel(), videoCall.getIs_free());
            dialog.show();
        }

    }

    @Override
    protected void initSet() {

        //设置viewPage的缓存页数
        //mainViewPage.setOffscreenPageLimit(2);
        //设置adapter
        mainViewPage.setAdapter(new FragAdapter(getSupportFragmentManager(), fragmentList));
        //设置字体大小
        mainTabSegment.setTabTextSize(ConvertUtils.dp2px(12));
        //设置 Tab 选中状态下的颜色
        mainTabSegment.setDefaultSelectedColor(getResources().getColor(R.color.admin_color));
        //关联viewPage
        mainTabSegment.setupWithViewPager(mainViewPage, false);
        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        PermissionUtils.requestPermissions(this, REQUEST_PERMISSION, permission, this);
    }


    //初始化ViewPage和tab
    private void addTabAndViewPage() {

        //添加关联的fragment
        fragmentList = new ArrayList<>();
        fragmentList.add(
                new IndexPageFragment());
        fragmentList.add(
                new IndexOrderFragment());

        if (StringUtils.toInt(ConfigModel.getInitData().getOpen_video_chat()) == 1) {
            if (SaveData.getInstance().getUserInfo().getSex() == 1) {

                fragmentList.add(
                        new VideoPlayFragment());
            } else {

                fragmentList.add(
                        new VideoPushFragment());
            }
        }

        fragmentList.add(
                new VideoSmallFragment());
        fragmentList.add(
                new MsgFragment());
        fragmentList.add(
                new UserPageFragment());
        //添加tab
        mainTabSegment.addTab(
                new QMUITabSegment.Tab(
                        getResources().getDrawable(R.drawable.main_screen_drawable_ranking_unselected),
                        getResources().getDrawable(R.drawable.main_screen_drawable_ranking_selected),
                        getString(R.string.index),
                        false,
                        true));

        if (StringUtils.toInt(ConfigModel.getInitData().getOpen_video_chat()) == 1) {
            mainTabSegment.addTab(
                    new QMUITabSegment.Tab(
                            getResources().getDrawable(R.drawable.main_screen_drawable_ranking_unselected),
                            getResources().getDrawable(R.drawable.main_screen_drawable_ranking_selected),
                            null,
                            false,
                            true));
        }
        mainTabSegment.addTab(
                new QMUITabSegment.Tab(
                        getResources().getDrawable(R.drawable.main_screen_drawable_order_unselected),
                        getResources().getDrawable(R.drawable.main_screen_drawable_order_selected),
                        getString(R.string.order),
                        false,
                        true));
        mainTabSegment.addTab(
                new QMUITabSegment.Tab(
                        getResources().getDrawable(R.drawable.main_screen_drawable_video_unselected),
                        getResources().getDrawable(R.drawable.main_screen_drawable_video_selected),
                        getString(R.string.video),
                        false,
                        true));
        mainTabSegment.addTab(
                new QMUITabSegment.Tab(
                        getResources().getDrawable(R.drawable.main_screen_drawable_message_unselected),
                        getResources().getDrawable(R.drawable.main_screen_drawable_message_selected),
                        getString(R.string.message),
                        false,
                        true));
        mainTabSegment.addTab(
                new QMUITabSegment.Tab(
                        getResources().getDrawable(R.drawable.main_screen_drawable_mine_unselected),
                        getResources().getDrawable(R.drawable.main_screen_drawable_mine_selected),
                        getString(R.string.me),
                        false,
                        true));
    }


    @Override
    protected void initPlayerDisplayData() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocalEvent(LocalEvent var1) {

//        if(MyApplication.getInstances().getLocation().get("city") != null){
//            Api.doRefreshCity(uId,MyApplication.getInstances().getLocation().get("city"),null);
//        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewMsgEventThread(EImOnNewMessages var1) {
        initUnReadMessage();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginTimSuccess(CuckooOnLoginTimSuccessEvent var1) {
        initUnReadMessage();
    }


    @Override
    protected void doLogout() {
        super.doLogout();
        AppManager.getAppManager().appExit(getNowContext());//退出操作
    }


    //初始化未读消息
    private void initUnReadMessage() {

        int count = IMUtils.getIMUnReadMessageCount();
        if(count > 0){
            unReadMsg.setVisibility(View.VISIBLE);
            unReadMsg.setText(String.valueOf(count));
        }else{
            unReadMsg.setVisibility(View.GONE);
        }
    }

    //系统消息
    private void showSystemMsg() {
        int isShowSystemMsg = CuckooSharedPreUtil.getInt(this,"IS_SHOW_SYSTEM_MSG");

        if(!TextUtils.isEmpty(RequestConfig.getConfigObj().getMainSystemMessage()) && isShowSystemMsg == 0){

            new QMUIDialog.MessageDialogBuilder(getNowContext())
                    .setTitle(getString(R.string.system_notice))
                    .setMessage(RequestConfig.getConfigObj().getMainSystemMessage())
                    .addAction(0, "不再提示", QMUIDialogAction.ACTION_PROP_NEGATIVE, new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            CuckooSharedPreUtil.put(MainActivity.this,"IS_SHOW_SYSTEM_MSG",1);
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }


    private void showUpdateDialog() {
        if (appUpdateDialog != null) {
            appUpdateDialog.dismiss();
        }

        appUpdateDialog = AppUpdateDialog.checkUpdate(this);
    }

    @Override
    protected boolean currentPageFinsh() {
        return true;
    }

    @Override
    public void onPermissionGranted() {

    }

    @Override
    public void onPermissionDenied(String[] deniedPermissions) {
        ToastUtils.showLong(R.string.not_have_permission);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ConfigModel.getInitData().getIs_force_upgrade() == 1) {

            showUpdateDialog();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //初始化未读消息
        initUnReadMessage();

    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserOnlineHeartUtils.getInstance().stopHeartTime();
    }
}
