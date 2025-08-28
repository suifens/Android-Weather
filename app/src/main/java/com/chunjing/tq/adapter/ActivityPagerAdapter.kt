package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ActivityPagerAdapter(
    activity: FragmentActivity,
    var list: List<Fragment>,
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(fragments: List<Fragment>) {
        list = fragments
        notifyDataSetChanged()
    }
}