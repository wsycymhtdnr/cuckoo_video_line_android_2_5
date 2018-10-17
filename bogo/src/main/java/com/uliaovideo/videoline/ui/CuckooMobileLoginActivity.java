package com.uliaovideo.videoline.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.api.ApiUtils;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.inter.JsonCallback;
import com.uliaovideo.videoline.json.JsonRequestUserBase;
import com.uliaovideo.videoline.ui.common.LoginUtils;
import com.uliaovideo.videoline.utils.Utils;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallAdapter;
import com.fm.openinstall.model.AppData;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Response;

public class CuckooMobileLoginActivity extends BaseActivity {

    @BindView(R.id.tv_send_code)
    TextView tv_send_code;

    @BindView(R.id.et_mobile)
    EditText et_mobile;

    @BindView(R.id.et_code)
    EditText et_code;

    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_cuckoo_mobile_login;
    }

    @Override
    protected void initView() {
        getTopBar().setTitle(getString(R.string.login));
    }

    @Override
    protected void initSet() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initPlayerDisplayData() {

    }

    @OnClick({R.id.tv_send_code,R.id.btn_submit})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_send_code:

                clickSendCode();
                break;
            case R.id.btn_submit:

                clickDoLogin();
                break;
            default:
                break;
        }
    }

    private void clickDoLogin() {

        if (!tv_send_code.getText().toString().equals("")) {
            doPhoneLogin(et_mobile.getText().toString(), et_code.getText().toString());
        } else {
            showToastMsg(getString(R.string.mobile_login_code_not_empty));
        }
    }

    //发送验证码
    private void clickSendCode() {

        if (Utils.isMobile(et_mobile.getText().toString())) {
            sendCode(et_mobile.getText().toString());
            tv_send_code.setEnabled(false);

            new CountDownTimer(60 * 1000,1000){

                @Override
                public void onTick(long l) {
                    tv_send_code.setText("（" + (l/1000) + "）");
                }

                @Override
                public void onFinish() {
                    tv_send_code.setEnabled(true);
                }
            }.start();

        } else {
            showToastMsg(getString(R.string.mobile_login_mobile_error));
        }
    }


    //手机登录
    private void doPhoneLogin(final String mobile, final String code) {

        showLoadingDialog(getString(R.string.loading_login));

        //获取OpenInstall安装数据
        OpenInstall.getInstall(new AppInstallAdapter() {
            @Override
            public void onInstall(AppData appData) {
                //获取渠道数据
                String channelCode = appData.getChannel();
                //获取自定义数据
                String bindData = appData.getData();

                String inviteCode = "";
                if(!TextUtils.isEmpty(bindData)){
                    JSONObject data = JSON.parseObject(bindData);
                    inviteCode = data.getString("invite_code");

                }

                Api.userLogin(mobile, code, inviteCode, new JsonCallback() {
                    @Override
                    public Context getContextToJson() {
                        return getNowContext();
                    }

                    @Override
                    public void onSuccess(String s, Call call, Response response) {

                        tipD.dismiss();
                        JsonRequestUserBase requestObj = JsonRequestUserBase.getJsonObj(s);
                        if (requestObj.getCode() == 1) {

                            //是否完善资料
                            if (requestObj.getData().getIs_reg_perfect() == 1) {

                                LoginUtils.doLogin(CuckooMobileLoginActivity.this, requestObj.getData());
                                finish();
                            } else {
                                Intent intent = new Intent(getNowContext(), PerfectRegisterInfoActivity.class);
                                intent.putExtra(PerfectRegisterInfoActivity.USER_LOGIN_INFO, requestObj.getData());
                                startActivity(intent);
                                finish();
                            }
                        }
                        showToastMsg(requestObj.getMsg());
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        tipD.dismiss();
                    }
                });
            }
        });

    }


    /**
     * 发送验证码
     */
    private void sendCode(String str) {
        Api.sendCodeByRegister(str, new JsonCallback() {
            @Override
            public Context getContextToJson() {
                return getNowContext();
            }

            @Override
            public void onSuccess(String s, Call call, Response response) {
                super.onSuccess(s, call, response);
                showToastMsg(ApiUtils.getJsonObj2(s).getString("msg"));
            }
        });
    }

    @Override
    protected boolean hasTopBar() {
        return true;
    }
}
