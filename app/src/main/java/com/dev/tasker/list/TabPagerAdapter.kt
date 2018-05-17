package com.dev.tasker.list

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.dev.tasker.R
import com.dev.tasker.list.fragment.BaseTabFragment
import com.dev.tasker.list.fragment.CurrentTabFragment
import com.dev.tasker.list.fragment.DoneTabFragment
import com.dev.tasker.list.fragment.PendingTabFragment

class TabPagerAdapter(val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): BaseTabFragment? {
        return when (position) {
            0 -> PendingTabFragment()
            1 -> CurrentTabFragment()
            2 -> DoneTabFragment()
            else -> null
        }
    }
 
    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> context.getString(R.string.pending)
            1 -> context.getString(R.string.current)
            2 -> context.getString(R.string.done)
            else -> null
        }
    }
}