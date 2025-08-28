package com.goodtech.weatherlib.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * com.goodtech.weatherlib.view
 * @param spanCount     列数
 * @param rowSpacing    行间距
 * @param columnSpacing 列间距
 */
class GridSpaceItemDecoration(val spanCount: Int, val rowSpacing: Int, val columnSpacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // 获取view 在adapter中的位置。
        val column = position%spanCount // view 所在的列

        outRect.left = column * columnSpacing / spanCount   // column * (列间距 * (1f / 列数))
        outRect.right = columnSpacing - (column + 1) * columnSpacing / spanCount    // 列间距 - (column + 1) * (列间距 * (1f /列数))

        // 如果position > 行数，说明不是在第一行，则不指定行高，其他行的上间距为 top=mRowSpacing
        if (position >= spanCount) {
            outRect.top = rowSpacing // item top
        }
    }
}