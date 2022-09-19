package com.goodtech.tq.cityList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMAdSize;
import com.bytedance.msdk.api.v2.GMDislikeCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdLoadCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeExpressAdListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMVideoListener;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.citySearch.viewholder.CityHolder;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.manager.AdFeedManager;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.MessageAlert;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CityListActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "CityListActivity";

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        MobclickAgent.onPause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        if (mAdFeedManager != null) {
            mAdFeedManager.destroy();
        }
        mGMNativeAd = null;

        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    public static void redirectTo(Activity ctx) {
        Intent intent = new Intent(ctx, CityListActivity.class);
        ctx.startActivity(intent);
        ctx.overridePendingTransition(R.anim.in_from_left, R.anim.out_from_right);
    }

    private ImageButton mCloseBtn;
    private Button mCancelBtn;
    private Button mEditBtn;
    private boolean mEdit = false;
    private boolean mHadEdit = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CityListRecyclerAdapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private ArrayList<CityMode> mCityModes;
    private CityListProvider mProvider;
    private CityHolder mShowAnimHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        EventBus.getDefault().register(this);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        mCloseBtn = findViewById(R.id.button_close);
        mCancelBtn = findViewById(R.id.button_city_cancel);
        mEditBtn = findViewById(R.id.button_city_edit);
        mBannerContainer = findViewById(R.id.bannerContainer);

        //noinspection ConstantConditions
        mRecyclerView = findViewById(R.id.recycler_city);
        mLayoutManager = new LinearLayoutManager(CityListActivity.this, RecyclerView.VERTICAL, false);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(CityListActivity.this, R.drawable.material_shadow_z3));

        //adapter
        mProvider = new CityListProvider();
        mAdapter = new CityListRecyclerAdapter(CityListActivity.this, mProvider);
        mAdapter.setOnItemClickListener(new CityListRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CityMode cityMode) {
                if (cityMode.getCid() != 0) {
                    EventBus.getDefault().post(new MessageEvent().setCityIndex(position));
                    finishToRight();
                } else {
                    if (checkPermission()) {
                        MessageAlert alert = new MessageAlert(CityListActivity.this,
                                (dialog, which) -> LocationHelper.getInstance().startWithDelay(CityListActivity.this, true));
                        if (!isFinishing()) {
                            alert.show();
                        }
                    } else {
                        LocationHelper.getInstance().startWithDelay(CityListActivity.this);
                    }
                }
            }

            @Override
            public void onShowDelete(CityHolder holder) {
                if (mShowAnimHolder != null && mShowAnimHolder != holder) {
                    mShowAnimHolder.hideDeleteAnim();
                }
                mShowAnimHolder = holder;
            }

            @Override
            public void onDeleteCity(int position, CityMode cityMode) {
                mProvider.removeItem(position);
                mAdapter.notifyDataSetChanged(true);
            }
        });

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);      // wrap for dragging

        final GeneralItemAnimator animator = new DraggableItemAnimator();

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(CityListActivity.this, R.drawable.list_divider_h), true));

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        setClickListener();

        initAdLoader();
        initNativeExpressAD();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.isSuccessLocation()) {
            BaseApp.getInstance().startIntent(CityListActivity.this);
            mHandler.postDelayed(() -> {
                mProvider.getData();
                mAdapter.notifyDataSetChanged(false);
            }, 100);
        }
        TipHelper.dismissProgressDialog();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void backAction(){
        if (!mEdit) {
            finishToRight();
        }
    }

    private void setClickListener() {

        findViewById(R.id.button_close).setOnClickListener(this);
        //  添加城市
        findViewById(R.id.city_add).setOnClickListener(this);
        findViewById(R.id.button_city_edit).setOnClickListener(this);
        findViewById(R.id.button_city_cancel).setOnClickListener(this);
    }

    private void setEdit(boolean edit) {
        mEdit = edit;
        mAdapter.notifyDataSetChanged(edit);

        if (edit) {
            mEditBtn.setText(getString(R.string.button_done));
            mCancelBtn.setVisibility(View.VISIBLE);
            mCloseBtn.setVisibility(View.GONE);
            findViewById(R.id.city_add).setVisibility(View.GONE);
        } else {
            mEditBtn.setText(getString(R.string.button_edit));
            mCancelBtn.setVisibility(View.GONE);
            mCloseBtn.setVisibility(View.VISIBLE);
            findViewById(R.id.city_add).setVisibility(View.VISIBLE);
        }
        if (mShowAnimHolder != null) {
            mShowAnimHolder.hideDeleteAnim();
            mShowAnimHolder = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_close:
                EventBus.getDefault().post(new MessageEvent().needReload(true));
                finishToRight();
                break;
            case R.id.city_add:
                //  添加城市
                Intent intent = new Intent(CityListActivity.this, CitySearchActivity.class);
                startActivity(intent);
                break;
            case R.id.button_city_edit:
                if (mEdit) {
                    mHadEdit = true;
                    mProvider.saveData();
                }
                //  点击编辑/取消按钮
                setEdit(!mEdit);
                break;
            case R.id.button_city_cancel:
                mProvider.resetData();
                setEdit(false);
                break;
        }
    }

    /**
     * banner
     */

    private FrameLayout mBannerContainer;
    //广告是否加载成功了
    private boolean mIsLoaded;
    //广告加载成功并展示
    private boolean mIsLoadedAndShow;
    //广告管理类
    private AdFeedManager mAdFeedManager;
    // banner广告事件的监听
    private GMNativeAd mGMNativeAd; //原生广告model

    private void initAdLoader() {
        mAdFeedManager = new AdFeedManager(this, new GMNativeAdLoadCallback() {
            @Override
            public void onAdLoaded(List<GMNativeAd> ads) {
                if (ads == null || ads.isEmpty()) {
                    Log.e(TAG, "on FeedAdLoaded: ad is null!");
                    //TToast.show(getContext(), "广告加载失败！");
                    return;
                }
                mIsLoaded = true;
                mGMNativeAd = ads.get(0);
                if (mGMNativeAd != null && !mIsLoadedAndShow) {
                    showAd();
                }
            }

            @Override
            public void onAdLoadedFail(AdError adError) {
                //TToast.show(getContext(), "广告加载失败！");
                Log.e(TAG, "load feed ad error : " + adError.code + ", " + adError.message);
            }
        });
    }

    private void initNativeExpressAD() {
        mIsLoaded = false;
        removeAdView();
        mAdFeedManager.loadAdWithCallback(Constants.PGE_EXPRESS_POS_ID2, 1, GMAdConstant.TYPE_EXPRESS_AD, DeviceUtils.getScreenWidthDpi(this.getApplicationContext()));
    }

    /**
     * 展示原生广告
     */
    private void showAd() {
        if (!mIsLoaded || mAdFeedManager == null || mGMNativeAd == null) {
            //TToast.show(getContext(), "请先加载广告");
            // initNativeExpressAD();
            return;
        }
        if (!mGMNativeAd.isReady()) {
            //TToast.show(getContext(), "广告已经无效，请重新请求");
            // initNativeExpressAD();
            return;
        }
        mIsLoaded = false;
        mIsLoadedAndShow = true;

        mBannerContainer.setVisibility(View.VISIBLE);
        View view = null;
        if (mGMNativeAd.isExpressAd()) { //模板
            view = getExpressAdView(mBannerContainer, mGMNativeAd);
            view.setBackgroundColor(Color.TRANSPARENT);
        } else {
            //TToast.show(requireActivity(), "图片展示样式错误");
        }

        if (view != null) {
            view.setLayoutParams(new
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mBannerContainer.removeAllViews();
            mBannerContainer.addView(view);
        }
    }

    //渲染模板广告
    @SuppressWarnings("RedundantCast")
    private View getExpressAdView(ViewGroup parent, @NonNull final GMNativeAd ad) {
        final ExpressAdViewHolder adViewHolder;
        View convertView = null;
        try {
            convertView = LayoutInflater.from(this).inflate(R.layout.listitem_ad_native_express, parent, false);
            adViewHolder = new ExpressAdViewHolder();
            adViewHolder.mAdContainerView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_express);
            convertView.setTag(adViewHolder);

            //判断是否存在dislike按钮
            if (ad.hasDislike()) {
                ad.setDislikeCallback(this, new GMDislikeCallback() {
                    @Override
                    public void onSelected(int position, String value) {
                        //TToast.show(requireActivity(), "点击 " + value);
                        //用户选择不喜欢原因后，移除广告展示
                        removeAdView();
                    }

                    @Override
                    public void onCancel() {
                        //TToast.show(requireActivity(), "dislike 点击了取消");
                        Log.d(TAG, "dislike 点击了取消");
                    }

                    /**
                     * 拒绝再次提交
                     */
                    @Override
                    public void onRefuse() {

                    }

                    @Override
                    public void onShow() {

                    }
                });
            }

            //设置点击展示回调监听
            ad.setNativeAdListener(new GMNativeExpressAdListener() {
                @Override
                public void onAdClick() {
                    Log.d(TAG, "onAdClick");
                    //TToast.show(requireActivity(), "模板广告被点击");
                }

                @Override
                public void onAdShow() {
                    Log.d(TAG, "onAdShow");
                    //TToast.show(requireActivity(), "模板广告show");

                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    //TToast.show(requireActivity(), "模板广告渲染失败code=" + code + ",msg=" + msg);
                    Log.d(TAG, "onRenderFail   code=" + code + ",msg=" + msg);

                }

                // ** 注意点 ** 不要在广告加载成功回调里进行广告view展示，要在onRenderSucces进行广告view展示，否则会导致广告无法展示。
                @Override
                public void onRenderSuccess(float width, float height) {
                    Log.d(TAG, "onRenderSuccess");
                    //TToast.show(requireActivity(), "模板广告渲染成功:width=" + width + ",height=" + height);
                    //回调渲染成功后将模板布局添加的父View中
                    if (adViewHolder.mAdContainerView != null) {
                        //获取视频播放view,该view SDK内部渲染，在媒体平台可配置视频是否自动播放等设置。
                        int sWidth;
                        int sHeight;
                        /**
                         * 如果存在父布局，需要先从父布局中移除
                         */
                        final View video = ad.getExpressView(); // 获取广告view  如果存在父布局，需要先从父布局中移除
                        if (width == GMAdSize.FULL_WIDTH && height == GMAdSize.AUTO_HEIGHT) {
                            sWidth = FrameLayout.LayoutParams.MATCH_PARENT;
                            sHeight = FrameLayout.LayoutParams.WRAP_CONTENT;
                        } else {
                            sWidth = DeviceUtils.getScreenWidth(BaseApp.getInstance());
                            sHeight = (int) ((sWidth * height) / width);
                        }
                        if (video != null) {
                            /**
                             * 如果存在父布局，需要先从父布局中移除
                             */
                            DeviceUtils.removeFromParent(video);
                            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(sWidth, sHeight);
                            adViewHolder.mAdContainerView.removeAllViews();
                            adViewHolder.mAdContainerView.addView(video, layoutParams);
                        }
                    }
                }
            });


            //视频广告设置播放状态回调（可选）
            ad.setVideoListener(new GMVideoListener() {

                @Override
                public void onVideoStart() {
                    //TToast.show(requireActivity(), "模板广告视频开始播放");
                    Log.d(TAG, "onVideoStart");
                }

                @Override
                public void onVideoPause() {
                    //TToast.show(requireActivity(), "模板广告视频暂停");
                    Log.d(TAG, "onVideoPause");

                }

                @Override
                public void onVideoResume() {
                    //TToast.show(requireActivity(), "模板广告视频继续播放");
                    Log.d(TAG, "onVideoResume");

                }

                @Override
                public void onVideoCompleted() {
                    //TToast.show(requireActivity(), "模板播放完成");
                    Log.d(TAG, "onVideoCompleted");
                }

                @Override
                public void onVideoError(AdError adError) {
                    //TToast.show(requireActivity(), "模板广告视频播放出错");
                    Log.d(TAG, "onVideoError");
                }

                @Override
                public void onProgressUpdate(long l, long l1) {

                }
            });

            ad.render();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private static class ExpressAdViewHolder {
        FrameLayout mAdContainerView;
    }

    private void removeAdView() {
        if (mBannerContainer != null) {
            mBannerContainer.removeAllViews();
        }
    }

}