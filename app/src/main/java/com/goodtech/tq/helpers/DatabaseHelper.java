package com.goodtech.tq.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.goodtech.tq.models.CityMode;

/**
 * com.goodtech.tq.helpers
 */
//将assets中的数据库导入到项目中databases
public class DatabaseHelper {
    private final int BUFFER_SIZE = 400000;
    public static final String DB_NAME = "city.db"; //保存的数据库文件名
    public static final String PACKAGE_NAME = "com.goodtech.tq";//包名
    public static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME + "/databases";  //存放数据库的位置
    private static DatabaseHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Context context;

    public static DatabaseHelper getInstance(Context context) {
        if (mDbHelper == null) {
            synchronized (DatabaseHelper.class) {
                if (mDbHelper == null) {
                    mDbHelper = new DatabaseHelper(context);
                }
            }
        }
        return mDbHelper;
    }

    DatabaseHelper(Context context) {
        this.context = context;
    }

    public void openDatabase() {
        File dFile = new File(DB_PATH);//判断路径是否存在，不存在则创建路径
        if (!dFile.exists()) {
            dFile.mkdir();
        }
        this.mDatabase = this.openDatabase(DB_PATH + "/" + DB_NAME);
    }

    private SQLiteDatabase openDatabase(String dbfile) {
        try {
            if (!(new File(dbfile).exists())) {
                InputStream is = this.context.getResources().getAssets().open("city.db"); //欲导入的数据库
                FileOutputStream fos = new FileOutputStream(dbfile);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile,
                    null);
            return db;
        } catch (FileNotFoundException e) {
            Log.e("Database", "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Database", "IO exception");
            e.printStackTrace();
        }
        return null;
    }

    public void closeDatabase() {
        this.mDatabase.close();
    }

    public ArrayList<CityMode> queryCity(String name) {

        if (TextUtils.isEmpty(name)) {
            return null;
        }

        name = name.replace("'", "");
        name = name.replace(" ", "");
        name = name.replace("%", "");

//        String sql = String.format("select * from city where mergerName like '%%%s%%'", name);

        // 构建查询语句
        StringBuilder query = new StringBuilder("SELECT *, (LENGTH(mergerName) - LENGTH(REPLACE(mergerName, '" + name.charAt(0) + "', '')))");
        for (int i = 1; i < name.length(); i++) {
            query.append("+ (LENGTH(mergerName) - LENGTH(REPLACE(mergerName, '").append(name.charAt(i)).append("', ''))) ");
        }
        query.append("AS match_count FROM city WHERE ");
        for (int i = 0; i < name.length(); i++) {
            query.append("mergerName LIKE '%").append(name.charAt(i)).append("%' OR ");
        }
        query = new StringBuilder(query.substring(0, query.length() - 4));  // 去除最后一个OR
        query.append(" ORDER BY match_count DESC");

        Cursor cursor = mDatabase.rawQuery(String.valueOf(query), null);

        ArrayList<CityMode> list = new ArrayList<>();
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    CityMode cityMode = new CityMode();
                    cityMode.resolveCour(cursor);
                    list.add(cityMode);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public ArrayList<CityMode> searchCitiesWithCode(String cityCode) {

        // 构建查询语句
//        StringBuilder query = new StringBuilder("SELECT *, (LENGTH(mergerName) - LENGTH(REPLACE(mergerName, '" + name.charAt(0) + "', '')))");
//        for (int i = 1; i < name.length(); i++) {
//            query.append("+ (LENGTH(mergerName) - LENGTH(REPLACE(mergerName, '").append(name.charAt(i)).append("', ''))) ");
//        }
//        query.append("AS match_count FROM city WHERE ");
//        for (int i = 0; i < name.length(); i++) {
//            query.append("mergerName LIKE '%").append(name.charAt(i)).append("%' OR ");
//        }
//        query = new StringBuilder(query.substring(0, query.length() - 4));  // 去除最后一个OR
//        query.append(" ORDER BY match_count DESC");

        String queryStr = String.format("select * from city where cityCode = %s order by cityId asc limit 0,10", cityCode);

        Cursor cursor = mDatabase.rawQuery(queryStr, null);

        ArrayList<CityMode> list = new ArrayList<>();
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    CityMode cityMode = new CityMode();
                    cityMode.resolveCour(cursor);
                    list.add(cityMode);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return list;
    }

}

