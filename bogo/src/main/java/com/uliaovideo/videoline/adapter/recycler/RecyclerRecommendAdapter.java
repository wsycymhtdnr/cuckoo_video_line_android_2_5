package com.uliaovideo.videoline.adapter.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.uliaovideo.videoline.widget.BGLevelTextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.api.ApiUtils;
import com.uliaovideo.videoline.helper.SelectResHelper;
import com.uliaovideo.videoline.json.jsonmodle.TargetUserData;
import com.uliaovideo.videoline.utils.StringUtils;
import com.uliaovideo.videoline.utils.Utils;

import java.util.List;

/**
 * RecyclerView-recommendPage适配器
 * Created by wp on 2017/12/28 0028.
 */
public class RecyclerRecommendAdapter extends BaseQuickAdapter<TargetUserData,BaseViewHolder>{
    private Context context;
    private int itemWidth;
    private int itemHeight;
    private int dp1;
    private int margin;
    //private final int margin;

    //构造方法,用于传入数据参数
    public RecyclerRecommendAdapter(Context context,@Nullable List<TargetUserData> data) {
        super(R.layout.adapter_user,data);
        this.context = context;

        dp1 = ConvertUtils.dp2px(1);
        margin = dp1 * 2;
        itemWidth = ScreenUtils.getScreenWidth() / 2 - dp1 * 8;
        itemHeight = itemWidth + itemWidth / 3;
    }
    @Override
    protected void convert(BaseViewHolder helper, TargetUserData item) {

        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemWidth);
        params.setMargins(margin,margin,margin,margin);
        helper.getConvertView().setLayoutParams(params);
        //初始化数据显示
        if (ApiUtils.isTrueUrl(item.getAvatar())){
            Utils.loadHttpImg(context,Utils.getCompleteImgUrl(item.getAvatar()), (ImageView) helper.getView(R.id.pagemsg_background));
        }
        helper.setImageResource(R.id.pagemsg_view_dian,SelectResHelper.getOnLineRes(StringUtils.toInt(item.getIs_online())));
        helper.setText(R.id.pagemsg_view_isonline,StringUtils.toInt(item.getIs_online()) == 1 ? "在线" : "离线");
        helper.setText(R.id.pagemsg_view_nice,item.getEvaluation());
        helper.setText(R.id.pagemsg_view_name,item.getUser_nickname());

        ((BGLevelTextView)helper.getView(R.id.tv_level)).setLevelInfo(item.getSex(),item.getLevel());
    }

}