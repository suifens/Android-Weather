package com.goodtech.tq.models

import android.os.Parcel
import android.os.Parcelable
import com.goodtech.tq.R
import java.io.Serializable

/**
 * 聚合数据生活指数模型类
 * 包含各种生活指数的详细信息和建议
 * 实现Serializable接口，支持数据序列化传输
 */
data class LifeEntity(
    val chuanyi: LifeItemBean? = null,
    val daisan: LifeItemBean? = null,
    val diaoyu: LifeItemBean? = null,
    val ganmao: LifeItemBean? = null,
    val guomin: LifeItemBean? = null,
    val kongtiao: LifeItemBean? = null,
    val shushidu: LifeItemBean? = null,
    val xiche: LifeItemBean? = null,
    val yundong: LifeItemBean? = null,
    val ziwaixian: LifeItemBean? = null,
    //  更新时间
    var updateTime: Long = 0
) : Serializable {

    fun lifeImageWith(item: LifeItemBean?): Int {
        return when (item) {
            chuanyi -> R.drawable.life_1
            daisan -> R.drawable.life_2
            xiche -> R.drawable.life_3
            diaoyu -> R.drawable.life_4
            yundong -> R.drawable.life_5
            ganmao -> R.drawable.life_6
            kongtiao -> R.drawable.life_7
            guomin -> R.drawable.life_8
            else -> R.drawable.life_1
        }
    }

    fun lifeTitleWith(item: LifeItemBean?): String {
        return when (item) {
            chuanyi -> "穿衣指数"
            daisan -> "带伞指数"
            xiche -> "洗车指数"
            diaoyu -> "钓鱼指数"
            yundong -> "运动指数"
            ganmao -> "感冒指数"
            kongtiao -> "空调指数"
            guomin -> "过敏指数"
            else -> ""
        }
    }
}

data class LifeResult(
    val city: String,
    val life: LifeEntity? = null
) : Serializable

data class LifeItemBean(
    val des: String,
    val v: String = ""
) : Serializable 