package com.goodtech.tq.modules.video.djx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.djx.IDJXWidget
import com.goodtech.tq.R
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.BusEvent
import com.goodtech.tq.common.bus.event.DJXStartEvent

class DjxVideoActivity : AppCompatActivity() {

    private var djxWidget: IDJXWidget? = null

    private val listener: (BusEvent) -> Unit = {
        if (it is DJXStartEvent) {
            if (it.isSuccess) {
                init()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_djx_video)

        Bus.getInstance().addListener(listener)
        if (DJXSdk.isStartSuccess()) {
            init()
        }

    }

    private fun init() {
        djxWidget = DJXHolder.loadDramaDraw(null, null, null)
        djxWidget?.fragment?.userVisibleHint = true

        djxWidget?.let {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fl_container, it.fragment)
                this.commitNowAllowingStateLoss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        djxWidget?.fragment?.onResume()
    }

    override fun onPause() {
        super.onPause()
        djxWidget?.fragment?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        Bus.getInstance().removeListener(listener)
    }


}