package com.goodtech.tq.modules.others.video.fragment;

import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.viewpager.widget.ViewPager;

import com.goodtech.tq.R;
import com.goodtech.tq.modules.others.video.render.TikTokRenderViewFactory;
import com.goodtech.tq.modules.others.video.VerticalViewPager;
import com.goodtech.tq.modules.others.video.VideoPagerAdapter;
import com.goodtech.tq.modules.others.video.bean.TiktokBean;
import com.goodtech.tq.modules.others.video.cache.PreloadManager;
import com.goodtech.tq.modules.others.video.cache.ProxyVideoCacheManager;
import com.goodtech.tq.modules.others.video.controller.TikTokController;

import java.util.ArrayList;
import java.util.List;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.player.VideoViewManager;
import xyz.doikki.videoplayer.util.L;

public class VideoPagerFragment extends VideoBaseFragment {

    /**
     * 当前播放位置
     */
    private int mCurPos;
    private List<TiktokBean> mVideoList = new ArrayList<>();
    private VideoPagerAdapter mVideoPagerAdapter;
    private VerticalViewPager mViewPager;
    private PreloadManager mPreloadManager;
    private TikTokController mController;
    private VideoView mVideoView;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_video_pager;
    }

    protected VideoViewManager getVideoViewManager() {
        return VideoViewManager.instance();
    }

    @Override
    protected void initView() {
        super.initView();
        initViewPager();
        initVideoView();
        mPreloadManager = PreloadManager.getInstance(requireContext());

        mCurPos = 0;

        mViewPager.setCurrentItem(mCurPos);

        mViewPager.post(() -> startPlay(mCurPos));
    }

    @Override
    protected void initData() {
        super.initData();
    }

    private void initVideoView() {
        mVideoView = new VideoView(requireContext());
        mVideoView.setLooping(true);

        //以下只能二选一，看你的需求
        mVideoView.setRenderViewFactory(TikTokRenderViewFactory.create());
//        mVideoView.setScreenScaleType(VideoView.SCREEN_SCALE_CENTER_CROP);

        mController = new TikTokController(requireContext());
        mVideoView.setVideoController(mController);
    }

    private void initViewPager() {
        mViewPager = findViewById(R.id.vvpager);
        mViewPager.setOffscreenPageLimit(4);
        mVideoPagerAdapter = new VideoPagerAdapter(mVideoList);
        mViewPager.setAdapter(mVideoPagerAdapter);
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            private int mCurItem;

            /**
             * VerticalViewPager是否反向滑动
             */
            private boolean mIsReverseScroll;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (position == mCurItem) {
                    return;
                }
                mIsReverseScroll = position < mCurItem;
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == mCurPos) return;
                startPlay(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == VerticalViewPager.SCROLL_STATE_DRAGGING) {
                    mCurItem = mViewPager.getCurrentItem();
                }

                if (state == VerticalViewPager.SCROLL_STATE_IDLE) {
                    mPreloadManager.resumePreload(mCurPos, mIsReverseScroll);
                } else {
                    mPreloadManager.pausePreload(mCurPos, mIsReverseScroll);
                }
            }
        });
    }

    private void startPlay(int position) {
        int count = mViewPager.getChildCount();
        for (int i = 0; i < count; i ++) {
            View itemView = mViewPager.getChildAt(i);
            VideoPagerAdapter.ViewHolder viewHolder = (VideoPagerAdapter.ViewHolder) itemView.getTag();
            if (viewHolder.mPosition == position) {
                mVideoView.release();
                removeViewFormParent(mVideoView);

                TiktokBean tiktokBean = mVideoList.get(position);
                String playUrl = mPreloadManager.getPlayUrl(tiktokBean.videoDownloadUrl);
                L.i("startPlay: " + "position: " + position + "  url: " + playUrl);
                mVideoView.setUrl(playUrl);
                //请点进去看isDissociate的解释
                mController.addControlComponent(viewHolder.mTikTokView, true);
                viewHolder.mPlayerContainer.addView(mVideoView, 0);
                mVideoView.start();
                mCurPos = position;
                break;
            }
        }
    }

    public void addData(View view) {
        // mVideoList.addAll(DataUtil.getTiktokDataFromAssets(this));
        mVideoPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPreloadManager.removeAllPreloadTask();
        //清除缓存，实际使用可以不需要清除，这里为了方便测试
        ProxyVideoCacheManager.clearAllCache(requireContext());
    }

    /**
     * 将View从父控件中移除
     */
    private void removeViewFormParent(View v) {
        if (v == null) return;
        ViewParent parent = v.getParent();
        if (parent instanceof FrameLayout) {
            ((FrameLayout) parent).removeView(v);
        }
    }
}
