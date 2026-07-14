package com.chunjing.tq.db.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

const val LOCATION_ID = "100000"

@Entity(tableName = "city")
class CityEntity() : Parcelable, Serializable {

    @PrimaryKey
    var cityId: String = ""

    var cityName: String = ""

    var cityCode: String = ""

    var shortName: String = ""

    var mergerName: String = ""

    var latitude: String = ""

    var longitude: String = ""

    var pinyin: String = ""

    /** 展示顺序：定位城固定为 0，其余从 1 递增 */
    var sortOrder: Int = 0

    fun isLocal(): Boolean {
        return cityId == LOCATION_ID
    }

    fun setLocal() {
        cityId = LOCATION_ID
    }

    constructor(parcel: Parcel) : this() {
        cityId = parcel.readString().toString()
        cityName = parcel.readString().toString()
        cityCode = parcel.readString().toString()
        shortName = parcel.readString().toString()
        mergerName = parcel.readString().toString()
        latitude = parcel.readString().toString()
        longitude = parcel.readString().toString()
        pinyin = parcel.readString().toString()
        sortOrder = parcel.readInt()
    }

    @Ignore
    constructor(id: String,
                name: String,
                shortName: String,
                mergerName: String,
                lat: String,
                lon: String,
                pinyin: String = "",
    ) : this() {
        cityId = id
        cityName = name
        this.shortName = shortName
        this.mergerName = mergerName
        latitude = lat
        longitude = lon
        this.pinyin = pinyin
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cityId)
        parcel.writeString(cityName)
        parcel.writeString(cityCode)
        parcel.writeString(shortName)
        parcel.writeString(mergerName)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(pinyin)
        parcel.writeInt(sortOrder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CityEntity> {
        override fun createFromParcel(parcel: Parcel): CityEntity {
            return CityEntity(parcel)
        }

        override fun newArray(size: Int): Array<CityEntity?> {
            return arrayOfNulls(size)
        }
    }
}