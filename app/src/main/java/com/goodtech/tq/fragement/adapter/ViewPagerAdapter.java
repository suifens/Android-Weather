package com.goodtech.tq.fragement.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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

    public void replaceAll(List<Fragment> fragments){
        this.mFragments = fragments;
        notifyDataSetChanged();
        
    }
}
