package com.chunjing.tq.db.entity

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.Entity

/**
 * com.chunjing.tq.db.entity
 * 天气背景图片
 */
@SuppressLint("KotlinNullnessAnnotation")
@Entity(tableName = "WeatherImg", primaryKeys = ["tempType", "timeType"])
class WeatherBgEntity() : Parcelable {

    var imgPath: String = ""

    @Nullable
    var videoPath: String = ""

    @Nullable
    var startColor: String = ""
    @Nullable
    var endColor: String = ""

    @NonNull
    var tempType: String = ""
    @NonNull
    var timeType: String = ""

    constructor(parcel: Parcel) : this() {
        endColor = parcel.readString().toString()
        imgPath = parcel.readString().toString()
        videoPath = parcel.readString().toString()
        startColor = parcel.readString().toString()
        tempType = parcel.readString().toString()
        timeType = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(endColor)
        parcel.writeString(imgPath)
        parcel.writeString(videoPath)
        parcel.writeString(startColor)
        parcel.writeString(tempType)
        parcel.writeString(timeType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WeatherBgEntity> {
        override fun createFromParcel(parcel: Parcel): WeatherBgEntity {
            return WeatherBgEntity(parcel)
        }

        override fun newArray(size: Int): Array<WeatherBgEntity?> {
            return arrayOfNulls(size)
        }
    }

}