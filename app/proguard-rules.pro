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

  -dontwarn org.apache.http.**
  -dontwarn android.net.http.AndroidHttpClient

#  EventBus出现 'its super classes have no public methods with the @Subscribe annotation' 的错误
  -keepattributes *Annotation*
  -keepclassmembers class * {
      @org.greenrobot.eventbus.Subscribe <methods>;
  }
  -keep enum org.greenrobot.eventbus.ThreadMode { *; }

#  AssertionError (GSON 2.8.6): java.lang.NoSuchFieldException: UNKNOWN
  -keep class com.goodtech.tq.models.** { *; }

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