package com.goodtech.tq.modules.weather.holder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goodtech.tq.databinding.WeatherItemAirLifeBinding
import com.goodtech.tq.models.JuheLifeModel

class GTAirLifeHolder(val binding: WeatherItemAirLifeBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun getBinding(context: Context, parent: ViewGroup): WeatherItemAirLifeBinding {
            return WeatherItemAirLifeBinding.inflate(LayoutInflater.from(context), parent, false)
        }
    }

    fun setupLife(lifeModel: JuheLifeModel?, color: Int) {
        if (color != 0) {
            binding.itemDressing.setTextColor(color)
            binding.itemSport.setTextColor(color)
            binding.itemUmbrella.setTextColor(color)
            binding.itemFishing.setTextColor(color)
            binding.itemCold.setTextColor(color)
            binding.itemCar.setTextColor(color)
            binding.itemAirConditioner.setTextColor(color)
            binding.itemAllergy.setTextColor(color)
            binding.itemComfort.setTextColor(color)
        }
        if (lifeModel != null) {
            binding.itemDressing.setState(if (lifeModel.chuanyi != null) lifeModel.chuanyi.v else "")
            binding.itemSport.setState(if (lifeModel.yundong != null) lifeModel.yundong.v else "")
            binding.itemUmbrella.setState(if (lifeModel.daisan != null) lifeModel.daisan.v else "")
            binding.itemFishing.setState(if (lifeModel.diaoyu != null) lifeModel.diaoyu.v else "")
            binding.itemCold.setState(if (lifeModel.ganmao != null) lifeModel.ganmao.v else "")
            binding.itemCar.setState(if (lifeModel.xiche != null) lifeModel.xiche.v else "")
            binding.itemAirConditioner.setState(if (lifeModel.kongtiao != null) lifeModel.kongtiao.v else "")
            binding.itemAllergy.setState(if (lifeModel.guomin != null) lifeModel.guomin.v else "")
            binding.itemComfort.setState(if (lifeModel.shushidu != null) lifeModel.shushidu.v else "")
        }
    }

}