package com.uliaovideo.videoline.fragment;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ConvertUtils;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.FragAdapter;
import com.uliaovideo.videoline.base.BaseFragment;

import java.util.ArrayList;

public class IndexOrderFragment extends BaseFragment{

    //功能
    private QMUIViewPager rollViewViewpage;
    private QMUITabSegment rollTabSegment;
    private FragAdapter mFragAdapter;


    @Override
    protected View getBaseView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_cuckoo_order,null);
    }

    @Override
    protected void initView(View view) {

        rollTabSegment = view.findViewById(R.id.tab_segment);
        rollViewViewpage = view.findViewById(R.id.roll_view_viewpage);
        ArrayList<Fragment> fragmentList = new ArrayList();
        fragmentList.add(new CharmFragment());
        fragmentList.add(new MoneyFragment());

        ArrayList<String> titleList = new ArrayList();
        titleList.add(getString(R.string.charm));
        titleList.add(getString(R.string.caiqi));

        rollViewViewpage.setOffscreenPageLimit(1);
        mFragAdapter = new FragAdapter(getChildFragmentManager(),fragmentList);
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
    protected void initDate(View view) {

    }

    @Override
    protected void initSet(View view) {

    }

    @Override
    protected void initDisplayData(View view) {

    }
}
