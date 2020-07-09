package com.goodtech.tq.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goodtech.tq.R;

public class DisagreeAlert extends AlertDialog implements View.OnClickListener {

    private TextView mMessageTv;

    private Button mCancelBtn;

    private Button mConfirmBtn;

    private DisagreeAlertListener mListener;

    public interface DisagreeAlertListener {
        void onConfirmClick(View view);
    }

    public DisagreeAlert(Context context, DisagreeAlertListener listener) {
        super(context);
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_disagree);
        mMessageTv = (TextView) findViewById(R.id.tv_dialog_message);
        mCancelBtn = (Button) findViewById(R.id.btn_dialog_cancel);
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn = (Button) findViewById(R.id.btn_dialog_confirm);
        mConfirmBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_cancel:
                this.dismiss();
                break;
            case R.id.btn_dialog_confirm:
                if (mListener != null) {
                    mListener.onConfirmClick(v);
                }
                this.dismiss();
                break;
        }
    }
}
