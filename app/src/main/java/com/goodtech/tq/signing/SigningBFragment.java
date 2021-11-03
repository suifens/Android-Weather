package com.goodtech.tq.signing;

import com.goodtech.tq.R;

public class SigningBFragment extends SigningFragment {

    @Override
    protected int getViewLayoutRes() {
        return R.layout.fragment_signing_b;
    }

    /**
     * 配置数据
     * @param am 是否是早上
     */
    protected void configData(boolean am) {
        super.configData(am);
        mPlaceholder = am ? R.drawable.bg_morning_b : R.drawable.bg_night_b;
        mBgImgView.setImageResource(mPlaceholder);
        mWriterTv.setText(am ? "希望阳光很暖\n微风不燥" : "从遇见你开始\n凛冬散尽星河长明");
    }

    @Override
    protected void recoverWriter(boolean am) {
        super.recoverWriter(am);
        mWriterTv.setText(am ? "希望阳光很暖\n微风不燥" : "从遇见你开始\n凛冬散尽星河长明");
    }
}
