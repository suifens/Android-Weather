package com.goodtech.tq.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.goodtech.tq.news.NewsBean.ResultBean.DataBean;

import java.util.ArrayList;
import java.util.List;

/**
 * com.goodtech.tq.db
 */
public class NewsDbHelper extends BaseDbHelper {

    protected static final String TABLE_NAME = "news_info";
    public static final String COL_NEWS_KEY = "uniquekey";
    public static final String COL_NEWS_TITLE = "title";
    public static final String COL_NEWS_DATE = "date";
    public static final String COL_NEWS_CATEGORY = "category";
    public static final String COL_NEWS_AUTHOR = "author_name";
    public static final String COL_NEWS_URL = "url";
    public static final String COL_NEWS_PIC = "thumbnail_pic_s";
    public static final String COL_NEWS_PIC_2 = "thumbnail_pic_s02";
    public static final String COL_NEWS_PIC_3 = "thumbnail_pic_s03";


    public NewsDbHelper(Context context) { super(context); }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    public static void createTable(SQLiteDatabase db) {
        String[] columnClause = new String[]{
                COL_NEWS_KEY + " VARCHAR(64)",
                COL_NEWS_TITLE + " text",
                COL_NEWS_DATE + " VARCHAR(64)",
                COL_NEWS_CATEGORY + " VARCHAR(64)",
                COL_NEWS_AUTHOR + " VARCHAR(64)",
                COL_NEWS_URL + " VARCHAR(64)",
                COL_NEWS_PIC + " VARCHAR(64)",
                COL_NEWS_PIC_2 + " VARCHAR(64)",
                COL_NEWS_PIC_3 + " VARCHAR(64)",
        };
        db.execSQL(makeCreateTableSql(TABLE_NAME, columnClause));
    }

    public void insertDataBeans(List<DataBean> dataBeans) {
        if (dataBeans != null && dataBeans.size() > 0) {
            mdbHelper.beginTransaction();
            for (DataBean dataBean : dataBeans) {
                insert(dataBean);
            }
            mdbHelper.setTransactionSuccessful();
            mdbHelper.endTransaction();
        }
    }

    public void insert(DataBean news) {
        if (update(news) == 0) {
            mdbHelper.insert(TABLE_NAME, makeContentValues(news));
        }
    }

    public int update(DataBean DataBean) {
        ContentValues values = makeContentValues(DataBean);
        String whereClause = COL_NEWS_KEY + "=?";
        String[] whereArgs = new String[]{DataBean.getUniquekey()};
        return mdbHelper.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public List<DataBean> queryNewsList(String category, int page, int row) {
        String sqlSb = "select * from " + getTableName() +
                " where " + COL_NEWS_CATEGORY + " = '" + category +
                "' order by " + COL_NEWS_DATE + " desc " +
                "limit " + page + "," + row;
        Cursor cursor = mdbHelper.rawQuery(sqlSb, null);
        List<DataBean> results = new ArrayList<>();
        if (cursor == null) {
            return results;
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    DataBean model = new DataBean();
                    model.resolveCour(cursor);
                    results.add(model);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return results;
    }

    protected ContentValues makeContentValues(DataBean model) {
        ContentValues values = new ContentValues();
        values.put(COL_NEWS_KEY, model.getUniquekey());
        values.put(COL_NEWS_TITLE, model.getTitle());
        values.put(COL_NEWS_DATE, model.getDate());
        values.put(COL_NEWS_CATEGORY, model.getCategory());
        values.put(COL_NEWS_AUTHOR, model.getAuthor_name());
        values.put(COL_NEWS_URL, model.getUrl());
        values.put(COL_NEWS_PIC, model.getThumbnail_pic_s());
        values.put(COL_NEWS_PIC_2, model.getThumbnail_pic_s02());
        values.put(COL_NEWS_PIC_3, model.getThumbnail_pic_s03());
        return values;
    }

}
