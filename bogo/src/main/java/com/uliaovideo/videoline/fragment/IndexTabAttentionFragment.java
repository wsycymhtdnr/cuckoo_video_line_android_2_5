package com.uliaovideo.videoline.fragment;


import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.uliaovideo.chat.ui.ChatActivity;
import com.uliaovideo.videoline.adapter.recycler.RecyclerRecommendAdapter;
import com.uliaovideo.videoline.api.Api;
import com.uliaovideo.videoline.base.BaseListFragment;
import com.uliaovideo.videoline.json.JsonRequestsTarget;
import com.uliaovideo.videoline.json.jsonmodle.TargetUserData;
import com.uliaovideo.videoline.manage.SaveData;
import com.uliaovideo.videoline.modle.UserModel;
import com.uliaovideo.videoline.ui.HomePageActivity;
import com.uliaovideo.videoline.ui.common.Common;
import com.uliaovideo.videoline.utils.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzy.okgo.callback.StringCallback;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Response;

/**
 * 首页关注列表
 */
public class IndexTabAttentionFragment extends BaseListFragment<TargetUserData> {

    @Override
    protected void initDate(View view) {

    }

    @Override
    protected void initSet(View view) {

    }

    @Override
    protected void initDisplayData(View view) {

    }

    @Override
    protected void requestGetData(boolean isCache) {

        Api.doRequestGetAttentionList(SaveData.getInstance().getId(),page,new StringCallback(){

            @Override
            public void onSuccess(String s, Call call, Response response) {
                mSwRefresh.setRefreshing(false);

                JsonRequestsTarget requestObj = JsonRequestsTarget.getJsonObj(s);
                if (requestObj.getCode() == 1){
                    onLoadDataResult(requestObj.getData());
                }else{
                    showToastMsg(getContext(),requestObj.getMsg());
                }
            }

            @Override
            public void onError(Call call, Response response, Exception e) {
                super.onError(call, response, e);
                mSwRefresh.setRefreshing(false);

            }
        });
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManage() {
        return new GridLayoutManager(getContext(),2);
    }

    @Override
    protected BaseQuickAdapter getBaseQuickAdapter() {
        return new RecyclerRecommendAdapter(getContext(),dataList);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

        Common.jumpUserPage(getContext(),dataList.get(position).getId());
    }
}
