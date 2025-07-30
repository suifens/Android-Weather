package com.gengee.insaitlib.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.gengee.insaitlib.R;

public class MessageAlert extends AlertDialog implements View.OnClickListener {

    private CharSequence mTitle;
    private CharSequence mMessage;
    private int mImageId;
    private boolean mCancelable = true;
    private boolean mShowIgnore = false;
    private boolean mIsIgnore = false;
    private ImageView mIgnoreTypeImgV;

    private CharSequence mCancelText;
    private CharSequence mConfirmText;
    private int mConfirmBg;
    private int mCancelBg;
    private int mConfirmTextColor;
    private int mCancelTextColor;
    private boolean mDismissAfterClick = true;

    private boolean mShowClose = false;
    private CharSequence mIgnoreText;

    private OnClickListener mConfirmListener;
    private OnClickListener mCancelListener;
    private OnClickListener mIgnoreListener;

    public MessageAlert(Context context) {
        super(context, R.style.MyDialog);
        setCanceledOnTouchOutside(false);
    }

    public MessageAlert(Context context, OnClickListener confirmListener) {
        super(context, R.style.MyDialog);
        mConfirmListener = confirmListener;
        setCanceledOnTouchOutside(false);
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);
        ImageView mLogoImgV = findViewById(R.id.img_dialog_logo);
        TextView mTitleTv = findViewById(R.id.tv_dialog_title);
        TextView mMessageTv = findViewById(R.id.tv_dialog_message);
        Button mCancelBtn = findViewById(R.id.btn_dialog_cancel);
        View mIgnoreView = findViewById(R.id.layout_ignore);
        if (mIgnoreView != null) mIgnoreView.setOnClickListener(this);
        TextView mIgnoreTv = findViewById(R.id.tv_ignore);
        if (mCancelBtn != null) mCancelBtn.setOnClickListener(this);
        Button mConfirmBtn = findViewById(R.id.btn_dialog_confirm);
        if (mConfirmBtn != null) mConfirmBtn.setOnClickListener(this);
        mIgnoreTypeImgV = findViewById(R.id.img_ignore_type);

        ImageView closeImgV = findViewById(R.id.closeImgV);
        if (mShowClose && closeImgV != null) {
            closeImgV.setVisibility(View.VISIBLE);
            closeImgV.setOnClickListener(this);
        }

        if (mMessage != null && mMessage.length() > 0) {
            mMessageTv.setVisibility(View.VISIBLE);
            mMessageTv.setText(mMessage);
        } else {
            mMessageTv.setVisibility(View.GONE);
        }

        if (mTitle != null && mTitle.length() > 0) {
            mTitleTv.setVisibility(View.VISIBLE);
            mTitleTv.setText(mTitle);
        } else {
            mTitleTv.setVisibility(View.GONE);
        }

        if (mImageId != 0) {
            mLogoImgV.setVisibility(View.VISIBLE);
            mLogoImgV.setImageResource(mImageId);
        } else {
            mLogoImgV.setVisibility(View.GONE);
        }

        if (mCancelText != null) {
            mCancelBtn.setText(mCancelText);
        }

        if (mConfirmText != null) {
            mConfirmBtn.setText(mConfirmText);
        }

        if (mConfirmBg != 0) {
            mConfirmBtn.setBackgroundResource(mConfirmBg);
        }

        if (mCancelBg != 0) {
            mCancelBtn.setBackgroundResource(mCancelBg);
        }
        
        if (mConfirmTextColor != 0) {
            mConfirmBtn.setTextColor(mConfirmTextColor);
        }

        if (mCancelTextColor != 0) {
            mCancelBtn.setTextColor(mCancelTextColor);
        }
        mCancelBtn.setVisibility(mCancelable ? View.VISIBLE : View.GONE);

        mIgnoreView.setVisibility(mShowIgnore ? View.VISIBLE : View.GONE);
        if (mIgnoreText != null) {
            mIgnoreTv.setText(mIgnoreText);
        }
        changeIgnore(mIsIgnore);
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

    //  设置不再提醒文案
    public void setIgnoreText(@Nullable CharSequence text) {
        this.mIgnoreText = text;
    }
    public void setShowIgnore(boolean show) {
        this.mShowIgnore = show;
    }

    public boolean isIgnore() {
        return mIsIgnore;
    }

    public void setIgnore(boolean isIgnore) {
        this.mIsIgnore = isIgnore;
    }

    public void setShowClose(boolean showClose) {
        this.mShowClose = showClose;
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

    public void setIgnoreListener(OnClickListener ignoreListener) {
        this.mIgnoreListener = ignoreListener;
    }

    /**
     * 是否显示取消按钮
     */
    public void setCancelable(boolean cancelable) {
        this.mCancelable = cancelable;
    }

    /**
     * 点击后是否自动dismiss
     */
    public void setDismissAfterClick(boolean dismissAfterClick) {
        this.mDismissAfterClick = dismissAfterClick;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_dialog_cancel) {
            if (mCancelListener != null) {
                mCancelListener.onClick(this, 0);
            }
            if (mDismissAfterClick) this.dismiss();
        } else if (id == R.id.btn_dialog_confirm) {
            if (mConfirmListener != null) {
                mConfirmListener.onClick(this, 1);
            }
            if (mDismissAfterClick) this.dismiss();
        } else if (id == R.id.layout_ignore) {
            changeIgnore(!mIsIgnore);
            if (mIgnoreListener != null) {
                mIgnoreListener.onClick(this, 2);
            }
        } else if (id == R.id.closeImgV) {
            this.dismiss();
        }
    }

    private void changeIgnore(boolean ignored) {
        mIsIgnore = ignored;
        if (ignored) {
            mIgnoreTypeImgV.setImageResource(R.drawable.ic_circle_s);
        } else {
            mIgnoreTypeImgV.setImageResource(0);
        }
    }
}
