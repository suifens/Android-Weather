package com.chunjing.tq.ui.activity.vm

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.chunjing.tq.bean.juhe.JuheBean
import com.chunjing.tq.bean.LifeEntity
import com.chunjing.tq.bean.LifeResult
import com.chunjing.tq.db.entity.CityEntity
import com.chunjing.tq.ext.JUHE_LIFE
import com.chunjing.tq.ui.base.BaseViewModel
import com.goodtech.weatherlib.net.HttpUtils

/**
 * com.chunjing.tq.ui.activity.vm
 */
class LifeViewModel : BaseViewModel() {

    val lifeLiveData = MutableLiveData<LifeEntity?>()

    private val mLifeEntities: MutableMap<String, LifeEntity> = HashMap()



    fun getLifeDetails(cityMode: CityEntity) {
        launchSilent {

            var city: String = cityMode.cityName
            if (!TextUtils.isEmpty(city)) {
                if (city.endsWith("市")) {
                    city = city.replace("市", "")
                }
                if (city.endsWith("县")) {
                    city = city.replace("县", "")
                }
                if (city.endsWith("区")) {
                    city = city.replace("区", "")
                }
            }

            val url = String.format(JUHE_LIFE, city)
            val result = HttpUtils.get<JuheBean<LifeResult>>(url)
            result?.result?.life.let { entity ->
//                    WeatherSpUtils.saveCityLife(entity.toString(), cityMode.cityId)
                if (entity != null) {
                    mLifeEntities[cityMode.cityId] = entity
                }
                lifeLiveData.postValue(entity)
                return@launchSilent
            }
        }
        lifeLiveData.postValue(null)
    }

}