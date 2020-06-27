package com.goodtech.tq.cityList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.citySearch.CitySearchActivity;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.models.CityMode;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class CityListActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public static void redirectTo(Activity ctx) {
        Intent intent = new Intent(ctx, CityListActivity.class);
        ctx.startActivity(intent);
        ctx.overridePendingTransition(R.anim.in_from_left, R.anim.out_from_right);
    }

    private Button mEditBtn;
    private boolean mEdit = false;

    private RecyclerView mRecyclerView;
    private CityListRecyclerAdapter mAdapter;
    private ArrayList<CityMode> mCityModes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        mEditBtn = findViewById(R.id.button_city_edit);
        mRecyclerView = findViewById(R.id.recycler_city);

        mAdapter = new CityListRecyclerAdapter(this, mCityModes);
        mAdapter.setOnItemClickListener(new CityListRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CityMode cityMode) {
                
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        setClickListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.notifyDataSetChanged(LocationSpHelper.getCityListAndLocation());
    }

    private void setClickListener() {

        findViewById(R.id.button_close).setOnClickListener(this);
        //  添加城市
        findViewById(R.id.city_add).setOnClickListener(this);
        findViewById(R.id.button_city_edit).setOnClickListener(this);
    }

    private void setEdit(boolean edit) {
        mEdit = edit;
        mEditBtn.setText(getString(edit ? R.string.button_edit : R.string.button_cancel));
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
                //  点击编辑/取消按钮
                setEdit(!mEdit);
                break;
        }
    }
}
