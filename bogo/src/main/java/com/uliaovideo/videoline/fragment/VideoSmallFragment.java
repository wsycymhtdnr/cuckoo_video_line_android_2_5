package com.uliaovideo.videoline.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.uliaovideo.videoline.R;
import com.uliaovideo.videoline.adapter.FragAdapter;
import com.uliaovideo.videoline.api.ApiUtils;
import com.uliaovideo.videoline.base.BaseFragment;
import com.uliaovideo.videoline.helper.ImageUtil;
import com.uliaovideo.videoline.ui.PushShortVideoActivity;
import com.uliaovideo.videoline.ui.VideoRecordActivity;
import com.uliaovideo.videoline.utils.BGVideoFile;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUIViewPager;
import com.qmuiteam.qmui.widget.popup.QMUIListPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小视频
 * Created by fly on 2017/12/28 0028.
 */
public class VideoSmallFragment extends BaseFragment {
    //功能
    private QMUIViewPager rollViewViewpage;//viewPager
    private QMUITopBar rollTopBar;//标题
    private QMUITabSegment rollTabSegment;//顶部bar
    private View topBarView;
    private QMUIListPopup mPopup;//Popup框

    private List fragmentList;//fragment
    private List titleList;//标题

    private FragAdapter mFragAdapter;//适配器

    ////////////////////////////////////////////初始化操作////////////////////////////////////////////
    @Override
    protected View getBaseView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_index_videosmall, container, false);
    }

    @Override
    protected void initView(View view) {
        //顶部bar布局
        topBarView = LayoutInflater.from(getContext()).inflate(R.layout.view_top_page_small, null);
        rollTabSegment = topBarView.findViewById(R.id.mQMUITabSegment);
        rollTopBar = view.findViewById(R.id.video_tab_segment);
        rollViewViewpage = view.findViewById(R.id.video_view_viewpage);
    }

    @Override
    protected void initDate(View view) {
        fragmentList = new ArrayList();
        fragmentList.add(getAboutFragment(ApiUtils.VideoType.reference));
        fragmentList.add(getAboutFragment(ApiUtils.VideoType.latest));
        fragmentList.add(getAboutFragment(ApiUtils.VideoType.attention));
        //fragmentList.add(getAboutFragment(ApiUtils.VideoType.near));

        titleList = new ArrayList();
        titleList.add(getString(R.string.recommend));
        titleList.add(getString(R.string.novice));
        titleList.add(getString(R.string.follow));
        //titleList.add("附近");
    }

    @Override
    protected void initSet(View view) {
        //设置topbar中布局
        rollTopBar.setCenterView(topBarView);
        //右布局
        rollTopBar.addRightImageButton(R.drawable.produce_video_entrance, R.id.prize_to_upload).setOnClickListener(this);

        rollViewViewpage.setOffscreenPageLimit(3);

        mFragAdapter = new FragAdapter(getChildFragmentManager(), fragmentList);
        mFragAdapter.setTitleList(titleList);
        rollViewViewpage.setAdapter(mFragAdapter);

        //设置字体大小
        rollTabSegment.setTabTextSize(ConvertUtils.dp2px(12));
        //设置 Tab 选中状态下的颜色
        rollTabSegment.setDefaultSelectedColor(getResources().getColor(R.color.admin_color));
        //关联viewPage
        rollTabSegment.setupWithViewPager(rollViewViewpage, true, false);
    }

    @Override
    protected void initDisplayData(View view) {

    }

    ////////////////////////////////////////////监听方法处理//////////////////////////////////////////
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prize_to_upload:
                initListPopupIfNeed();
                mPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);//显示动画
                mPopup.setPreferredDirection(QMUIPopup.DIRECTION_BOTTOM);//位置
                mPopup.show(v);//关联控件并显示
                break;
        }
    }


    ////////////////////////////////////////////本地工具方法//////////////////////////////////////////

    /**
     * 根据不同的type获取不同的VideoRecyclerFragment
     *
     * @param type 类型
     * @return VideoRecyclerFragment
     */
    private Fragment getAboutFragment(String type) {
        VideoRecyclerFragment vf = new VideoRecyclerFragment();
        vf.setType(type);
        return vf;
    }

    /**
     * 初始化一个Popup框
     */
    private void initListPopupIfNeed() {
        if (mPopup == null) {
            final String[] listItems = new String[]{
                    getString(R.string.right_text_top),
                    getString(R.string.right_text_bottom)
            };
            int[] imgs = new int[]{
                    R.drawable.icon_record_video,
                    R.drawable.icon_upload_video
            };
            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < imgs.length; i++) {
                Map<String, Object> listem = new HashMap<String, Object>();
                listem.put("img", imgs[i]);
                listem.put("text", listItems[i]);
                data.add(listem);
            }
            String[] from = {"text", "img"};
            int[] to = {R.id.right_text_top, R.id.left_img_top};
            SimpleAdapter simAdapter = new SimpleAdapter(getContext(), data, R.layout.adapter_popuplist, from, to);
            mPopup = new QMUIListPopup(getContext(), QMUIPopup.DIRECTION_NONE, simAdapter);
            mPopup.create(QMUIDisplayHelper.dp2px(getContext(), 140), QMUIDisplayHelper.dp2px(getContext(), 100), new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    showToastMsg(getContext(), listItems[i]);
                    if (i == 0) {
                        /*Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        //设置视频录制的最长时间
                        intent.putExtra (MediaStore.EXTRA_DURATION_LIMIT,30);
                        //设置视频录制的画质
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        startActivityForResult (intent, 1);*/
                        Intent intent = new Intent(getContext(), VideoRecordActivity.class);
                        startActivity(intent);
                    } else {


                        clickSelectVideo();
                    }
                    mPopup.dismiss();
                }
            });
            //消失监听
            mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                }
            });
        }
    }

    //选择本地视频文件
    private void clickSelectVideo() {

        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofVideo())
                .selectionMode(PictureConfig.SINGLE)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == -1 && requestCode == PictureConfig.CHOOSE_REQUEST) {
                //录制监听
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                LogUtils.i("选择视频");
                if (selectList.size() > 0) {
                    LocalMedia videoFile = selectList.get(0);
                    Bitmap thumb = BGVideoFile.getVideoThumbnail(videoFile.getPath());
                    if (thumb != null) {

                        File thumbFile = ImageUtil.getSaveFile(thumb, new File(videoFile.getPath()).getName());
                        //发布编辑
                        Intent intent = new Intent(getContext(), PushShortVideoActivity.class);
                        intent.putExtra(PushShortVideoActivity.VIDEO_PATH, videoFile.getPath());
                        intent.putExtra(PushShortVideoActivity.VIDEO_COVER_PATH, thumbFile.getCanonicalPath());
                        startActivity(intent);

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
