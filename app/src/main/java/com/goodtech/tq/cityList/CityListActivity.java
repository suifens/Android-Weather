package com.goodtech.tq.cityList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.citySearch.viewholder.CityHolder;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.models.CityMode;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class CityListActivity extends BaseActivity implements View.OnClickListener {

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
                    finish();

                } else {

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
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        } else {
            mEditBtn.setText(getString(R.string.button_edit));
            mCancelBtn.setVisibility(View.GONE);
            mCloseBtn.setVisibility(View.VISIBLE);
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
                finish();
                overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
                break;
            case R.id.city_add:
                //  添加城市
                Intent intent = new Intent(CityListActivity.this, CitySearchActivity.class);
                startActivity(intent);
                break;
            case R.id.button_city_edit:
                if (mEdit) {
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
}
