package com.goodtech.tq.modules.citySearch;

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

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItemV2;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.services.poisearch.PoiResultV2;
import com.amap.api.services.poisearch.PoiSearchV2;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.goodtech.tq.R;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.activity.MainActivity;
import com.goodtech.tq.app.App;
import com.goodtech.tq.eventbus.CityEvent;
import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CitySearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, View.OnClickListener, PoiSearchV2.OnPoiSearchListener {

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

    private PoiSearchV2 mPoiSearch = null;

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
        } else {
            LocationHelper.getInstance().startWithDelay(CitySearchActivity.this, isRefresh);
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
        }

        initSearchView();

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        init();

        ServiceSettings.updatePrivacyShow(this, true, true);
        if (SpUtils.getInstance().isAgreePermission()) {
            ServiceSettings.updatePrivacyAgree(this, true);
        }
    }

    private void init() {

        ((ViewStub) findViewById(R.id.stub_view)).inflate();

        //  定位
        mRecommendHeaderView = findViewById(R.id.header_recommend);
        mRecommendHeaderView.setListener(new CityRecommendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CityMode cityMode) {
                if (cityMode != null && !cityMode.getPoiId().isEmpty()) {
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
                if (cityMode != null && !cityMode.getPoiId().isEmpty()) {
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
        int screenWidth = ScreenUtils.getScreenWidth(); //屏幕宽度
        int itemWidth = Utils.dp2pxInt(100); //每个item的宽度
        mRecommendView.addItemDecoration(new SpaceItemDecoration((screenWidth - itemWidth * 3) / 6));

        //  搜索列表
        mSearchListView = findViewById(R.id.recycler_search);
        mSearchListView.setVisibility(View.GONE);
        mSearchAdapter = new CityRecyclerAdapter(this, null);
        mSearchAdapter.setOnItemClickListener((view, position, poiItem) -> {
            CityMode cityMode = new CityMode();
            cityMode.setCity(poiItem.getCityName());
            cityMode.setMergerName(poiItem.getTitle());
            cityMode.setCid(Integer.parseInt(poiItem.getAdCode()));
            cityMode.setLat(String.valueOf(poiItem.getLatLonPoint().getLatitude()));
            cityMode.setLon(String.valueOf(poiItem.getLatLonPoint().getLongitude()));
            addCity(cityMode);
        });
        mSearchListView.setAdapter(mSearchAdapter);

        mEmptyView = findViewById(R.id.layout_no_data);

        findViewById(R.id.search_btn_cancel).setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.isSuccessLocation()) {
            mHandler.post(() -> mRecommendHeaderView.updateLocation());
            // 手动刷新定位后，通知首页刷新城市列表与顶部城市信息
            EventBus.getDefault().post(new MessageEvent().needReload(true));
            EventBus.getDefault().post(new CityEvent().setCityIndex(0));

            App.instance.startIntent(CitySearchActivity.this);

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
            isRefresh = false;
        }
        TipHelper.dismissProgressDialog();
    }

    private void addCity(CityMode cityMode) {
        if (cityMode != null) {
            int index = LocationSpHelper.addCity(cityMode);
            if (index == -1) {
                WeatherHttpHelper helper = new WeatherHttpHelper(getApplicationContext());
                helper.getBaseUrl(() -> helper.fetchWeather(cityMode));
                EventBus.getDefault().post(new CityEvent().addCity(true));
            } else {
                EventBus.getDefault().post(new CityEvent().setCityIndex(index));
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
        SearchView.SearchAutoComplete textView = mSearchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
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
//            ArrayList<CityMode> list = DatabaseHelper.getInstance(this).queryCity(s);
            mRecommendView.setVisibility(View.GONE);
            mRecommendHeaderView.setVisibility(View.GONE);

            // 创建PoiSearch对象
            try {
                mPoiSearch = new PoiSearchV2(this, null);
                mPoiSearch.setOnPoiSearchListener(this);
            } catch (AMapException e) {
                throw new RuntimeException(e);
            }

            // 创建查询对象
            PoiSearchV2.Query query = new PoiSearchV2.Query(s, "", "");
            // 设置每页数量
            query.setPageSize(10);
            // 设置页码
            query.setPageNum(0);

            // 设置查询参数并开始搜索
            mPoiSearch.setQuery(query);
            mPoiSearch.searchPOIAsyn();

        } else if (mRecommendView != null) {
            mSearchListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
            mRecommendView.setVisibility(View.VISIBLE);
            mRecommendHeaderView.setVisibility(View.VISIBLE);
        }
        return false;
    }

    //<editor-fold desc="location search">
    @Override
    public void onPoiSearched(PoiResultV2 poiResultV2, int i) {
        Log.e(TAG, "onPoiSearched: " );
        if (poiResultV2.getPois().isEmpty()) {
            mSearchListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mSearchListView.setVisibility(View.VISIBLE);
            mSearchAdapter.notifyDataSetChanged(poiResultV2.getPois());
            mEmptyView.setVisibility(View.GONE);
            for (PoiItemV2 item : poiResultV2.getPois()) {
                Log.e(TAG, "onPoiSearched: adName = " + item.getAdName());
                Log.e(TAG, "onPoiSearched: cityName = " + item.getCityName());
                Log.e(TAG, "onPoiSearched: getProvinceName = " + item.getProvinceName());
                Log.e(TAG, "onPoiSearched: " + item.getLatLonPoint());
                Log.e(TAG, "onPoiSearched: getTitle = " + item.getTitle());
                Log.e(TAG, "onPoiSearched: " + item.getSnippet());
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItemV2 poiItemV2, int i) {
        Log.e(TAG, "onPoiItemSearched: " );
    }
    //</editor-fold>

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
