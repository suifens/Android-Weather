package com.goodtech.tq.modules.others.constellation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.goodtech.tq.R;
import com.goodtech.tq.fragment.BaseFragment;
import com.goodtech.tq.models.constellation.ConsDayMode;

public class ConsDetailFragment extends BaseFragment {

    private ScoreView mAllScoreView;
    private ScoreView mWorkScoreView;
    private ScoreView mLoveScoreView;
    private ScoreView mMoneyScoreView;
    private TextView mHealthTv;
    private TextView mFriendTv;
    private TextView mColorTv;
    private TextView mNumberTv;
    private ConsItemView mConsItemView;

    private ConsDayMode mConsModel;

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
        View view = inflater.inflate(R.layout.fragment_cons_detail, container, false);
        mAllScoreView = view.findViewById(R.id.score_all);
        mWorkScoreView = view.findViewById(R.id.score_work);
        mLoveScoreView = view.findViewById(R.id.score_love);
        mMoneyScoreView  = view.findViewById(R.id.score_money);
        mHealthTv = view.findViewById(R.id.tv_health);
        mFriendTv = view.findViewById(R.id.tv_friend);
        mColorTv = view.findViewById(R.id.tv_color);
        mNumberTv = view.findViewById(R.id.tv_number);
        mConsItemView = view.findViewById(R.id.view_cons);
        return view;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public ConsDayMode getConsModel() {
        return mConsModel;
    }

    @SuppressLint("SetTextI18n")
    public void setConsDetailModel(ConsDayMode consDayModel) {
        this.mConsModel = consDayModel;
        mHandler.post(() -> {
            if (mConsModel != null) {
                mAllScoreView.setScore(Math.round(Float.parseFloat(mConsModel.all)/20));
                mWorkScoreView.setScore(Math.round(Float.parseFloat(mConsModel.work)/20));
                mLoveScoreView.setScore(Math.round(Float.parseFloat(mConsModel.love)/20));
                mMoneyScoreView.setScore(Math.round(Float.parseFloat(mConsModel.money)/20));
                mHealthTv.setText(String.format("%s%%", mConsModel.health));
                mFriendTv.setText(mConsModel.QFriend);
                mColorTv.setText(mConsModel.color);
                mNumberTv.setText("" + mConsModel.number);
                mConsItemView.setData(R.drawable.ic_all, "整体", mConsModel.summary);
            }
        });
    }

}
