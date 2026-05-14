package com.gengee.insaitlib.ui.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

public abstract class BaseVmActivity<T extends ViewBinding, V extends ViewModel> extends BindingActivity<T> {

    protected V viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init() {
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
//        autoBindView();
        super.init();
    }

    private void autoBindView() {
        Class<T> vClass = (Class<T>) resolveBindingClassFromHierarchy();
        try {
            Method inflate = vClass.getMethod("inflate", LayoutInflater.class);
            mBinding = (T) inflate.invoke(null,getLayoutInflater());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析 ViewModel 类型。Kotlin 子类或混淆后 {@link Class#getGenericSuperclass()} 可能不是 {@link ParameterizedType}，
     * 因此沿继承链查找；仍失败时请子类重写本方法（Kotlin 中 override 即可）。
     */
    @SuppressWarnings("unchecked")
    public Class<V> getViewModelClass() {
        Class<?> vmClass = resolveViewModelClassFromHierarchy();
        if (vmClass != null && ViewModel.class.isAssignableFrom(vmClass)) {
            return (Class<V>) vmClass;
        }
        throw new IllegalStateException(
                "无法从泛型签名解析 ViewModel 类型: " + getClass().getName()
                        + "。请在 Activity 中重写 getViewModelClass()，或检查 ProGuard/R8 是否保留 -keepattributes Signature。");
    }

    private Class<?> resolveViewModelClassFromHierarchy() {
        Class<?> cursor = getClass();
        while (cursor != null && cursor != Object.class) {
            Type genericSuper = cursor.getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericSuper;
                Type raw = pt.getRawType();
                if (raw instanceof Class<?>) {
                    Class<?> rawClass = (Class<?>) raw;
                    if (!BaseVmActivity.class.isAssignableFrom(rawClass)) {
                        cursor = cursor.getSuperclass();
                        continue;
                    }
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length >= 2) {
                        Class<?> vmClass = resolveTypeToClass(args[1]);
                        if (vmClass != null && ViewModel.class.isAssignableFrom(vmClass)) {
                            return vmClass;
                        }
                    }
                }
            }
            cursor = cursor.getSuperclass();
        }
        return null;
    }

    private Class<?> resolveBindingClassFromHierarchy() {
        Class<?> cursor = getClass();
        while (cursor != null && cursor != Object.class) {
            Type genericSuper = cursor.getGenericSuperclass();
            if (genericSuper instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericSuper;
                Type raw = pt.getRawType();
                if (raw instanceof Class<?>) {
                    Class<?> rawClass = (Class<?>) raw;
                    if (!BaseVmActivity.class.isAssignableFrom(rawClass)) {
                        cursor = cursor.getSuperclass();
                        continue;
                    }
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length >= 1) {
                        Class<?> bindingClass = resolveTypeToClass(args[0]);
                        if (bindingClass != null && ViewBinding.class.isAssignableFrom(bindingClass)) {
                            return bindingClass;
                        }
                    }
                }
            }
            cursor = cursor.getSuperclass();
        }
        throw new IllegalStateException(
                "无法从泛型签名解析 ViewBinding 类型: " + getClass().getName());
    }

    private static Class<?> resolveTypeToClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] upper = wt.getUpperBounds();
            if (upper.length > 0) {
                return resolveTypeToClass(upper[0]);
            }
        }
        if (type instanceof ParameterizedType) {
            return resolveTypeToClass(((ParameterizedType) type).getRawType());
        }
        return null;
    }

    @Override
    public T bindView() {

        return null;
    }
}

