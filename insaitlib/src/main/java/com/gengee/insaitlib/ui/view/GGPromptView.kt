package com.gengee.insaitlib.ui.view

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.gengee.insaitlib.R


/**
 */
class GGPromptView {

    enum class GGMessageType {
        Message,
        Warning,
        Error,
        Success
    }

    companion object {
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            GGPromptView()
        }
    }

    var mToast: Toast? = null
    var lastShowTime: Long = 0

    /**
     * 展示toast==LENGTH_SHORT
     * @param msg
     */
    fun show(context: Context, msg: String, type: GGMessageType) {
        runOnUiThread {
            if (mToast == null || System.currentTimeMillis() - lastShowTime > 3000) {
                mToast?.cancel()
                show(context, msg, type, Toast.LENGTH_SHORT)
            }
        }
    }

    /**
     * 展示toast==LENGTH_LONG
     * @param msg
     */
    fun showLong(context: Context, msg: String, type: GGMessageType) {
        show(context, msg, type, Toast.LENGTH_LONG)
    }


    private fun show(context: Context, massage: String, type: GGMessageType, show_length: Int) {
        try {
            //使用布局加载器，将编写的toast_layout布局加载进来
            val view: View = LayoutInflater.from(context).inflate(R.layout.view_prompt, null)
            //获取ImageView
            val image: ImageView = view.findViewById(R.id.imageView) as ImageView
            //设置图片
            var imgRes: Int = when (type) {
                GGMessageType.Error -> R.drawable.ic_error
                GGMessageType.Success -> R.drawable.ic_success_s
                else -> R.drawable.ic_notice
            }
            image.setImageResource(imgRes)

            val promptView = view.findViewById(R.id.promptView) as View
            if (type == GGMessageType.Success) {
                promptView.setBackgroundColor(Color.parseColor("#0AC97E"))
            } else {
                promptView.setBackgroundColor(Color.parseColor("#FF6A61"))
            }
            //获取TextView
            val title = view.findViewById(R.id.titleTv) as TextView
            //设置显示的内容
            title.text = massage

            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);//设置Toast可以布局到系统状态栏的下面
            val toast = Toast(context)
            //设置Toast要显示的位置，水平居中并在底部，X轴偏移0个单位，Y轴偏移70个单位，
            toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, 0)
            //设置显示时间
            toast.duration = show_length
            toast.setView(view)
            toast.show()
            this.mToast = toast
            lastShowTime = System.currentTimeMillis()

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG", "show: ${e.toString()}")
        }
    }
}
