package com.uliaovideo.videoline.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.inter.MenuDialogClick;
import com.uliaovideo.videoline.json.JsonRequestBase;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.rengwuxian.materialedittext.MaterialEditText;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 加盟合作页
 */

public class ToJoinActivity extends BaseActivity{
    private QMUITopBar mTopBar;//顶部bar
    private MaterialEditText editText;//举报信息输入框
    private TextView selectText;//选择文本
    private Button rightBtn;//提交按钮

    private String[] items = {"渠道推展推广合作","大主播入驻合作","工会负责人合作","代理商合作","城市代理人合作","取消"};

    @Override
    protected Context getNowContext() {
        return ToJoinActivity.this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_tojoin_layout;
    }

    @Override
    protected void initView() {
        mTopBar = findViewById(R.id.tojoin_topBar);
        editText = findViewById(R.id.tojoin_edit_text);
        selectText = findViewById(R.id.tojoin_select_text);
    }

    @Override
    protected void initSet() {
        setOnclickListener(R.id.tojoin_cause);
        mTopBar.setTitle("加盟合作");
        mTopBar.addLeftImageButton(R.drawable.icon_back_black,R.id.left_btn).setOnClickListener(this);
        rightBtn = mTopBar.addRightTextButton("提交",R.id.right_btn);
        rightBtn.setTextColor(getResources().getColor(R.color.black));
        rightBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initPlayerDisplayData() {

    }

    //////////////////////////////////////////监听事件操作////////////////////////////////////////////
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tojoin_cause:
                doSelectCause();
                break;
            case R.id.left_btn:
                finish();
                break;
            case R.id.right_btn:
                doSubmit();
                break;
        }
    }

    /////////////////////////////////////////业务逻辑处理/////////////////////////////////////////////

    /**
     * 执行提交操作
     */
    private void doSubmit() {
        String way = selectText.getText().toString();
        String content = editText.getText().toString();

        showLoadingDialog(getString(R.string.loading_upload_data));
        Api.doJoinIn(uId,uToken,way,content,new StringCallback(){

            @Override
            public void onSuccess(String s, Call call, Response response) {
                hideLoadingDialog();
                JsonRequestBase jsonObj = JsonRequestBase.getJsonObj(s, JsonRequestBase.class);
                if(jsonObj.getCode() == 1){
                    showToastMsg(getString(R.string.upload_success));
                    finish();
                }else{
                    showToastMsg(jsonObj.getMsg());
                }

            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                hideLoadingDialog();
            }
        });
    }

    /**
     * 执行选择加盟原因操作
     */
    private void doSelectCause() {
        showMenuDialog(items, new MenuDialogClick() {
            @Override
            public void OnMenuItemClick(DialogInterface dialog, int which) {
                if (which != items.length-1){
                    selectText.setText(items[which]);
                    hideView(R.id.tojoin_right_img);
                }
            }
        });
    }
}
