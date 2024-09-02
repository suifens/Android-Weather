package com.goodtech.tq.modules.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.goodtech.tq.R;
import com.bytedance.sdk.dp.DPPageState;
import com.bytedance.sdk.dp.DPSdk;
import com.bytedance.sdk.dp.DPWidgetDrawParams;
import com.bytedance.sdk.dp.IDPAdListener;
import com.bytedance.sdk.dp.IDPDrawListener;
import com.bytedance.sdk.dp.IDPWidget;
import com.goodtech.tq.common.bus.Bus;
import com.goodtech.tq.common.bus.IBusListener;
import com.goodtech.tq.common.bus.event.DPStartEvent;

import java.util.List;
import java.util.Map;

/**
 * 沉浸式小视频场景展示：全屏样式
 * Create by hanweiwei on 2020-04-21.
 */
public class DrawVideoFullScreenActivity extends AppCompatActivity {
    private static final String TAG = DrawVideoFullScreenActivity.class.getSimpleName();
    public static final String CHANNEL_TYPE = "channel_type";
    public static final String CONTENT_TYPE = "content_type";
    public static final String IS_HIDE_FOLLOW = "is_hide_follow";
    public static final String IS_HIDE_CHANNLE_NAME = "is_hide_channle_name";

    private IDPWidget mIDPWidget;
    private Fragment mDrawFragment;

    private long mGroupId;

    private long mLastBackTime = -1;
    private int mChannelType = DPWidgetDrawParams.DRAW_CHANNEL_TYPE_RECOMMEND_FOLLOW;
    private int mContentType = DPWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_VIDEO;
    private boolean mIsHideFollow = true;
    private boolean mIsHideChannelName = true;

    private boolean isInited = false;
    private final IBusListener function = event -> {
        if (event instanceof DPStartEvent) {
            if (((DPStartEvent) event).isSuccess) {
                init();
            }
        }
    };

    public static void start(Activity activity, int channelType, boolean isHideFollow, boolean isHideChannelName) {
        Intent intent = new Intent(activity, DrawVideoFullScreenActivity.class);
        intent.putExtra(CHANNEL_TYPE, channelType);
        intent.putExtra(IS_HIDE_FOLLOW, isHideFollow);
        intent.putExtra(IS_HIDE_CHANNLE_NAME, isHideChannelName);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_activity_draw_video_full_screen);
        Intent intent = getIntent();
        if (intent != null) {
            mChannelType = intent.getIntExtra(CHANNEL_TYPE, DPWidgetDrawParams.DRAW_CHANNEL_TYPE_RECOMMEND_FOLLOW);
            mContentType = intent.getIntExtra(CONTENT_TYPE, DPWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_VIDEO);
            mIsHideFollow = intent.getBooleanExtra(IS_HIDE_FOLLOW, false);
            mIsHideChannelName = intent.getBooleanExtra(IS_HIDE_CHANNLE_NAME, false);
        }

