package com.goodtech.tq.modules.others.video.adapter;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.goodtech.tq.modules.others.video.fragment.VideoPagerFragment;

import java.util.List;

/**
 * List主页适配器
 * Created by Doikki on 2018/1/3.
 */
public class ListPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> mTitles;
    private SparseArrayCompat<Fragment> mFragments = new SparseArrayCompat<>();

    @SuppressLint("WrongConstant")
    public ListPagerAdapter(FragmentManager fm, List<String> titles) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mTitles = titles;
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        Fragment fragment = mFragments.get(position);
        if (fragment == null) {
            switch (position) {
                default:
                case 0:
                case 1:
                case 2:
                    fragment = new VideoPagerFragment();
                    break;
            }
            mFragments.put(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return mTitles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
