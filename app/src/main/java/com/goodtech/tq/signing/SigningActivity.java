package com.goodtech.tq.signing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.SigningInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.collection.LruCache;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.goodtech.tq.R;
import com.goodtech.tq.app.BaseApp;
import com.goodtech.tq.base.BaseShareActivity;
import com.goodtech.tq.base.share.ShareFootView;
import com.goodtech.tq.base.share.ShareType;
import com.goodtech.tq.db.NewsDbHelper;
import com.goodtech.tq.db.SignDbHelper;
import com.goodtech.tq.fragment.adapter.ViewPagerAdapter;
import com.goodtech.tq.helpers.picture.PictureSelectHelper;
import com.goodtech.tq.models.CityMode;
import com.goodtech.tq.models.Hourly;
import com.goodtech.tq.models.db.SignRecord;
import com.goodtech.tq.utils.DeviceUtils;
import com.goodtech.tq.utils.ShotUtil;
import com.goodtech.tq.utils.TimeUtils;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.CommonBottomSheet;
import com.goodtech.tq.views.InputAlert;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.qq.e.comm.constants.Sig;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SigningActivity extends BaseShareActivity {

    private static final String EXTRA_WEATHER_HOURLY = "weather_hourly";
    private static final String EXTRA_CITY = "city";
    private static final String EXTRA_CONTINUOUS = "continuous";

    public static void redirectTo(Context ctx, Hourly model, CityMode city, int continuous) {
        Intent intent = new Intent(ctx, SigningActivity.class);
        intent.putExtra(EXTRA_CITY, city);
        intent.putExtra(EXTRA_WEATHER_HOURLY, model);
        intent.putExtra(EXTRA_CONTINUOUS, continuous);
        ctx.startActivity(intent);
    }

    protected ViewPager2 mViewPager;
    protected RadioGroup mRgIndicator;
    protected int mCurPageIndex = -1;
    protected List<Fragment> mFragments;
    protected Hourly mHourly;
    protected CityMode mCityMode;
    protected int mContinuous;
    private CommonBottomSheet mBottomSheet;
    private boolean isLoad; //  是否加载了
    private ShareFootView mFootView;
    private Button mMorningBtn;
    private Button mNightBtn;
    private SignDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);
        //  配置station
        configStationBar(findViewById(R.id.private_station_bar));

        mViewPager = findViewById(R.id.view_pager);
        mRgIndicator = findViewById(R.id.indicator);
        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        mFootView = findViewById(R.id.foot_view);

        mHourly = getIntent().getParcelableExtra(EXTRA_WEATHER_HOURLY);
        mCityMode = getIntent().getParcelableExtra(EXTRA_CITY);

        //  设置滑动回调
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (mCurPageIndex != position) {
                    onPageChange(position);
                }
            }
        });

        //  fragment列表配置
        List<Fragment> fragments = new ArrayList<>();
        addFragments(fragments, true);
        addFragments(fragments, false);

        setPagerViews(fragments);

        configClickListener();

        mDbHelper = new SignDbHelper(BaseApp.getInstance());
        SignRecord record = new SignRecord();
        long current = System.currentTimeMillis();
        record.setDateDay(com.goodtech.tq.utils.TimeUtils.longToString(current, "yyyy-MM-dd"));
        record.setCreateTime(current);
        //  设置早晚签到
        record.setSignType(TimeUtils.isDaytime(current) ? "MORNING" : "NIGHT");
        long insert = mDbHelper.insert(record);
        Log.e("TAG", "shareImage: insert = " + insert);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("TAG", "onResume: ");
        TipHelper.dismissProgressDialog();
        mContinuous = mDbHelper.queryContinuousCount();

        if (!isLoad) {
            isLoad = true;
            //  segment 初始化
            mHandler.postDelayed(() -> {
                //  持续天数
                for (Fragment fragment : mFragments) {
                    ((SigningFragment) fragment).setListener(new SigningFragment.SigningListener() {
                        @Override
                        public void onEditPressed() {
                            inputTitleDialog();
                        }

                        @Override
                        public void onCameraPressed() {
                            showAvatarPickerDialog();
                        }
                    });
                }
                mViewPager.setCurrentItem(TimeUtils.isDaytime(System.currentTimeMillis()) ? 0 : 3, false);
            }, 0);
        }
    }

    @Override
    protected void shareImage(Bitmap saveBitmap, ShareType shareType) {
        super.shareImage(saveBitmap, shareType);
    }

    /**
     * 点击配置
     */
    private void configClickListener() {
        //  保存到相册
        findViewById(R.id.layout_download).setOnClickListener(v -> onShareTypePressed(ShareType.Save));

        //  微信
        findViewById(R.id.layout_share_wechat).setOnClickListener(v -> onShareTypePressed(ShareType.WeChat));

        //  朋友圈
        findViewById(R.id.layout_share_wechat_moments).setOnClickListener(v -> onShareTypePressed(ShareType.WeChatMoments));

        //  QQ
        findViewById(R.id.layout_share_qq).setOnClickListener(v -> onShareTypePressed(ShareType.QQ));

        mMorningBtn = findViewById(R.id.btn_morning);
        mNightBtn = findViewById(R.id.btn_night);

        mMorningBtn.setOnClickListener(v -> {
            mViewPager.setCurrentItem(0);
            mHandler.postDelayed(() -> onPageChange(0), 500);
        });

        mNightBtn.setOnClickListener(v -> {
            mViewPager.setCurrentItem(3);
            mHandler.postDelayed(() -> onPageChange(3), 500);
        });
    }

    protected void changeSegmentType(boolean am) {
        mMorningBtn.setSelected(am ? true : false);
        mNightBtn.setSelected(am ? false : true);
    }

    /**
     * 配置 pager
     */
    protected void setPagerViews(List<Fragment> pagerViews) {

        this.mFragments = pagerViews;

        if (pagerViews.size() > 1) {
            mRgIndicator.setVisibility(View.VISIBLE);
        } else {
            mRgIndicator.setVisibility(View.GONE);
        }

        ViewPagerAdapter trainsPageAdapter = new ViewPagerAdapter(this, mFragments);
        mViewPager.setAdapter(trainsPageAdapter);

        int width = DeviceUtils.dip2px(this, 8);
        int height = DeviceUtils.dip2px(this, 8);
        int margin = DeviceUtils.dip2px(this, 10);
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(width, height);
        for (int i = 0; i < 3; i++) {
            RadioButton tempButton = new RadioButton(this);
            tempButton.setEnabled(false);
            tempButton.setChecked(false);
            tempButton.setBackgroundResource(R.drawable.sl_rdobtn_guide_page);   // 设置RadioButton的背景图片
            tempButton.setButtonDrawable(null);
            if (i > 0) {
                layoutParams.leftMargin = margin;
            }
            mRgIndicator.addView(tempButton, layoutParams);
        }
    }

    protected void setIndicator(int position) {
        if (position >= 0 && position < mRgIndicator.getChildCount()) {
            mRgIndicator.check(mRgIndicator.getChildAt(position).getId());
        }
    }

    protected void onPageChange(int position) {
        mCurPageIndex = position;
        setIndicator(position % 3);
        changeSegmentType(position / 3 == 0);
        SigningFragment fragment = (SigningFragment) mFragments.get(position);
        fragment.updateData(mHourly, mCityMode, mContinuous);
    }

    /**
     * 添加fragment
     *
     * @param fragments 列表
     * @param am        是否为早晨
     */
    private void addFragments(List<Fragment> fragments, boolean am) {
        SigningAFragment aFragment = new SigningAFragment();
        aFragment.setupConfig(am);
        fragments.add(aFragment);

        SigningBFragment bFragment = new SigningBFragment();
        bFragment.setupConfig(am);
        fragments.add(bFragment);

        SigningCFragment cFragment = new SigningCFragment();
        cFragment.setupConfig(am);
        fragments.add(cFragment);
    }

    /**
     * 获取截图
     */
    @Override
    protected void getScreenShotBitmap() {

        if (saveBitmap != null && !saveBitmap.isRecycled()) {
            saveBitmap.recycle();
            saveBitmap = null;
        }

        SigningFragment fragment = (SigningFragment) mFragments.get(mCurPageIndex);
        fragment.startScreenshot();
        mRgIndicator.setVisibility(View.INVISIBLE);

        saveBitmap = shotRecyclerView(fragment.getView()); // captureView(mRecyclerView);

        fragment.endScreenshot();
        mRgIndicator.setVisibility(View.VISIBLE);
    }

    private Bitmap shotRecyclerView(View view) {
        Bitmap bigBitmap;
        int height = 0;
        Paint paint = new Paint();
        int iHeight = 0;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);

        Bitmap containBit = ShotUtil.getViewBp(view);
        if (containBit != null) {
            bitmaCache.put(String.valueOf(0), containBit);
        }
        height += view.getMeasuredHeight();

        Bitmap footCache = ShotUtil.getViewBp(mFootView);
        if (footCache != null) {
            bitmaCache.put(String.valueOf(1), footCache);
        }
        height += mFootView.getMeasuredHeight();

        bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);

        Canvas bigCanvas = new Canvas(bigBitmap);
        bigCanvas.drawColor(Color.TRANSPARENT);

        for (int i = 0; i < 2; i++) {
            Bitmap tempBitmap = bitmaCache.get(String.valueOf(i));
            if (tempBitmap == null) break;
            if (tempBitmap.isRecycled()) {
                Log.e("", i + " temp bitmap is recycled");
                continue;
            }
            bigCanvas.drawBitmap(tempBitmap, 0f, iHeight, paint);
            iHeight += tempBitmap.getHeight();

            // TODO: 出现trying to use a recycled bitmap报错的原因，就是使用的相同的实例，并且之前recycle()过。所以过滤 headerView
            if (i != 0 && !tempBitmap.isRecycled()) {
                tempBitmap.recycle();
            }
        }
        return bigBitmap;
    }

    /**
     * 照相机回调
     */
    private OnResultCallbackListener<LocalMedia> mPictureCallback = new OnResultCallbackListener<LocalMedia>() {
        @Override
        public void onResult(List<LocalMedia> result) {
            mHandler.post(() -> {
                if (result.size() > 0) {
                    LocalMedia image = result.get(0);
                    if (!TextUtils.isEmpty(image.getCutPath())) {
                        SigningFragment fragment = (SigningFragment) mFragments.get(mCurPageIndex);
                        fragment.changeBgImage(new File(image.getCutPath()));
                    }
                }
            });
        }

        @Override
        public void onCancel() {

        }
    };

    @SuppressLint("CheckResult")
    protected void showAvatarPickerDialog() {
        final Activity activity = this;
        //    指定下拉列表的显示数据
        final String[] selectItems = getResources().getStringArray(R.array.file_select_item);
        if (mBottomSheet != null && mBottomSheet.isShowing()) {
            return;
        }
        mBottomSheet = new CommonBottomSheet(activity, selectItems[0], selectItems[1]);
        mBottomSheet.setCommonBottomSheetListener((item, itemTitle) -> {
            if (mRxPermissions == null) {
                mRxPermissions = new RxPermissions(activity);
            }
            if (item == 1) {
                mRxPermissions.request(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                PictureSelectHelper.showCamera(SigningActivity.this, mPictureCallback);
                            }
                        });
            } else {
                mRxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                PictureSelectHelper.showGallery(SigningActivity.this, mPictureCallback);
                            }
                        });
            }
        });
        mBottomSheet.show();
    }

    /**
     * 心灵鸡汤输入框
     */
    protected void inputTitleDialog() {

        InputAlert alert = new InputAlert(this);
        alert.setTitle("编辑文字");
        alert.setFocusable(true);
        if (mCurPageIndex % 3 == 0) {
            alert.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        }
        alert.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        TextKeyListener keyListener = new TextKeyListener(TextKeyListener.Capitalize.NONE, true);
        alert.setKeyListener(keyListener);

        alert.setConfirmListener(inputText -> {
            SigningFragment fragment = (SigningFragment) mFragments.get(mCurPageIndex);
            fragment.changeWriter(inputText);
        });

        alert.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFragments != null && !mFragments.isEmpty()) {
            mFragments.clear();
            mFragments = null;
        }
        mViewPager = null;
    }
}