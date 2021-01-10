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
import java.util.List;

/**
 * com.goodtech.tq
 */
public class NewsActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<String> list = new ArrayList<>();

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

        list = new ArrayList<>();
        list.add("头条");
        list.add("社会");
        list.add("国内");
        list.add("国际");
        list.add("娱乐");
        list.add("体育");
        list.add("军事");
        list.add("科技");
        list.add("财经");

        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            //得到当前页的标题，也就是设置当前页面显示的标题是tabLayout对应标题

            @Override
            public CharSequence getPageTitle(int position) {
                return list.get(position);
            }

            @NotNull
            @Override
            public Fragment getItem(int position) {
                NewsFragment newsFragment = new NewsFragment();
                //判断所选的标题，进行传值显示
                Bundle bundle = new Bundle();
                switch (list.get(position)) {
                    case "头条":
                        bundle.putString("name", "top");
                        break;
                    case "社会":
                        bundle.putString("name", "shehui");
                        break;
                    case "国内":
                        bundle.putString("name", "guonei");
                        break;
                    case "国际":
                        bundle.putString("name", "guoji");
                        break;
                    case "娱乐":
                        bundle.putString("name", "yule");
                        break;
                    case "体育":
                        bundle.putString("name", "tiyu");
                        break;
                    case "军事":
                        bundle.putString("name", "junshi");
                        break;
                    case "科技":
                        bundle.putString("name", "keji");
                        break;
                    case "财经":
                        bundle.putString("name", "caijing");
                        break;
                    case "时尚":
                        bundle.putString("name", "shishang");
                        break;
                }
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
                return FragmentStatePagerAdapter.POSITION_NONE;
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
