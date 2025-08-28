package com.chunjing.tq.utils

import android.content.Context
import com.chunjing.tq.R

/**
 * Created by niuchong on 2019/4/7.
 */
object PopularUtil {

    @JvmStatic
    fun getCityImgRes(context: Context, name: String): Int {
        return when(name) {
                "北京" -> R.drawable.city_beijing
                "上海" -> R.drawable.city_shanghai
                "重庆" -> R.drawable.city_chongqing
                "深圳" -> R.drawable.city_shenzhen
                "郑州" -> R.drawable.city_zhengzhou
                "广州" -> R.drawable.city_guangzhou
                "成都" -> R.drawable.city_chengdu
                "杭州" -> R.drawable.city_hangzhou
                "苏州" -> R.drawable.city_suzhou
                "厦门" -> R.drawable.city_xiamen
                "青岛" -> R.drawable.city_qingdao
                "南京" -> R.drawable.city_nanjiing
                "天津" -> R.drawable.city_tianjin
                else -> 0
        }
            
    }
    
}