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
import com.goodtech.tq.app.WeatherApp;
import com.goodtech.tq.db.NewsDbHelper;
import com.goodtech.tq.helpers.WeatherSpHelper;
import com.goodtech.tq.httpClient.ApiClient;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.fragment.BaseFragment;
import com.goodtech.tq.models.NewsDataBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewsFragment extends BaseFragment {
    private ListView listView;
    private SmartRefreshLayout refreshLayout;
    private List<NewsDataBean> list;
    private static final int UPNEWS_INSERT = 0;
    private int page = 0;
    private final int row = 10;
    private static final int SELECT_REFLSH = 1;
    private NewsDbHelper dbHelper;
    private NewsType newsType;
    private static final String TAG = "NewsFragment";

    String responseDate;
    @SuppressLint("HandlerLeak")
    private final Handler newsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPNEWS_INSERT:
//                    list = ((NewsBean) msg.obj).getResult().getData();
                    list = (List<NewsDataBean>) msg.obj;
                    if (listView.getAdapter() == null) {
                        NewsTabAdapter adapter = new NewsTabAdapter(getActivity(), list);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                    refreshLayout.finishRefresh();
                    break;
                case SELECT_REFLSH:
                    list = (List<NewsDataBean>) msg.obj;
                    NewsTabAdapter NewsTabAdapter = new NewsTabAdapter(getActivity(), list);
                    listView.setAdapter(NewsTabAdapter);
                    NewsTabAdapter.notifyDataSetChanged();
                    break;
                default:
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new NewsDbHelper(WeatherApp.getInstance());
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
//        fab = (FloatingActionButton) view.findViewById(R.id.fab);
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
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                page++;
                // 下一步实现从数据库中读取数据刷新到listview适配器中
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        int offset = page * row;
                        List<NewsDataBean> newsBeanList = dbHelper.queryNewsList(newsType.cnKey, offset, row);
                        list.addAll(newsBeanList);
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
                    }
                }).start();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //获取点击条目的路径，传值显示webview页面
                String url = list.get(position).getUrl();
                String uniquekey = list.get(position).getUniquekey();
                Intent intent = new Intent(getActivity(), WebActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("uniquekey", uniquekey);
                startActivity(intent);

            }
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


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                OkHttpClient okHttpClient = new OkHttpClient();
//                Request request = new Request.Builder()
//                        .url(path)
//                        .build();
//                try {
//                    Response response = okHttpClient.newCall(request).execute();
//                    if (response.body() != null) {
//                        responseDate = Objects.requireNonNull(response.body()).string();
//                        Log.e("newsFragment", "run: responseData " + responseDate);
//                        NewsBean newsBean = new Gson().fromJson(responseDate, NewsBean.class);
//
//                        if (!"10012".equals("" + newsBean.getError_code())) {
//                            dbHelper.insertDataBeans(newsBean.getResult().getData());
//                        }
//
//                        Message msg = newsHandler.obtainMessage();
//                        msg.what = UPNEWS_INSERT;
//                        msg.obj = newsBean;
//                        newsHandler.sendMessage(msg);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
////                catch (IOException e) {
////                    Log.e("newsFragment exception", "" + e);
////                    e.printStackTrace();
////                }
//
//            }
//
//        }).start();
    }

}
