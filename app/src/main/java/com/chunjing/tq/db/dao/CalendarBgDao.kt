package com.chunjing.tq.db.dao

import androidx.room.*
import com.chunjing.tq.db.entity.CacheEntity
import com.chunjing.tq.db.entity.CalendarBgEntity

@Dao
interface CalendarBgDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCalendarBg(cache: CalendarBgEntity): Long

    @Query("select *from CalendarImg where holidayTime=:holidayTime")
    fun getCalendarBg(holidayTime: String): CalendarBgEntity?

    @Delete
    fun deleteCalendarBg(cache: CalendarBgEntity): Int

    @Query("DELETE FROM CalendarImg")
    fun deleteAllImg(): Int
}