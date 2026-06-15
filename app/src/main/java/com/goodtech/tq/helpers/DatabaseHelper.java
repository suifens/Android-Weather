package com.goodtech.tq.helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;

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

    private void ensureOpen() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            openDatabase();
        }
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
        ensureOpen();
        if (mDatabase == null) {
            return null;
        }

        String keyword = name.trim().replace("'", "").replace("%", "");
        if (TextUtils.isEmpty(keyword)) {
            return null;
        }

        String like = "%" + keyword + "%";
        String sql = "SELECT * FROM city WHERE "
                + "(mergerName LIKE ? OR cityName LIKE ? OR pinyin LIKE ? OR shortName LIKE ?) "
                + "ORDER BY depth ASC, LENGTH(mergerName) ASC LIMIT 30";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{like, like, like, like});

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

    /**
     * 定位反查 cityCode：以 city.db 为准（010/021/0755...），对齐纯净天气 resolveLocationCityCode。
     */
    public CityMode resolveLocationCity(String cityName, String district, String poiName) {
        ensureOpen();
        if (mDatabase == null) {
            return null;
        }

        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        addKeywordCandidate(candidates, cityName);
        addKeywordCandidate(candidates, district);
        addKeywordCandidate(candidates, poiName);
        if (!TextUtils.isEmpty(district) && !TextUtils.isEmpty(poiName)) {
            addKeywordCandidate(candidates, district + " " + poiName);
        }

        for (String keyword : candidates) {
            ArrayList<CityMode> list = queryCity(keyword);
            if (list == null || list.isEmpty()) {
                continue;
            }
            for (CityMode cityMode : list) {
                if (!TextUtils.isEmpty(cityMode.getCityCode())) {
                    return cityMode;
                }
            }
        }
        return null;
    }

    private static void addKeywordCandidate(LinkedHashSet<String> candidates, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        String trimmed = value.trim();
        if (!trimmed.isEmpty()) {
            candidates.add(trimmed);
        }
        String stripped = stripAdministrativeSuffix(trimmed);
        if (!TextUtils.isEmpty(stripped)) {
            candidates.add(stripped);
        }
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex > 0) {
            candidates.add(trimmed.substring(0, spaceIndex).trim());
        }
    }

    private static String stripAdministrativeSuffix(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return value
                .replace("特别行政区", "")
                .replace("自治州", "")
                .replace("地区", "")
                .replace("省", "")
                .replace("市", "")
                .replace("县", "")
                .replace("区", "")
                .replace("盟", "")
                .trim();
    }

    public ArrayList<CityMode> searchCitiesWithCode(String cityCode) {
        ensureOpen();
        if (mDatabase == null || TextUtils.isEmpty(cityCode)) {
            return null;
        }
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

        String queryStr = "SELECT * FROM city WHERE cityCode = ? ORDER BY id ASC LIMIT 10";
        Cursor cursor = mDatabase.rawQuery(queryStr, new String[]{cityCode});

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

