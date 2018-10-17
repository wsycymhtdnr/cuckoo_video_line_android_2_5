package com.uliaovideo.videoline.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.UserCashRecordAdapter;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.dialog.InputCashInfoDialog;
import com.uliaovideo.videoline.event.CuckooCashEvent;
import com.uliaovideo.videoline.event.EImOnCloseVideoLine;
import com.uliaovideo.videoline.event.EWxPayResultCodeComplete;
import com.uliaovideo.videoline.json.JsonRequestBase;
import com.uliaovideo.videoline.json.JsonRequestUserIncomePageInfo;
import com.uliaovideo.videoline.modle.CashBean;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.widget.QMUITopBar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by 魏鹏 on 2018/3/3.
 * email:1403102936@qq.com
 * 山东布谷鸟网络科技有限公司著
 * @dw 用户收益
 */
public class UserIncomeActivity extends BaseActivity implements BaseQuickAdapter.RequestLoadMoreListener {

    private QMUITopBar qmuiTopBarLayout;
    private RecyclerView mRvContentList;
    private TextView mTvIncome,mTvMoney;
    private Button mBtnCash;

    private UserCashRecordAdapter userCashRecordAdapter;
    private List<CashBean> cashRecordList = new ArrayList<>();

    //页数
    private int page = 1;

    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_user_income;
    }

    @Override
    protected void initView() {

        qmuiTopBarLayout = findViewById(R.id.qmui_top_bar);
        mTvIncome = findViewById(R.id.tv_income);
        mRvContentList = findViewById(R.id.rv_content_list);
        mBtnCash = findViewById(R.id.btn_cash);
        mTvMoney = findViewById(R.id.tv_money);
        mBtnCash.setOnClickListener(this);
        mRvContentList.setLayoutManager(new LinearLayoutManager(this));
        initTopBar();
    }

    private void initTopBar() {

        qmuiTopBarLayout.setBackgroundColor(getResources().getColor(R.color.admin_color));
        qmuiTopBarLayout.addLeftImageButton(R.drawable.icon_back_white,R.id.all_backbtn).setOnClickListener(this);
        TextView title = qmuiTopBarLayout.setTitle(getString(R.string.income));
        title.setTextColor(getResources().getColor(R.color.white));
    }


    @Override
    protected void initSet() {
        userCashRecordAdapter = new UserCashRecordAdapter(cashRecordList);
        mRvContentList.setAdapter(userCashRecordAdapter);
        userCashRecordAdapter.setOnLoadMoreListener(this,mRvContentList);
        userCashRecordAdapter.disableLoadMoreIfNotFullPage();

    }

    @Override
    protected void initData() {

        requestGetPageInfo();
    }

    @Override
    protected void initPlayerDisplayData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cash:

                clickCash();
                break;

            case R.id.all_backbtn:

                finish();
                break;

            default:

                break;
        }
    }

    private void clickCash() {

        InputCashInfoDialog dialog = new InputCashInfoDialog(this);
        dialog.show();
    }

    //获取当前页面信息
    private void requestGetPageInfo(){

        Api.doGetCashPageInfo(uId,uToken,page,new StringCallback(){

            @Override
            public void onSuccess(String s, Call call, Response response) {
                JsonRequestUserIncomePageInfo jsonObj =
                        (JsonRequestUserIncomePageInfo) JsonRequestBase.getJsonObj(s,JsonRequestUserIncomePageInfo.class);
                if(jsonObj.getCode() == 1){

                    mTvMoney.setText(String.valueOf(jsonObj.getMoney()));
                    mTvIncome.setText(jsonObj.getIncome());
                    if(page == 1){
                        cashRecordList.clear();
                    }

                    //是否加载完毕
                    if(jsonObj.getList().size() == 0){

                        userCashRecordAdapter.loadMoreEnd();
                    }else{

                        userCashRecordAdapter.loadMoreComplete();
                    }
                    cashRecordList.addAll(jsonObj.getList());
                    userCashRecordAdapter.notifyDataSetChanged();
                }else{
                    userCashRecordAdapter.loadMoreEnd();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCashEvent(CuckooCashEvent event){

        page = 1;
        requestGetPageInfo();

    }

    @Override
    public void onLoadMoreRequested() {

        page ++;
        requestGetPageInfo();
    }

}
