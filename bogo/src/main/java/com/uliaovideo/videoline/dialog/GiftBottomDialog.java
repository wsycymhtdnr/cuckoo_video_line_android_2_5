package com.uliaovideo.videoline.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.recycler.RecycleViewGiftItemAdapter;
import com.uliaovideo.videoline.adapter.recycler.RecyclerGiftCount;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.json.JsonRequestBase;
import com.uliaovideo.videoline.json.JsonRequestDoPrivateSendGif;
import com.uliaovideo.videoline.json.JsonRequestGetGiftList;
import com.uliaovideo.videoline.manage.SaveData;
import com.uliaovideo.videoline.modle.GiftModel;
import com.lzy.okgo.callback.StringCallback;
import com.qmuiteam.qmui.widget.QMUIPagerAdapter;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by 魏鹏 on 2018/3/6.
 * email:1403102936@qq.com
 * 山东布谷鸟网络科技有限公司著
 */

public class GiftBottomDialog extends BottomSheetDialog implements View.OnClickListener {

    private QMUIViewPager qmuiViewPager;
    private QMUIPagerAdapter qmuiPagerAdapter;
    private RecyclerView mRvGiftCountList;
    private RecyclerGiftCount mGiftCountAdapter;

    private List<String> mGiftCountList = new ArrayList<>();
    private List<GiftModel> giftModelList = new ArrayList<>();
    private List<View> giftPageItemList = new ArrayList<>();

    private DoSendGiftListen doSendGiftListen;
    //0 正常聊天赠送礼物 1 一对一视频赠送礼物
    private int type = 0;
    private String chanelId = "";


    public static final String TO_USER_ID = "TO_USER_ID";
    public static final String TYPE = "TYPE";

    private String giftId;
    private String toUserId;


    public GiftBottomDialog(@NonNull Context context, String toUserId) {
        super(context, R.style.dialogGift);

        this.toUserId = toUserId;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_gift, null);
        setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //BGViewUtil.setBackgroundDrawable(getContentView(), new BGDrawable().color(Color.parseColor("#50FFFFFF")));

        initView();
        initData();
    }

    private void initData() {

        requestGetGift();
    }

    private void initView() {

        //礼物数量
        mRvGiftCountList = findViewById(R.id.rv_count_list);
        QMUIRoundButton qmuiRoundButton = findViewById(R.id.btn_send);
        qmuiRoundButton.setOnClickListener(this);

        LinearLayoutManager countLayoutManage = new LinearLayoutManager(getContext());
        countLayoutManage.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRvGiftCountList.setLayoutManager(countLayoutManage);

        mGiftCountList.add("5");
        mGiftCountList.add("10");
        mGiftCountList.add("15");
        mGiftCountList.add("20");
        mGiftCountList.add("30");

        mGiftCountAdapter = new RecyclerGiftCount(getContext(), mGiftCountList);
        mRvGiftCountList.setAdapter(mGiftCountAdapter);


        qmuiViewPager = findViewById(R.id.view_page);
        qmuiPagerAdapter = new QMUIPagerAdapter() {
            @Override
            protected Object hydrate(ViewGroup container, int position) {
                return giftPageItemList.get(position);
            }

            @Override
            protected void populate(ViewGroup container, Object item, int position) {

                container.addView(giftPageItemList.get(position));
            }

            @Override
            protected void destroy(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                return giftPageItemList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        };
        qmuiViewPager.setAdapter(qmuiPagerAdapter);
    }


    public void setType(int type) {
        this.type = type;
    }

    public void setChanelId(String chanelId) {
        this.chanelId = chanelId;
    }

    private void requestGetGift() {

        Api.doRequestGetGift(new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                JsonRequestGetGiftList jsonObj = (JsonRequestGetGiftList) JsonRequestBase.getJsonObj(s, JsonRequestGetGiftList.class);
                if (jsonObj.getCode() == 1) {
                    giftModelList.clear();
                    giftModelList.addAll(jsonObj.getList());
                    refreshGiftAdapter();
                } else {
                    ToastUtils.showLong(jsonObj.getMsg());
                }
            }
        });
    }

    //刷新礼物列表
    private void refreshGiftAdapter() {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_gift_recycleview, null);
        RecyclerView giftListView = view.findViewById(R.id.rv_content_list);
        final RecycleViewGiftItemAdapter giftItemAdapter = new RecycleViewGiftItemAdapter(getContext(), giftModelList);
        giftListView.setAdapter(giftItemAdapter);
        giftListView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        giftItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                giftItemAdapter.setSelected(position);

                giftId = giftModelList.get(position).getId();
                mGiftCountAdapter.setSelected(-1);
            }
        });
        giftPageItemList.add(view);

        qmuiViewPager.setAdapter(qmuiPagerAdapter);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_send:
                doSendGift();
                break;

            default:
                break;
        }
    }

    public void setDoSendGiftListen(DoSendGiftListen doSendGiftListent) {
        this.doSendGiftListen = doSendGiftListent;
    }

    private void doSendGift() {

        String uid = SaveData.getInstance().getId();
        String token = SaveData.getInstance().getToken();

        String count = "1";
        if (mGiftCountAdapter.getSelected() != -1) {

            count = mGiftCountList.get(mGiftCountAdapter.getSelected());
        }
        Api.doSendGift(uid, token, count, toUserId, giftId, chanelId, new StringCallback() {

            @Override
            public void onSuccess(String s, Call call, Response response) {

                JsonRequestDoPrivateSendGif jsonObj = (JsonRequestDoPrivateSendGif)
                        JsonRequestBase.getJsonObj(s, JsonRequestDoPrivateSendGif.class);
                if (jsonObj.getCode() == 1) {

                    if (doSendGiftListen != null) {
                        doSendGiftListen.onSuccess(jsonObj);
                    }
                } else {

                    ToastUtils.showLong(jsonObj.getMsg());
                }

            }
        });
    }

    public interface DoSendGiftListen {

        void onSuccess(JsonRequestDoPrivateSendGif sendGif);
    }
}
