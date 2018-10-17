package com.uliaovideo.videoline.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.uliaovideo.videoline.R;

public class CuckooShareDialogView extends RelativeLayout implements View.OnClickListener {

    private LinearLayout ll_wechat;
    private LinearLayout ll_qq;
    private LinearLayout ll_qrcode;
    private LinearLayout ll_pyq;

    private CuckooShareDialogViewCallback callback;


    public CuckooShareDialogView(Context context) {
        super(context);
        init(context);
    }

    public CuckooShareDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CuckooShareDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = inflate(context, R.layout.view_share_dialog, null);
        addView(view);

        ll_wechat = view.findViewById(R.id.ll_wechat);
        ll_qq = view.findViewById(R.id.ll_qq);
        ll_qrcode = view.findViewById(R.id.ll_qrcode);
        ll_pyq = view.findViewById(R.id.ll_pyq);

        ll_pyq.setOnClickListener(this);
        ll_wechat.setOnClickListener(this);
        ll_qq.setOnClickListener(this);
        ll_qrcode.setOnClickListener(this);

    }

    public void setShowItem(boolean isQQ, boolean isWechat, boolean isQrcode) {

        ll_qq.setVisibility(isQQ ? VISIBLE : GONE);
        ll_qrcode.setVisibility(isQrcode ? VISIBLE : GONE);
        ll_pyq.setVisibility(isWechat ? VISIBLE : GONE);
        ll_wechat.setVisibility(isWechat ? VISIBLE : GONE);
    }


    public CuckooShareDialogViewCallback getCallback() {
        return callback;
    }

    public void setCallback(CuckooShareDialogViewCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.ll_wechat:
                clickWechat();
                break;
            case R.id.ll_qq:
                clickQQ();
                break;
            case R.id.ll_qrcode:
                clickQrcode();
                break;
            case R.id.ll_pyq:
                clickPyq();
                break;
        }
    }

    private void clickPyq() {
        if (callback != null) {
            callback.onClickPyq();
        }
    }

    private void clickQrcode() {
        if (callback != null) {
            callback.onClickQrcode();
        }
    }

    private void clickQQ() {
        if (callback != null) {
            callback.onClickQQ();
        }
    }

    private void clickWechat() {
        if (callback != null) {
            callback.onClickWeChat();
        }
    }

    public interface CuckooShareDialogViewCallback {

        void onClickWeChat();

        void onClickQQ();

        void onClickQrcode();


        void onClickPyq();
    }


}