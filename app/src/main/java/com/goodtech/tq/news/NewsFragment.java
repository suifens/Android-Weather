package com.goodtech.tq.news;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.msdk.api.AdSlot;
import com.bytedance.msdk.api.GMAdEcpmInfo;
import com.bytedance.msdk.api.v2.GMAdConstant;
import com.bytedance.msdk.api.v2.GMMediationAdSdk;
import com.bytedance.msdk.api.v2.GMSettingConfigCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAd;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMNativeAdLoadCallback;
import com.bytedance.msdk.api.v2.ad.nativeAd.GMUnifiedNativeAd;
import com.bytedance.msdk.api.v2.slot.GMAdOptionUtil;
import com.bytedance.msdk.api.v2.slot.GMAdSlotNative;
import com.bytedance.msdk.api.v2.slot.paltform.GMAdSlotGDTOption;
import com.goodtech.tq.R;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.db.NewsDbHelper;
import com.goodtech.tq.fragment.BaseFragment;
import com.goodtech.tq.httpClient.ApiClient;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.models.NewsDataBean;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.DeviceUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewsFragment extends BaseFragment {
    private ListView listView;
    private SmartRefreshLayout refreshLayout;
    private List<Object> mDataList;
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
                    mDataList = (List<Object>) msg.obj;
                    if (listView.getAdapter() == null) {
                        mAdapter = new NewsTabAdapter(getActivity(), mDataList);
                        listView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        loadListAdWithCallback();
                    }
                    refreshLayout.finishRefresh();
                    break;
                case SELECT_REFLSH:
                    mDataList = (List<Object>) msg.obj;
                    mAdapter = new NewsTabAdapter(getActivity(), mDataList);
                    listView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    loadListAdWithCallback();
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
        if (mAds != null) {
            for (GMNativeAd ad : mAds) {
                ad.resume();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_item, container, false);
        listView = view.findViewById(R.id.listView);
        refreshLayout = view.findViewById(R.id.swipe_refresh);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                    mDataList.addAll(newsBeanList);
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        NewsTabAdapter adapter = (NewsTabAdapter) listView.getAdapter();
                        if (adapter == null) {
                            adapter = new NewsTabAdapter(getActivity(), mDataList);
                            listView.setAdapter(adapter);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
                refreshLayout.finishLoadMore(500);
            }).start();
        });

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            //获取点击条目的路径，传值显示webview页面
            String url = ((NewsDataBean) mDataList.get(position)).getUrl();
            String uniquekey = ((NewsDataBean) mDataList.get(position)).getUniquekey();
            Intent intent = new Intent(getActivity(), WebActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("uniquekey", uniquekey);
            startActivity(intent);
        });

        mHandler.postDelayed(this::loadListAdWithCallback, 500);
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

    private GMUnifiedNativeAd mTTAdNative;
    private List<GMNativeAd> mAds = new ArrayList<>();
    private HashMap<GMNativeAd, Integer> mAdViewPositionMap = new HashMap<>();
    private static int sumCount;

    /**
     * config回调
     */
    private GMSettingConfigCallback mSettingConfigCallback = new GMSettingConfigCallback() {

        @Override
        public void configLoad() {
            Log.e(TAG, "load ad 在config 回调中加载广告");
            loadListAd();
        }
    };


    private void loadListAdWithCallback() {
        /**
         * 判断当前是否存在config 配置 ，如果存在直接加载广告 ，如果不存在则注册config加载回调
         */
        if (GMMediationAdSdk.configLoadSuccess()) {
            Log.e(TAG, "load ad 当前config配置存在，直接加载广告");
            loadListAd();
        } else {
            Log.e(TAG, "load ad 当前config配置不存在，正在请求config配置....");
            GMMediationAdSdk.registerConfigCallback(mSettingConfigCallback); //不能使用内部类，否则在ondestory中无法移除该回调
        }
    }

    /**
     * 加载feed广告
     */
    private void loadListAd() {
        mTTAdNative = new GMUnifiedNativeAd(getContext(), Constants.PGE_EXPRESS_POS_ID);//模板视频

        // 针对Gdt Native自渲染广告，可以自定义gdt logo的布局参数。该参数可选,非必须。
        FrameLayout.LayoutParams gdtNativeAdLogoParams =
                new FrameLayout.LayoutParams(
                        DeviceUtils.dip2px(getContext(), 40),
                        DeviceUtils.dip2px(getContext(), 13),
                        Gravity.RIGHT | Gravity.TOP); // 例如，放在右上角


        GMAdSlotGDTOption.Builder adSlotNativeBuilder = GMAdOptionUtil.getGMAdSlotGDTOption()
                .setNativeAdLogoParams(gdtNativeAdLogoParams);

        /**
         * 创建feed广告请求类型参数GMAdSlotNative,具体参数含义参考文档
         * 备注
         * 1: 如果是信息流自渲染广告，设置广告图片期望的图片宽高 ，不能为0
         * 2:如果是信息流模板广告，宽度设置为希望的宽度，高度设置为0(0为高度选择自适应参数)
         */
        GMAdSlotNative adSlotNative = new GMAdSlotNative.Builder()
                .setGMAdSlotBaiduOption(GMAdOptionUtil.getGMAdSlotBaiduOption().build())//百度相关的配置
                .setGMAdSlotGDTOption(adSlotNativeBuilder.build())//gdt相关的配置
                .setAdmobNativeAdOptions(GMAdOptionUtil.getAdmobNativeAdOptions())//admob相关配置
                .setAdStyleType(GMAdConstant.TYPE_EXPRESS_AD)//必传，表示请求的模板广告还是原生广告，AdSlot.TYPE_EXPRESS_AD：模板广告 ； AdSlot.TYPE_NATIVE_AD：原生广告
                // 备注
                // 1:如果是信息流自渲染广告，设置广告图片期望的图片宽高 ，不能为0
                // 2:如果是信息流模板广告，宽度设置为希望的宽度，高度设置为0(0为高度选择自适应参数)
                .setImageAdSize((int) DeviceUtils.getScreenWidth(getContext()), 340)// 必选参数 单位dp ，详情见上面备注解释
                .setAdCount(3)//请求广告数量为1到3条
                .build();


        new AdSlot.Builder()
                .setAdCount(2);

        //请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        /**
         * 注：每次加载信息流广告的时候需要新建一个GMUnifiedNativeAd，否则可能会出现广告填充问题
         * (例如：mTTAdNative = new GMUnifiedNativeAd(this, mAdUnitId);）
         */
        mTTAdNative.loadAd(adSlotNative, new GMNativeAdLoadCallback() {
            @Override
            public void onAdLoaded(List<GMNativeAd> ads) {
                /**
                 * 获取已经加载的clientBidding ，多阶底价广告的相关信息
                 */
                List<GMAdEcpmInfo> gmAdEcpmInfos = mTTAdNative.getMultiBiddingEcpm();
                if (gmAdEcpmInfos != null) {
                    for (GMAdEcpmInfo info : gmAdEcpmInfos) {
                        Log.e(TAG, "***多阶+client相关信息*** AdNetworkPlatformId" + info.getAdNetworkPlatformId()
                                + "  AdNetworkRitId:" + info.getAdNetworkRitId()
                                + "  ReqBiddingType:" + info.getReqBiddingType()
                                + "  PreEcpm:" + info.getPreEcpm()
                                + "  LevelTag:" + info.getLevelTag()
                                + "  ErrorMsg:" + info.getErrorMsg());
                    }
                }

                /**
                 * 获取获取当前缓存池的全部信息
                 */
                List<GMAdEcpmInfo> gmCacheInfos = mTTAdNative.getCacheList();
                if (gmCacheInfos != null) {
                    for (GMAdEcpmInfo info : gmCacheInfos) {
                        Log.e(TAG, "   ");
                        Log.e(TAG, "***缓存池的全部信息*** AdNetworkPlatformId" + info.getAdNetworkPlatformId()
                                + "  AdNetworkRitId:" + info.getAdNetworkRitId()
                                + "  ReqBiddingType:" + info.getReqBiddingType()
                                + "  PreEcpm:" + info.getPreEcpm()
                                + "  LevelTag:" + info.getLevelTag()
                                + "  ErrorMsg:" + info.getErrorMsg());
                    }
                }


                // if (listView != null) {
                //     listView.setLoadingFinish();
                // }

                if (ads == null || ads.isEmpty()) {
                    Log.e(TAG, "on FeedAdLoaded: ad is null!");
                    return;
                }

                for (GMNativeAd ttNativeAd : ads) {
                    GMAdEcpmInfo gmAdEcpmInfo = ttNativeAd.getBestEcpm();
                    if (gmAdEcpmInfo != null) {
                        Log.e(TAG, "   ");
                        Log.e(TAG, "***实时填充的广告信息***  adNetworkPlatformName: " + gmAdEcpmInfo.getAdNetworkPlatformName() + "   adNetworkRitId：" + gmAdEcpmInfo.getAdNetworkRitId() + "   preEcpm: " + gmAdEcpmInfo.getPreEcpm());
                    }
                }

                if (mAds != null)
                    mAds.addAll(ads);
                //总广告数量
                int adCount = ads.size();
                Log.d(TAG, "onAdLoaded feed adCount=" + adCount);
                for (int i = 0; i < 10 * adCount + 2; i++) {
                    mDataList.add(null);
                }
                //每隔10条放一条广告
                int idx = 0;
                for (int i = 1; i < mDataList.size(); i++) {
                    if (i % 5 == 0 && idx < ads.size()) {
                        if ((i + sumCount - 1) < mDataList.size()) {
                            mDataList.set(i + sumCount - 1, ads.get(idx));
                        }
                        idx++;
                    }
                    if (idx > ads.size()) break;
                }
                //记录容器中的元素数量
                sumCount = mDataList.size();
                mAdapter.notifyDataSetChanged();

                // 获取本次waterfall加载中，加载失败的adn错误信息。
                if (mTTAdNative != null)
                    Log.d(TAG, "feed adLoadInfos: " + mTTAdNative.getAdLoadInfoList().toString());
            }

            @Override
            public void onAdLoadedFail(com.bytedance.msdk.api.AdError adError) {
                // if (mListView != null) {
                //     mListView.setLoadingFinish();
                // }
                Log.e(TAG, "load feed ad error : " + adError.code + ", " + adError.message);
                // 获取本次waterfall加载中，加载失败的adn错误信息。
                if (mTTAdNative != null)
                    Log.d(TAG, "feed adLoadInfos: " + mTTAdNative.getAdLoadInfoList().toString());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //注销config回调
        GMMediationAdSdk.unregisterConfigCallback(mSettingConfigCallback);
        if (mAds != null) {
            for (GMNativeAd ad : mAds) {
                ad.destroy();
            }
        }
        mAds = null;
        mHandler.removeCallbacksAndMessages(null);
        sumCount = 0; //静态变量
    }
}
