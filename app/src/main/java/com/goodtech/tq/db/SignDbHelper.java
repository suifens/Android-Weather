package com.goodtech.tq.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.goodtech.tq.models.db.SignRecord;

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

    public void insert(SignRecord record) {
        if (update(record) == 0) {
            mdbHelper.insert(TABLE_NAME, makeContentValues(record));
        }
    }

    public int update(SignRecord record) {
        ContentValues values = makeContentValues(record);
        String whereClause = COL_SIGN_DAY + "=? AND " + COL_SIGN_TYPE + "=?" ;
        String[] whereArgs = new String[]{record.getDateDay(), record.getSignType()};
        return mdbHelper.update(TABLE_NAME, values, whereClause, whereArgs);
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
