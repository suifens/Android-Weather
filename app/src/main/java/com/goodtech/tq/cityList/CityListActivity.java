package com.goodtech.tq.cityList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.msdk.adapter.TToast;
import com.bytedance.msdk.api.AdError;
import com.bytedance.msdk.api.nativeAd.TTNativeAdAppInfo;
import com.bytedance.msdk.api.nativeAd.TTViewBinder;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMAdDislike;
import com.bytedance.msdk.api.v2.GMDislikeCallback;
import com.bytedance.msdk.api.v2.ad.banner.GMBannerAdListener;
import com.bytedance.msdk.api.v2.ad.banner.GMBannerAdLoadCallback;
import com.bytedance.msdk.api.v2.ad.banner.GMNativeAdInfo;
import com.bytedance.msdk.api.v2.ad.banner.GMNativeToBannerListener;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMViewBinder;
import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.MainActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.citySearch.viewholder.CityHolder;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.manager.AdBannerManager;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DownloadConfirmHelper;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.MessageAlert;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class CityListActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "CityListActivity";

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (mAdBannerManager != null) {
            mAdBannerManager.onResume();
        }
    }

    @Override
    protected void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        MobclickAgent.onPause(this);
        super.onPause();
        if (mAdBannerManager != null) {
            mAdBannerManager.onPause();
        }
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

        if (mAdBannerManager != null) {
            mAdBannerManager.destroy();
        }

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
                                (dialog, which) -> openLocationPermission(false));
                        if (!isFinishing()) {
                            alert.show();
                        }
                    } else {
                        TipHelper.showProgressDialog(CityListActivity.this, false);
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

        initListener();
        initAdLoader();
        //  banner
        configBanner();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.isSuccessLocation()) {
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
    private AdBannerManager mAdBannerManager;
    // banner广告事件的监听
    private GMBannerAdListener mAdBannerListener;

    private void configBanner() {
        mBannerContainer = this.findViewById(R.id.bannerContainer);
        mIsLoadedAndShow = true;
        clearStatus();
        if (mAdBannerListener != null) {
            mAdBannerManager.loadAdWithCallback(Constants.PGE_BANNER_POS_ID);
        }
    }

    private void initAdLoader() {
        mAdBannerManager = new AdBannerManager(this, new GMBannerAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(com.bytedance.msdk.api.AdError adError) {
                TToast.show(CityListActivity.this, "广告加载失败");
                mIsLoaded = false;
                Log.e(TAG, "load banner ad error : " + adError.code + ", " + adError.message);
                mBannerContainer.removeAllViews();
                mAdBannerManager.printLoadFailAdnInfo();// 获取本次waterfall加载中，加载失败的adn错误信息。
            }

            @Override
            public void onAdLoaded() {
                TToast.show(CityListActivity.this, "广告加载成功");
                Log.i(TAG, "banner load success ");
                mIsLoaded = true;
                if (mIsLoadedAndShow) {
                    showBannerAd();
                }
                mAdBannerManager.printLoadAdInfo(); //已经加载广告的信息
            }
        }, mAdBannerListener);
    }

    /**
     * 清除状态
     */
    private void clearStatus() {
        //重置load标识
        mIsLoaded = false;
        //清空banner父容器
        mBannerContainer.removeAllViews();
    }

    private void initListener() {
        
        mAdBannerListener = new GMBannerAdListener() {

            @Override
            public void onAdOpened() {
                Log.d(TAG, "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "onAdClosed");
                if (mBannerContainer != null) {
                    mBannerContainer.removeAllViews();
                }
                if (mAdBannerManager != null && mAdBannerManager.getBannerAd() != null) {
                    mAdBannerManager.getBannerAd().destroy();
                }
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "onAdClicked");
            }

            @Override
            public void onAdShow() {
                Log.d(TAG, "onAdShow");
                mIsLoaded = false;
                if (mAdBannerManager != null) {
                    mAdBannerManager.printShowAdInfo();//已经展示的广告信息
                }
            }

            /**
             * show失败回调。如果show时发现无可用广告（比如广告过期），会触发该回调。
             * 开发者应该结合自己的广告加载、展示流程，在该回调里进行重新加载。
             * @param adError showFail的具体原因
             */
            @Override
            public void onAdShowFail(AdError adError) {
                Log.d(TAG, "onAdShowFail");
                mIsLoaded = false;
            }
        };
    }

    /**
     * 展示广告
     */
    private void showBannerAd() {
        /**
         * 加载成功才能展示
         */
        if (mIsLoaded && mAdBannerManager != null) {
            /**
             * 在添加banner的View前需要清空父容器
             */
            mBannerContainer.removeAllViews();
            if (mAdBannerManager.getBannerAd() != null) {
                // 在调用getBannerView之前，可以选择使用isReady进行判断，当前是否有可用广告。
                if (!mAdBannerManager.getBannerAd().isReady()) {
                    // TToast.show(this, "广告已经无效，建议重新请求");
                    return;
                }
                //横幅广告容器的尺寸必须至少与横幅广告一样大。如果您的容器留有内边距，实际上将会减小容器大小。如果容器无法容纳横幅广告，则横幅广告不会展示
                /**
                 * mBannerViewAd.getBannerView()一个广告对象只能调用一次，第二次为null
                 */
                View view = mAdBannerManager.getBannerAd().getBannerView();
                if (view != null) {
                    mBannerContainer.addView(view);
                } else {
                    // TToast.show(this, "请重新加载广告");
                }
            }
        } else {
            // TToast.show(this, "请先加载广告");
        }
    }

}