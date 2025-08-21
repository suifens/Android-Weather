package com.goodtech.tq.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.djx.IDJXWidget
import com.bytedance.sdk.djx.utils.StatusBarUtil
import com.goodtech.tq.R
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.BusEvent
import com.goodtech.tq.common.bus.event.DJXStartEvent
import com.goodtech.tq.databinding.FragmentDrawDramaBinding
import com.goodtech.tq.databinding.FragmentHomeBinding
import com.goodtech.tq.modules.video.djx.DJXHolder

class DrawDramaFragment : BaseFragment() {

    private var djxWidget: IDJXWidget? = null
    private var _binding: FragmentDrawDramaBinding? = null
    private val binding get() = _binding!!

    private val listener: (BusEvent) -> Unit = {
        if (it is DJXStartEvent) {
            if (it.isSuccess) {
                init()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawDramaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Bus.getInstance().addListener(listener)
        if (DJXSdk.isStartSuccess()) {
            init()
        }
    }

    private fun init() {
        djxWidget = DJXHolder.loadDramaDraw(null, null, null)
        djxWidget?.fragment?.userVisibleHint = userVisibleHint

        djxWidget?.let {
            childFragmentManager.beginTransaction().apply {
                replace(R.id.draw_drama_frame, it.fragment)
                this.commitNowAllowingStateLoss()
            }
        }
        val params = binding.drawDramaFrame.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = StatusBarUtil.getStatusBarHeight(requireActivity())
        binding.drawDramaFrame.layoutParams = params
    }

    override fun onResume() {
        super.onResume()
        djxWidget?.fragment?.onResume()
    }

    override fun onPause() {
        super.onPause()
        djxWidget?.fragment?.onPause()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        djxWidget?.fragment?.onHiddenChanged(hidden)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        djxWidget?.fragment?.userVisibleHint = isVisibleToUser
    }

    override fun onDestroy() {
        super.onDestroy()
        Bus.getInstance().removeListener(listener)
    }
}