package com.chunjing.tq.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chunjing.tq.db.dao.CacheDao
import com.chunjing.tq.db.dao.CalendarBgDao
import com.chunjing.tq.db.dao.CityDao
import com.chunjing.tq.db.dao.WeatherBgDao
import com.chunjing.tq.db.entity.CacheEntity
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.db.entity.WeatherBgEntity
import com.chunjing.tq.db.entity.CalendarBgEntity

/**
 */
@Database(
    entities = [WeatherBgEntity::class, CacheEntity::class, CityEntity::class, CalendarBgEntity::class],
    version = 2,
    exportSchema = false
)
internal abstract class AppDatabase : RoomDatabase() {

    abstract fun weatherBgDao(): WeatherBgDao

    abstract fun cacheDao(): CacheDao

    abstract fun cityDao(): CityDao

    abstract fun calendarBgDao(): CalendarBgDao

    companion object {
        private const val DATABASE_NAME = "sw-weather.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance
                    ?: buildDatabase(
                        context
                    )
                        .also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context, AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION1_2)
                .allowMainThreadQueries()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.e("", "db：onCreate")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
//                        //LogUtils.LOGE("db：onOpen")
                    }
                })
                .build()
        }
    }

    object MIGRATION1_2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            //1.创建一个新的符合Entity字段的新表user_new
            database.execSQL(
                "CREATE TABLE WeatherBgImg (imgPath TEXT," +
                        " videoPath TEXT," +
                        " startColor TEXT," +
                        " endColor TEXT," +
                        " tempType TEXT NOT NULL," +
                        " timeType TEXT NOT NULL," +
                        " PRIMARY KEY(tempType, timeType))"
            )
            //2.将旧表user中的数据拷贝到新表user_new中
            database.execSQL(("INSERT INTO WeatherBgImg(imgPath,videoPath,startColor,endColor,tempType,timeType) " + "SELECT imgPath,videoPath,startColor,endColor,tempType,timeType FROM WeatherImg"))
            //3.删除旧表user
            database.execSQL("DROP TABLE WeatherImg")
            //4.将新表user_new重命名为user,升级完毕
            database.execSQL("ALTER TABLE WeatherBgImg RENAME TO WeatherImg")
//            database.close()
        }
    }
}