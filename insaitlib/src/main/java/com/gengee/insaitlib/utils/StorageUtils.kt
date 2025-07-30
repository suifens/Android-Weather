package com.gengee.insaitlib.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Environment
import android.os.StatFs
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * com.gengee.insaitlib
 */
object StorageUtils {

    fun getAvailableRom(): Double {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val size = blockSize * availableBlocks / 1024 / 1024 / 1024.0 //返回G
        Log.d("HomeFragment", "--- getAvailableRom: 剩余容量 $size G")
        return size
    }

    /**
     * 压缩图片到目标大小以下
     */
    fun compressBmpFileToTargetSize(file: File, targetSize: Long) {
        if (file.length() > targetSize) {
            // 每次宽高各缩小一半
            val ratio = 2
            // 获取图片原始宽高
            val options = BitmapFactory.Options()
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            var targetWidth = options.outWidth / ratio
            var targetHeight = options.outHeight / ratio

            // 压缩图片到对应尺寸
            val baos = ByteArrayOutputStream()
            val quality = 100
            var result = generateScaledBmp(bitmap, targetWidth, targetHeight, baos, quality)

            // 计数保护，防止次数太多太耗时。
            var count = 0
            while (baos.size() > targetSize && count <= 10) {
                targetWidth /= ratio
                targetHeight /= ratio
                count++

                // 重置，不然会累加
                baos.reset()
                result = generateScaledBmp(result, targetWidth, targetHeight, baos, quality)
            }
            try {
                val fos = FileOutputStream(file)
                fos.write(baos.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 图片缩小一半
     */
    fun generateScaledBmp(
        srcBmp: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        baos: ByteArrayOutputStream?,
        quality: Int
    ): Bitmap {
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val rect = Rect(0, 0, result.width, result.height)
        canvas.drawBitmap(srcBmp, null, rect, null)
        if (!srcBmp.isRecycled) {
            srcBmp.recycle()
        }
        baos?.let { result.compress(Bitmap.CompressFormat.JPEG, quality, it) }
        return result
    }
}