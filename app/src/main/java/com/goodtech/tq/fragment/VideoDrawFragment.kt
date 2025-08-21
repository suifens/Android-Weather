package com.goodtech.tq.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.bytedance.sdk.djx.utils.StatusBarUtil
import com.bytedance.sdk.dp.DPSdk
import com.bytedance.sdk.dp.DPWidgetDrawParams
import com.bytedance.sdk.dp.IDPDrawListener
import com.bytedance.sdk.dp.IDPWidget
import com.goodtech.tq.R
import com.goodtech.tq.common.bus.Bus
import com.goodtech.tq.common.bus.BusEvent
import com.goodtech.tq.common.bus.event.DPStartEvent
import com.goodtech.tq.databinding.MediaFragmentWrapperBinding
import com.goodtech.tq.modules.video.VideoUtils

class VideoDrawFragment: BaseFragment() {
    private var dpWidget: IDPWidget? = null
    private var _binding: MediaFragmentWrapperBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "DrawFragment"
    }

    private var isInited = false
    private var isDestroyed = false

    val function: (BusEvent) -> Unit = {
        if (it is DPStartEvent) {
            if (it.isSuccess && !isDestroyed) {
                init()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MediaFragmentWrapperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Bus.getInstance().addListener(function)
        if (DPSdk.isStartSuccess()) {
            init()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true
        Bus.getInstance().removeListener(function)
    }

    fun init() {
        if (isInited || isDestroyed || !isAdded || context == null) {
            return
        }
        try {
            initDrawWidget()
            isInited = true
        } catch (e: Exception) {
            Log.e(TAG, "初始化视频组件失败", e)
        }
        val params = binding.flContainer.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin = StatusBarUtil.getStatusBarHeight(requireActivity())
        binding.flContainer.layoutParams = params
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        try {
            dpWidget?.fragment?.onResume()
        } catch (e: Exception) {
            Log.e(TAG, "onResume失败", e)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        try {
            dpWidget?.fragment?.onPause()
        } catch (e: Exception) {
            Log.e(TAG, "onPause失败", e)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        Log.d(TAG, "setUserVisibleHint $isVisibleToUser")
        try {
            dpWidget?.fragment?.userVisibleHint = isVisibleToUser
        } catch (e: Exception) {
            Log.e(TAG, "setUserVisibleHint失败", e)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.d(TAG, "onHiddenChanged $hidden")
        try {
            dpWidget?.fragment?.onHiddenChanged(hidden)
        } catch (e: Exception) {
            Log.e(TAG, "onHiddenChanged失败", e)
        }
    }

    private fun initDrawWidget() {
        if (!VideoUtils.isVideoSDKAvailable() || isDestroyed || !isAdded || context == null) {
            if (!isDestroyed && isAdded && context != null) {
                this.view?.postDelayed({
                    initDrawWidget()
                }, 100)
            }
            return
        }
        
        VideoUtils.executeVideoOperationSafely {
            val params = DPWidgetDrawParams.obtain()
                .adOffset(0) //单位 dp，为 0 时可以不设置
                .drawContentType(DPWidgetDrawParams.DRAW_CONTENT_TYPE_ONLY_VIDEO)
                .hideClose(true, null)
                .listener(object : IDPDrawListener() {
                    override fun onDPRefreshFinish() {
                        Log.d(TAG, "onDPRefreshFinish")
                    }

                    override fun onDPPageChange(position: Int) {
                        Log.d(TAG, "onDPPageChange: $position")
                    }

                    override fun onDPVideoPlay(map: Map<String, Any>) {
                        Log.d(TAG, "onDPVideoPlay")
                    }

                    override fun onDPVideoCompletion(map: Map<String, Any>) {
                        Log.d(TAG, "onDPVideoCompletion: ")
                    }

                    override fun onDPVideoOver(map: Map<String, Any>) {
                        Log.d(TAG, "onDPVideoOver")
                    }

                    override fun onDPClose() {
                        Log.d(TAG, "onDPClose")
                    }

                    override fun onDPRequestStart(map: Map<String, Any>?) {
                        Log.d(TAG, "onDPRequestStart")
                    }

                    override fun onDPRequestSuccess(list: List<Map<String, Any>>) {
                        Log.d(TAG, "onDPRequestSuccess")
                    }

                    override fun onDPRequestFail(
                        code: Int, msg: String, map: Map<String, Any>?
                    ) {
                        Log.d(TAG, "onDPRequestFail")
                    }

                    override fun onDPClickAuthorName(map: Map<String, Any>) {
                        Log.d(TAG, "onDPClickAuthorName")
                    }

                    override fun onDPClickAvatar(map: Map<String, Any>) {
                        Log.d(TAG, "onDPClickAvatar")
                    }

                    override fun onDPClickComment(map: Map<String, Any>) {
                        Log.d(TAG, "onDPClickComment")
                    }

                    override fun onDPClickLike(
                        isLike: Boolean, map: Map<String, Any>
                    ) {
                        Log.d(TAG, "onDPClickLike")
                    }

                    override fun onDPVideoPause(map: Map<String, Any>) {
                        Log.d(TAG, "onDPVideoPause")
                    }

                    override fun onDPVideoContinue(map: Map<String, Any>) {
                        Log.d(TAG, "onDPVideoContinue")
                    }

                    override fun onDPClickShare(map: MutableMap<String, Any>?) {
                        Log.d(TAG, "onDPClickShare $map")
                    }

                    override fun onChannelTabChange(channel: Int) {
                        Log.d(TAG, "onChannelTabChange, is $channel")
                    }
                })

            dpWidget = VideoUtils.createVideoWidgetSafely(context, params, null)
            
            dpWidget?.fragment?.userVisibleHint = userVisibleHint
            dpWidget?.let {
                if (isAdded && !isDestroyed && context != null) {
                    try {
                        childFragmentManager.beginTransaction().apply {
                            replace(R.id.fl_container, it.fragment)
                            this.commitNowAllowingStateLoss()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "添加Fragment失败", e)
                    }
                }
            }
        }
    }
}