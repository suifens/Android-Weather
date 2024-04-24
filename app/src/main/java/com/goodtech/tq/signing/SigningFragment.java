package com.goodtech.tq.signing;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Size;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.goodtech.tq.R;
import com.goodtech.tq.fragment.BaseFragment;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.utils.ImageUtils;
import com.goodtech.tq.utils.TimeUtils;

import java.io.File;

public class SigningFragment extends BaseFragment {

    protected ImageView mBgImgView;   //  背景
    protected ImageView mTitleImgV;   //  早安/晚安文字图片
    protected ImageButton mEditBtn;
    protected ImageButton mCameraBtn;
    protected TextView mWriterTv; //  叙述文案
    protected TextView mContinueTitle;    //  连续打卡title
    protected TextView mContinueCountTv;  //  连续打卡天数
    protected TextView mTimeTitle;    //  时间title
    protected TextView mTimeTv;   //  时间
    protected ImageView mWeatherIcon; //  天气图标
    protected TextView mTempTv;   //  温度
    protected TextView mAddressTv;    //  地址
    protected TextView mDayTv;    //  日期
    protected SigningListener mListener;
    protected boolean isAM; //是否早起
    protected int mPlaceholder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setupCacheViews() {
        super.setupCacheViews();
        if (mCacheView != null) {
            mBgImgView = mCacheView.findViewById(R.id.img_background);
            mTitleImgV = mCacheView.findViewById(R.id.img_title);
            mEditBtn = mCacheView.findViewById(R.id.btn_edit);
            mCameraBtn = mCacheView.findViewById(R.id.btn_camera);
            mWriterTv = mCacheView.findViewById(R.id.tv_writer);
            mContinueTitle = mCacheView.findViewById(R.id.tv_title_continue);
            mContinueCountTv = mCacheView.findViewById(R.id.tv_continue);
            mTimeTitle = mCacheView.findViewById(R.id.tv_title_now);
            mTimeTv = mCacheView.findViewById(R.id.tv_time_now);
            mWeatherIcon = mCacheView.findViewById(R.id.icon_weather);
            mTempTv = mCacheView.findViewById(R.id.tv_temp);
            mAddressTv = mCacheView.findViewById(R.id.tv_address);
            mDayTv = mCacheView.findViewById(R.id.tv_day);
            configPressed();
            configData(isAM);
        }
    }

    public void setupConfig(boolean am) {
        this.isAM = am;
    }

    /**
     * 配置数据
     * @param am 是否是早上 
     */
    protected void configData(boolean am) {
        mTitleImgV.setImageResource(am ? R.drawable.ic_good_morning : R.drawable.ic_good_night);
        mContinueTitle.setText(am ? "连续早起" : "连续早睡");
        mTimeTitle.setText(am ? "今日早起" : "今日早睡");
    }

    /**
     * 恢复文案
     */
    protected void recoverWriter(boolean am) {

    }

    @SuppressLint("DefaultLocale")
    public void updateData(Hourly hourly, CityMode cityMode, int continueCount) {
        if (mWeatherIcon != null) {
            mWeatherIcon.setImageResource(ImageUtils.weatherImageRes(hourly.icon_cd));
            if (hourly.metric != null) {
                mTempTv.setText(String.format("%d", hourly.metric.temp));
            }
            //  地址
            if (cityMode != null) {
                mAddressTv.setText(cityMode.getMergerName());
            }

            //  时间
            long current = System.currentTimeMillis();
            mDayTv.setText(TimeUtils.longToString(current, "MM月dd日"));
            mTimeTv.setText(TimeUtils.longToString(current, "HH:mm"));

            //  连续天数
            mContinueCountTv.setText(String.valueOf(continueCount));
        } else {
            mHandler.postDelayed(() -> {
                updateData(hourly, cityMode, continueCount);
            }, 200);
        }
    }

    public void startScreenshot() {
        mCameraBtn.setVisibility(View.INVISIBLE);
        mEditBtn.setVisibility(View.INVISIBLE);
    }

    public void endScreenshot() {
        mCameraBtn.setVisibility(View.VISIBLE);
        mEditBtn.setVisibility(View.VISIBLE);
    }
    
    public void setListener(SigningListener listener) {
        this.mListener = listener;
    }
    
//    public void changeBgImage(Uri uri) {
//        Glide.with(this)
//                .load(uri)
//                .placeholder(mPlaceholder)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(mBgImgView);
////        mBgImgView.setImageURI(uri);
//    }

    public void changeBgImage(File file) {
        Glide.with(this)
                .load(file)
                .placeholder(mPlaceholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mBgImgView);
    }

    public void changeWriter(String title) {
        if (!TextUtils.isEmpty(title)) {
            mWriterTv.setText(title);
        }
    }

    private void configPressed() {
        mEditBtn.setOnClickListener(v -> {
            if (mListener != null) mListener.onEditPressed();
        });

        mCameraBtn.setOnClickListener(v -> {
            if (mListener != null) mListener.onCameraPressed();
        });
    }

    public interface SigningListener {
        void onEditPressed();
        void onCameraPressed();
    }

}
