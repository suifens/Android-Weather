package com.goodtech.tq.others.video;

import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import com.goodtech.tq.activity.BaseActivity;
import com.goodtech.tq.R;
import com.goodtech.tq.others.video.adapter.ListPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        configStationBar(findViewById(R.id.private_station_bar));
        findViewById(R.id.button_back).setOnClickListener(v -> finish());


        ViewPager viewPager = findViewById(R.id.view_pager);

        List<String> titles = new ArrayList<>();
        titles.add("1");
        titles.add("2");
        titles.add("3");

        viewPager.setAdapter(new ListPagerAdapter(getSupportFragmentManager(), titles));

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

    }




}