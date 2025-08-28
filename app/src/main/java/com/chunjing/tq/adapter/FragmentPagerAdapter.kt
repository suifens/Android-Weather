package com.chunjing.tq.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentPagerAdapter(
    fragment: Fragment,
    var list: List<Fragment>,
) : FragmentStateAdapter(fragment) {

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