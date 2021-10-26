package com.goodtech.tq.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.goodtech.tq.app.BaseApp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSource;
import okio.Okio;
import okio.Sink;


/**
 * @author wangrengshun <wangrengshun@gengee.cn>
 */

public class FileUtils {
    private final static String TAG = FileUtils.class.getSimpleName();
    //内存卡路径
    public static String STORAGE_BASE_DIR;
    public static String STORAGE_CACHE_DIR;
    //外存卡路径
    public static String STORAGE_BASE_SD_DIR;
    public static String STORAGE_BASE_SD_LOG_DIR;
    public static final int DIR_TYPE_DOWNLOAD = 1;
    public static final int DIR_TYPE_IMAGE = 2;
    public static final int DIR_TYPE_VIDEO = 3;
    public static final int DIR_TYPE_ACTIVITY_VIDEO = 4;
    public static final int DIR_TYPE_CACHE_IMAGE = 5;
    private static final String STORAGE_EXTERNAL_PATH = "insaitjoy";
    private static final String STORAGE_EXTERNAL_LOG_PATH = "instaitjoy_log";
    
    // 下载目录
    private final static String STORAGE_DOWNLOAD_DIR = "/downloads/";
    private final static String STORAGE_IMAGE = "/image/";
    private final static String STORAGE_VIDEO = "/video/";
    private final static String STORAGE_ACTIVITY_VIDEO = "/activity_video/";
    
    static {
        STORAGE_BASE_SD_DIR = new File(Environment.getExternalStorageDirectory(), STORAGE_EXTERNAL_PATH).getAbsolutePath();
        STORAGE_BASE_SD_LOG_DIR = new File(Environment.getExternalStorageDirectory(), STORAGE_EXTERNAL_LOG_PATH).getAbsolutePath();
        STORAGE_BASE_DIR = BaseApp.getInstance().getFilesDir().getAbsolutePath() + "/" + STORAGE_EXTERNAL_PATH;
        Log.i(TAG, "STORAGE_BASE_DIR=>" + STORAGE_BASE_DIR);
        Log.i(TAG, "STORAGE_BASE_SD_DIR=>" + STORAGE_BASE_SD_DIR);
    }
    
    /**
     * 根据类型获取路径目录
     *
     * @param type
     * @return
     */
    public static String getDirByType(int type) {
        switch (type) {
            case DIR_TYPE_IMAGE:
                return getFilesDir(STORAGE_IMAGE);
            case DIR_TYPE_DOWNLOAD:
                return getFilesDir(STORAGE_DOWNLOAD_DIR);
            case DIR_TYPE_VIDEO:
                return getFilesDir(STORAGE_VIDEO);
            case DIR_TYPE_ACTIVITY_VIDEO:
                return getFilesDir(STORAGE_ACTIVITY_VIDEO);
            case DIR_TYPE_CACHE_IMAGE:
                return getCacheDir(STORAGE_IMAGE);
            default:
                return getFilesDir(STORAGE_EXTERNAL_PATH);
        }
    }

