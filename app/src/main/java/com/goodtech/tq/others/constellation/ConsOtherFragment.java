package com.goodtech.tq.others.constellation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goodtech.tq.R;
import com.goodtech.tq.fragment.BaseFragment;
import com.goodtech.tq.models.constellation.ConsDayMode;
import com.goodtech.tq.models.constellation.ConsMonthMode;
import com.goodtech.tq.models.constellation.ConsWeekMode;
import com.goodtech.tq.models.constellation.ConsYearMode;

public class ConsOtherFragment extends BaseFragment {

    private LinearLayout mContainerLayout;
    private ConsWeekMode mWeekModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cons_other, container, false);
        mContainerLayout = view.findViewById(R.id.linear_layout);
        return view;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void clear() {
        mHandler.post(() -> {
            if (mContainerLayout != null) {
                mContainerLayout.removeAllViewsInLayout();
            }
        });
    }

    public ConsWeekMode getWeekModel() {
        return mWeekModel;
    }

    public void setConsWeekModel(ConsWeekMode model) {
        this.mWeekModel = model;
        mHandler.post(() -> {
            mContainerLayout.removeAllViewsInLayout();
            if (model != null) {
                if (!TextUtils.isEmpty(model.health)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(R.drawable.ic_heart, "健康", model.health);
                    mContainerLayout.addView(itemView);
                }
                if (!TextUtils.isEmpty(model.work)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(R.drawable.ic_work, "工作", model.work);
                    mContainerLayout.addView(itemView);
                }
                if (!TextUtils.isEmpty(model.love)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(R.drawable.ic_love, "爱情", model.love);
                    mContainerLayout.addView(itemView);
                }
                if (!TextUtils.isEmpty(model.money)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(R.drawable.ic_financial, "理财", model.money);
                    mContainerLayout.addView(itemView);
                }
            }

        });
    }

    private ConsMonthMode mMonthModel;
    public ConsMonthMode getMonthMode() {
        return mMonthModel;
    }

    public void setConsMonthModel(ConsMonthMode model) {
        this.mMonthModel = model;
        mHandler.post(() -> {
            mContainerLayout.removeAllViewsInLayout();
            if (model != null) {
                if (!TextUtils.isEmpty(model.all)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(0, null, model.all);
                    mContainerLayout.addView(itemView);
                }
                if (!TextUtils.isEmpty(model.health)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(R.drawable.ic_heart, "健康", model.health);
                    mContainerLayout.addView(itemView);
                }
                if (!TextUtils.isEmpty(model.work)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(R.drawable.ic_work, "工作", model.work);
                    mContainerLayout.addView(itemView);
                }
                if (!TextUtils.isEmpty(model.love)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(R.drawable.ic_love, "爱情", model.love);
                    mContainerLayout.addView(itemView);
                }
                if (!TextUtils.isEmpty(model.money)) {
                    ConsItemView itemView = new ConsItemView(getContext());
                    itemView.setData(R.drawable.ic_financial, "理财", model.money);
                    mContainerLayout.addView(itemView);
                }
            }

        });
    }

    private ConsYearMode mYearMode;
    public ConsYearMode getYearMode() {
        return mYearMode;
    }

    public void setConsYearModel(ConsYearMode model) {
        this.mYearMode = model;
        mHandler.post(() -> {
            mContainerLayout.removeAllViewsInLayout();
            if (model != null) {
                if (model.career != null) {
                    for (int i = 0; i < model.career.length; i++) {
                        String career = model.career[i];
                        if (!TextUtils.isEmpty(career)) {
                            ConsItemView itemView = new ConsItemView(getContext());
                            if (i == 0) {
                                itemView.setData(R.drawable.ic_work, "工作", career);
                            } else {
                                itemView.setData(0, null, career);
                            }
                            mContainerLayout.addView(itemView);
                        }
                    }
                }

                if (model.love != null) {
                    for (int i = 0; i < model.love.length; i++) {
                        String love = model.love[i];
                        if (!TextUtils.isEmpty(love)) {
                            ConsItemView itemView = new ConsItemView(getContext());
                            if (i == 0) {
                                itemView.setData(R.drawable.ic_love, "爱情", love);
                            } else {
                                itemView.setData(0, null, love);
                            }
                            mContainerLayout.addView(itemView);
                        }
                    }
                }

                if (model.finance != null) {
                    for (int i = 0; i < model.finance.length; i++) {
                        String finance = model.finance[i];
                        if (!TextUtils.isEmpty(finance)) {
                            ConsItemView itemView = new ConsItemView(getContext());
                            if (i == 0) {
                                itemView.setData(R.drawable.ic_financial, "理财", finance);
                            } else {
                                itemView.setData(0, null, finance);
                            }
                            mContainerLayout.addView(itemView);
                        }
                    }
                }
            }

        });
    }
}
