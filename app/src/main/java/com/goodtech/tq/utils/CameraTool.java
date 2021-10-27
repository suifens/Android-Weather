package com.goodtech.tq.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.goodtech.tq.app.BaseApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.gengee.cropimage.camera.CropImageIntentBuilder;

/**
 * 拍照实用工具类。
 *
 * @author baoxiaofeng <baoxiaofeng@gengee.cn>
 */
public class CameraTool {
    
    private static final String TAG = "CameraTool";
    public static final int REQUEST_CODE_CAMERA = 10001;
    public static final int REQUEST_CODE_ALBUM = 10002;
    public static final int REQUEST_CODE_CROP = 10003;
    private final String IMAGE_NAME = "tempImage.png";
    private String CROPPED_IMAGE_NAME = "tempImage_cropped.png";
    private final Activity mActivity;
    private int mOutputX = 400; // pixel
    private int mOutputY = 400; // pixel
    private boolean mEnableCrop = true; // 拍照或者选取照片完毕后是否裁剪。
    
    public CameraTool(Activity activity) {
        mActivity = activity;
    }
    
    
    public interface CameraToolCallback {
        /**
         * 未裁剪的情况下回调。
         */
        void afterCamera(File imgFile);
        
        /**
         * 未裁剪的情况下回调。
         */
        void afterAlbum(File imgFile);
        
        /**
         * 裁剪的情况下回调。
         */
        void afterCrop(File imgFile);
    }
    
    private CameraToolCallback mCallback;
    
    public void setCameraToolCallback(CameraToolCallback callback) {
        mCallback = callback;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CAMERA:
                    afterCamera();
                    break;
                case REQUEST_CODE_ALBUM:
                    afterAlbum(data);
                    break;
                case REQUEST_CODE_CROP:
                    afterCrop(data);
                    break;
            }
        }
    }
    
    /**
     * 设置输出的图片大小。
     */
    public void setOutputSize(int widthPixel, int heightPixel) {
        mOutputX = widthPixel;
        mOutputY = heightPixel;
    }
    
    /**
     * 是否裁剪。
     */
    public void enableCrop(boolean enable) {
        mEnableCrop = enable;
    }

    private File getImageFile(String imgName) {
        return new File(FileUtils.getDirByType(FileUtils.DIR_TYPE_CACHE_IMAGE),
                imgName);
    }

    private void startActivityForResult(Intent intent, int resquestCode) {
        if (mActivity != null) {
            mActivity.startActivityForResult(intent, resquestCode);
        }else {
            Log.e(TAG, "mActivity and mFragment are both null!");
        }
    }
    
    // cut picture
    private void cropPhoto(Uri uri) {// 裁剪
//		Intent intent = new Intent("com.android.camera.action.CROP");
//		intent.setDataAndType(uri, "image/*");
//		intent.putExtra("crop", "true");
//		intent.putExtra("aspectX", 1); //
//		intent.putExtra("aspectY", 1); //
//		intent.putExtra("outputX", mOutputX); //
//		intent.putExtra("outputY", mOutputY); //
//		intent.putExtra("return-data", false); //
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getImageFile(CROPPED_IMAGE_NAME)));
//		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//
//		startActivityForResult(intent, REQUEST_CODE_CROP);
    
        CROPPED_IMAGE_NAME = java.util.UUID.randomUUID().toString() + ".png";
        startImageZoom(uri, Uri.fromFile(getImageFile(CROPPED_IMAGE_NAME)));
    }
    
    /**
     * 启动图片剪裁
     *
     * @param uri             待裁剪图片文件的uri
     * @param croppedImageUri 剪裁完图片文件的uri
     */
    private void startImageZoom(Uri uri, Uri croppedImageUri) {
        
        //设置输出图片文件的Uri，裁剪框设为蓝色，并将裁切出的大小设置为200*200 像素大小的正方形
        CropImageIntentBuilder cropImage = new CropImageIntentBuilder(mOutputX, mOutputY, croppedImageUri);
//		cropImage.setOutlineColor(0xFF03A9F4);
        cropImage.setSourceImage(uri);
    
        startActivityForResult(cropImage.getIntent(mActivity), REQUEST_CODE_CROP);
    }
    
    
    public void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = getImageFile(IMAGE_NAME);
        Uri photoURI = FileProvider.getUriForFile(mActivity, BaseApp.getInstance().getPackageName() + ".provider", imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }
    
    public void startAlbum() {
//		Intent intent = new Intent(Intent.ACTION_PICK, null);
//		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//		startActivityForResult(intent, REQUEST_CODE_ALBUM);
        
        Intent intent = new Intent();
        // For Android versions of KitKat or later, we use a
        // different intent to ensure
        // we can get the file path from the returned intent URI
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }
    
    /**
     * 拍照返回后
     */
    private void afterCamera() {
        File imgFile = getImageFile(IMAGE_NAME);
        if (mEnableCrop) {
            cropPhoto(Uri.fromFile(imgFile)); // 切图
        } else if (mCallback != null) {
            mCallback.afterCamera(imgFile);
        }
    }
    
    /**
     * 相册选图返回后
     */
    private void afterAlbum(Intent data) {
        if (data != null) {
            if (mEnableCrop) {
                cropPhoto(data.getData()); // 切图
            } else if (mCallback != null) {
                mCallback.afterAlbum(uriToFile(data.getData()));
            }
        }
    }
    
    /**
     * 将Uri转换为File。
     */
    public File uriToFile(Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String absPath = null;
        if (scheme == null)
            absPath = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            absPath = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            assert getContext() != null;
            Cursor cursor = getContext().getContentResolver().query(uri,
                    new String[]{ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(ImageColumns.DATA);
                    if (index > -1) {
                        absPath = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        if (absPath != null && TextUtils.isEmpty(absPath)) {
            return new File(absPath);
        } else {
            return null;
        }
    }
    
    /**
     * 获取指定文件大小,必须是文件不能是文件夹
     */
    public static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                boolean created = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    private Context getContext() {
        return mActivity;
    }
    
    /**
     * 裁剪图片完成后
     */
    private void afterCrop(Intent data) {
        if (data != null && mCallback != null) {
            mCallback.afterCrop(getImageFile(CROPPED_IMAGE_NAME));
        }
    }
    
}
