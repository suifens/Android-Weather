package com.goodtech.tq.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.goodtech.tq.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputAlert extends Dialog implements View.OnClickListener {

    private TextView mTitleTv;
    private TextView mMessageTv;
    private Button mCancelBtn;
    private Button mConfirmBtn;
    private EditText mEditTv;

    private CharSequence mTitle;
    private CharSequence mMessage;
    private CharSequence mEditText;
    private CharSequence mEditHint;
    private boolean mFocusable;
    private int mType = -1;
    private KeyListener mInput;
    private InputFilter[] mFilters;
    private int mMaxEms;

    private InputAlertListener mConfirmListener;

    public interface InputAlertListener {
        void onConfirmClick(String inputText);
    }

    public InputAlert(Context context) {
        super(context, R.style.MyDialog);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
    }

    public void setConfirmListener(InputAlertListener listener) {
        mConfirmListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);

        mTitleTv = (TextView) findViewById(R.id.tv_dialog_title);
        mMessageTv = (TextView) findViewById(R.id.tv_dialog_message);
        mEditTv = (EditText) findViewById(R.id.edit_dialog_input);
        mCancelBtn = (Button) findViewById(R.id.btn_dialog_cancel);
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn = (Button) findViewById(R.id.btn_dialog_confirm);
        mConfirmBtn.setOnClickListener(this);

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

        if (mEditText != null) {
            mEditTv.setText(mEditText);
        }
        if (mEditHint != null) {
            mEditTv.setHint(mEditHint);
        }

        mEditTv.setFocusable(mFocusable);
        if (mType > -1) {
            mEditTv.setInputType(mType);
        }
        if (mInput != null) {
            mEditTv.setKeyListener(mInput);
        }
        if (mFilters != null) {
            mEditTv.setFilters(mFilters);
        }
        if (mMaxEms > 0) {
            mEditTv.setMaxEms(mMaxEms);
        }

        this.setOnShowListener(dialog -> mEditTv.postDelayed(() -> {
            //设置可获得焦点
            mEditTv.setFocusable(true);
            mEditTv.setFocusableInTouchMode(true);
            //请求获得焦点
            mEditTv.requestFocus();

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mEditTv, InputMethodManager.SHOW_IMPLICIT);
        },200));
    }

    public void setMessage(@StringRes int messageId) {
        setMessage(getContext().getString(messageId));
    }

    public void setTitle(@StringRes int titleId) {
        setTitle(getContext().getString(titleId));
    }

    public void setMessage(@Nullable CharSequence message) {
        this.mMessage = message;
    }

    public void setTitle(@Nullable CharSequence title) {
        this.mTitle = title;
    }

    public void setEditText(@StringRes int editTextId) {
        setEditText(getContext().getString(editTextId));
    }

    public void setEditText(@Nullable CharSequence editText) {
        this.mEditText = editText;
    }

    public void setEditTextHint(@StringRes int hintId) {
        setEditTextHint(hintId);
    }

    public void setEditTextHint(@Nullable CharSequence hint) {
        this.mEditHint = hint;
    }

    public void setFocusable(boolean focusable) {
        this.mFocusable = focusable;
    }

    public void setInputType(int type) {
        this.mType = type;
    }

    public void setKeyListener(KeyListener input) {
        this.mInput = input;
    }

    public void setFilters(InputFilter[] filters) {
        this.mFilters = filters;
    }

    public void setMaxEms(int maxEms) {
        this.mMaxEms = maxEms;
    }

    public void show() {
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        super.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_cancel:
                this.dismiss();
                break;
            case R.id.btn_dialog_confirm:
                if (mConfirmListener != null) {
                    /**
                     * 去掉首尾空格 - trim()：
                     * str.getText().toString().trim();
                     * 去掉所有空格 - replaceAll(" " , "") ;
                     * str.getText().toString().replaceAll(" " ,"");
                     */
                    mConfirmListener.onConfirmClick(mEditTv.getText().toString().trim());
                }
                this.dismiss();
                break;
        }
    }

    public class NameLengthFilter implements InputFilter {
        int MAX_EN;
        String regEx = "[\\u4e00-\\u9fa5]";

        public NameLengthFilter(int mAX_EN) {
            super();
            MAX_EN = mAX_EN;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            int destCount = dest.toString().length() + getChineseCount(dest.toString());
            int sourceCount = source.toString().length() + getChineseCount(source.toString());
            if (destCount + sourceCount > MAX_EN) {
                int surplusCount = MAX_EN - destCount;
                StringBuilder result = new StringBuilder();
                int index = 0;
                while (surplusCount > 0) {
                    char c = source.charAt(index);
                    if (isChinese(String.valueOf(c))) {
                        if (sourceCount >= 2) {
                            result.append(c);
                        }
                        surplusCount = surplusCount - 2;
                    } else {
                        result.append(c);
                        surplusCount = surplusCount - 1;
                    }
                    index++;
                }
                return result.toString();
            } else {
                return source;
            }
        }

        private int getChineseCount(String str) {
            int count = 0;
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(str);
            while (m.find()) {
                for (int i = 0; i <= m.groupCount(); i++) {
                    count = count + 1;
                }
            }
            return count;
        }

        private boolean isChinese(String source) {
            return Pattern.matches(regEx, source);
        }
    }

}
