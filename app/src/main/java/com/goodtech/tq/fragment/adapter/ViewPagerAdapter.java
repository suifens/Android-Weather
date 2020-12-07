package com.goodtech.tq.fragment.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    //存放fragment的集合
    private List<Fragment> mFragments;

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> mFragments) {
        super(fm);
        this.mFragments = mFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void replaceAll(List<Fragment> fragments){
        this.mFragments = fragments;
        notifyDataSetChanged();
    }
}
