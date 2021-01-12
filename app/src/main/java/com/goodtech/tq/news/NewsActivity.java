package com.goodtech.tq.news;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.goodtech.tq.BaseActivity;
import com.goodtech.tq.R;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * com.goodtech.tq
 */
public class NewsActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<NewsType> list = new ArrayList<>();
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        configStationBar(findViewById(R.id.private_station_bar));

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isFirstLoad) {
            isFirstLoad = false;

            list = Arrays.asList(NewsType.values());

            viewPager.setOffscreenPageLimit(list.size());
            viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
                //得到当前页的标题，也就是设置当前页面显示的标题是tabLayout对应标题

                @Override
                public CharSequence getPageTitle(int position) {
                    return list.get(position).cnKey;
                }

                @NotNull
                @Override
                public Fragment getItem(int position) {
                    NewsFragment newsFragment = new NewsFragment();
                    //判断所选的标题，进行传值显示
                    Bundle bundle = new Bundle();
                    bundle.putString("type", list.get(position).toString());
                    newsFragment.setArguments(bundle);
                    return newsFragment;
                }

                @NonNull
                @Override
                public Object instantiateItem(@NonNull ViewGroup container, int position) {

                    return super.instantiateItem(container, position);
                }

                @Override
                public int getItemPosition(@NonNull Object object) {
                    return POSITION_NONE;
                }

                @Override
                public int getCount() {
                    return list.size();
                }
            });

            //TabLayout要与ViewPager关联显示
            tabLayout.setupWithViewPager(viewPager);
        }
    }
}
