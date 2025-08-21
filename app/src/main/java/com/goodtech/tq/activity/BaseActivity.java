package com.goodtech.tq.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.goodtech.tq.R;
import com.goodtech.tq.app.App;
import com.goodtech.tq.modules.others.test.PrivacyWebActivity;
import com.goodtech.tq.utils.Constants;
import com.goodtech.tq.utils.SpUtils;
import com.goodtech.tq.utils.StatusBarUtil;
import com.goodtech.tq.utils.TipHelper;
import com.goodtech.tq.views.PermissionAlert;

/**
 * com.goodtech.tq
 */
public class BaseActivity extends AppCompatActivity {

    protected Handler mHandler = new Handler(Looper.getMainLooper());

    protected View mStationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarUtil.setImmerseStatusBarSystemUiVisibility(this);
    }

     /**
     * 配置station bar
     */
    public void configStationBar(View stationBar) {
        mStationBar = stationBar;

        ConstraintLayout.LayoutParams bars = new ConstraintLayout.LayoutParams(stationBar.getLayoutParams());
        bars.height = bars.height + BarUtils.getStatusBarHeight();
        stationBar.setLayoutParams(bars);
    }

    protected void insetStatusBar(View view) {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return;
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets stateBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(stateBars.left, stateBars.top, stateBars.right, stateBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            }
        });
    }

    protected void insetSystemBarBottom(View view) {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return;
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets stateBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(stateBars.left, 0, stateBars.right, stateBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            }
        });
    }

    protected void insetNavigationBar(View view) {
        if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return;
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                v.setPadding(navigationBars.left, navigationBars.top, navigationBars.right, navigationBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            }
        });
    }

    public void showPermissionDialog(Activity activity, View.OnClickListener confirmListener) {
        new PermissionAlert(this, new PermissionAlert.PermissionAlertListener() {
            @Override
            public void onConfirmClick(View view) {
                SpUtils.getInstance().setPermissionAgree(true);
                App.instance.startUsingApp(activity);

                if (confirmListener != null) {
                    confirmListener.onClick(view);
                }
            }

            @Override
            public void onCancelClick(View view) {

            }

            @Override
            public void onAgreementClick(View view) {
                PrivacyWebActivity.redirectTo(activity,
                        Constants.URL_AGREEMENT,
                        getResources().getString(R.string.title_agreement),
                        "Agreement");
            }

            @Override
            public void onPrivateClick(View view) {
                PrivacyWebActivity.redirectTo(activity,
                        Constants.URL_PRIVACY,
                        getResources().getString(R.string.title_private),
                        "Privacy");
            }
        }).show();
    }

//     @SuppressLint("CheckResult")
//     @TargetApi(Build.VERSION_CODES.M)
//     protected void openLocationPermission(boolean phoneState) {
//         RxPermissions rxPermissions = new RxPermissions(this);
//         rxPermissions.requestEach(Manifest.permission.ACCESS_FINE_LOCATION
//                 , Manifest.permission.ACCESS_COARSE_LOCATION).subscribe(permission ->
//         {
//             if (permission.granted) {
//                 LocationHelper.getInstance().startWithDelay(this);
//             } else {
//                 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                 startActivity(intent);
//             }
//         });
//     }
//
//     @Override
//     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//         if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
//             checkOrStartLocation();
//         }
//     }
//
//     protected void checkOrStartLocation() {
//         App.instance.configLocation(this, false);
//         if (!isLocationEnabled()) {
//             Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//             startActivity(intent);
//             return;
//         }
//         LocationHelper.getInstance().startWithDelay(this);
//     }
//
//     protected static final int PERMISSION_REQUEST_COARSE_LOCATION = 10100;
//     public boolean checkLocationPermission() {
//         if (checkPermission()) {
//             return false;
//         }
//         return isLocationEnabled();
//     }
//
    protected boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED;
        }
    }

    protected boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED;
        }
    }
//
//     /**
//      * 判断定位服务是否开启
//      * @return true 表示开启
//      */
//     public boolean isLocationEnabled() {
//         int locationMode;
// //        String locationProviders;
// //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//             try {
//                 locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
//             } catch (Settings.SettingNotFoundException e) {
//                 e.printStackTrace();
//                 return false;
//             }
//             return locationMode != Settings.Secure.LOCATION_MODE_OFF;
// //        } else {
// //            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
// //            return !TextUtils.isEmpty(locationProviders);
// //        }
//     }
//
//     protected void requestLocationPermissions() {
//         if (!isLocationEnabled()) {
//             startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), PERMISSION_REQUEST_COARSE_LOCATION);
//         } else {
//             LocationHelper.getInstance().startWithDelay(this);
//         }
//     }
//
    protected boolean isLocationServicesAvailable(Context context) {
        int locationMode = 0;
        String locationProviders;
        boolean isAvailable = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }
        boolean finePermissionCheck = !checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        return isAvailable && finePermissionCheck;
    }

    protected void finishToRight() {
        finish();
        overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
    }

    protected void finishToLeft() {
        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_from_right);
    }

    protected void removeTicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mHandler.hasCallbacks(mCheckTicker)) {
                mHandler.removeCallbacks(mCheckTicker);
            }
        } else {
            mHandler.removeCallbacks(mCheckTicker);
        }
    }

    protected int scanCount = 0;
    protected final Runnable mCheckTicker = new Runnable() {
        public void run() {
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            if (scanCount++ > 5) {
                removeTicker();
                TipHelper.dismissProgressDialog();
            } else {
                mHandler.postAtTime(mCheckTicker, next);
            }
        }
    };

}
