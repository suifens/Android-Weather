package com.chunjing.tq.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

/**
 * Created by shiju.wang on 2018/2/27.
 */

public abstract class BaseVmFragment<T extends ViewBinding, V extends ViewModel> extends BaseFragment<T> {

    protected V viewModel;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressWarnings("unchecked")
    public Class<V> getViewModelClass() {
        return ViewModelTypeResolver.resolve(this, BaseVmFragment.class);
    }
}