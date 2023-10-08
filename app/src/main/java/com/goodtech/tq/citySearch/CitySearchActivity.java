package com.goodtech.tq.citySearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.activity.MainActivity;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.utils.Utils;
import com.goodtech.tq.views.LocationAlert;

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
    private boolean mFirstLoad = true;
    private boolean isRefresh;

    public static void redirectTo(Context ctx, boolean isStart) {
        Log.e(TAG, "onStartWeather: " + System.currentTimeMillis());
        Intent intent = new Intent(ctx, CitySearchActivity.class);
        intent.putExtra(EXTRA_START, isStart);
        ctx.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (mRecommendHeaderView != null) {
            mRecommendHeaderView.hideSoftInput(this);
            mRecommendHeaderView.onStop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecommendHeaderView != null) {
            mRecommendHeaderView.onStart();
        }
        Log.e(TAG, "onResume: " + System.currentTimeMillis());
    }

    private void toGetLocation() {
        if (!SpUtils.getInstance().isAgreePermission()) {
            showPermissionDialog(this, view -> toGetLocation());
            return;
        }
        if (checkPermission()) {
            LocationHelper.getInstance().startWithDelay(CitySearchActivity.this, true);
//            LocationAlert alert = new LocationAlert(CitySearchActivity.this,
//                    (dialog, which) -> {
//                        LocationHelper.getInstance().startWithDelay(CitySearchActivity.this, true);
//                    });
//            alert.setCancelListener(((dialog, which) -> {
//                //  取消定位权限判断的时间
//                SpUtils.getInstance().putLong(Constants.TIME_LOCATION_CANCEL, System.currentTimeMillis());
//            }));
//
//            if (!isFinishing()) {
//                alert.show();
//            }
        } else {
            LocationHelper.getInstance().startWithDelay(CitySearchActivity.this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);
        Button mCancelBtn = findViewById(R.id.search_btn_cancel);
        if (getIntent().getBooleanExtra(EXTRA_START, false)) {
            mCancelBtn.setVisibility(View.GONE);
            isStart = true;
            if (SpUtils.getInstance().isAgreePermission()) {
                mHandler.postDelayed(() -> {
                    BaseApp.getInstance().startUsingApp(CitySearchActivity.this);
                }, 500);
            }
        }

        initSearchView();

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        init();
    }

    private void init() {

        ((ViewStub) findViewById(R.id.stub_view)).inflate();

        //  定位
        mRecommendHeaderView = findViewById(R.id.header_recommend);
        mRecommendHeaderView.setListener(new CityRecommendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CityMode cityMode) {
                if (cityMode != null && cityMode.getCid() != 0) {
                    addCity(cityMode);
                } else if (!CitySearchActivity.this.isFinishing()) {
                    toGetLocation();
                }
            }

            @Override
            public void onRefreshClick() {
                isRefresh = true;
                toGetLocation();
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
                if (cityMode != null && cityMode.getCid() != 0) {
                    addCity(cityMode);
                }
            }

            @Override
            public void onRefreshClick() {

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
        mSearchAdapter.setOnItemClickListener((view, position, cityMode) -> addCity(cityMode));
        mSearchListView.setAdapter(mSearchAdapter);

        mEmptyView = findViewById(R.id.layout_no_data);

        findViewById(R.id.search_btn_cancel).setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.isSuccessLocation()) {
            mHandler.post(() -> mRecommendHeaderView.updateLocation());

            BaseApp.getInstance().startIntent(CitySearchActivity.this);

            if (LocationSpHelper.getLocation() != null && !isRefresh) {
                if (!CitySearchActivity.this.isFinishing()) {
                    TipHelper.showProgressDialog(this);
                }
                //  能够获取到定位
                mHandler.postDelayed(() -> {
                    Log.e(TAG, "message activity");
                    Intent intent = new Intent(CitySearchActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finishToRight();
                    TipHelper.dismissProgressDialog();
                }, 500);
                if (mRecommendHeaderView != null) {
                    mRecommendHeaderView.hideSoftInput(this);
                }
                return;
            }
        }
        TipHelper.dismissProgressDialog();
    }

    private void addCity(CityMode cityMode) {
        if (cityMode != null) {
            int index = LocationSpHelper.addCity(cityMode);
            if (index == -1) {
                WeatherHttpHelper helper = new WeatherHttpHelper(getApplicationContext());
                helper.getBaseUrl(() -> helper.fetchWeather(cityMode));
                EventBus.getDefault().post(new MessageEvent().addCity(true));
            } else {
                EventBus.getDefault().post(new MessageEvent().setCityIndex(index));
            }
        }

        isStart = false;

        if (mRecommendHeaderView != null) {
            mRecommendHeaderView.hideSoftInput(this);
        }

        TipHelper.showProgressDialog(this);
        mHandler.postDelayed(() -> {
            Intent intent = new Intent(CitySearchActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finishToRight();
        }, 200);
    }

    private void initSearchView() {
        // 输入搜索关键字
        SearchView mSearchView = findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);
        //设置SearchView默认为展开显示
        mSearchView.setIconified(false);
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.clearFocus();
        SearchView.SearchAutoComplete textView = mSearchView.findViewById(R.id.search_src_text);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.search_btn_cancel) {
            finishToRight();
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
        private final int space;  //位移间距

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NotNull Rect outRect, @NotNull View view,
                                   RecyclerView parent, @NotNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) % 3 == 0) {
                outRect.left = 0; //第一列左边贴边
            } else {
                if (parent.getChildAdapterPosition(view) % 3 == 1) {
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
