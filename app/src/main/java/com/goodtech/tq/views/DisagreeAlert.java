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
import com.goodtech.tq.modules.others.test.PrivacyWebActivity;
import com.goodtech.tq.utils.Constants;

public class DisagreeAlert extends AlertDialog implements View.OnClickListener {

    private static final String SDK_LIST_STR = "《第三方SDK信息共享清单》";

    private Context mContext;
    private TextView mMessageTv;
    private TextView mSdkListTv;

    private Button mCancelBtn;

    private Button mConfirmBtn;

    private DisagreeAlertListener mListener;

    public interface DisagreeAlertListener {
        void onConfirmClick(View view);
        void onCancelClick(View view);
        void onAgreementClick(View view);
        void onPrivateClick(View view);
        void onVisitorClick(View view);
    }

    public DisagreeAlert(Context context, DisagreeAlertListener listener) {
        super(context, R.style.PermissionAlertTheme);
        mContext = context;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_disagree);
        mMessageTv = findViewById(R.id.tv_dialog_message);
        mSdkListTv = findViewById(R.id.tv_dialog_sdk_list_link);
        mCancelBtn = findViewById(R.id.btn_dialog_cancel);
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn = findViewById(R.id.btn_dialog_confirm);
        mConfirmBtn.setOnClickListener(this);
        findViewById(R.id.btn_dialog_visitor).setOnClickListener(this);
        configSpannable();
        configSdkListLink();
    }

    private void configSpannable() {
        String permissionStr = mContext.getString(R.string.msg_disagree);
        SpannableString spannableString = new SpannableString(permissionStr);
        String agreementStr = mContext.getString(R.string.agreement_title);
        int agreementStart = permissionStr.indexOf(agreementStr);
        int agreementEnd = agreementStart + agreementStr.length();
        String privateStr = mContext.getString(R.string.private_title);
        int privateStart = permissionStr.indexOf(privateStr);
        int privateEnd = privateStart + privateStr.length();
        int sdkListStart = permissionStr.indexOf(SDK_LIST_STR);
        int sdkListEnd = sdkListStart + SDK_LIST_STR.length();

        if (agreementStart >= 0) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (mListener != null) {
                        mListener.onAgreementClick(widget);
                    }
                }
            };
            spannableString.setSpan(clickableSpan, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableString.setSpan(new UnderlineSpan() {
                @Override
                public void updateDrawState(TextPaint ds) {
                    ds.setColor(mContext.getResources().getColor(R.color.color_theme));
                    ds.setUnderlineText(false);
                }
            }, agreementStart, agreementEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        if (privateStart >= 0) {
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
                    ds.setColor(mContext.getResources().getColor(R.color.color_theme));
                    ds.setUnderlineText(false);
                }
            }, privateStart, privateEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        if (sdkListStart >= 0) {
            ForegroundColorSpan colorSp = new ForegroundColorSpan(Color.parseColor("#00C4FF"));
            spannableString.setSpan(colorSp, sdkListStart, sdkListEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            ClickableSpan sdkListClickable = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    openSdkListPage();
                }
            };
            spannableString.setSpan(sdkListClickable, sdkListStart, sdkListEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        mMessageTv.setMovementMethod(LinkMovementMethod.getInstance());
        mMessageTv.setText(spannableString);
    }

    private void configSdkListLink() {
        String footerStr = mContext.getString(R.string.permission_sdk_list_footer);
        SpannableString spannableString = new SpannableString(footerStr);
        int sdkListStart = footerStr.indexOf(SDK_LIST_STR);
        if (sdkListStart >= 0) {
            int sdkListEnd = sdkListStart + SDK_LIST_STR.length();
            ForegroundColorSpan colorSp = new ForegroundColorSpan(Color.parseColor("#00C4FF"));
            spannableString.setSpan(colorSp, sdkListStart, sdkListEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    openSdkListPage();
                }
            };
            spannableString.setSpan(clickableSpan, sdkListStart, sdkListEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        mSdkListTv.setMovementMethod(LinkMovementMethod.getInstance());
        mSdkListTv.setText(spannableString);
    }

    private void openSdkListPage() {
        PrivacyWebActivity.redirectTo(mContext,
                Constants.URL_PRIVACY_LIST,
                mContext.getResources().getString(R.string.title_share_list),
                "SdkList");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_dialog_cancel) {
            if (mListener != null) {
                mListener.onCancelClick(v);
            }
            this.dismiss();
        } else if (v.getId() == R.id.btn_dialog_confirm) {
            if (mListener != null) {
                mListener.onConfirmClick(v);
            }
            this.dismiss();
        } else if (v.getId() == R.id.btn_dialog_visitor) {
            if (mListener != null) {
                mListener.onVisitorClick(v);
            }
            this.dismiss();
        }
    }
}
