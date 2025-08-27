package com.goodtech.tq.activity;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.CSJAdError;
import com.bytedance.sdk.openadsdk.CSJSplashAd;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.goodtech.tq.BuildConfig;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.httpClient.WeatherHttpHelper;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SplashViewModel extends AndroidViewModel {
    private static final String TAG = "SplashViewModel";
    private static final int AD_TIME_OUT = 3000;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY = 1000; // 1秒后重试
    private static final long AD_EXPIRE_TIME = 30 * 60 * 1000; // 广告有效期30分钟
    private static final long MEMORY_THRESHOLD = 100 * 1024 * 1024; // 100MB内存阈值

    private ExecutorService executorService;
    private final MutableLiveData<Boolean> isAdLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isAdShown = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isWeatherDataReady = new MutableLiveData<>(false);
    private final MutableLiveData<CSJSplashAd> splashAdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPreloading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> queueSize = new MutableLiveData<>(0);
    private final MutableLiveData<String> preloadStrategy = new MutableLiveData<>("默认策略");
    
    private CSJSplashAd mCsjSplashAd;
    private final Queue<AdCacheItem> adQueue = new LinkedList<>();
    private TTAdNative.CSJSplashAdListener mCSJSplashAdListener;
    private CSJSplashAd.SplashAdListener mCSJSplashInteractionListener;
    private final AtomicBoolean isPreloadingInProgress = new AtomicBoolean(false);
    private int retryCount = 0;
    private int preloadCount = 0;
    private long lastPreloadTime = 0;

    private static class AdCacheItem {
        final CSJSplashAd ad;
        final long loadTime;

        AdCacheItem(CSJSplashAd ad) {
            this.ad = ad;
            this.loadTime = System.currentTimeMillis();
        }

        boolean isValid() {
            return ad != null &&
                   (System.currentTimeMillis() - loadTime) < AD_EXPIRE_TIME;
        }
    }

    public SplashViewModel(@NonNull Application application) {
        super(application);
        executorService = Executors.newSingleThreadExecutor();
        initAdListeners();
        adjustPreloadStrategy();
    }

    private void adjustPreloadStrategy() {
        if (executorService == null || executorService.isShutdown()) {
            Log.e(TAG, "ExecutorService is not available");
            return;
        }

        try {
            executorService.execute(() -> {
                int maxQueueSize = calculateMaxQueueSize();
                String strategy = "默认策略";
                
                if (!isNetworkAvailable()) {
                    strategy = "无网络，停止预加载";
                    maxQueueSize = 0;
                } else if (isLowMemory()) {
                    strategy = "低内存，减少预加载";
                    maxQueueSize = Math.min(maxQueueSize, 1);
                } else if (isWifiConnected()) {
                    strategy = "WiFi网络，增加预加载";
                    maxQueueSize = Math.min(maxQueueSize, 3);
                }

                preloadStrategy.postValue(strategy);
                if (maxQueueSize > adQueue.size()) {
                    preloadNextAd();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to adjust preload strategy", e);
        }
    }

    private int calculateMaxQueueSize() {
        int baseSize = 2; // 基础预加载数量
        
        // 根据内存使用情况调整
        if (isLowMemory()) {
            baseSize = 1;
        }
        
        // 根据网络状态调整
        if (isWifiConnected()) {
            baseSize = 3;
        } else if (!isNetworkAvailable()) {
            baseSize = 0;
        }
        
        // 根据用户行为调整
        if (preloadCount > 10 && System.currentTimeMillis() - lastPreloadTime < 3600000) {
            // 如果用户频繁使用，增加预加载
            baseSize++;
        }
        
        return baseSize;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && 
               activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private boolean isLowMemory() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return usedMemory > MEMORY_THRESHOLD;
    }

    private void initAdListeners() {
        mCSJSplashAdListener = new TTAdNative.CSJSplashAdListener() {
            @Override
            public void onSplashRenderSuccess(CSJSplashAd csjSplashAd) {
                isAdLoaded.postValue(true);
                mCsjSplashAd = csjSplashAd;
                csjSplashAd.setSplashAdListener(mCSJSplashInteractionListener);
                splashAdLiveData.postValue(csjSplashAd);
                
                // 更新预加载策略
                preloadCount++;
                lastPreloadTime = System.currentTimeMillis();
                adjustPreloadStrategy();
            }

            @Override
            public void onSplashLoadSuccess(CSJSplashAd csjSplashAd) {
                Log.d(TAG, "splash load success");
            }

            @Override
            public void onSplashLoadFail(CSJAdError csjAdError) {
                Log.d(TAG, "splash load fail, errCode: " + csjAdError.getCode() + ", errMsg: " + csjAdError.getMsg());
                isAdLoaded.postValue(false);
                
                // 重试加载
                if (retryCount < MAX_RETRY_COUNT && executorService != null && !executorService.isShutdown()) {
                    retryCount++;
                    try {
                        executorService.execute(() -> {
                            try {
                                Thread.sleep(RETRY_DELAY);
                                loadSplashAd();
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Retry interrupted", e);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to schedule retry", e);
                    }
                }
            }

            @Override
            public void onSplashRenderFail(CSJSplashAd csjSplashAd, CSJAdError csjAdError) {
                Log.d(TAG, "splash render fail, errCode: " + csjAdError.getCode() + ", errMsg: " + csjAdError.getMsg());
                isAdLoaded.postValue(false);
            }
        };

        mCSJSplashInteractionListener = new CSJSplashAd.SplashAdListener() {
            @Override
            public void onSplashAdShow(CSJSplashAd csjSplashAd) {
                isAdShown.postValue(true);
                Log.d(TAG, "splash show");
            }

            @Override
            public void onSplashAdClick(CSJSplashAd csjSplashAd) {
                Log.d(TAG, "splash click");
            }

            @Override
            public void onSplashAdClose(CSJSplashAd csjSplashAd, int i) {
                Log.d(TAG, "splash close");
                isAdShown.postValue(false);
                // 广告关闭后调整预加载策略
                adjustPreloadStrategy();
            }
        };
    }

    public void loadSplashAd() {
        // 检查队列中是否有有效的广告
        AdCacheItem validAd = getValidAdFromQueue();
        if (validAd != null) {
            mCsjSplashAd = validAd.ad;
            isAdLoaded.postValue(true);
            splashAdLiveData.postValue(mCsjSplashAd);
            return;
        }

        // 如果没有有效的预加载广告，则加载新广告
        String mAdUnitId = BuildConfig.PGE_SPLASH_POS_ID;
        TTAdNative adNativeLoader = TTAdSdk.getAdManager().createAdNative(getApplication());

        int width = SizeUtils.px2dp(ScreenUtils.getScreenWidth());
        int height = SizeUtils.px2dp(ScreenUtils.getScreenHeight());

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mAdUnitId)
                .setExpressViewAcceptedSize(width, height)
                .build();

        adNativeLoader.loadSplashAd(adSlot, mCSJSplashAdListener, AD_TIME_OUT);
    }

    private AdCacheItem getValidAdFromQueue() {
        while (!adQueue.isEmpty()) {
            AdCacheItem item = adQueue.poll();
            if (item.isValid()) {
                queueSize.postValue(adQueue.size());
                return item;
            } else {
                // 销毁过期的广告
                if (item.ad != null && item.ad.getMediationManager() != null) {
                    item.ad.getMediationManager().destroy();
                }
            }
        }
        queueSize.postValue(0);
        return null;
    }

    private void preloadNextAd() {
        if (isPreloadingInProgress.get() || adQueue.size() >= calculateMaxQueueSize()) {
            return;
        }

        isPreloadingInProgress.set(true);
        isPreloading.postValue(true);

        String mAdUnitId = BuildConfig.PGE_SPLASH_POS_ID;
        TTAdNative adNativeLoader = TTAdSdk.getAdManager().createAdNative(getApplication());

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mAdUnitId)
                .build();

        adNativeLoader.loadSplashAd(adSlot, new TTAdNative.CSJSplashAdListener() {
            @Override
            public void onSplashRenderSuccess(CSJSplashAd csjSplashAd) {
                adQueue.offer(new AdCacheItem(csjSplashAd));
                queueSize.postValue(adQueue.size());
                isPreloading.postValue(false);
                isPreloadingInProgress.set(false);
                Log.d(TAG, "Preload ad success, queue size: " + adQueue.size());
            }

            @Override
            public void onSplashLoadSuccess(CSJSplashAd csjSplashAd) {
                Log.d(TAG, "Preload ad load success");
            }

            @Override
            public void onSplashLoadFail(CSJAdError csjAdError) {
                Log.d(TAG, "Preload ad load fail: " + csjAdError.getMsg());
                isPreloading.postValue(false);
                isPreloadingInProgress.set(false);
            }

            @Override
            public void onSplashRenderFail(CSJSplashAd csjSplashAd, CSJAdError csjAdError) {
                Log.d(TAG, "Preload ad render fail: " + csjAdError.getMsg());
                isPreloading.postValue(false);
                isPreloadingInProgress.set(false);
            }
        }, AD_TIME_OUT);
    }

    public void prepareWeatherData() {
        if (!LocationSpHelper.getCityListAndLocation().isEmpty() && 
            executorService != null && !executorService.isShutdown()) {
            try {
                executorService.execute(() -> {
                    SpUtils.getInstance().remove(Constants.TIME_LOCATION);
                    SpUtils.getInstance().remove(Constants.TIME_WEATHER);
                    
                    String saveVersion = SpUtils.getInstance().getString(SpUtils.VERSION_APP, "");
                    if (!saveVersion.equals("0")) {
                        WeatherHttpHelper httpHelper = new WeatherHttpHelper(getApplication());
                        httpHelper.getBaseUrl(httpHelper::fetchCitiesWeather);
                    }
                    
                    SpUtils.getInstance().putBoolean("hadShowInterstitialAD", false);
                    isWeatherDataReady.postValue(true);
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to prepare weather data", e);
                isWeatherDataReady.postValue(true);
            }
        } else {
            isWeatherDataReady.postValue(true);
        }
    }

    public LiveData<Boolean> getIsAdLoaded() {
        return isAdLoaded;
    }

    public LiveData<Boolean> getIsAdShown() {
        return isAdShown;
    }

    public LiveData<Boolean> getIsWeatherDataReady() {
        return isWeatherDataReady;
    }

    public LiveData<CSJSplashAd> getSplashAd() {
        return splashAdLiveData;
    }

    public LiveData<Boolean> getIsPreloading() {
        return isPreloading;
    }

    public LiveData<Integer> getQueueSize() {
        return queueSize;
    }

    public LiveData<String> getPreloadStrategy() {
        return preloadStrategy;
    }

    public void destroyAd() {
        if (mCsjSplashAd != null && mCsjSplashAd.getMediationManager() != null) {
            mCsjSplashAd.getMediationManager().destroy();
        }
        // 清理队列中的所有广告
        while (!adQueue.isEmpty()) {
            AdCacheItem item = adQueue.poll();
            if (item != null && item.ad != null && item.ad.getMediationManager() != null) {
                item.ad.getMediationManager().destroy();
            }
        }
        queueSize.postValue(0);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 