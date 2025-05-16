package com.herry.test.app.sample.me

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.libs.widget.configure.SystemUIShowBehavior
import com.herry.libs.widget.configure.SystemUIVisibility
import com.herry.libs.widget.extension.navigateTo
import com.herry.libs.widget.extension.setOnSingleClickListener
import com.herry.test.R
import com.herry.test.app.base.nav.BaseNavFragment
import com.herry.test.app.keepchildvm.KeepChildVMTabAdapter

class MeFragment: BaseNavFragment() {

    override fun onSystemUIAppearances(context: Context): SystemUIAppearances =
        SystemUIAppearances.getDefaultSystemUIAppearances(context).apply {
            isFullScreen = true
            showBehavior = SystemUIShowBehavior.TRANSIENT_BARS_BY_SWIPE
            statusBar?.visibility = SystemUIVisibility.HIDE
        }

    private var container: View? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tabAdapter: KeepChildVMTabAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return this.container ?: inflater.inflate(R.layout.me_fragment, container, false)
            ?.apply { init(this) }.also { this.container = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // checks if the adapter is already initialized, preventing potential state restoration issues.
        if (!::tabAdapter.isInitialized) {
            init(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the reference to the view
        container = null
    }

    private fun init(view: View?) {
        view ?: return

        view.findViewById<View?>(R.id.me_fragment_menu)?.setOnSingleClickListener {
            navigateTo(destinationId = R.id.account_fragment)
        }

        // ViewPager2와 TabLayout 초기화
        viewPager = view.findViewById(R.id.me_fragment_viewpager)
        tabLayout = view.findViewById(R.id.me_fragment_tabLayout)

        // Adapter 설정
        tabAdapter = KeepChildVMTabAdapter(this)
        // set offscreenPageLimit to control how many fragments should remain active.
        // This helps keep memory usage low while ensuring smooth navigation between tabs.
        viewPager.offscreenPageLimit = tabAdapter.getItemCount()
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