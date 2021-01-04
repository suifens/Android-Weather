package com.goodtech.tq.views;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.goodtech.tq.R;

public class MessageAlert extends AlertDialog implements View.OnClickListener {

    private TextView mTitleTv;
    private TextView mMessageTv;
    private Button mCancelBtn;
    private Button mConfirmBtn;

    private CharSequence mTitle;
    private CharSequence mMessage;
    private int mImageId;
    private boolean mCancelable = true;

    private CharSequence mCancelText;
    private CharSequence mConfirmText;
    private int mConfirmBg;
    private int mCancelBg;
    private int mConfirmTextColor;
    private int mCancelTextColor;

    private OnClickListener mConfirmListener;
    private OnClickListener mCancelListener;

    public MessageAlert(Context context, OnClickListener confirmListener) {
        super(context);
        mConfirmListener = confirmListener;
        setCanceledOnTouchOutside(false);
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);
        mTitleTv = findViewById(R.id.tv_dialog_title);
        mMessageTv = findViewById(R.id.tv_dialog_message);
        mCancelBtn = findViewById(R.id.btn_dialog_cancel);
        if (mCancelBtn != null) mCancelBtn.setOnClickListener(this);
        mConfirmBtn = findViewById(R.id.btn_dialog_confirm);
        if (mConfirmBtn != null) mConfirmBtn.setOnClickListener(this);

//        if (mMessage != null && mMessage.length() > 0) {
//            mMessageTv.setVisibility(View.VISIBLE);
//            mMessageTv.setText(mMessage);
//        } else {
//            mMessageTv.setVisibility(View.GONE);
//        }
//
//        if (mTitle != null && mTitle.length() > 0) {
//            mTitleTv.setVisibility(View.VISIBLE);
//            mTitleTv.setText(mTitle);
//        } else {
//            mTitleTv.setVisibility(View.GONE);
//        }
//
//        if (mCancelText != null) {
//            mCancelBtn.setText(mCancelText);
//        }
//
//        if (mConfirmText != null) {
//            mConfirmBtn.setText(mConfirmText);
//        }
//
//        if (mConfirmBg != 0) {
//            mConfirmBtn.setBackgroundResource(mConfirmBg);
//        }
//
//        if (mCancelBg != 0) {
//            mCancelBtn.setBackgroundResource(mCancelBg);
//        }
//
//        if (mConfirmTextColor != 0) {
//            mConfirmBtn.setTextColor(mConfirmTextColor);
//        }
//
//        if (mCancelTextColor != 0) {
//            mCancelBtn.setTextColor(mCancelTextColor);
//        }
//
//        mCancelBtn.setVisibility(mCancelable ? View.VISIBLE : View.GONE);
    }

    public void setTitle(@StringRes int titleId) {
        setTitle(getContext().getString(titleId));
    }
    public void setTitle(@Nullable CharSequence title) {
        this.mTitle = title;
    }

    public void setMessage(@StringRes int messageId) {
        setMessage(getContext().getString(messageId));
    }
    public void setMessage(@Nullable CharSequence message) {
        this.mMessage = message;
    }

    /**
     *  取消按钮文案
     */
    public void setCancelText(@StringRes int textId) {
        setCancelText(getContext().getString(textId));
    }
    public void setCancelText(@Nullable CharSequence text) {
        this.mCancelText = text;
    }

    public void setCancelBackground(int bgRes) {
        this.mCancelBg = bgRes;
    }
    
    public void setCancelTextColor(int color) {
        this.mCancelTextColor = color;
    }

    /**
     *  确定按钮文案
     */
    public void setConfirmText(@StringRes int textId) {
        setConfirmText(getContext().getString(textId));
    }
    public void setConfirmText(@Nullable CharSequence text) {
        this.mConfirmText = text;
    }

    public void setConfirmBackground(int bgRes) {
        this.mConfirmBg = bgRes;
    }
    
    public void setConfirmTextColor(int color) {
        this.mConfirmTextColor = color;
    }

    public void setIconImage(@DrawableRes int resId) {
        this.mImageId = resId;
    }

    /**
     * 取消回调
     */
    public void setCancelListener(OnClickListener cancelListener) {
        this.mCancelListener = cancelListener;
    }

    public void setConfirmListener(OnClickListener confirmListener) {
        this.mConfirmListener = confirmListener;
    }

    /**
     * 是否显示取消按钮
     */
    public void setCancelable(boolean cancelable) {
        this.mCancelable = cancelable;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_cancel: {
                if (mCancelListener != null) {
                    mCancelListener.onClick(this, 0);
                }
                this.dismiss();
            }
                break;

            case R.id.btn_dialog_confirm: {
                if (mConfirmListener != null) {
                    mConfirmListener.onClick(this, 1);
                }
                this.dismiss();
            }
                break;
        }
    }
}
