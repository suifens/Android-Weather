package com.goodtech.weatherlib.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * com.goodtech.weatherlib.view
 */
class SpaceItemDecoration(val spaceValue: HashMap<String, Int>) : RecyclerView.ItemDecoration() {
    
    companion object {
        val TOP_SPACE = "top_space"
        val BOTTOM_SPACE = "bottom_space"
        val LEFT_SPACE = "left_space"
        val RIGHT_SPACE = "right_space"
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (spaceValue[LEFT_SPACE] != null)
            outRect.left = spaceValue[LEFT_SPACE]!!
        if (spaceValue[RIGHT_SPACE] != null)
            outRect.right = spaceValue[RIGHT_SPACE]!!
        if (spaceValue[BOTTOM_SPACE] != null)
            outRect.bottom = spaceValue[BOTTOM_SPACE]!!
        if (spaceValue[TOP_SPACE] != null)
            outRect.top = spaceValue[TOP_SPACE]!!
    }
}