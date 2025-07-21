package com.goodtech.tq.fragment.adapter;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * ViewPager2 适配器
 * 负责管理天气 Fragment 的显示
 * 
 * @author wangrengshun <wangrengshun@gengee.cn>
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    private static final String TAG = "ViewPagerAdapter";
    private static final int RETRY_DELAY_MS = 50;
    private static final int MAX_RETRY_DELAY_MS = 100;

    // Fragment 列表
    private List<Fragment> mFragments;
    
    // 主线程 Handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ViewPagerAdapter(FragmentActivity activity, List<Fragment> fragments) {
        super(activity);
        this.mFragments = fragments;
    }

    /**
     * 替换所有 Fragment
     * 使用延迟执行避免 FragmentManager 事务冲突
     */
    @SuppressLint("NotifyDataSetChanged")
    public void replaceAll(List<Fragment> fragments) {
        try {
            this.mFragments = fragments;
            // 使用 post 延迟执行，避免在 Fragment 生命周期中直接调用
            mainHandler.post(this::notifyDataSetChangedSafely);
        } catch (Exception e) {
            Log.e(TAG, "Error in replaceAll: " + e.getMessage());
        }
    }

    /**
     * 安全地通知数据集变化
     */
    private void notifyDataSetChangedSafely() {
        try {
            notifyDataSetChanged();
        } catch (IllegalStateException e) {
            Log.e(TAG, "FragmentManager transaction conflict: " + e.getMessage());
            // 如果 FragmentManager 正在执行事务，延迟重试
            mainHandler.postDelayed(this::notifyDataSetChangedSafely, MAX_RETRY_DELAY_MS);
        } catch (Exception e) {
            Log.e(TAG, "Error in notifyDataSetChanged: " + e.getMessage());
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (isValidPosition(position)) {
            return mFragments.get(position);
        }
        Log.w(TAG, "Invalid position: " + position + ", fragments size: " + mFragments.size());
        return createDefaultFragment();
    }

    @Override
    public int getItemCount() {
        return mFragments != null ? mFragments.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        // 为每个 Fragment 提供稳定的 ID，避免不必要的重建
        if (isValidPosition(position)) {
            Fragment fragment = mFragments.get(position);
            return fragment != null ? fragment.hashCode() : position;
        }
        return super.getItemId(position);
    }

    @Override
    public boolean containsItem(long itemId) {
        // 检查是否包含指定 ID 的 Fragment
        if (mFragments == null) return false;
        
        for (Fragment fragment : mFragments) {
            if (fragment != null && fragment.hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查位置是否有效
     */
    private boolean isValidPosition(int position) {
        return mFragments != null && position >= 0 && position < mFragments.size();
    }

    /**
     * 创建默认 Fragment
     */
    private Fragment createDefaultFragment() {
        // 返回一个空的 Fragment 作为默认值
        return new Fragment();
    }

    /**
     * 获取 Fragment 列表
     */
    public List<Fragment> getFragments() {
        return mFragments;
    }

    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return mFragments == null || mFragments.isEmpty();
    }

    /**
     * 获取指定位置的 Fragment
     */
    public Fragment getFragment(int position) {
        return isValidPosition(position) ? mFragments.get(position) : null;
    }
}
