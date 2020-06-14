package com.goodtech.tq.fragement;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class BaseFragment extends Fragment {
    
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    protected View mCacheView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mCacheView == null) {
            mCacheView = inflater.inflate(getViewLayoutRes(),container, false);
            setupCacheViews();
        }

        ViewGroup parent = (ViewGroup) mCacheView.getParent();
        if (parent != null) {
            parent.removeView(mCacheView);
        }
        return mCacheView;
    }

    /**
     * 初始化界面
     */
    protected void setupCacheViews() {
        // TODO: 2016/9/7  子类继承重写该方法
    }


    /**
     * 可以重写，重写的布局文件里面必须include包含incl_refreshable_recyclerview文件布局
     *
     * @return
     */
    protected int getViewLayoutRes() {
        return -1;
    }

}
