package com.goodtech.tq.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.goodtech.tq.eventbus.MessageEvent;
import com.goodtech.tq.models.db.SignRecord;

import org.greenrobot.eventbus.EventBus;

/**
 * com.goodtech.tq.db
 * 签到数据库操作
 */
public class SignDbHelper extends BaseDbHelper {

    protected static final String TABLE_NAME = "sign";
    public static final String COL_SIGN_DAY = "date_day";
    public static final String COL_SIGN_TYPE = "sign_type";
    public static final String COL_SIGN_DATE = "create_time";
    public static final String COL_SIGN_CONTINUE = "continue_sign";

    public SignDbHelper(Context context) { super(context); }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    public static void createTable(SQLiteDatabase db) {
        String[] columnClause = new String[]{
                COL_SIGN_DAY + " VARCHAR(64)",
                COL_SIGN_TYPE + " VARCHAR(64)",
                COL_SIGN_DATE + " INTEGER",
                COL_SIGN_CONTINUE + " INTEGER",
        };
        db.execSQL(makeCreateTableSql(TABLE_NAME, columnClause));
    }

    public long insert(SignRecord record) {
        if (!hadSigning(record)) {
            EventBus.getDefault().post(new MessageEvent().needReload(true));
            return mdbHelper.insert(TABLE_NAME, makeContentValues(record));
        }
        return -1;
    }

    public int update(SignRecord record) {
        ContentValues values = makeContentValues(record);
        String whereClause = COL_SIGN_DAY + "=? AND " + COL_SIGN_TYPE + "=?" ;
        String[] whereArgs = new String[]{record.getDateDay(), record.getSignType()};
        return mdbHelper.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * 是否有
     */
    public boolean hadSigning(SignRecord record) {
        StringBuffer sqlSb = new StringBuffer();
        sqlSb.append("select count(" + COL_SIGN_TYPE + ") ");
        sqlSb.append("from ");
        sqlSb.append(getTableName() + " ");
        sqlSb.append("where " + COL_SIGN_DAY + " = '" + record.getDateDay() + "' ");
        sqlSb.append("and " + COL_SIGN_TYPE + " = '" + record.getSignType() + "' ");
        Cursor cursor = mdbHelper.rawQuery(sqlSb.toString(), null);
        int count = 0;
        if (cursor == null) {
            return false;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    count = cursor.getInt(0);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return count > 0;
    }

    /**
     * 是否有
     */
    public boolean hadSigning(String dateDay) {
        StringBuffer sqlSb = new StringBuffer();
        sqlSb.append("select count(" + COL_SIGN_TYPE + ") ");
        sqlSb.append("from ");
        sqlSb.append(getTableName() + " ");
        sqlSb.append("where " + COL_SIGN_DAY + " = '" + dateDay + "' ");
        Cursor cursor = mdbHelper.rawQuery(sqlSb.toString(), null);
        int count = 0;
        if (cursor == null) {
            return false;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    count = cursor.getInt(0);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return count > 0;
    }

    /**
     * 获取连续
     */
    public int queryContinuousCount(String signType) {

        StringBuffer sqlSb = new StringBuffer();
        sqlSb.append("select count(" + COL_SIGN_TYPE + ") from ");
        sqlSb.append(getTableName() + " ");
        sqlSb.append("where " + COL_SIGN_TYPE + " = '" + signType + "' ");

        Cursor cursor = mdbHelper.rawQuery(sqlSb.toString(), null);
        int count = 0;
        if (cursor == null) {
            return count;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    count = cursor.getInt(0);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return count;
    }

    public int queryContinuousCount() {

        StringBuffer sqlSb = new StringBuffer();
        sqlSb.append("select count(DISTINCT " + COL_SIGN_DAY + ") from ");
        sqlSb.append(getTableName() + " ");

        Cursor cursor = mdbHelper.rawQuery(sqlSb.toString(), null);
        int count = 0;
        if (cursor == null) {
            return count;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    count = cursor.getInt(0);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return count;
    }

    protected ContentValues makeContentValues(SignRecord record) {
        ContentValues values = new ContentValues();
        values.put(COL_SIGN_DAY, record.getDateDay());
        values.put(COL_SIGN_DATE, record.getCreateTime());
        values.put(COL_SIGN_TYPE, record.getSignType());
        values.put(COL_SIGN_CONTINUE, record.getContinueSign());
        return values;
    }

}