    protected static String getFilesDir(String filePath) {
        String dir = null;
        if (TextUtils.isEmpty(STORAGE_BASE_DIR)) {
            STORAGE_BASE_DIR = BaseApp.getInstance().getFilesDir().getAbsolutePath() + "/" + STORAGE_EXTERNAL_PATH;
        }
        filePath = STORAGE_BASE_DIR + filePath;

        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                dir = file.getAbsolutePath();
            }
        } else {
            dir = BaseApp.getInstance().getCacheDir().getPath();
        }
        Log.e(TAG, "getDirByType: path = " + dir);
        return dir + "/";
    }

    protected static String getCacheDir(String filePath) {
        String dir = null;
        if (TextUtils.isEmpty(STORAGE_CACHE_DIR)) {
            STORAGE_CACHE_DIR = BaseApp.getInstance().getCacheDir().getAbsolutePath() + "/" + STORAGE_EXTERNAL_PATH;
        }
        filePath = STORAGE_CACHE_DIR + filePath;

        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                dir = file.getAbsolutePath();
            }
        } else {
            dir = BaseApp.getInstance().getCacheDir().getPath();
        }
        Log.e(TAG, "getCacheDir: path = " + dir);
        return dir + "/";
    }
    
    /**
     * 根据创建目录
     * @return
     */
    public static String createFileLogDir(String dirName) {
        String dir = null;
        String filePath = "/"+dirName+"/";
        
        if (isExistSDCard()) {
            if (TextUtils.isEmpty(STORAGE_BASE_SD_LOG_DIR)) {
                STORAGE_BASE_SD_LOG_DIR = new File(Environment.getExternalStorageDirectory(), STORAGE_EXTERNAL_LOG_PATH).getAbsolutePath();
            }
            filePath = STORAGE_BASE_SD_LOG_DIR + filePath;
        } else {
            if (TextUtils.isEmpty(STORAGE_BASE_SD_LOG_DIR)) {
                STORAGE_BASE_SD_LOG_DIR = BaseApp.getInstance().getFilesDir().getAbsolutePath() + "/" + STORAGE_EXTERNAL_LOG_PATH;
            }
            filePath = STORAGE_BASE_SD_LOG_DIR + filePath;
        }
        
        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                dir = file.getAbsolutePath();
            }
        } else {
            dir = BaseApp.getInstance().getCacheDir().getPath();
        }
        return dir + "/";
    }
    
    public static String getDownPath(String fileName) {
        return getDirByType(DIR_TYPE_DOWNLOAD) + fileName;
    }
    
    public static String getImagePath(String fileName) {
        return getDirByType(DIR_TYPE_IMAGE) + fileName;
    }
    
    public static String getVideoPath(String fileName) {
        return getDirByType(DIR_TYPE_VIDEO) + fileName;
    }
    public static String getActivityVideoPath(String fileName) {
        return getDirByType(DIR_TYPE_ACTIVITY_VIDEO) + fileName;
    }

    public static boolean copyTempVideoPath(File src, File dest) {
        if (!src.getAbsolutePath().equals(dest.getAbsolutePath())) {
            try {
                InputStream in = new FileInputStream(src);
                FileOutputStream out = new FileOutputStream(dest);
                byte[] buf = new byte[1024];

                int len;
                while ((len = in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    
    /**
     * 第三方固件上级获取路径需要加“file://”
     * @param fileName
     * @return
     */
    public static String getSensorUpgradePath(String fileName) {
        return "file://" + fileName;
    }
    
    /**
     * 是否存在外置内存卡
     *
     * @return
     */
    public static boolean isExistSDCard() {
//        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
//            Log.i(TAG, "isExistSDCard = true");
//            return true;
//        } else {
//            Log.i(TAG, "isExistSDCard = false");
//            return false;
//        }
        return false;
    }
    
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }
    
    /**
     * 判断文件是否存在
     * @return
     */
    public static boolean isFileExist(String path){
        File file = new File(path);
        return file.exists();
    }
    /**
     * 在SD卡上创建目录
     * @param dirPath 目录
     * @return 文件目录
     */
    public File createDir(String dirPath){
        File dir = new File(dirPath);
        dir.mkdir();
        return dir;
    }
    
    
    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName
     *            要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }
    
    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }
    
    /**
     * 删除目录及目录下的文件
     *
     * @param dir
     *            要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

    public static List<String> getAllFilePath(String path) {
        File file = new File(path);
        File[] files=file.listFiles();
        if (files == null){
            Log.e("error","空目录");
            return null;
        }
        List<String> s = new ArrayList<>();
        for (File value : files) {
            if (value.getPath().contains("__MACOSX")) {
                continue;
            }
            s.add(value.getAbsolutePath());
        }
        return s;
    }
    
    /**
     * @param fileName 文件名称及后缀(例如：xx.xml)
     * @return
     */
    public static String getFromAssetsByFileName(String fileName) {
        InputStreamReader inputReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            inputReader = new InputStreamReader(BaseApp.getInstance().getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            while ((line = bufReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputReader != null) {
                try {
                    inputReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuffer.toString();
    }
    
    /**
     * 根据byte数组生成文件
     *
     * @param bytes 生成文件用到的byte数组
     */
    public static void createFileWithByte(byte[] bytes, String fileName) {
        // TODO Auto-generated method stub
        
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(fileName);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
            createFileWithByte(e.getMessage().getBytes(), fileName);
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void writeFile(Context context,
                                 String imagePath,
                                 ContentValues values,
                                 ContentResolver contentResolver,
                                 Uri item,
                                 boolean deleteTemp) {
        try (OutputStream rw = contentResolver.openOutputStream(item, "rw")) {
            // Write data into the pending image.
            Sink sink = Okio.sink(rw);
            BufferedSource buffer = Okio.buffer(Okio.source(new File(imagePath)));
            buffer.readAll(sink);
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            contentResolver.update(item, values, null, null);
            if (deleteTemp) {
                new File(imagePath).delete();
            }

            Cursor query = context.getContentResolver().query(item, null, null, null);
            if (query != null) {
                int count = query.getCount();
                Log.e("writeFile","writeFile result :" + count);
                query.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
