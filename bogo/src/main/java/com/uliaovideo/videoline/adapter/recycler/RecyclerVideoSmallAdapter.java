package com.uliaovideo.videoline.adapter.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uliaovideo.videoline.MyApplication;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.api.ApiUtils;
import com.uliaovideo.videoline.inter.AdapterOnItemClick;
import com.uliaovideo.videoline.json.jsonmodle.VideoModel;
import com.uliaovideo.videoline.utils.Utils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * recycler-video-small适配器
 */

public class RecyclerVideoSmallAdapter extends BaseQuickAdapter<VideoModel,BaseViewHolder>{


    public RecyclerVideoSmallAdapter(@Nullable List<VideoModel> data) {
        super(R.layout.adapter_video_list,data);
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoModel item) {
        //图片背景和标题

        helper.setText(R.id.adapter_video_title,item.getTitle());
        //数据显示
        helper.setText(R.id.left_start_number,item.getViewed());
        helper.setText(R.id.left_love_number,item.getFollow_num());
        //是否付费
        helper.setGone(R.id.videolist_masking,item.getStatus().equals("2"));

        if(item.getStatus().equals("2")){
            Utils.loadHttpImgBlue(MyApplication.getInstances(),item.getImg(), (ImageView) helper.getView(R.id.adapter_video_image),0);
        }else{
            Utils.loadHttpImg(MyApplication.getInstances(),Utils.getCompleteImgUrl(item.getImg()), (ImageView) helper.getView(R.id.adapter_video_image));
        }
    }
}
