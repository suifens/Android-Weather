package com.goodtech.tq.citySearch.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.goodtech.tq.R;
import com.goodtech.tq.cityList.CityListRecyclerAdapter;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.WeatherModel;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.ImageUtils;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

/**
 * com.goodtech.tq.fragment.viewholder
 */
public class CityHolder extends AbstractDraggableItemViewHolder implements View.OnClickListener {
    private Context mContext;
    private CityListRecyclerAdapter.OnItemClickListener mListener;
    private TextView mCityNameTv;
    private CityMode mCityMode;
    private LinearLayout mWeatherLayout;
    private ImageView mWeatherIcon;
    private TextView mTempTv;
    private ImageView mLocationTip;
    public RelativeLayout mContainer;
    public View mDragHandle;
    public ImageView mDeleteBtn;

    private boolean isEdit;

    public CityHolder(Context context, View view, CityListRecyclerAdapter.OnItemClickListener listener) {
        super(view);
        mContext = context;
        mContainer = view.findViewById(R.id.container);
        mContainer.setOnClickListener(this);
        mDragHandle = view.findViewById(R.id.img_city_drag);
        mDeleteBtn = view.findViewById(R.id.img_delete);
        mDeleteBtn.setOnClickListener(this);
        mCityNameTv = view.findViewById(R.id.tv_city_name);
        mLocationTip = view.findViewById(R.id.img_location);
        mWeatherLayout = view.findViewById(R.id.layout_weather);
        mWeatherIcon = view.findViewById(R.id.img_icon);
        mTempTv = view.findViewById(R.id.tv_temperature);
        mListener = listener;
        view.findViewById(R.id.btn_delete).setOnClickListener(this);
    }

    @SuppressLint("DefaultLocale")
    public void setCityMode(@NonNull CityMode mode, WeatherModel weatherModel, boolean isEdit) {
        this.isEdit = isEdit;
        this.mCityMode = mode;

        if (mode.location) {
            if (TextUtils.isEmpty(mode.city)) {
                mCityNameTv.setText("定位");
            } else {
                if (mCityNameTv != null) {
                    mCityNameTv.setText(mode.city);
                }
            }
            mLocationTip.setVisibility(View.VISIBLE);
        } else {
            if (mCityNameTv != null) {
                mCityNameTv.setText(mode.city);
            }
            mLocationTip.setVisibility(View.GONE);
            if (isEdit) {
                mDeleteBtn.setVisibility(View.VISIBLE);
                mDragHandle.setVisibility(View.VISIBLE);
                mWeatherLayout.setVisibility(View.GONE);
                mContainer.setOnClickListener(null);
            } else {
                mDeleteBtn.setVisibility(View.GONE);
                mDragHandle.setVisibility(View.GONE);
                mWeatherLayout.setVisibility(View.VISIBLE);
                mContainer.setOnClickListener(this);
            }
        }

        if (weatherModel != null && weatherModel.observation != null) {
            mWeatherIcon.setVisibility(View.VISIBLE);
            mWeatherIcon.setImageResource(ImageUtils.weatherImageRes(weatherModel.observation.wxIcon));
            if (weatherModel.observation != null && weatherModel.observation.metric != null) {
                mTempTv.setText(String.format("%d℃", weatherModel.observation.metric.temp));
            }
            mTempTv.setTextColor(ContextCompat.getColor(mTempTv.getContext(), R.color.color_00c4ff));
        } else {
            mWeatherIcon.setVisibility(View.GONE);
            mTempTv.setText("数据更新中");
            mTempTv.setTextColor(ContextCompat.getColor(mTempTv.getContext(), R.color.color_4a4a4a));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_delete:
                showDeleteAnim();
                mContainer.setOnClickListener(this);
                if (mListener != null) {
                    mListener.onShowDelete(this);
                }
                break;
            case R.id.container:
                if (isEdit) {
                    hideDeleteAnim();
                    mContainer.setOnClickListener(null);
                    if (mListener != null) {
                        mListener.onShowDelete(null);
                    }
                } else {
                    if (mListener != null) {
                        mListener.onItemClick(v, getAdapterPosition(), mCityMode);
                    }
                }
                break;
            case R.id.btn_delete:
                if (mListener != null) {
                    mListener.onDeleteCity(getAdapterPosition(), mCityMode);
                }
                break;
        }
    }

    public void showDeleteAnim() {
//        mContainer.startAnimation(showAnim);
        moveAnimation(mContainer, 0, -70);
    }

    public void hideDeleteAnim() {
//        mContainer.startAnimation(hideAnim);
        moveAnimation(mContainer, 0, 0);
    }

    private void moveAnimation(final View view, final int fromMargin, final int toMargin) {
        AnimationSet mAnimatorSet = new AnimationSet(true);
        TranslateAnimation moveUpAnimation = new TranslateAnimation(0F,
                dpToPx(toMargin), 0, 0);
        moveUpAnimation.setDuration(300);//设置动画变化的持续时间
        moveUpAnimation.setFillEnabled(true);//使其可以填充效果从而不回到原地
        moveUpAnimation.setFillAfter(true);//不回到起始位置
        mAnimatorSet.addAnimation(moveUpAnimation);
        view.startAnimation(mAnimatorSet);

        mAnimatorSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                //动画结束后更新view到终点位置
                FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(view.getLayoutParams());
                ll.setMargins(dpToPx(toMargin), 0, dpToPx(fromMargin - toMargin), 0);
                view.setLayoutParams(ll);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public static int dpToPx(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }
}
