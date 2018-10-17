package com.uliaovideo.videoline.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.ToastUtils;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.PayMenuAdapter;
import com.uliaovideo.videoline.adapter.recycler.RecycleViewRechargeRuleAdapter;
import com.uliaovideo.videoline.alipay.AlipayService;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.event.EWxPayResultCodeComplete;
import com.uliaovideo.videoline.json.JsonRequestBase;
import com.uliaovideo.videoline.json.JsonRequestGetRechargeRule;
import com.uliaovideo.videoline.json.JsonRequestRecharge;
import com.uliaovideo.videoline.modle.ConfigModel;
import com.uliaovideo.videoline.modle.PayMenuModel;
import com.uliaovideo.videoline.modle.RechargeRuleModel;
import com.uliaovideo.videoline.paypal.PayPalHandle;
import com.uliaovideo.videoline.utils.StringUtils;
import com.uliaovideo.videoline.utils.Utils;
import com.uliaovideo.videoline.wxpay.WChatPayService;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzy.okgo.callback.StringCallback;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.qmuiteam.qmui.widget.QMUITopBar;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author 魏鹏
 * email 1403102936@qq.com
 * 山东布谷鸟网络科技有限公司著
 * @dw 充值列表
 */
public class RechargeActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

    private QMUITopBar qmuiTopBar;
    private RecyclerView mRvRechargeRule, mRvRechargePayMenu;
    private Button mBtnRecharge,mBtnPayPal;

    private static final String TAG = "RechargeActivity";
    private RecycleViewRechargeRuleAdapter mRechargeRuleAdapter;

    private List<RechargeRuleModel> mRechargeRuleDataList = new ArrayList<>();
    private List<PayMenuModel> mRechargePayMenuDataList = new ArrayList<>();

    private int selectItemPos = -1;
    private int selectPayItemPos = -1;
    private PayMenuAdapter payMenuAdapter;


    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_recharge;
    }

    @Override
    protected void initView() {

        qmuiTopBar = findViewById(R.id.qmui_top_bar);
        mBtnRecharge = findViewById(R.id.btn_recharge);
        mBtnPayPal = findViewById(R.id.btn_pay_pal);
        mRvRechargePayMenu = findViewById(R.id.rv_content_list_pay);
        mRvRechargeRule = findViewById(R.id.rv_recharge_rule_list);

        mRvRechargeRule.setLayoutManager(new GridLayoutManager(this, 2));
        mRvRechargeRule.setAdapter(mRechargeRuleAdapter = new RecycleViewRechargeRuleAdapter(this, mRechargeRuleDataList));
        mRechargeRuleAdapter.setOnItemClickListener(this);

        mRvRechargePayMenu.setLayoutManager(new LinearLayoutManager(this));

        //支付列表适配
        payMenuAdapter = new PayMenuAdapter(mRechargePayMenuDataList);
        mRvRechargePayMenu.setAdapter(payMenuAdapter);
        payMenuAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                selectPayItemPos = position;
                payMenuAdapter.setSelectPos(position);
            }
        });
        if(ConfigModel.getInitData().getOpen_pay_pal() == 1){
            mBtnPayPal.setVisibility(View.VISIBLE);
        }

        mBtnRecharge.setOnClickListener(this);
        mBtnPayPal.setOnClickListener(this);
    }

    @Override
    protected void initSet() {
        initTopBar();
    }

    private void initTopBar() {

        qmuiTopBar.addLeftImageButton(R.drawable.icon_back_black, R.id.all_backbtn).setOnClickListener(this);
        qmuiTopBar.setTitle(getString(R.string.recharge));
    }

    @Override
    protected void initData() {
        if(ConfigModel.getInitData().getOpen_pay_pal() == 1){
            PayPalHandle.getInstance().startPayPalService(this);
        }
        requestData();
    }


    @Override
    protected void initPlayerDisplayData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.all_backbtn:
                finishNow();
                break;
            case R.id.btn_recharge:
                startPay();
                //showToastMsg("sdk已集成待配置相关KEY信息!");
                break;
            case R.id.btn_pay_pal:
                clickPayPal();
                break;
            default:

                break;
        }
    }

    //PayPal支付
    private void clickPayPal() {

        if (mRechargeRuleDataList.size() == 0 || selectItemPos == -1) {
            showToastMsg(getString(R.string.please_chose_recharge_rule));
            return;
        }

        RechargeRuleModel rechargeRuleModel = mRechargeRuleDataList.get(selectItemPos);
        if(rechargeRuleModel.getPay_pal_money().equals("")){
            ToastUtils.showLong("未设置PayPal支付价格");
            return;
        }
        PayPalHandle.getInstance().doPayPalPay(this,rechargeRuleModel.getPay_pal_money());
    }

    private void startPay() {

        if (mRechargeRuleDataList.size() == 0 || selectItemPos == -1) {
            showToastMsg(getString(R.string.please_chose_recharge_rule));
            return;
        }
        if (mRechargePayMenuDataList.size() == 0 || selectPayItemPos == -1) {
            showToastMsg(getString(R.string.please_chose_recharge_type));
            return;
        }

        showLoadingDialog(getString(R.string.loading_now_submit_order));
        RechargeRuleModel rechargeRuleModel = mRechargeRuleDataList.get(selectItemPos);
        String pid = mRechargePayMenuDataList.get(selectPayItemPos).getId();
        Api.doRequestCharge(uId, uToken, rechargeRuleModel.getId(), pid, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                hideLoadingDialog();
                JsonRequestRecharge jsonObj = (JsonRequestRecharge) JsonRequestBase.getJsonObj(s, JsonRequestRecharge.class);
                if (jsonObj.getCode() == 1) {

                    payService(jsonObj);
                } else {
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

    private void payService(JsonRequestRecharge jsonObj) {

        if (StringUtils.toInt(jsonObj.getPay().getIs_wap()) == 1) {
            //从其他浏览器打开
            Utils.openWeb(this, jsonObj.getPay().getPost_url());
            return;
        }

        int type = StringUtils.toInt(jsonObj.getPay().getType());
        if (type == 1) {

            AlipayService alipayService = new AlipayService(this);
            alipayService.payV2(jsonObj.getPay().getPay_info());
        } else {
            WChatPayService alipayService = new WChatPayService(this);
            alipayService.callWxPay(JSON.parseObject(jsonObj.getPay().getPay_info()));
        }
    }

    //获取充值页面数据
    private void requestData() {

        Api.doRequestGetChargeRule(uId, uToken, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                JsonRequestGetRechargeRule jsonObj =
                        (JsonRequestGetRechargeRule) JsonRequestBase.getJsonObj(s, JsonRequestGetRechargeRule.class);

                if (jsonObj.getCode() == 1) {
                    mRechargeRuleDataList.addAll(jsonObj.getList());
                    mRechargeRuleAdapter.notifyDataSetChanged();

                    mRechargePayMenuDataList.clear();
                    mRechargePayMenuDataList.addAll(jsonObj.getPay_list());
                    payMenuAdapter.notifyDataSetChanged();
                } else {

                    showToastMsg(jsonObj.getMsg());
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm =
                    data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i(TAG, confirm.toJSONObject().toString(4));
                    Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                    //这里可以把PayPal带回来的json数据传给服务器以确认你的款项是否收到或者收全
                    //可以直接把 confirm.toJSONObject() 这个带给服务器，
                    showLoadingDialog("正在获取支付结果...");
                    //得到服务器返回的结果，你就可以跳转成功页面或者做相应的处理了
                    PayPalHandle.getInstance().confirmPayResult(RechargeActivity.this,
                            requestCode,resultCode,data,mRechargeRuleDataList.get(selectItemPos).getId(),new PayPalHandle.DoResult(){

                        @Override
                        public void confirmSuccess() {
                            hideLoadingDialog();
                            ToastUtils.showLong("支付成功！");
                        }

                        @Override
                        public void confirmNetWorkError() {
                            hideLoadingDialog();
                        }

                        @Override
                        public void customerCanceled() {
                            hideLoadingDialog();
                            ToastUtils.showLong("取消支付！");
                        }

                        @Override
                        public void confirmFuturePayment() {

                            hideLoadingDialog();
                        }

                        @Override
                        public void invalidPaymentConfiguration() {

                            hideLoadingDialog();
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "The user canceled.");
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i(
                    TAG,
                    "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ConfigModel.getInitData().getOpen_pay_pal() == 1){
            PayPalHandle.getInstance().stopPayPalService(this);
        }
    }

    /**
     * @param context 上下文
     * @dw 跳转充值页面
     */
    public static void startRechargeActivity(Context context) {
        Intent intent = new Intent(context, RechargeActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

        mRechargeRuleAdapter.setSelectId(mRechargeRuleDataList.get(position).getId());
        selectItemPos = position;
    }
}
