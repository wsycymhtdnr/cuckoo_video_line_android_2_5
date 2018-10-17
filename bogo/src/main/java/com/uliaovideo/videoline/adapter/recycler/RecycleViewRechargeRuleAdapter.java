package com.uliaovideo.videoline.adapter.recycler;

import android.content.Context;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.modle.RechargeRuleModel;
import com.uliaovideo.videoline.utils.StringUtils;

import java.util.List;

/**
 * Created by 魏鹏 on 2018/3/2.
 * @author 山东布谷鸟网络科技有限公司著
 * @dw 充值规则
 */

public class RecycleViewRechargeRuleAdapter extends BaseQuickAdapter<RechargeRuleModel,BaseViewHolder> {

    private Context mContext;
    private String selectId;

    public RecycleViewRechargeRuleAdapter(Context context, @Nullable List<RechargeRuleModel> data) {
        super(R.layout.item_recharge_rule,data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, RechargeRuleModel item) {

        helper.setText(R.id.item_tv_txt,item.getMoney() + "(" +  item.getFormatCoin() + ")");
        if(StringUtils.toInt(item.getGive()) != 0){
            helper.setText(R.id.item_tv_give,"赠送：" + item.getGive());
            helper.setGone(R.id.item_tv_give,true);
        }else{

            helper.setGone(R.id.item_tv_give,false);
        }

        if(StringUtils.toInt(selectId) == StringUtils.toInt(item.getId())){
            helper.setTextColor(R.id.item_tv_txt,mContext.getResources().getColor(R.color.admin_color));
            helper.setBackgroundRes(R.id.ll_content,R.drawable.bg_recharge_rule_item_select);
        }else{
            helper.setTextColor(R.id.item_tv_txt,mContext.getResources().getColor(R.color.black));
            helper.setBackgroundRes(R.id.ll_content,R.drawable.bg_recharge_rule_item);
        }
    }

    public void setSelectId(String selectId) {
        this.selectId = selectId;
        notifyDataSetChanged();
    }
}
