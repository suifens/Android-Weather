package com.goodtech.tq.base

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup


fun removeFromParent(view: View?) {
    if (view != null) {
        val vp = view.parent
        if (vp is ViewGroup) {
            vp.removeView(view)
        }
    }
}

fun hitTest(v: View, x: Int, y: Int): Boolean {
    val tx = (v.translationX + 0.5f).toInt()
    val ty = (v.translationY + 0.5f).toInt()
    val left = v.left + tx
    val right = v.right + tx
    val top = v.top + ty
    val bottom = v.bottom + ty

    return (x >= left) && (x <= right) && (y >= top) && (y <= bottom)
}

private val EMPTY_STATE = intArrayOf()

fun clearState(drawable: Drawable?) {
    drawable?.setState(EMPTY_STATE)
}
