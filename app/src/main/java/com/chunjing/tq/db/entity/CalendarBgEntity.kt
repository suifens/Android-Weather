package com.chunjing.tq.db.entity

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * com.chunjing.tq.db.entity
 * 天气背景图片
 */
@SuppressLint("KotlinNullnessAnnotation")
@Entity(tableName = "CalendarImg")
class CalendarBgEntity() : Parcelable {

    @PrimaryKey
    var holidayTime: String = ""

    var holiday: String = ""
    @Nullable
    var imgPath: String = ""
    var duration: String = "1"

    constructor(parcel: Parcel) : this() {
        holidayTime = parcel.readString().toString()
        holiday = parcel.readString().toString()
        imgPath = parcel.readString().toString()
        duration = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(holidayTime)
        parcel.writeString(holiday)
        parcel.writeString(imgPath)
        parcel.writeString(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CalendarBgEntity> {
        override fun createFromParcel(parcel: Parcel): CalendarBgEntity {
            return CalendarBgEntity(parcel)
        }

        override fun newArray(size: Int): Array<CalendarBgEntity?> {
            return arrayOfNulls(size)
        }
    }

}