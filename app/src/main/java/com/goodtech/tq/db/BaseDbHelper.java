package com.goodtech.tq.db;

import android.content.Context;

public abstract class BaseDbHelper {
    public static final String COL_LOCAL_ID = "_id"; // 本地表的id
    public static final String COL_SRV_ID = "id"; // 服务端记录的id，由本地创建UUID
    public static final String COL_USERID = "userId"; // 训练者的Id
    public static final String COL_CREATE_TIME = "createTime"; // 训练时间
    public static final String COL_UPDATE_TIME = "updateTime"; // 从服务器获取的时间
    public static final String COL_IS_UPLOADED = "isUploaded"; // 是否已经上传到服务器
    public SqliteDbHelper mdbHelper;

    public BaseDbHelper(Context context) {
        mdbHelper = SqliteDbHelper.getInstance(context);
    }

    /**
     * @param tableName     表名
     * @param columnClauses 插入字段的子句，格式：'[column] [valueType]'，示例：'COL_USERID INTEGER'
     * @return 创建表的sql语句。
     */
    protected static String makeCreateTableSql(String tableName, String... columnClauses) {
        StringBuilder builder = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(tableName).append(" ( ")
                .append(COL_LOCAL_ID).append(" INTEGER PRIMARY KEY");
//                .append(COL_SRV_ID).append(" VARCHAR(64), ")
//                .append(COL_USERID).append(" VARCHAR(64), ")
//                .append(COL_CREATE_TIME).append(" INTEGER, ")
//                .append(COL_UPDATE_TIME).append(" INTEGER, ")
//                .append(COL_IS_UPLOADED).append(" INTEGER");

        for (String columnClause : columnClauses) {
            builder.append(",").append(columnClause);
        }
        return builder
                .append(")")
                .toString();
    }

    protected abstract String getTableName();
}
