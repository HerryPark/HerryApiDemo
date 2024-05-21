package com.herry.test.app.font.downloadable

import android.graphics.Typeface
import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.model.NodeRoot
import com.herry.test.app.base.mvp.BasePresenter

interface DownloadableFontContract {
    interface View: MVPView<Presenter> {
        val fontsRoot: NodeRoot
        fun onUpdateText(fontName: String, typeface: Typeface? = Typeface.DEFAULT)
        fun onLoading(show: Boolean)
        fun onFailed(reason: Int)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun selectFont(model: FontListItemForm.Model)
        abstract fun applyFont()
    }

}