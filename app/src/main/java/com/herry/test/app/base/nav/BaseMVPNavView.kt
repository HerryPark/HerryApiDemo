package com.herry.test.app.base.nav

import android.content.Context
import android.os.Bundle
import com.herry.libs.app.activity_caller.module.ACError
import com.herry.libs.mvp.MVPPresenter
import com.herry.libs.mvp.MVPPresenterViewModelFactory
import com.herry.libs.mvp.MVPView
import com.herry.libs.mvp.MVPViewCreation

@Suppress("unused")
abstract class BaseMVPNavView<V: MVPView<P>, P: MVPPresenter<V>>: BaseNavFragment(), MVPView<P>, MVPViewCreation<V, P> {

    override var presenter: P? = null

    override fun getViewContext(): Context? = context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = this.presenter ?: run {
            val viewModel = MVPPresenterViewModelFactory.create(this, this)
            viewModel?.presenter?.apply {
                relaunched(viewModel.recreated)
            }
        }
        this.presenter?.onAttach(onCreatePresenterView()) ?: finishFragment(null)
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter?.onDetach()
    }

    override fun onStart() {
        super.onStart()

        presenter?.onStart()
    }

    override fun onResume() {
        super.onResume()

        presenter?.onResume()
    }

    override fun onPause() {
        presenter?.onPause()

        super.onPause()
    }

    override fun onStop() {
        presenter?.onStop()

        super.onStop()
    }

    override fun error(throwable: Throwable) {
        activityCaller?.call(
                ACError.Caller(throwable) {
                    onError(it)
                }
        )
    }

    open fun onError(throwable: Throwable) {

    }

    override fun onTransitionStart() {
        super.onTransitionStart()

        if(presenter is BaseMVPNavPresenter<*>) {
            (presenter as BaseMVPNavPresenter<*>).navTransitionStart()
        }
    }

    override fun onTransitionEnd() {
        super.onTransitionEnd()

        if(presenter is BaseMVPNavPresenter<*>) {
            (presenter as BaseMVPNavPresenter<*>).navTransitionEnd()
        }
    }

    final override fun showViewLoading() {
        showLoading()
    }

    final override fun hideViewLoading(success: Boolean) {
        hideLoading()
    }
}