package com.uliaovideo.videoline.adapter.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ScreenUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.manage.RequestConfig;
import com.uliaovideo.videoline.modle.GiftModel;
import com.uliaovideo.videoline.utils.Utils;

import java.util.List;

/**
 * Created by 魏鹏 on 2018/3/6.
 * email:1403102936@qq.com
 * 山东布谷鸟网络科技有限公司著
 */

public class RecycleViewGiftItemAdapter extends BaseQuickAdapter<GiftModel,BaseViewHolder> {

    private int itemWidth = 0;
    private int selected = -1;
    private Context context;

    public RecycleViewGiftItemAdapter(Context context,@Nullable List<GiftModel> data) {
        super(R.layout.item_gift,data);
        itemWidth = ScreenUtils.getScreenWidth() / 4;
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, GiftModel item) {

        View view = helper.getConvertView();
        view.setLayoutParams(new RelativeLayout.LayoutParams(itemWidth,itemWidth));
        Utils.loadHttpImg(context,Utils.getCompleteImgUrl(item.getImg()), (ImageView) helper.getView(R.id.iv_img));
        helper.setText(R.id.tv_name,item.getName());
        helper.setText(R.id.tv_coin,item.getCoin() + RequestConfig.getConfigObj().getCurrency());

        helper.setVisible(R.id.iv_selected,helper.getPosition() == selected);

    }

    public void setSelected(int selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }
}
