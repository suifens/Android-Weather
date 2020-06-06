package com.goodtech.tq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

public class CityListActivity extends BaseActivity implements View.OnClickListener {

    private Button mEditBtn;
    private boolean mEdit = false;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        mEditBtn = findViewById(R.id.button_city_edit);
        mRecyclerView = findViewById(R.id.recycler_city);
        setClickListener();
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
                break;
            case R.id.city_add:
                //  添加城市

                break;
            case R.id.button_city_edit:
                //  点击编辑/取消按钮
                setEdit(!mEdit);
                break;
        }
    }
}
