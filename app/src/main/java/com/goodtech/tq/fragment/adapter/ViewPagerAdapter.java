package com.goodtech.tq.fragment.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class ViewPagerAdapter extends FragmentStateAdapter {

    //存放fragment的集合
    private List<Fragment> mFragments;

    public ViewPagerAdapter(FragmentActivity activity, List<Fragment> mFragments) {
        super(activity);
        this.mFragments = mFragments;
    }

    public void replaceAll(List<Fragment> fragments){
        this.mFragments = fragments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (mFragments.size() > position) {
            return mFragments.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }
}
