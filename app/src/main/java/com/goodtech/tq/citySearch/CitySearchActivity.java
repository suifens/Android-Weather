package com.goodtech.tq.citySearch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.MainActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.utils.Utils;
import com.goodtech.tq.views.MessageAlert;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CitySearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, View.OnClickListener {

    private static final String TAG = "CitySearchActivity";
    private static final String EXTRA_START = "extra_start";

    private RecyclerView mRecommendView;
    private RecyclerView mSearchListView;
    private CityRecyclerAdapter mSearchAdapter;
    private CityRecommendHeaderView mRecommendHeaderView;

    private View mEmptyView;
    private boolean isStart;

    public static void redirectTo(Context ctx, boolean isStart) {
        Intent intent = new Intent(ctx, CitySearchActivity.class);
        intent.putExtra(EXTRA_START, isStart);
        ctx.startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isStart) {
            WeatherApp.getInstance().startLocation();

            if (LocationSpHelper.getLocation().cid != 0) {
                isStart = false;
                //  能够获取到定位
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TipHelper.showProgressDialog(CitySearchActivity.this);
                        Log.e(TAG, "run: resume activity");
                        Intent intent = new Intent(CitySearchActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }, 1000);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);
        initSearchView();

        EventBus.getDefault().register(this);

        Button mCancelBtn = findViewById(R.id.search_btn_cancel);
        if (getIntent().getBooleanExtra(EXTRA_START, false)) {
            mCancelBtn.setVisibility(View.GONE);
            isStart = true;
        }

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        mRecommendHeaderView = findViewById(R.id.header_recommend);
        mRecommendHeaderView.setListener(new CityRecommendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CityMode cityMode) {
                Log.d(TAG, "recommend onItemClick: view " + view + " position = " + position);

                if (cityMode != null && cityMode.cid != 0) {
                    addCity(cityMode);
                } else {
                    if (checkLocationPermission()) {
                        TipHelper.showProgressDialog(CitySearchActivity.this, false);
                        WeatherApp.getInstance().requestLocation();
                        mHandler.post(mCheckTicker);
                    } else {
                        MessageAlert alert = new MessageAlert(CitySearchActivity.this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestLocationPermissions();
                            }
                        });
                        alert.show();
                    }
                }
            }
        });

        //  推荐列表
        mRecommendView = findViewById(R.id.recycler_recommend);
        mRecommendView.setVisibility(View.VISIBLE);
        final ArrayList<CityMode> recommends = CityHelper.getRecommends(this);
        CityRecommendAdapter mRecommendAdapter = new CityRecommendAdapter(this, recommends);
        mRecommendAdapter.setOnItemClickListener(new CityRecommendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CityMode cityMode) {
                Log.d(TAG, "recommend onItemClick: view " + view + " position = " + position);

                if (cityMode != null && cityMode.cid != 0) {
                    addCity(cityMode);
                }
            }
        });
        mRecommendView.setAdapter(mRecommendAdapter);
        //  样式
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mRecommendView.setLayoutManager(gridLayoutManager);
        int screenWidth = DeviceUtils.getScreenWidth(this); //屏幕宽度
        int itemWidth = Utils.dp2pxInt(100); //每个item的宽度
        mRecommendView.addItemDecoration(new SpaceItemDecoration((screenWidth - itemWidth * 3) / 6));

        //  搜索列表
        mSearchListView = findViewById(R.id.recycler_search);
        mSearchListView.setVisibility(View.GONE);
        mSearchAdapter = new CityRecyclerAdapter(this, null);
        mSearchAdapter.setOnItemClickListener(new CityRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CityMode cityMode) {
                Log.d(TAG, "recommend onItemClick: view " + view + " position = " + position);

                addCity(cityMode);
            }
        });
        mSearchListView.setAdapter(mSearchAdapter);

        mEmptyView = findViewById(R.id.layout_no_data);

        findViewById(R.id.search_btn_cancel).setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.isSuccessLocation()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRecommendHeaderView.updateLocation();
                }
            });

            if (isStart && LocationSpHelper.getLocation().cid != 0) {
                TipHelper.showProgressDialog(this);
                isStart = false;
                //  能够获取到定位
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TipHelper.dismissProgressDialog();
                        Log.e(TAG, "message activity");
                        Intent intent = new Intent(CitySearchActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }, 1000);
                return;
            }
        }
        TipHelper.dismissProgressDialog();
    }

    private void addCity(CityMode cityMode) {
        if (cityMode != null) {
            boolean success = LocationSpHelper.addCity(cityMode);
            if (success) {
                WeatherHttpHelper helper = new WeatherHttpHelper(getApplicationContext());
                helper.fetchWeather(cityMode);

                EventBus.getDefault().post(new MessageEvent().addCity(true));
            }
        }

        isStart = false;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(CitySearchActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }, 200);
    }

    private void initSearchView() {
        // 输入搜索关键字
        SearchView mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);
        //设置SearchView默认为展开显示
        mSearchView.setIconified(false);
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(false);
        SearchView.SearchAutoComplete textView = mSearchView.findViewById(R.id.search_src_text);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.search_btn_cancel) {
            this.finish();
        }
    }

    /**
     * 搜索城市
     */
    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Log.d(TAG, "onQueryTextSubmit: " + s);
        if (!TextUtils.isEmpty(s)) {
            ArrayList<CityMode> list = DatabaseHelper.getInstance(this).queryCity(s);
            mRecommendView.setVisibility(View.GONE);
            mRecommendHeaderView.setVisibility(View.GONE);
            if (list != null && list.size() > 0) {
                mSearchListView.setVisibility(View.VISIBLE);
                mSearchAdapter.notifyDataSetChanged(list);
                mEmptyView.setVisibility(View.GONE);
            } else {
                mSearchListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        } else if (mRecommendView != null) {
            mSearchListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
            mRecommendView.setVisibility(View.VISIBLE);
            mRecommendHeaderView.setVisibility(View.VISIBLE);
        }
        return false;
    }

    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int space;  //位移间距
        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NotNull Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) %3 == 0) {
                outRect.left = 0; //第一列左边贴边
            } else {
                if (parent.getChildAdapterPosition(view) %3 == 1) {
                    outRect.left = space;//第二列移动一个位移间距
                } else {
                    outRect.left = space * 2;//由于第二列已经移动了一个间距，所以第三列要移动两个位移间距就能右边贴边，且item间距相等
                }
            }

            if (parent.getChildAdapterPosition(view) >= 3) {
                outRect.top = Utils.dp2pxInt(10);
            } else {
                outRect.top = 0;
            }
        }

    }
}
