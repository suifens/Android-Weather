package com.goodtech.tq.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.AppUtils;
import com.goodtech.tq.R;
import com.goodtech.tq.app.App;
import com.goodtech.tq.fragment.DrawDramaFragment;
import com.goodtech.tq.fragment.HomeFragment;
import com.goodtech.tq.fragment.VideoDrawFragment;
import com.goodtech.tq.modules.removeAd.RemoveAdActivity;
import com.goodtech.tq.httpClient.ApiResponseHandler;
import com.goodtech.tq.httpClient.ErrorCode;
import com.goodtech.tq.httpClient.JuHeHelper;
import com.goodtech.tq.location.helper.LocationHelper;
import com.goodtech.tq.utils.IntentReceiver;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.views.popup.UpdatePopup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lxj.xpopup.XPopup;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private final BroadcastReceiver receiver = new IntentReceiver();

    // ViewPager2和TabLayout
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private MainPagerAdapter mPagerAdapter;
    private List<Fragment> mFragmentList;
    
    // 状态变量
    private int mCurrentTabIndex = 0;
    private long mBackTime;
    
    // 颜色资源
    private int mTabCheckedColor;
    private int mTabUncheckColor;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getInstance().setMainActivity(this);
        initColors();
        setupViewPager();
//        setupTabs();
        handleIntent();
        initApp();
//        registerEventBus();
    }

    private void initColors() {
        mTabCheckedColor = ContextCompat.getColor(this, R.color.black);
        mTabUncheckColor = ContextCompat.getColor(this, R.color.color_8f);
    }

    private void setupViewPager() {
        mViewPager = findViewById(R.id.viewpager_main);
        mFragmentList = new ArrayList<>();
        
        // 添加Fragment
        mFragmentList.add(new HomeFragment());
//        mFragmentList.add(new DrawDramaFragment());
//        mFragmentList.add(new VideoDrawFragment());
        
        mPagerAdapter = new MainPagerAdapter(this, mFragmentList);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setUserInputEnabled(false);
        // 设置ViewPager2页面切换监听
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mCurrentTabIndex = position;
//                changeTab(false);
            }
        });
    }

    private void setupTabs() {
        mTabLayout = findViewById(R.id.tablayout_main);

        // 使用TabLayoutMediator连接TabLayout和ViewPager2
        TabLayoutMediator mediator = new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            tab.setCustomView(getTabView(this, TabInfo.values()[position].unTabIcon, TabInfo.values()[position].tabTitle));
        });
        mediator.attach();
        
        // 设置初始选中状态
//        changeTab(false);
    }

    private void handleIntent() {
        // 处理Intent，可以设置初始tab
        int tabIndex = getIntent().getIntExtra("RESUME_TAB_INDEX", 0);
        if (tabIndex != 0 && tabIndex < mFragmentList.size()) {
            mViewPager.setCurrentItem(tabIndex, false);
        }
    }

    private void initApp() {
        Log.e(TAG, "onCreate: ");
        
        // 检查app版本
        mHandler.postDelayed(this::checkNewVersion, 1000);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void backAction() {
        long current = System.currentTimeMillis();
        if (current - mBackTime < 2 * 1000) {
            finish();
        } else {
            mBackTime = current;
            Toast.makeText(MainActivity.this.getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SpUtils.getInstance().isAgreePermission()) {
            MobclickAgent.onResume(this);
        }
        Log.e(TAG, "onResume: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (SpUtils.getInstance().isAgreePermission()) {
            MobclickAgent.onPause(this);
        }
    }

    @Override
    protected void onStop() {
        if (SpUtils.getInstance().isAgreePermission()) {
            LocationHelper.getInstance().stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
        unregisterReceiver(receiver);
    }

//    // 事件处理
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(CityEvent event) {
//        // 处理城市相关事件
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(MessageEvent event) {
//        // 处理消息事件
//    }

    private void changeTab(boolean changeText) {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                View view = tab.getCustomView();
                ImageView icon = view.findViewById(R.id.tab_content_image);
                TextView text = view.findViewById(R.id.tab_content_text);
                TabInfo tabInfo = TabInfo.values()[i];
                
                if (changeText) {
                    text.setText(getString(tabInfo.tabTitle));
                }
                
                if (i == mCurrentTabIndex) {
                    icon.setImageResource(tabInfo.tabIcon);
                    text.setTextColor(mTabCheckedColor);
                } else {
                    icon.setImageResource(tabInfo.unTabIcon);
                    text.setTextColor(mTabUncheckColor);
                }
            }
        }
    }

    /**
     * 切换到短剧标签页
     */
    public void switchToDramaTab() {
        mViewPager.setCurrentItem(TabInfo.DRAMA.tabIndex, true);
    }

    private View getTabView(android.content.Context context, int tabIcon, int tabTitle) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_tab_content, null);
        ImageView icon = view.findViewById(R.id.tab_content_image);
        TextView text = view.findViewById(R.id.tab_content_text);
        icon.setImageResource(tabIcon);
        text.setText(context.getString(tabTitle));
        return view;
    }

    private void checkNewVersion() {
        JuHeHelper.getInstance().fetchNewVersion(new ApiResponseHandler() {
            @Override
            public void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode) {
                try {
                    if (success) {
                        if (jsonObject != null && !jsonObject.isNull("data")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            String version = data.getString("newVersion");
                            String[] versionTemp = version.split("\\.");
                            String curVersion = AppUtils.getAppVersionName();
                            String[] curTemp = curVersion.split("\\.");
                            boolean needUpdate = false;
                            for (int i = 0; i < versionTemp.length; i++) {
                                if (Integer.parseInt(versionTemp[i]) > Integer.parseInt(curTemp[i])) {
                                    needUpdate = true;
                                    break;
                                }
                            }
                            if (needUpdate
                                    && System.currentTimeMillis() - SpUtils.getInstance().getLong(version, 0L) > 2 * 24 * 60 * 60 * 1000) {
                                showUpdatePopup(version);
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showUpdatePopup(String version) {
        SpUtils.getInstance().putLong(version, System.currentTimeMillis());

        UpdatePopup popup = new UpdatePopup(this);
        popup.setupVersion(version, () -> {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "未能跳转到应用商店", Toast.LENGTH_SHORT).show();
            }
        });
        new XPopup.Builder(this)
                .isDestroyOnDismiss(false)
                .asCustom(popup)
                .show();
    }

    // ViewPager2适配器
    private static class MainPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> mFragments;

        public MainPagerAdapter(FragmentActivity fragmentActivity, List<Fragment> fragments) {
            super(fragmentActivity);
            this.mFragments = fragments;
        }

        @Override
        public Fragment createFragment(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getItemCount() {
            return mFragments.size();
        }
    }

    // Tab信息枚举
    private enum TabInfo {
        HOME(0, R.string.tab_home, R.drawable.ic_home_selected, R.drawable.ic_home_unselected),
        DRAMA(1, R.string.tab_drama, R.drawable.tab_ic_duanju_s, R.drawable.tab_ic_duanju_n),
        SETTING(2, R.string.tab_video, R.drawable.tab_ic_shiping_s, R.drawable.tab_ic_shiping_n);

        private final int tabIndex;
        private final int tabTitle;
        private final int tabIcon;
        private final int unTabIcon;

        TabInfo(int tabIndex, int tabTitle, int tabIcon, int unTabIcon) {
            this.tabIndex = tabIndex;
            this.tabTitle = tabTitle;
            this.tabIcon = tabIcon;
            this.unTabIcon = unTabIcon;
        }
    }
}
