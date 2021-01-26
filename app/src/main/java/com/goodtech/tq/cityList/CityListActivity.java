package com.goodtech.tq.cityList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.citySearch.viewholder.CityHolder;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.Constants;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;

public class CityListActivity extends BaseActivity implements View.OnClickListener, UnifiedBannerADListener {

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

        if (bv != null) {
            bv.destroy();
        }

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
                if (cityMode.cid != 0) {
                    EventBus.getDefault().post(new MessageEvent().setCityIndex(position));
                    finishToRight();
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

        //  banner
//        configBanner();

        setClickListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
//        this.getBanner().loadAD();
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

    ViewGroup bannerContainer;
    UnifiedBannerView bv;
    String posId;

    private void configBanner() {
        bannerContainer = (ViewGroup) this.findViewById(R.id.bannerContainer);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (bv != null) {
            bv.setLayoutParams(getUnifiedBannerLayoutParams());
        }
    }

    private UnifiedBannerView getBanner() {
        if(this.bv != null){
            bannerContainer.removeView(bv);
            bv.destroy();
        }
        String posId = Constants.BANNER_ID;
        this.posId = posId;
        this.bv = new UnifiedBannerView(this, posId, this);
        // 不需要传递tags使用下面构造函数
        // this.bv = new UnifiedBannerView(this, Constants.APPID, posId, this);
        bannerContainer.addView(bv, getUnifiedBannerLayoutParams());
        return this.bv;
    }

    /**
     * banner2.0规定banner宽高比应该为6.4:1 , 开发者可自行设置符合规定宽高比的具体宽度和高度值
     *
     * @return
     */
    private FrameLayout.LayoutParams getUnifiedBannerLayoutParams() {
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        return new FrameLayout.LayoutParams(screenSize.x,  Math.round(screenSize.x / 6.4F));
    }

    @Override
    public void onNoAD(AdError adError) {
        String msg = String.format(Locale.getDefault(), "onNoAD, error code: %d, error msg: %s",
                adError.getErrorCode(), adError.getErrorMsg());
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onADReceive() {
        Log.i(TAG, "onADReceive");
    }

    @Override
    public void onADExposure() {
        Log.i(TAG, "onADExposure");
    }

    @Override
    public void onADClosed() {
        Log.i(TAG, "onADClosed");
    }

    @Override
    public void onADClicked() {
        Log.i(TAG, "onADClicked : " + (bv.getExt() != null? bv.getExt().get("clickUrl") : ""));
    }

    @Override
    public void onADLeftApplication() {
        Log.i(TAG, "onADLeftApplication");
    }

    @Override
    public void onADOpenOverlay() {
        Log.i(TAG, "onADOpenOverlay");
    }

    @Override
    public void onADCloseOverlay() {
        Log.i(TAG, "onADCloseOverlay");
    }

}