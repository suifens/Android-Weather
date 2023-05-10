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
public class LinkDbHelper extends BaseDbHelper {

    protected static final String TABLE_NAME = "link";
    public static final String COL_LINK_TEMP = "tempType";
    public static final String COL_LINK_USING = "usingType";
    public static final String COL_LINK_IMG = "imgPath";
    public static final String COL_LINK_H5 = "H5link";

    public LinkDbHelper(Context context) { super(context); }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    public static void createTable(SQLiteDatabase db) {
        String[] columnClause = new String[]{
                COL_LINK_TEMP + " VARCHAR(64)",
                COL_LINK_USING + " VARCHAR(64)",
                COL_LINK_IMG + " VARCHAR(64)",
                COL_LINK_H5 + " VARCHAR(64)",
        };
        db.execSQL(makeCreateTableSql(TABLE_NAME, columnClause));
    }

    public long insert(SignRecord record) {
        if (!hadSigning(record)) {
            long insertRt = mdbHelper.insert(TABLE_NAME, makeContentValues(record));
            if (insertRt != -1) {
                EventBus.getDefault().post(new MessageEvent().needReload(true));
            }
            return insertRt;
        }
        return -1;
    }

    public int update(SignRecord record) {
        ContentValues values = makeContentValues(record);
        String whereClause = COL_LINK_TEMP + "=? AND " + COL_LINK_USING + "=?" ;
        String[] whereArgs = new String[]{record.getDateDay(), record.getSignType()};
        return mdbHelper.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    /**
     * 是否有
     */
    public boolean hadSigning(SignRecord record) {
        StringBuffer sqlSb = new StringBuffer();
        sqlSb.append("select count(" + COL_LINK_USING + ") ");
        sqlSb.append("from ");
        sqlSb.append(getTableName() + " ");
        sqlSb.append("where " + COL_LINK_TEMP + " = '" + record.getDateDay() + "' ");
        sqlSb.append("and " + COL_LINK_USING + " = '" + record.getSignType() + "' ");
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
        sqlSb.append("select count(" + COL_LINK_TEMP + ") ");
        sqlSb.append("from ");
        sqlSb.append(getTableName() + " ");
        sqlSb.append("where " + COL_LINK_TEMP + " = '" + dateDay + "' ");
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
        sqlSb.append("select count(" + COL_LINK_USING + ") from ");
        sqlSb.append(getTableName() + " ");
        sqlSb.append("where " + COL_LINK_USING + " = '" + signType + "' ");

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
        sqlSb.append("select count(DISTINCT " + COL_LINK_TEMP + ") from ");
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
        values.put(COL_LINK_TEMP, record.getDateDay());
        values.put(COL_LINK_IMG, record.getCreateTime());
        values.put(COL_LINK_USING, record.getSignType());
        values.put(COL_LINK_H5, record.getContinueSign());
        return values;
    }

}
