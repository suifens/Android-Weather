package com.goodtech.tq.helpers.picture;

import android.app.Activity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.camera.CustomCameraView;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureSelectorUIStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;

public class PictureSelectHelper {

    public static void showGallery(Activity activity, OnResultCallbackListener<LocalMedia> result) {
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                .setPictureUIStyle(PictureSelectorUIStyle.ofDefaultStyle())
                .setPictureWindowAnimationStyle(PictureWindowAnimationStyle.ofDefaultWindowAnimationStyle())// 自定义相册启动退出动画
                .isWeChatStyle(false)// 是否开启微信图片选择风格
                .isUseCustomCamera(false)// 是否使用自定义相机
                .isWithVideoImage(false)// 图片和视频是否可以同选,只在ofAll模式下有效
                .setCustomCameraFeatures(CustomCameraView.BUTTON_STATE_BOTH)// 设置自定义相机按钮状态
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(4)// 每行显示个数
                .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                .isAndroidQTransform(true)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && .isEnableCrop(false);有效,默认处理
                .isDisplayOriginalSize(true)// 是否显示原文件大小，isOriginalImageControl true有效
                .isEditorImage(true)//是否编辑图片
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选
                .isSingleDirectReturn(true)// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                .isCamera(false)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .setCameraImageFormat(PictureMimeType.JPEG) // 相机图片格式后缀,默认.jpeg
                .isEnableCrop(true)// 是否裁剪
                .isCompress(true)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
                .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                .isCropDragSmoothToCenter(true)// 裁剪框拖动时图片自动跟随居中
                .circleDimmedLayer(false)// 是否圆形裁剪
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                .compressQuality(60)// 图片压缩后输出质量
                .cutOutQuality(50)// 裁剪输出质量 默认100
                .minimumCompressSize(100)// 小于多少kb的图片不压缩
                .forResult(result);
    }

    public static void showCamera(Activity activity, OnResultCallbackListener<LocalMedia> result) {
        PictureSelector.create(activity)
                .openCamera(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                .setPictureUIStyle(PictureSelectorUIStyle.ofDefaultStyle())
                .setPictureWindowAnimationStyle(PictureWindowAnimationStyle.ofDefaultWindowAnimationStyle())// 自定义相册启动退出动画
                .isWeChatStyle(false)// 是否开启微信图片选择风格
                .isUseCustomCamera(false)// 是否使用自定义相机
                .isWithVideoImage(false)// 图片和视频是否可以同选,只在ofAll模式下有效
                .setCustomCameraFeatures(CustomCameraView.BUTTON_STATE_BOTH)// 设置自定义相机按钮状态
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                .isAndroidQTransform(true)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对compress(false); && .isEnableCrop(false);有效,默认处理
                .isDisplayOriginalSize(true)// 是否显示原文件大小，isOriginalImageControl true有效
                .isEditorImage(true)//是否编辑图片
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选
                .isSingleDirectReturn(true)// 单选模式下是否直接返回，PictureConfig.SINGLE模式下有效
                .setCameraImageFormat(PictureMimeType.JPEG) // 相机图片格式后缀,默认.jpeg
                .isEnableCrop(true)// 是否裁剪
                .isCompress(true)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .hideBottomControls(false)// 是否显示uCrop工具栏，默认不显示
                .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
                .isCropDragSmoothToCenter(true)// 裁剪框拖动时图片自动跟随居中
                .circleDimmedLayer(false)// 是否圆形裁剪
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .showCropGrid(true)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                .compressQuality(60)// 图片压缩后输出质量
                .cutOutQuality(50)// 裁剪输出质量 默认100
                .minimumCompressSize(100)// 小于多少kb的图片不压缩
                .forResult(result);
    }
}
