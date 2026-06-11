package com.chunjing.tq.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * 仅检测 manifest &lt;queries&gt; 中声明的包是否安装，避免枚举全量应用列表。
 */
object PackageUtils {

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
