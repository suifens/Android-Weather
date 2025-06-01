package com.goodtech.tq.ad;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DislikeInfo;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.bytedance.sdk.openadsdk.mediation.ad.MediationExpressRenderListener;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationNativeManager;
import com.goodtech.tq.base.callback.DataCallback;
import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.base.AppExtKt;

import java.util.List;

/**
 * com.goodtech.tq.ad
 */
public class AdFeedActivity extends BaseActivity {

    protected static final String TAG = "AdFeedActivity";
    private FrameLayout mFeedContainer;

    protected void loadFeedAd(String codeId, int width, DataCallback<TTFeedAd> callback) {
        // 1、创建AdSlot对象 */
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setExpressViewAcceptedSize(width, 0) // 单位px
                .setAdCount(1) // 请求广告数量为1到3条 （优先采用平台配置的数量）
                .build();

        // 2、创建TTAdNative对象 */
        TTAdNative adNativeLoader = TTAdSdk.getAdManager().createAdNative(this);

        // 3、创建加载、展示监听器 */
//        initListeners();
        TTAdNative.FeedAdListener adListener = getFeedAdListener(callback);

        // 4、加载广告 */
        adNativeLoader.loadFeedAd(adSlot, adListener);
    }
    /**
     * 展示原生广告
     */
    protected boolean showAd(FrameLayout feedContainer, boolean loadSuccess, boolean isLoadedAndShow, TTFeedAd feedAd) {
        if (!loadSuccess || feedAd == null) {
            //TToast.show(getContext(), "请先加载广告");
            // initNativeExpressAD();
            return false;
        }
        loadSuccess = false;
        isLoadedAndShow = true;

        mFeedContainer = feedContainer;
        feedContainer.setVisibility(View.VISIBLE);
        showFeedAd(feedContainer, feedAd);
        return true;
    }

    private void showFeedAd(FrameLayout feedContainer, TTFeedAd mTTFeedAd) {
        if (mTTFeedAd == null) {
            Log.i("TAG", "请先加载广告或等待广告加载完毕后再调用show方法");
            return;
        }
        mTTFeedAd.setDislikeCallback(this, getDislikeCallback());
        // 5、展示广告 
        MediationNativeManager manager = mTTFeedAd.getMediationManager();
        if (manager != null) {
            if (manager.isExpress()) { // --- 模板feed流广告
                mTTFeedAd.setExpressRenderListener(new MediationExpressRenderListener() {
                    @Override
                    public void onRenderFail(View view, String s, int i) {
                        Log.d("TAG", "feed express render fail, errCode: " + i + ", errMsg: " + s);
                    }

                    @Override
                    public void onAdClick() {
                        Log.d("TAG", "feed express click");
                    }

                    @Override
                    public void onAdShow() {
                        Log.d("TAG", "feed express show");
                    }

                    @Override
                    public void onRenderSuccess(View view, float v, float v1, boolean b) {
                        Log.d("TAG", "feed express render success");
                        View expressFeedView = mTTFeedAd.getAdView(); // *** 注意不要使用onRenderSuccess参数中的view ***
                        AppExtKt.removeFromParent(expressFeedView);
                        feedContainer.removeAllViews();
                        feedContainer.addView(expressFeedView);
                    }
                });
                mTTFeedAd.render(); // 调用render方法进行渲染，在onRenderSuccess中展示广告
            }
            else {
                // --- 自渲染feed流广告
                // 自渲染广告返回的是广告素材，开发者自己将其渲染成view
                View feedView = FeedAdUtils.getFeedAdFromFeedInfo(mTTFeedAd, this, null, new TTNativeAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, TTNativeAd ttNativeAd) {
                        Log.d("TAG", "feed click");
                    }

                    @Override
                    public void onAdCreativeClick(View view, TTNativeAd ttNativeAd) {
                        Log.d("TAG", "feed creative click");
                    }

                    @Override
                    public void onAdShow(TTNativeAd ttNativeAd) {
                        Log.d("TAG", "feed show");
                    }
                });
                if (feedView != null) {
                    AppExtKt.removeFromParent(feedView);
                    feedContainer.removeAllViews();
                    feedContainer.addView(feedView);
                }
            }
        }
    }

    private TTAdDislike.DislikeInteractionCallback getDislikeCallback() {
        return new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int i, String s, boolean b) {
                // 用户点击dislike后回调
                mFeedContainer.removeAllViews();
            }

            @Override
            public void onCancel() {

            }
        };
    }

    protected TTAdNative.FeedAdListener getFeedAdListener(DataCallback<TTFeedAd> callback) {
        return new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int i, String s) {
                Log.d("TAG", "feed load fail, errCode: " + i + ", errMsg: " + s);
            }

            @Override
            public void onFeedAdLoad(List<TTFeedAd> list) {
                if (list != null && list.size() > 0) {
                    Log.d("TAG", "feed load success");
//                    feedAd = list.get(0);
                    callback.onComplete(list.get(0), "");
                } else {
                    Log.d("TAG", "feed load success, but list is null");
                }
            }
        };
    }

    private void bindDislikeAction(FrameLayout feedContainer, final TTFeedAd ad, boolean isCustomDislike) {
        if (isCustomDislike) {
            // 使用自定义Dislike
            final DislikeInfo dislikeInfo = ad.getDislikeInfo();
            if (dislikeInfo == null || dislikeInfo.getFilterWords() == null || dislikeInfo.getFilterWords().isEmpty()) {
                return;
            }
            final DislikeDialog dislikeDialog = new DislikeDialog(this, dislikeInfo);
            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
                @Override
                public void onItemClick(FilterWord filterWord) {
//                    mData.remove(ad);
//                    notifyDataSetChanged();
                    feedContainer.removeAllViews();
                }
            });
            ad.setDislikeDialog(dislikeDialog);
//            dislike.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dislikeDialog.show();
//                }
//            });
        } else {
            // 使用默认Dislike

            final TTAdDislike ttAdDislike = ad.getDislikeDialog(this);
            if (ttAdDislike != null) {

                ad.getDislikeDialog(this).setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onShow() {

                    }

                    @Override
                    public void onSelected(int position, String value, boolean enforce) {
                        if (enforce) {

//                            mData.remove(ad);
//                            notifyDataSetChanged();
//                            if (enforce) {
//                                TToast.show(mContext, "FeedListActivity 原生信息流 sdk强制移除View ");
//                            }
                            feedContainer.removeAllViews();
                            return;
                        }
//                        mData.remove(ad);
//                        notifyDataSetChanged();
                        feedContainer.removeAllViews();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
//            dislike.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (ttAdDislike != null)
//                        ttAdDislike.showDislikeDialog();
//                }
//            });
        }
    }
}
