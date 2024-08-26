package com.herry.libs.mvp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

class MVPPresenterViewModelFactory<V: MVPView<P>, P: MVPPresenter<V>>(private val view: MVPViewCreation<V, P>) : ViewModelProvider.Factory {

    private var created = false

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
            val presenter = view.onCreatePresenter()
            if (presenter != null) {
                if (modelClass.isInstance(presenter)) {
                    created = true
                    modelClass.cast(presenter)
                } else {
                    modelClass.getDeclaredConstructor().newInstance()
                }
            } else {
                throw IllegalArgumentException()
            }
        } catch (ex: Exception) {
            throw ex
        }
    }

    companion object {
        fun <V: MVPView<P>, P: MVPPresenter<V>> create(owner: ViewModelStoreOwner, viewModelView: MVPViewCreation<V, P>): MVPPresenterViewModel<V, P>? {
            return try {
                val factory = MVPPresenterViewModelFactory(viewModelView)

                @Suppress("UNCHECKED_CAST")
                val viewModel = ViewModelProvider(owner, factory)[MVPPresenter::class.java] as? P
                if (viewModel != null) {
                    MVPPresenterViewModel(viewModel, !factory.created)
                } else {
                    null
                }
            } catch (ex: Exception) {
                null
            }
        }
    }
}

data class MVPPresenterViewModel<V: MVPView<P>, P: MVPPresenter<V>>(val presenter: P?, val recreated: Boolean)
