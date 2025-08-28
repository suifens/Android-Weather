package com.chunjing.tq.db.dao

import androidx.room.*
import com.chunjing.tq.db.entity.CacheEntity

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCache(cache: CacheEntity): Long

    @Query("select *from cache where `key`=:key")
    fun getCache(key: String): CacheEntity?

    @Delete
    fun deleteCache(cache: CacheEntity): Int

}