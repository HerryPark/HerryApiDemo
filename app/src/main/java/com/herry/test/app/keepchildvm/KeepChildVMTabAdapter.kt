package com.herry.test.app.keepchildvm

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.herry.test.app.keepchildvm.child.KeepChildVMChildFragment

class KeepChildVMTabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2 // Tab 개수

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> KeepChildVMChildFragment.newInstance("Child 1")
            1 -> KeepChildVMChildFragment.newInstance("Child 2")
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }

    // Fragment의 ID를 고유하게 지정해주어 Fragment 상태 유지
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

//    // 구성 변경 시 Fragment가 재사용될 수 있도록 처리
//    override fun containsItem(itemId: Long): Boolean {
//        return itemId in 0..itemCount
//    }
}