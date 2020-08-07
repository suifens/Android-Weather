package com.goodtech.tq.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.Nullable;

/**
 * com.goodtech.tq.views
 */
public class HorizontalRecyclerView extends RecyclerView {
    int lastX = 0;
    int lastY = 0;
    public HorizontalRecyclerView(Context context) {
        super(context);
    }

    public HorizontalRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        /*--------解决垂直RecyclerView嵌套水平RecyclerView横向滑动不流畅问题 start --------*/
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        int dealtX = 0;
        int dealtY = 0;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dealtX = 0;
                dealtY = 0;
                // 保证子View能够接收到Action_move事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                dealtX += Math.abs(x - lastX);
                dealtY += Math.abs(y - lastY);
                // Log.i("dispatchTouchEvent", "dealtX:=" + dealtX);
                // Log.i("dispatchTouchEvent", "dealtY:=" + dealtY);
                // 这里是够拦截的判断依据是左右滑动，读者可根据自己的逻辑进行是否拦截
                if (dealtX >= dealtY) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                break;

        }
        /*--------解决垂直RecyclerView嵌套水平RecyclerView横向滑动不流畅问题 end --------*/


        /*---解决ViewPager嵌套垂直RecyclerView嵌套水平RecyclerView横向滑动到底后不滑动ViewPager start ---*/
        ViewParent parent =this;
        while(!((parent = parent.getParent()) instanceof ViewPager));// 循环查找viewPager
        parent.requestDisallowInterceptTouchEvent(true);
        /*---解决ViewPager嵌套垂直RecyclerView嵌套水平RecyclerView横向滑动到底后不滑动ViewPager start ---*/
        return super.dispatchTouchEvent(ev);
    }
}