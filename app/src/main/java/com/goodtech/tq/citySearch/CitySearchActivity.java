package com.goodtech.tq.citySearch;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.helpers.DatabaseHelper;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.Utils;

import java.util.ArrayList;

public class CitySearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, View.OnClickListener {

    private static final String TAG = "CitySearchActivity";
    private SearchView mSearchView;// 输入搜索关键字
    private RecyclerView mRecommendView;
    private RecyclerView mSearchListView;
    private CityRecommendAdapter mRecommendAdapter;
    private CityRecyclerAdapter mSearchAdapter;
    private CityRecommendHeaderView mRecommendHeaderView;

    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);
        initSearchView();

        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        mRecommendHeaderView = findViewById(R.id.header_recommend);
        mRecommendHeaderView.setListener(new CityRecommendAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, CityMode cityMode) {
                Log.d(TAG, "recommend onItemClick: view " + view + " position = " + position);

                if (cityMode != null && cityMode.cid != 0) {
                    addCity(cityMode);
                }
            }
        });

        /**
         * 推荐列表
         */
        mRecommendView = findViewById(R.id.recycler_recommend);
        mRecommendView.setVisibility(View.VISIBLE);
        final ArrayList<CityMode> recommends = CityHelper.getRecommends(this);
        mRecommendAdapter = new CityRecommendAdapter(this, recommends);
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
        mRecommendView.addItemDecoration(new SpaceItemDecoration((screenWidth - itemWidth* 3)/6));

        /**
         * 搜索列表
         */
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

    private void addCity(CityMode cityMode) {
        if (cityMode != null) {
            boolean success = LocationSpHelper.addCity(cityMode);
            if (success) {
                WeatherHttpHelper helper = new WeatherHttpHelper(getApplicationContext());
                helper.fetchWeather(cityMode);
            }
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 200);
    }

    private void initSearchView() {
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setOnQueryTextListener(this);
        //设置SearchView默认为展开显示
        mSearchView.setIconified(false);
        mSearchView.onActionViewExpanded();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.search_btn_cancel) {
            this.finish();
        }
    }

    public static boolean IsEmptyOrNullString(String s) {
        return (s == null) || (s.trim().length() == 0);
    }

    /**
     * 搜索城市
     */
    @Override
    public boolean onQueryTextSubmit(String s) {

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
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int space;  //位移间距
        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
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
