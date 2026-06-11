package com.chunjing.tq.utils

import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ToastUtils
import com.chunjing.tq.ext.ShareType
import com.chunjing.tq.ext.ShareType.*
import java.io.File


/**
 * com.chunjing.tq.utils
 * 图片文件分享
 */
object ShareFileUtils {

    /**
     * 分享文本
     *
     * @param context
     * @param path
     */
    fun shareUrl(context: Context, path: String?) {
        if (path == null || TextUtils.isEmpty(path)) {
            return
        }
        val it = Intent(Intent.ACTION_SEND)
        it.putExtra(Intent.EXTRA_TEXT, path)
        it.type = "text/plain"
        context.startActivity(Intent.createChooser(it, "分享APP"))
    }

    /**
     * 分享文件
     *
     * @param context
     * @param path
     */
    @JvmStatic
    fun shareFile(context: Context, path: String?) {
        if (TextUtils.isEmpty(path)) {
            return
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //传输图片或者文件 采用流的方式
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(path!!)))
        intent.type = "*/*" //分享文件
        context.startActivity(Intent.createChooser(intent, "分享"))
    }

    @JvmStatic
    fun shareImage(context: Context, shareType: ShareType, path: String) {
        when (shareType) {
            WeChat -> shareImageToWeChat(context, path)
            WeChatMoments -> shareImageToWeChatMoments(context, path)
            QQ -> shareImageToQQ(context, path)
            More -> shareImage(context, path)
        }
    }

    /**
     * 分享图片
     *
     * @param context
     * @param path
     */
    fun shareImage(context: Context, path: String) {
        shareImage(context, path, null, null)
    }

    /**
     * 分享图片到微信朋友
     */
    private fun shareImageToWeChat(context: Context, path: String) {
        if (!PackageUtils.isPackageInstalled(context, "com.tencent.mm")) {
            ToastUtils.showShort("您还没有安装微信")
            return
        }
        shareImage(context, path, "com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
    }

    /**
     * 分享图片到微信朋友圈
     */
    private fun shareImageToWeChatMoments(context: Context, path: String) {
        if (!PackageUtils.isPackageInstalled(context, "com.tencent.mm")) {
            ToastUtils.showShort("您还没有安装微信")
            return
        }
        shareImage(context, path, "com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
    }

    /**
     * 分享图片到微博
     */
    fun shareImageToWeibo(context: Context, path: String) {
        if (!PackageUtils.isPackageInstalled(context, "com.sina.weibo")) {
            ToastUtils.showShort("您还没有安装新浪微博")
            return
        }
        shareImage(context, path, "com.sina.weibo", "com.sina.weibo.EditActivity")
    }

    /**
     * 分享图片给QQ好友，单图
     */
    private fun shareImageToQQ(context: Context, path: String) {
        if (!PackageUtils.isPackageInstalled(context, "com.tencent.mobileqq")) {
            ToastUtils.showShort("您还没有安装QQ")
            return
        }
        shareImage(
            context,
            path,
            "com.tencent.mobileqq",
            "com.tencent.mobileqq.activity.JumpActivity"
        )
    }


    private fun shareImage(context: Context, path: String, pkg: String?, cls: String?) {
        try {

            if (!FileUtils.isFile(path)) {
                ToastUtils.showShort("图片不存在，请检查后重试")
            }

            val imageIntent = Intent(Intent.ACTION_SEND)
            val mimeType = "image/*"
            imageIntent.type = mimeType
            val shareUri: Uri = FileProvider.getUriForFile(
                context,
                "com.chunjing.tq.provider",
                File(path)
            )

            imageIntent.clipData = ClipData("", arrayOf(mimeType), ClipData.Item(shareUri))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 授权访问路径
                context.grantUriPermission(
                    context.packageName,
                    shareUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                if (pkg != null) {
                    //指定分享到app
                    if (pkg == "com.sina.weibo") {
                        //微博分享的需要特殊处理
                        imageIntent.setPackage(pkg)
                    } else {
                        val comp = ComponentName(pkg, cls?:"")
                        imageIntent.component = comp
                    }
                }
                imageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
//            else {
//                shareUri = Uri.fromFile(File(path))
//            }
            imageIntent.putExtra(Intent.EXTRA_STREAM, shareUri)
//            context.startActivity(Intent.createChooser(imageIntent, "分享"))

            val chooser = Intent.createChooser(imageIntent, "分享")
            val resInfoList: List<ResolveInfo> = context.packageManager
                .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    shareUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            context.startActivity(chooser)

        } catch (_: Exception) {
            ToastUtils.showShort("分享失败，未知错误")
        }
    }
}