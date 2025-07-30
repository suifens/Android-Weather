package com.gengee.insaitlib.ble.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * 日志工具类
 *
 */
@SuppressLint("SimpleDateFormat")
public class LogUtil {
	public static boolean mDebuggable = true;

	public static void d(String tag, String msg) {
		if (mDebuggable) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss SSS");
			String time = dateFormat.format(System.currentTimeMillis());
			Log.d(time + " " + tag, "" + msg);
		}
	}

	public static void i(String tag, String msg) {
		if (mDebuggable) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss SSS");
			String time = dateFormat.format(System.currentTimeMillis());
			Log.i(time + " " + tag, "" + msg);
		}
	}

	public static void w(String tag, String msg) {
		if (mDebuggable) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss SSS");
			String time = dateFormat.format(System.currentTimeMillis());
			Log.w(time + " " + tag, "" + msg);
		}
	}

	public static void e(String tag, String msg) {
		if (mDebuggable) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss SSS");
			String time = dateFormat.format(System.currentTimeMillis());
			Log.e(time + " " + tag, "" + msg);
		}
	}

}
