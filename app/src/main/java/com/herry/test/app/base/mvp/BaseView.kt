package com.herry.test.app.base.mvp

import android.os.Bundle
import com.herry.libs.app.activity_caller.module.ACError
import com.herry.libs.mvp.MVPPresenter
import com.herry.libs.mvp.MVPPresenterViewModelFactory
import com.herry.libs.mvp.MVPView
import com.herry.libs.mvp.MVPViewCreation
import com.herry.test.app.base.BaseFragment

abstract class BaseView<V: MVPView<P>, P: MVPPresenter<V>>: BaseFragment(), MVPView<P>, MVPViewCreation<V, P> {

    override var presenter: P? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.presenter = this.presenter ?: run {
            val viewModel = MVPPresenterViewModelFactory.create(this, this)
            viewModel?.presenter?.apply {
                relaunched(viewModel.recreated)
            }
        }
        this.presenter?.onAttach(onCreatePresenterView()) ?: activity?.finishAfterTransition()
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

    override fun showViewLoading() {
        showLoading()
    }

    override fun hideViewLoading(success: Boolean) {
        hideLoading()
    }
}