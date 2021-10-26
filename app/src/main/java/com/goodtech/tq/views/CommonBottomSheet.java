package com.goodtech.tq.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.blankj.utilcode.util.ScreenUtils;
import com.goodtech.tq.R;

public class CommonBottomSheet {

    protected Dialog mDialog;

    private String mFirstTitle;

    private String mSecondTitle;

    private Button mFirstBtn;

    private Button mSecondBtn;

    private CommonBottomSheetListener mListener;

    public CommonBottomSheet(Activity activity, String firstTitle, String secondTitle) {

        if (mDialog != null) {
            return;
        }

        mFirstTitle = firstTitle;
        mSecondTitle = secondTitle;

        mDialog = new Dialog(activity, R.style.MyDialog);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mDialog = null;
            }
        });

        final View contentView = LayoutInflater.from(activity).inflate(R.layout.view_common_bottom_sheet, null);

        mFirstBtn = contentView.findViewById(R.id.btn_sheet_first);
        mFirstBtn.setText(firstTitle);
        mFirstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickItem(0, mFirstTitle);
                }
                dismiss();
            }
        });

        mSecondBtn = contentView.findViewById(R.id.btn_sheet_second);
        mSecondBtn.setText(secondTitle);
        mSecondBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickItem(1, mSecondTitle);
                }
                dismiss();
            }
        });

        contentView.findViewById(R.id.select_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mDialog.setContentView(contentView);
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setWindowAnimations(R.style.dialogWindowAnim);
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = ScreenUtils.getScreenWidth();
        contentView.measure(0, 0);
        lp.height = contentView.getMeasuredHeight();

        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
    }

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    public void show() {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void setCommonBottomSheetListener(CommonBottomSheetListener listener) {
        mListener = listener;
    }

    public interface CommonBottomSheetListener {
        void onClickItem(int item, String itemTitle);
    }

}
