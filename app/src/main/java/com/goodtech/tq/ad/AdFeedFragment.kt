package com.goodtech.tq.ad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.TTFeedAd
import com.goodtech.tq.base.callback.DataCallback
import com.goodtech.tq.fragment.BaseFragment

open class AdFeedFragment : BaseFragment() {
    private var feedContainer: FrameLayout? = null
    private var loadSuccess = false
    private var isLoadedAndShow = false
    private var feedAd: TTFeedAd? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    protected fun loadFeedAd(codeId: String, width: Int, callback: DataCallback<TTFeedAd>) {
        AdManager.getInstance().loadFeedAd(requireActivity(), codeId, width, callback)
    }

    protected fun showAd(
        feedContainer: FrameLayout,
        loadSuccess: Boolean,
        isLoadedAndShow: Boolean,
        feedAd: TTFeedAd?
    ): Boolean {
        if (!loadSuccess || feedAd == null) {
            return false
        }
        
        this.loadSuccess = false
        this.isLoadedAndShow = true
        this.feedContainer = feedContainer
        this.feedAd = feedAd

        feedContainer.visibility = View.VISIBLE
        mHandler.post {
            AdManager.getInstance().showFeedAd(requireActivity(), feedContainer, feedAd)
        }
        return true
    }
} 