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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;

public class CitySearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, View.OnClickListener {

    private static final String TAG = "CitySearchActivity";
    private static final String EXTRA_START = "extra_start";

    private RecyclerView mRecommendView;
    private RecyclerView mSearchListView;
    private LocalCitySearchAdapter mLocalSearchAdapter;
    private CityRecommendHeaderView mRecommendHeaderView;

    private View mEmptyView;
    private boolean isStart;
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

    @Override
    protected void onDestroy() {
        CitySearchHelper.cancel(this);
        super.onDestroy();
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
        configStationBar(findViewById(R.id.private_station_bar));
        init();
    }

    private void init() {
        ((ViewStub) findViewById(R.id.stub_view)).inflate();

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
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mRecommendView.setLayoutManager(gridLayoutManager);
        int screenWidth = ScreenUtils.getScreenWidth();
        int itemWidth = Utils.dp2pxInt(100);
        mRecommendView.addItemDecoration(new SpaceItemDecoration((screenWidth - itemWidth * 3) / 6));

        mSearchListView = findViewById(R.id.recycler_search);
        mSearchListView.setVisibility(View.GONE);
        mSearchListView.setLayoutManager(new LinearLayoutManager(this));
        mLocalSearchAdapter = new LocalCitySearchAdapter();
        mLocalSearchAdapter.setOnItemClickListener(this::addCity);
        mSearchListView.setAdapter(mLocalSearchAdapter);

        mEmptyView = findViewById(R.id.layout_no_data);
        findViewById(R.id.search_btn_cancel).setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.isSuccessLocation()) {
            mHandler.post(() -> mRecommendHeaderView.updateLocation());
            EventBus.getDefault().post(new MessageEvent().needReload(true));
            EventBus.getDefault().post(new CityEvent().setCityIndex(0));

            App.instance.startIntent(CitySearchActivity.this);

            if (LocationSpHelper.getLocation() != null && !isRefresh) {
                if (mRecommendHeaderView != null) {
                    mRecommendHeaderView.hideSoftInput(this);
                }
                navigateToMain();
                return;
            }
            isRefresh = false;
        }
        TipHelper.dismissProgressDialog();
    }

    private void addCity(CityMode cityMode) {
        if (cityMode == null) {
            return;
        }

        int index = LocationSpHelper.addCity(cityMode);
        if (index == -1) {
            WeatherHttpHelper helper = new WeatherHttpHelper(getApplicationContext());
            helper.getBaseUrl(() -> helper.fetchWeather(cityMode));
            ArrayList<CityMode> allCities = LocationSpHelper.getCityListAndLocation();
            index = Math.max(allCities.size() - 1, 0);
            EventBus.getDefault().post(new CityEvent().setCityIndex(index));
        } else {
            EventBus.getDefault().post(new CityEvent().setCityIndex(index));
        }

        isStart = false;

        if (mRecommendHeaderView != null) {
            mRecommendHeaderView.hideSoftInput(this);
        }

        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(CitySearchActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishToRight();
    }

    private void initSearchView() {
        SearchView mSearchView = findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);
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

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (!TextUtils.isEmpty(s)) {
            mRecommendView.setVisibility(View.GONE);
            mRecommendHeaderView.setVisibility(View.GONE);
            searchLocalCities(s);
        } else {
            CitySearchHelper.cancel(this);
            if (mRecommendView != null) {
                mSearchListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.GONE);
                mRecommendView.setVisibility(View.VISIBLE);
                mRecommendHeaderView.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    private void searchLocalCities(String keyword) {
        final String requestKeyword = keyword.trim();
        CitySearchHelper.search(this, requestKeyword, (query, results) -> {
            if (isFinishing()) {
                return;
            }
            SearchView searchView = findViewById(R.id.search_view);
            String currentQuery = searchView != null ? searchView.getQuery().toString().trim() : "";
            if (!TextUtils.equals(query, currentQuery)) {
                return;
            }
            showSearchResults(query, results);
        });
    }

    private void showSearchResults(String keyword, List<CityMode> list) {
        if (list != null && !list.isEmpty()) {
            mLocalSearchAdapter.update(list, keyword);
            mSearchListView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mSearchListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NotNull Rect outRect, @NotNull View view,
                                   RecyclerView parent, @NotNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) % 3 == 0) {
                outRect.left = 0;
            } else {
                if (parent.getChildAdapterPosition(view) % 3 == 1) {
                    outRect.left = space;
                } else {
                    outRect.left = space * 2;
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
