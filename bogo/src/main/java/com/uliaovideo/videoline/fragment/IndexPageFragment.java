package com.uliaovideo.videoline.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.ConvertUtils;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.FragAdapter;
import com.uliaovideo.videoline.base.BaseFragment;
import com.uliaovideo.videoline.modle.ConfigModel;
import com.uliaovideo.videoline.modle.StarLevelModel;
import com.uliaovideo.videoline.ui.CuckooSearchActivity;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUIViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * 排行-推荐 页
 * Created by 魏鹏 on 2017/12/27 0027.
 */

public class IndexPageFragment extends BaseFragment {
    //功能
    private QMUIViewPager rollViewViewpage;
    private QMUITopBar rollTopBar;
    private QMUITabSegment rollTabSegment;
    private View topBarView;

    private List fragmentList;
    private List titleList;

    private FragAdapter mFragAdapter;

    @Override
    protected View getBaseView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_index_roll, container, false);
    }

    @Override
    protected void initView(View view) {

        rollTopBar = view.findViewById(R.id.roll_tab_segment);
        rollViewViewpage = view.findViewById(R.id.roll_view_viewpage);

        //顶部bar布局
        topBarView = LayoutInflater.from(getContext()).inflate(R.layout.view_top_page, null);
        rollTabSegment = topBarView.findViewById(R.id.mQMUITabSegment);

    }

    @Override
    protected void initDate(View view) {
        fragmentList = new ArrayList();
        fragmentList.add(new IndexTabAttentionFragment());
        fragmentList.add(new RecommendFragment());
        fragmentList.add(new NewPeopleFragment());
        //fragmentList.add(new CharmFragment());
        //fragmentList.add(new MoneyFragment());
        //fragmentList.add(new SameCityFragment());
        fragmentList.add(new OnlineFragment());

        titleList = new ArrayList();
        titleList.add(getString(R.string.follow));
        titleList.add(getString(R.string.recommend));
        titleList.add(getString(R.string.novice));
        titleList.add(getString(R.string.on_line));
        //titleList.add("魅力");
        //titleList.add("财气");
        //titleList.add("同城");

        ArrayList<StarLevelModel> list = ConfigModel.getInitData().getStar_level_list();
        if (list != null) {
            for (StarLevelModel item : list) {

                StarLevelListFragment levelListFragment = new StarLevelListFragment();
                Bundle intent = new Bundle();
                intent.putString(StarLevelListFragment.LEVEL_ID, item.getId());
                levelListFragment.setArguments(intent);
                fragmentList.add(levelListFragment);
                titleList.add(item.getLevel_name());
            }
        }

    }

    @Override
    protected void initSet(View view) {
        rollTopBar.setCenterView(topBarView);
        rollViewViewpage.setOffscreenPageLimit(1);
        mFragAdapter = new FragAdapter(getChildFragmentManager(), fragmentList);
        mFragAdapter.setTitleList(titleList);
        rollViewViewpage.setAdapter(mFragAdapter);

        //设置字体大小
        rollTabSegment.setTabTextSize(ConvertUtils.dp2px(12));
        //设置 Tab 选中状态下的颜色
        rollTabSegment.setDefaultSelectedColor(getResources().getColor(R.color.admin_color));
        //关联viewPage
        rollTabSegment.setupWithViewPager(rollViewViewpage, true, false);
        rollViewViewpage.setCurrentItem(1);
    }

    @Override
    protected void initDisplayData(View view) {

    }

    @OnClick({R.id.iv_search})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_search:
                clickSearch();
                break;

            default:
                break;
        }
    }

    private void clickSearch() {
        Intent intent = new Intent(getContext(), CuckooSearchActivity.class);
        startActivity(intent);
    }
}
