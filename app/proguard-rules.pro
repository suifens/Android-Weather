# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


    -dontobfuscate
    -dontoptimize

  -dontwarn org.apache.http.**
  -dontwarn android.net.http.AndroidHttpClient

#  EventBus出现 'its super classes have no public methods with the @Subscribe annotation' 的错误
  -keepattributes *Annotation*
  -keepclassmembers class ** {
      @org.greenrobot.eventbus.Subscribe <methods>;
  }
  -keep enum org.greenrobot.eventbus.ThreadMode { *; }

  # Only required if you use AsyncExecutor
  -keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
      <init>(java.lang.Throwable);
  }

#  AssertionError (GSON 2.8.6): java.lang.NoSuchFieldException: UNKNOWN
  -keep class com.goodtech.tq.models.** { *; }

#  保存四大组件
  -keep public class * extends android.app.Application
  -keep public class * extends android.app.Service
  -keep public class * extends android.content.BroadcastReceiver


#  友盟
  -keep class com.umeng.** {*;}

  -keepclassmembers class * {
     public <init> (org.json.JSONObject);
  }

  -keepclassmembers enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
  }

#  //SDK 9.2.4及以上版本自带oaid采集模块，不再需要开发者再手动引用oaid库，所以可以不添加这些混淆
  -keep class com.zui.**{*;}
  -keep class com.miui.**{*;}
  -keep class com.heytap.**{*;}
  -keep class a.**{*;}
  -keep class com.vivo.**{*;}

  -keep class com.haibin.calendarview.** { *; }

-keep class com.goodtech.tq.modules.others.calendar.view.CustomMonthView {
    public <init>(android.content.Context);
}
-keep class com.goodtech.tq.modules.others.calendar.view.CustomWeekView {
    public <init>(android.content.Context);
}

  -ignorewarnings
  -keepattributes *Annotation*
  -keepattributes Exceptions
  -keepattributes InnerClasses
  -keepattributes Signature
  -keepattributes SourceFile,LineNumberTable
  -keep class com.huawei.hianalytics.**{*;}
  -keep class com.huawei.updatesdk.**{*;}
  -keep class com.huawei.hms.**{*;}

# 微信 sdk 代码混淆
-keep class com.tencent.mm.opensdk.** {
    *;
}

-keep class com.tencent.wxop.** {
    *;
}

-keep class com.tencent.mm.sdk.** {
    *;
}


#PictureSelector 2.0
-keep class com.luck.picture.lib.** { *; }

#Ucrop
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

# pangle穿山甲 原有的
-keepclassmembers class * {
    *** getContext(...);
    *** getActivity(...);
    *** getResources(...);
    *** startActivity(...);
    *** startActivityForResult(...);
    *** registerReceiver(...);
    *** unregisterReceiver(...);
    *** query(...);
    *** getType(...);
    *** insert(...);
    *** delete(...);
    *** update(...);
    *** call(...);
    *** setResult(...);
    *** startService(...);
    *** stopService(...);
    *** bindService(...);
    *** unbindService(...);
    *** requestPermissions(...);
    *** getIdentifier(...);
   }

-keep class com.bytedance.pangle.** {*;}
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep class com.bytedance.frameworks.** { *; }

-keep class ms.bd.c.Pgl.**{*;}
-keep class com.bytedance.mobsec.metasec.ml.**{*;}

-keep class com.ss.android.**{*;}

-keep class com.bytedance.embedapplog.** {*;}
-keep class com.bytedance.embed_dr.** {*;}

-keep class com.bykv.vk.** {*;}


# applog混淆避免外部冲突
-keep public interface com.uodis.** { *; }

-keep public class com.bytedance.sdk.api.*.**{ *; }
-keep public class com.bytedance.sdk.api.**{ *; }
-keep public class com.bytedance.sdk.adapter.*.**{ *; }



# Mintegral 非AndroidX的混淆配置 15.7.27
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }

# baidu sdk
-ignorewarnings
-dontwarn com.baidu.mobads.sdk.api.**
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.baidu.mobads.** { *; }
-keep class com.style.widget.** {*;}
-keep class com.component.** {*;}
-keep class com.baidu.ad.magic.flute.** {*;}
-keep class com.baidu.mobstat.forbes.** {*;}

# 测试工具混淆
-keep class com.bytedance.mtesttools.api.** {
 public *;
}

# MSDK混淆
-keep class bykvm*.** { *; }
-keep class com.bytedance.msdk.adapter.**{ public *; }
-keep class com.bytedance.sdk.openadsdk.** {
 public *;
}

-keep class com.bytedance.msdk.base.TTBaseAd{*;}
-keep class com.bytedance.msdk.adapter.TTAbsAdLoaderAdapter{
    public *;
    protected <fields>;
}

 #ks 快手
-keep class org.chromium.** {*;}
-keep class org.chromium.** { *; }
-keep class aegon.chrome.** { *; }
-keep class com.kwai.**{ *; }
-dontwarn com.kwai.**
-dontwarn com.kwad.**
-dontwarn com.ksad.**
-dontwarn aegon.chrome.**


# Admob
-keep class com.google.android.gms.ads.MobileAds {
 public *;
}

#sigmob
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**

-keep class sun.misc.Unsafe { *; }
-dontwarn com.sigmob.**
-keep class com.sigmob.**.**{*;}

#oaid 不同的版本混淆代码不太一致，你注意你接入的oaid版本
-dontwarn com.bun.**
-keep class com.bun.** {*;}
-keep class a.**{*;}
-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.vs.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.opendeviceidlibrary.**{*;}
-keep class org.json.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}

#oaid
#-keep class XI.CA.XI.**{*;}
#-keep class XI.K0.XI.**{*;}
#-keep class XI.XI.K0.**{*;}
#-keep class XI.vs.K0.**{*;}
#-keep class XI.xo.XI.XI.**{*;}
#-keep class com.asus.msa.SupplementaryDID.**{*;}
#-keep class com.asus.msa.sdid.**{*;}
#-keep class com.bun.lib.**{*;}
#-keep class com.bun.miitmdid.**{*;}
#-keep class com.huawei.hms.ads.identifier.**{*;}
#-keep class com.samsung.android.deviceidservice.**{*;}
#-keep class org.json.**{*;}
#-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}


#klevin 游可赢
-keep class com.tencent.tgpa.lite.**{*;}
-keep class com.ihoc.mgpa.deviceid.**{*;}
-keep class com.tencent.klevin.**{*;}

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

    # 保持 Gson 的反射类不被混淆
    -keep class com.google.gson.** { *; }

    # 保持所有与 TypeToken 相关的类不被混淆
    -keep class com.google.gson.reflect.TypeToken { *; }
