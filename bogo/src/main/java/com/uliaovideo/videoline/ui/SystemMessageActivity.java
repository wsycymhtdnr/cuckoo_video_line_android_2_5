package com.uliaovideo.videoline.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.SystemMessageAdapter;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.api.ApiUtils;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.json.JsonRequestGetUploadSign;
import com.uliaovideo.videoline.json.JsonRequestSystemMessage;
import com.uliaovideo.videoline.manage.RequestConfig;
import com.uliaovideo.videoline.modle.SystemMessageModel;
import com.uliaovideo.videoline.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author 魏鹏
 * email 1403102936@qq.com
 * 山东布谷鸟网络科技有限公司著
 * @dw 系统消息
 */
public class SystemMessageActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {

    private QMUITopBar qmuiTopBarLayout;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView mRvContentList;

    private List<SystemMessageModel> list = new ArrayList<>();
    private SystemMessageAdapter systemMessageAdapter;

    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_system_message;
    }

    @Override
    protected void initView() {

        qmuiTopBarLayout = findViewById(R.id.qmui_top_bar);
        qmuiTopBarLayout.addLeftImageButton(R.drawable.icon_back_black, R.id.all_backbtn).setOnClickListener(this);
        mRvContentList = findViewById(R.id.rv_content_list);
        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.admin_color));

        mRvContentList.setLayoutManager(new LinearLayoutManager(this));
        systemMessageAdapter = new SystemMessageAdapter(list);
        mRvContentList.setAdapter(systemMessageAdapter);

        systemMessageAdapter.setOnItemChildClickListener(this);

    }

    @Override
    protected void initSet() {
        qmuiTopBarLayout.addLeftImageButton(R.drawable.icon_back_black, R.id.all_backbtn).setOnClickListener(this);
        qmuiTopBarLayout.setTitle(getString(R.string.system_msg));
    }

    @Override
    protected void initData() {
        requestGetData();

    }

    @Override
    protected void initPlayerDisplayData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.all_backbtn:

                finish();
            default:
                break;
        }
    }

    private void requestGetData() {
        Api.doRequestGetSystemMessageList(uId, uToken, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                refreshLayout.setRefreshing(false);
                hideLoadingDialog();
                JsonRequestSystemMessage jsonObj = (JsonRequestSystemMessage) JsonRequestSystemMessage.getJsonObj(s, JsonRequestSystemMessage.class);
                if (jsonObj.getCode() == 1) {

                    list.clear();
                    list.addAll(jsonObj.getList());
                    systemMessageAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        switch (view.getId()) {
            case R.id.item_tv_url:

                WebViewActivity.openH5Activity(this, true, list.get(position).getTitle(), list.get(position).getUrl());
                break;
            default:
                break;
        }
    }
}
