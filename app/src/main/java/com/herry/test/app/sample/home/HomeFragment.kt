package com.herry.test.app.sample.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.herry.libs.helper.ToastHelper
import com.herry.libs.util.ViewUtil
import com.herry.libs.util.navigation.NavUI
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.libs.widget.extension.disableTooltip
import com.herry.libs.widget.extension.findNestedNavHostFragment
import com.herry.libs.widget.extension.getCurrentFragment
import com.herry.libs.widget.extension.setFragmentNotifyListener
import com.herry.test.R
import com.herry.test.app.base.nestednav.BaseMVPNestedNavView

class HomeFragment: BaseMVPNestedNavView<HomeContract.View, HomeContract.Presenter>(), HomeContract.View {
    override fun onSystemUIAppearances(context: Context): SystemUIAppearances? = null

    override fun onCreatePresenter(): HomeContract.Presenter = HomePresenter()

    override fun onCreatePresenterView(): HomeContract.View = this

    private var container: View? = null

    private var bottomNavHostFragment: NavHostFragment? = null
    private var bottomNavView: BottomNavigationView? = null

    private var pressedBackKey = false

    private val pressedBackKeyHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.container == null) {
            this.container = inflater.inflate(R.layout.home_fragment, container, false)
            init(this.container)
        } else {
            ViewUtil.removeViewFormParent(this.container)
        }
        return this.container
    }

    private fun init(view: View?) {
        val context = view?.context ?: return

        bottomNavHostFragment = findNestedNavHostFragment(R.id.home_fragment_container)?.apply {
            setFragmentNotifyListener { _, bundle ->
                onReceivedFromBottomNavFragments(bundle)
            }
            addNestedNavHostFragment(this)
        }

        bottomNavView = view.findViewById<BottomNavigationView?>(R.id.home_fragment_bottom_nav_view)?.apply {
            val navController = bottomNavHostFragment?.navController
            if (navController != null) {
                NavUI.setupWithNavController(
                    navigationBarView = this,
                    navController = navController,
                    animBuilder = NavUI.navAnimNone,
                    onIsSelectable = { _ ->
                        true
                    },
                    onItemSelected = { menuItem ->
                        presenter?.setCurrent(HomeTab.generate(menuItem.itemId) ?: return@setupWithNavController)
                    },
                    onItemUnselected = { _ ->
                    },
                    onItemReselected = { _ ->
                    }
                )

                setOnItemReselectedListener { menuItem ->
                    (bottomNavHostFragment?.getCurrentFragment() as? NavigationBarView.OnItemReselectedListener)?.onNavigationItemReselected(menuItem)
                }
            }

            disableTooltip()
        }
    }

    override fun onSelectTab(tab: HomeTab, isStart: Boolean, startArgs: Bundle?) {
        bottomNavView?.selectedItemId = tab.id
    }

    private fun onReceivedFromBottomNavFragments(bundle: Bundle) {
        // nothing
    }

    override fun onResume() {
        super.onResume()

        resetPressedBackKey()
    }

    private fun resetPressedBackKey() {
        pressedBackKeyHandler.removeCallbacksAndMessages(null)
        pressedBackKey = false
    }

    override fun onNavigateUp(): Boolean {
        if (!pressedBackKey) {
            ToastHelper.showToast(activity, "뒤로가기 버튼을 한번\n더 누르면 앱이 종료됩니다.")
            pressedBackKey = true
            pressedBackKeyHandler.postDelayed({ resetPressedBackKey() }, 2000L)
        } else {
            finishActivity(false)
        }

        return true
    }
}