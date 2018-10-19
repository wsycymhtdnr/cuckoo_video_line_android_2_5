package com.uliaovideo.videoline.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.blankj.utilcode.util.ConvertUtils;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.FragAdapter;
import com.uliaovideo.videoline.base.BaseActivity;
import com.uliaovideo.videoline.fragment.CharmFragment;
import com.uliaovideo.videoline.fragment.IndexTabAttentionFragment;
import com.uliaovideo.videoline.fragment.MoneyFragment;
import com.uliaovideo.videoline.fragment.NewPeopleFragment;
import com.uliaovideo.videoline.fragment.OnlineFragment;
import com.uliaovideo.videoline.fragment.RecommendFragment;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUIViewPager;

import java.util.ArrayList;
//排行界面
public class CuckooOrderActivity extends BaseActivity {
    //功能
    private QMUIViewPager rollViewViewpage;
    private QMUITabSegment rollTabSegment;
    private FragAdapter mFragAdapter;

    @Override
    protected Context getNowContext() {
        return this;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_cuckoo_order;
    }

    @Override
    protected void initView() {

        rollTabSegment = findViewById(R.id.tab_segment);
        rollViewViewpage = findViewById(R.id.roll_view_viewpage);
        ArrayList<Fragment> fragmentList = new ArrayList();
        fragmentList.add(new CharmFragment());
        fragmentList.add(new MoneyFragment());

        ArrayList<String> titleList = new ArrayList();
        titleList.add(getString(R.string.charm));
        titleList.add(getString(R.string.caiqi));

        rollViewViewpage.setOffscreenPageLimit(1);
        mFragAdapter = new FragAdapter(getSupportFragmentManager(),fragmentList);
        mFragAdapter.setTitleList(titleList);
        rollViewViewpage.setAdapter(mFragAdapter);

        //设置字体大小
        rollTabSegment.setTabTextSize(ConvertUtils.dp2px(12));
        //设置 Tab 选中状态下的颜色
        rollTabSegment.setDefaultSelectedColor(getResources().getColor(R.color.admin_color));
        //关联viewPage
        rollTabSegment.setupWithViewPager(rollViewViewpage,true,false);
        rollViewViewpage.setCurrentItem(0);
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
}
