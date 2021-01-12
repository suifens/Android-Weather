package com.goodtech.tq.news;

import android.database.Cursor;

import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_KEY;
import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_TITLE;
import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_DATE;
import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_CATEGORY;
import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_AUTHOR;
import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_URL;
import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_PIC;
import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_PIC_2;
import static com.goodtech.tq.db.NewsDbHelper.COL_NEWS_PIC_3;

/**
 * com.goodtech.tq.news
 */
public class NewsDataBean {

    /**
     * uniquekey : e772b9e5d24afc8b53d8002662f12947
     * title : 沪交警已建43套抓拍“违法鸣号”电子警察，具体点位公布！
     * date : 2019-04-16 10:01
     * category : 头条
     * author_name : 上海发布
     * url : http://mini.eastday.com/mobile/190416100100694.html
     * thumbnail_pic_s : http://00imgmini.eastday.com/mobile/20190416/20190416100100_f6b5486e56644f7951e7e0c59bf06452_1_mwpm_03200403.jpg
     * thumbnail_pic_s02 : http://05imgmini.eastday.com/mobile/20190416/20190416095657_66b846bc6199324897b9fe9497567c33_2_mwpm_03200403.jpg
     * thumbnail_pic_s03 : http://05imgmini.eastday.com/mobile/20190416/2019041609_638024639195477f9bba3832e553c330_1608_mwpm_03200403.jpg
     */

    private String uniquekey;
    private String title;
    private String date;
    private String category;
    private String author_name;
    private String url;
    private String thumbnail_pic_s;
    private String thumbnail_pic_s02;
    private String thumbnail_pic_s03;

    public String getUniquekey() {
        return uniquekey;
    }

    public void setUniquekey(String uniquekey) {
        this.uniquekey = uniquekey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnail_pic_s() {
        return thumbnail_pic_s;
    }

    public void setThumbnail_pic_s(String thumbnail_pic_s) {
        this.thumbnail_pic_s = thumbnail_pic_s;
    }

    public String getThumbnail_pic_s02() {
        return thumbnail_pic_s02;
    }

    public void setThumbnail_pic_s02(String thumbnail_pic_s02) {
        this.thumbnail_pic_s02 = thumbnail_pic_s02;
    }

    public String getThumbnail_pic_s03() {
        return thumbnail_pic_s03;
    }

    public void setThumbnail_pic_s03(String thumbnail_pic_s03) {
        this.thumbnail_pic_s03 = thumbnail_pic_s03;
    }

    public void resolveCour(Cursor cursor) {
        this.uniquekey = cursor.getString(cursor.getColumnIndex(COL_NEWS_KEY));
        this.title = cursor.getString(cursor.getColumnIndex(COL_NEWS_TITLE));
        this.date = cursor.getString(cursor.getColumnIndex(COL_NEWS_DATE));
        this.category = cursor.getString(cursor.getColumnIndex(COL_NEWS_CATEGORY));
        this.author_name = cursor.getString(cursor.getColumnIndex(COL_NEWS_AUTHOR));
        this.url = cursor.getString(cursor.getColumnIndex(COL_NEWS_URL));
        this.thumbnail_pic_s = cursor.getString(cursor.getColumnIndex(COL_NEWS_PIC));
        this.thumbnail_pic_s02 = cursor.getString(cursor.getColumnIndex(COL_NEWS_PIC_2));
        this.thumbnail_pic_s03 = cursor.getString(cursor.getColumnIndex(COL_NEWS_PIC_3));
    }

}
