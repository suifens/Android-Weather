package com.goodtech.tq.views;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.goodtech.tq.R;

public class PermissionAlert extends AlertDialog implements View.OnClickListener {

    private Context mContext;
    private TextView mMessageTv;

    private Button mCancelBtn;

    private Button mConfirmBtn;

    private PermissionAlertListener mListener;

    public interface PermissionAlertListener {
        void onConfirmClick(View view);
        void onCancelClick(View view);
        void onAgreementClick(View view);
        void onPrivateClick(View view);
    }

    public PermissionAlert(Context context, PermissionAlertListener listener) {
        super(context, R.style.PermissionAlertTheme);
        mContext = context;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_permission);
        mMessageTv = (TextView) findViewById(R.id.tv_dialog_message);
        mCancelBtn = (Button) findViewById(R.id.btn_dialog_cancel);
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn = (Button) findViewById(R.id.btn_dialog_confirm);
        mConfirmBtn.setOnClickListener(this);
        configSpannable();
    }

    private void configSpannable() {
        String permissionStr = mContext.getString(R.string.msg_permission);
        SpannableString spannableString = new SpannableString(permissionStr);
        String agreementStr = mContext.getString(R.string.agreement_title);
        int agreementStart = permissionStr.indexOf(agreementStr);
        int agreementEnd = agreementStart + agreementStr.length();
        String privateStr = mContext.getString(R.string.private_title);
        int privateStart = permissionStr.indexOf(privateStr);
        int privateEnd = privateStart + privateStr.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (mListener != null) {
                    mListener.onAgreementClick(widget);
                }
            }
        };
        spannableString.setSpan(clickableSpan, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        ClickableSpan privateClickable = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (mListener != null) {
                    mListener.onPrivateClick(widget);
                }
            }
        };
        spannableString.setSpan(privateClickable, privateStart, privateEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        spannableString.setSpan(new UnderlineSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mContext.getResources().getColor(R.color.color_theme));//设置颜色
                ds.setUnderlineText(false);//去掉下划线
            }
        }, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new UnderlineSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mContext.getResources().getColor(R.color.color_theme));//设置颜色
                ds.setUnderlineText(false);//去掉下划线
            }
        }, privateStart, privateEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mMessageTv.setMovementMethod(LinkMovementMethod.getInstance());
        mMessageTv.setText(spannableString);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_cancel:
                if (mListener != null) {
                    mListener.onCancelClick(v);
                }
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