        Bus.getInstance().addListener(function);
        if (DPSdk.isStartSuccess()) {
            init();
        }
    }

    private void init() {
        if (isInited) {
            return;
        }
        //初始化draw组件
        initDrawWidget();
        mDrawFragment = mIDPWidget.getFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.draw_style1_frame, mDrawFragment)
                .commitAllowingStateLoss();
        isInited = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("android:support:fragments", null);
    }

    private void initDrawWidget() {
        mIDPWidget = DPHolder.INSTANCE.buildDrawWidget(DPWidgetDrawParams.obtain()
                .adOffset(0) //单位 dp，为 0 时可以不设置
                .drawContentType(mContentType)
                .drawChannelType(mChannelType)
                .hideFollow(mIsHideFollow)
                .hideChannelName(mIsHideChannelName)
                .hideClose(false, null)
                .listener(new IDPDrawListener() {
                    @Override
                    public void onDPRefreshFinish() {
                        log("onDPRefreshFinish");
                    }

                    @Override
                    public void onDPPageChange(int position) {
                        log("onDPPageChange: " + position);
                    }

                    @Override
                    public void onDPPageChange(int position, Map<String, Object> map) {
                        if (map == null) {
                            return;
                        }

                        log("onDPPageChange: " + position + ", map = " + map.toString());
                    }

                    @Override
                    public void onDPVideoPlay(Map<String, Object> map) {
                        log("onDPVideoPlay map = " + map.toString());
                    }

                    @Override
                    public void onDPVideoOver(Map<String, Object> map) {
                        log("onDPVideoOver map = " + map.toString());
                    }

                    @Override
                    public void onDPVideoCompletion(Map<String, Object> map) {
                        log("onDPVideoCompletion map = " + map.toString());
                    }

                    @Override
                    public void onDPClose() {
                        log("onDPClose");
                    }

                    @Override
                    public void onDPReportResult(boolean isSucceed) {
                        log("onDPReportResult isSucceed = " + isSucceed);
                    }

                    @Override
                    public void onDPPageStateChanged(DPPageState pageState) {
                        log("onDPPageStateChanged pageState = " + pageState.toString());
                    }

                    @Override
                    public void onDPReportResult(boolean isSucceed, Map<String, Object> map) {
                        log("onDPReportResult isSucceed = " + isSucceed + ", map = " + map.toString());
                    }

                    @Override
                    public void onDPRequestStart(@Nullable Map<String, Object> map) {
                        log("onDPRequestStart");
                    }

                    @Override
                    public void onDPRequestSuccess(List<Map<String, Object>> list) {
                        if (list == null) {
                            return;
                        }

                        for (int i = 0; i < list.size(); i++) {
                            log("onDPRequestSuccess i = " + i + ", map = " + list.get(i).toString());
                        }
                    }

                    @Override
                    public void onDPRequestFail(int code, String msg, @Nullable Map<String, Object> map) {
                        if (map == null) {
                            log("onDPRequestFail code = " + code + ", msg = " + msg);
                            return;
                        }
                        log("onDPRequestFail  code = " + code + ", msg = " + msg + ", map = " + map.toString());
                    }

                    @Override
                    public void onDPClickAuthorName(Map<String, Object> map) {
                        log("onDPClickAuthorName map = " + map.toString());
                    }

                    @Override
                    public void onDPClickAvatar(Map<String, Object> map) {
                        log("onDPClickAvatar map = " + map.toString());
                    }

                    @Override
                    public void onDPClickComment(Map<String, Object> map) {
                        log("onDPClickComment map = " + map.toString());
                    }

                    @Override
                    public void onDPClickLike(boolean isLike, Map<String, Object> map) {
                        log("onDPClickLike isLike = " + isLike + ", map = " + map.toString());
                    }

                    @Override
                    public void onDPVideoPause(Map<String, Object> map) {
                        log("onDPVideoPause map = " + map.toString());
                    }

                    @Override
                    public void onDPVideoContinue(Map<String, Object> map) {
                        log("onDPVideoContinue map = " + map.toString());
                    }

                    @Override
                    public void onDPClickShare(Map<String, Object> map) {
                        log("onDPClickShare map = " + map.toString());
                    }

                    @Override
                    public void onChannelTabChange(int channel) {
                        log("onChannelTabChange, is " + channel);
                    }
                })
                .adListener(new IDPAdListener() {
                    @Override
                    public void onDPAdRequest(Map<String, Object> map) {
                        log("onDPAdRequest map =  " + map.toString());
                    }

                    @Override
                    public void onDPAdRequestSuccess(Map<String, Object> map) {
                        log("onDPAdRequestSuccess map = " + map.toString());
                    }

                    @Override
                    public void onDPAdRequestFail(int code, String msg, Map<String, Object> map) {
                        log("onDPAdRequestFail map = " + map.toString());
                    }

                    @Override
                    public void onDPAdFillFail(Map<String, Object> map) {
                        log("onDPAdFillFail map = " + map.toString());
                    }

                    @Override
                    public void onDPAdShow(Map<String, Object> map) {
                        log("onDPAdShow map = " + map.toString());
                    }

                    @Override
                    public void onDPAdPlayStart(Map<String, Object> map) {
                        log("onDPAdPlayStart map = " + map.toString());
                    }

                    @Override
                    public void onDPAdPlayPause(Map<String, Object> map) {
                        log("onDPAdPlayPause map = " + map.toString());
                    }

                    @Override
                    public void onDPAdPlayContinue(Map<String, Object> map) {
                        log("onDPAdPlayContinue map = " + map.toString());
                    }

                    @Override
                    public void onDPAdPlayComplete(Map<String, Object> map) {
                        log("onDPAdPlayComplete map = " + map.toString());
                    }

                    @Override
                    public void onDPAdClicked(Map<String, Object> map) {
                        log("onDPAdClicked map = " + map.toString());
                    }
                }));
    }

    private static void log(String msg) {
        Log.d(TAG, String.valueOf(msg));
    }

    @Override
    public void onBackPressed() {
        if (mIDPWidget != null && !mIDPWidget.canBackPress()) {
            return;
        }

        if (mIDPWidget == null) {
            return;
        }

        long current = SystemClock.elapsedRealtime();
        if (current - mLastBackTime > 3000) {
            mLastBackTime = current;
            mIDPWidget.backRefresh();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mIDPWidget != null) {
            mIDPWidget.destroy();
        }
        Bus.getInstance().removeListener(function);
    }
}
