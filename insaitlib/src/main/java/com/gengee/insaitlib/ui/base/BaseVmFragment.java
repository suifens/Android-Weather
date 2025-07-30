package com.gengee.insaitlib.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.ParameterizedType;

/**
 * Created by shiju.wang on 2018/2/27.
 */

public abstract class BaseVmFragment<T extends ViewBinding, V extends ViewModel> extends BindingFragment<T> {

    protected V viewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        super.onViewCreated(view, savedInstanceState);
    }

    public Class<V> getViewModelClass() {
        Class<V> xClass = (Class<V>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        return xClass;
    }
}