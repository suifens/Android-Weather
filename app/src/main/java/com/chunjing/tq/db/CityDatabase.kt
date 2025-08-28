package com.chunjing.tq.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chunjing.tq.db.dao.CityDao
import com.chunjing.tq.db.entity.CityEntity

@Database(
    entities = [CityEntity::class],
    version = 1,
    exportSchema = false
)
internal abstract class CityDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao

    companion object {
        private const val DATABASE_NAME = "sw-city.db"

        @Volatile
        private var instance: CityDatabase? = null

        fun getInstance(context: Context): CityDatabase {
            return instance ?: synchronized(this) {
                instance
                    ?: buildDatabase(
                        context
                    )
                        .also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): CityDatabase {
            return Room.databaseBuilder(context, CityDatabase::class.java, DATABASE_NAME)
                .createFromAsset("city.db")
                .build()
        }
    }

    object MIGRATION0_1 : Migration(0, 1) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //1.创建一个新的符合Entity字段的新表user_new
            database.execSQL(
                "CREATE TABLE AdProfile_New (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                        + "name TEXT,"       //关注点1
                        + "url TEXT,"       //关注点1
                        + "md5 TEXT,"       //关注点1
                        + "size TEXT,"       //关注点1
                        + "download INTEGER NOT NULL,"     //关注点2
                        + "enable INTEGER NOT NULL)"     //关注点2
            )
            //2.将旧表user中的数据拷贝到新表user_new中
            database.execSQL(("INSERT INTO AdProfile_New(id,name,url,md5,size,download,enable) " + "SELECT id,name,url,md5,size,download,enable FROM AdProfile"))
            //3.删除旧表user
            database.execSQL("DROP TABLE AdProfile")
            //4.将新表user_new重命名为user,升级完毕
            database.execSQL("ALTER TABLE AdProfile_New RENAME TO AdProfile")
//            database.close()
        }
    }
}