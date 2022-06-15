package com.goodtech.tq.news;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goodtech.tq.R;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.db.NewsDbHelper;
import com.goodtech.tq.fragment.BaseFragment;
import com.goodtech.tq.httpClient.ApiClient;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.models.NewsDataBean;
import com.goodtech.tq.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsFragment extends BaseFragment implements NativeExpressAD.NativeExpressADListener {
    private ListView listView;
    private SmartRefreshLayout refreshLayout;
    private List<Object> list;
    private static final int UPNEWS_INSERT = 0;
    private int page = 0;
    private final int row = 30;
    private static final int SELECT_REFLSH = 1;
    private NewsDbHelper dbHelper;
    private NewsType newsType;
    private static final String TAG = "NewsFragment";

    private NewsTabAdapter mAdapter;

    String responseDate;
    @SuppressLint("HandlerLeak")
    private final Handler newsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPNEWS_INSERT:
//                    list = ((NewsBean) msg.obj).getResult().getData();
                    list = (List<Object>) msg.obj;
                    if (listView.getAdapter() == null) {
                        mAdapter = new NewsTabAdapter(getActivity(), list);
                        listView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        initNativeExpressAD();
                    }
                    refreshLayout.finishRefresh();
                    break;
                case SELECT_REFLSH:
                    list = (List<Object>) msg.obj;
                    mAdapter = new NewsTabAdapter(getActivity(), list);
                    listView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    initNativeExpressAD();
                    break;
                default:
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new NewsDbHelper(BaseApp.getInstance());
    }

    @Override
    public void onResume() {
        super.onResume();
        //  首次获取数据
        getDataFromNet(newsType.enKey);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_item, container, false);
        listView = view.findViewById(R.id.listView);
        refreshLayout = view.findViewById(R.id.swipe_refresh);
        return view;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //获取传递的值
        Bundle bundle = getArguments();
        String typeString = NewsType.TOP.toString();
        if (bundle != null) {
            typeString = bundle.getString("type", NewsType.TOP.toString());
        }

        newsType = NewsType.getType(typeString);
        //置顶功能
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listView.smoothScrollToPosition(0);
//            }
//        });
        //下拉刷新
//        refreshLayout.setColorSchemeResources(R.color.colorRed);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                page = 0;
                //异步加载数据
                getDataFromNet(newsType.enKey);
            }
        });
        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            page++;
            // 下一步实现从数据库中读取数据刷新到listview适配器中
            new Thread(() -> {

                int offset = page * row;
                List<NewsDataBean> newsBeanList = dbHelper.queryNewsList(newsType.cnKey, offset, row);
                if (newsBeanList != null) {
                    list.addAll(newsBeanList);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        NewsTabAdapter adapter = (NewsTabAdapter) listView.getAdapter();
                        if (adapter == null) {
                            adapter = new NewsTabAdapter(getActivity(), list);
                            listView.setAdapter(adapter);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
                refreshLayout.finishLoadMore(500);
            }).start();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            //获取点击条目的路径，传值显示webview页面
            String url = ((NewsDataBean)list.get(position)).getUrl();
            String uniquekey = ((NewsDataBean)list.get(position)).getUniquekey();
            Intent intent = new Intent(getActivity(), WebActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("uniquekey", uniquekey);
            startActivity(intent);

        });
    }

    private void getDataFromNet(final String data) {

        final String path = "http://v.juhe.cn/toutiao/index?type=" + data + "&key=927352c7b578ec641b4bac8799b5d40b";
        ApiClient.getInstance().get(path, null, new ApiResponseHandler(getContext()) {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                Log.e(TAG, "onResponse: " + jsonObject.toString());
                try {
                    if (success) {
                        Gson gson = new Gson();
                        String error = jsonObject.getString("error_code");
                        JsonObject result = new Gson().fromJson(jsonObject.optString("result"), JsonObject.class);
                        if (result != null) {
                            JsonArray data = new Gson().fromJson(result.get("data"), JsonArray.class);
                            List<NewsDataBean> dataBeans = gson.fromJson(data, new TypeToken<List<NewsDataBean>>() {}.getType());
                            if (!"10012".equals(error)) {
                                dbHelper.insertDataBeans(dataBeans);
                            }
                            Message msg = newsHandler.obtainMessage();
                            msg.what = UPNEWS_INSERT;
                            msg.obj = dataBeans;
                            newsHandler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "onResponse: exception " + e );
                    e.printStackTrace();
                }
            }
        });
    }


    // mark: --

    private NativeExpressAD mADManager;
    private List<NativeExpressADView> mAdViewList = new ArrayList<>();
    private HashMap<NativeExpressADView, Integer> mAdViewPositionMap = new HashMap<NativeExpressADView, Integer>();

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 使用完了每一个NativeExpressADView之后都要释放掉资源。
        if (mAdViewList != null) {
            for (NativeExpressADView view : mAdViewList) {
                view.destroy();
            }
        }
    }

//    private void initData() {
//        mAdapter = new CustomAdapter(mNormalDataList);
//        mRecyclerView.setAdapter(mAdapter);
//        initNativeExpressAD();
//    }

    /**
     */
    private void initNativeExpressAD() {
        ADSize adSize = new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT); // 消息流中用AUTO_HEIGHT
        mADManager = new NativeExpressAD(getContext(), adSize, Constants.NEWS_POS_ID, this);
        mADManager.loadAD(mAdapter.getCount() / 5);
    }

    @Override
    public void onNoAD(AdError adError) {
        Log.i(
                TAG,
                String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(),
                        adError.getErrorMsg()));
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        Log.i(TAG, "onADLoaded: " + adList.size());

        int count = mAdapter.getCount();
        int adCount = mAdViewList.size();

        for (int i = 0; i < adList.size(); i++) {
            int position = 10 * i + 3;
            if (position < list.size()) {
                NativeExpressADView view = adList.get(i);
                mAdViewPositionMap.put(view, position); // 把每个广告在列表中位置记录下来
                mAdapter.addADViewToPosition(position, adList.get(i));
                mAdapter.notifyDataSetChanged();
                Log.d(TAG,
                        i + ": eCPMLevel = " + view.getBoundData().getECPMLevel() + " , videoDuration = " + view.getBoundData().getVideoDuration());
            }
        }
        mAdViewList.addAll(adList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail: " + adView.toString());
    }

    @Override
    public void onRenderSuccess(NativeExpressADView adView) {
        Log.i(TAG, "onRenderSuccess: " + adView.toString());
    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        Log.i(TAG, "onADExposure: " + adView.toString());
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        Log.i(TAG, "onADClicked: " + adView.toString());
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        Log.i(TAG, "onADClosed: " + adView.toString());
        if (mAdapter != null) {
            int removedPosition = mAdViewPositionMap.get(adView);
            mAdapter.removeADView(removedPosition, adView);
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        Log.i(TAG, "onADLeftApplication: " + adView.toString());
    }
}
