package com.herry.test.app.keepchildvm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.herry.libs.log.Trace
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment
import com.herry.test.widget.TitleBarForm

class KeepChildVMFragment: BaseNavFragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tabAdapter: KeepChildVMTabAdapter

    private var container: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.keep_child_vm_fragment, container, false)
            ?.apply { init(this) }.also { this.container = it}
    }

    private fun init(view: View) {
        val context = view.context ?: return

        TitleBarForm(
            activity = { requireActivity() }
        ).apply {
            bindFormHolder(context, view.findViewById(R.id.keep_child_vm_fragment_title))
            bindFormModel(context, TitleBarForm.Model(title = "Keep Child ViewModel", backEnable = true))
        }

        // ViewPager2와 TabLayout 초기화
        viewPager = view.findViewById(R.id.keep_child_vm_fragment_viewpager)
        tabLayout = view.findViewById(R.id.keep_child_vm_fragment_tabLayout)

        // Adapter 설정
        tabAdapter = KeepChildVMTabAdapter(this)
        viewPager.adapter = tabAdapter

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tab 1"
                1 -> "Tab 2"
                else -> "Unknown"
            }
        }.attach()
    }
}