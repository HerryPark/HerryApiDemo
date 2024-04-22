package com.herry.test.app.sample.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.herry.libs.helper.ToastHelper
import com.herry.libs.util.ViewUtil
import com.herry.libs.widget.configure.SystemUIAppearances
import com.herry.test.R
import com.herry.test.app.base.nestednav.BaseNestedNavView
import com.herry.test.app.sample.home.form.HomeBottomNavControlForm
import com.herry.test.app.sample.home.form.HomeBottomNavFragmentForm

class HomeFragment: BaseNestedNavView<HomeContract.View, HomeContract.Presenter>(), HomeContract.View {
    override fun onSystemUIAppearances(context: Context): SystemUIAppearances? = null

    override fun onCreatePresenter(): HomeContract.Presenter = HomePresenter()

    override fun onCreatePresenterView(): HomeContract.View = this

    private var container: View? = null

    private val bottomNavFragmentForm = HomeBottomNavFragmentForm(this,
        onNavNotify = { bundle ->
            onReceivedFromBottomNavFragments(bundle)
        })

    private val bottomNavigatorForm = HomeBottomNavControlForm { selectedItemType ->
        presenter?.setCurrent(selectedItemType)
    }

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

        bottomNavFragmentForm.bindHolder(context, view.findViewById(R.id.home_fragment_bottom_navigator_fragment_form))
        bottomNavigatorForm.bindFormHolder(context, view.findViewById(R.id.home_fragment_bottom_navigator_form))
    }

    override fun onSelectTab(model: HomeBottomNavControlForm.Model, isStart: Boolean, startArgs: Bundle?) {
        val context = this.context ?: return

        // update bottom navigator
        bottomNavigatorForm.bindFormModel(context, model)
        // update bottom navigated screen
        bottomNavFragmentForm.setNavScreen(model.selected, isStart, startArgs)
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