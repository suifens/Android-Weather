package com.chunjing.tq.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.goodtech.weatherlib.dialog.LoadingDialog;

/**
 * Created by shiju.wang on 2018/2/27.
 */

public abstract class BaseFragment<T extends ViewBinding> extends Fragment {

    protected Activity mContext;
//    private LoadingDialog loadingDialog;
    private Boolean isLoaded = false;
    protected T mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = bindView();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {
            isLoaded = true;
            loadData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissLoading();
    }

    private LoadingDialog loadingDialog;

    protected void dismissLoading() {
        showLoading(false, null);
    }

    protected void showLoading(boolean show) {
        showLoading(show, null);
    }

    protected void showLoading(boolean show, String tip) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(requireActivity());
        }
        if (show) {
            if (tip != null && !tip.isEmpty()) {
                loadingDialog.setTip(tip);
            } else {
                loadingDialog.setTip("请稍候...");
            }
            loadingDialog.show();
        } else {
            loadingDialog.dismiss();
        }
    }

    /**
     * [页面跳转]
     *
     * @param clz
     */
    public void startActivity(Class<?> clz) {
        startActivity(new Intent(mContext, clz));
    }

//    public abstract int bindLayout();

    protected abstract T bindView();

    public abstract void initView(View view);

    public abstract void initEvent();

    /**
     * 数据初始化，只会执行一次
     */
    public abstract void loadData();

    @Override
    public void onDestroy() {
        super.onDestroy();
        isLoaded = false;
    }
}