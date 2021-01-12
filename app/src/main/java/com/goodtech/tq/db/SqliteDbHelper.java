package com.goodtech.tq.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "SqliteDbHelper";
    
    public static final String DB_NAME = "weather.db";
    
    // 请不要修改FIRST_DATABASE_VERSION的值，其为第一个数据库版本大小
    final int FIRST_DATABASE_VERSION = 1;
    
    //升级数据库版本请按顺序加1
    private static final int DATABASE_VERSION = 2;//first 4;
    
    private static SqliteDbHelper mDbHelper;
    private static SQLiteDatabase mDatabase;
    
    private SqliteDbHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        mDatabase = getWritableDatabase();
    }
    
    public static SqliteDbHelper getInstance(Context context) {
        if (mDbHelper == null) {
            synchronized (SqliteDbHelper.class) {
                if (mDbHelper == null) {
                    mDbHelper = new SqliteDbHelper(context);
                }
            }
        }
        return mDbHelper;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, FIRST_DATABASE_VERSION, DATABASE_VERSION);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int version = oldVersion; version < newVersion; version++) {
            switch (version) {
                case 1:
                    NewsDbHelper.createTable(db);
                    break;
                default:
                    break;
            }
        }
    }
    
    public long insert(String table, ContentValues values) {
        return mDatabase.insert(table, null, values);
    }
    
    public long replace(String table, ContentValues values) {
        return mDatabase.replace(table, null, values);
    }
    
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return mDatabase.update(table, values, whereClause, whereArgs);
    }
    
    public long delete(String table, String whereClause, String[] whereArgs) {
        return mDatabase.delete(table, whereClause, whereArgs);
    }
    
    public void clearTable(String tableName) {
        mDatabase.execSQL("DELETE FROM " + tableName);
    }
    
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs,
                        String groupBy, String having, String orderBy) {
        return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }
    
    /**
     * 分页查询
     */
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs,
                        String groupBy, String having, String orderBy, String limit) {
        return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy,
                limit);
    }
    
    // 批量处理数据库的操作
    public void beginTransaction() {
        mDatabase.beginTransaction();
    }
    
    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }
    
    public void endTransaction() {
        mDatabase.endTransaction();
    }
    
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDatabase.rawQuery(sql, selectionArgs);
    }
    
}
