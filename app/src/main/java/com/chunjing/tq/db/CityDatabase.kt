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
    version = 2,
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
                .createFromAsset(
                    "city.db",
                    object : RoomDatabase.PrepackagedDatabaseCallback() {
                        override fun onOpenPrepackagedDatabase(db: SupportSQLiteDatabase) {
                            // 资产库无 sortOrder，复制后立刻补齐，避免 Room 校验失败
                            ensureSortOrderColumn(db)
                        }
                    }
                )
                .addMigrations(MIGRATION1_2)
                .build()
        }

        private fun ensureSortOrderColumn(db: SupportSQLiteDatabase) {
            val cursor = db.query("PRAGMA table_info(city)")
            var hasSortOrder = false
            cursor.use {
                val nameIndex = it.getColumnIndex("name")
                while (it.moveToNext()) {
                    if (nameIndex >= 0 && it.getString(nameIndex) == "sortOrder") {
                        hasSortOrder = true
                        break
                    }
                }
            }
            if (!hasSortOrder) {
                db.execSQL("ALTER TABLE city ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    object MIGRATION1_2 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val cursor = database.query("PRAGMA table_info(city)")
            var hasSortOrder = false
            cursor.use {
                val nameIndex = it.getColumnIndex("name")
                while (it.moveToNext()) {
                    if (nameIndex >= 0 && it.getString(nameIndex) == "sortOrder") {
                        hasSortOrder = true
                        break
                    }
                }
            }
            if (!hasSortOrder) {
                database.execSQL("ALTER TABLE city ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
