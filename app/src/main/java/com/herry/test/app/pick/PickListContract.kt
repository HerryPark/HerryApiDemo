package com.herry.test.app.pick

import android.net.Uri
import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BasePresenter
import java.io.File

interface PickListContract {
    interface View : MVPView<Presenter>, INodeRoot {
        fun onScreen(type: PickType)
        fun onPicked(message: String)
    }

    abstract class Presenter : BasePresenter<View>() {
        abstract fun pick(type: PickType)
        abstract fun getToTakeTempFile(type: PickType): File?
        abstract fun getUriForFileProvider(file: File?): Uri?
        abstract fun picked(tempFile: File, picked: Uri?, type: PickType, success: Boolean)
    }

    enum class PickType {
        PICK_PHOTO,
        PICK_MOVIE,
        TAKE_PHOTO,
        TAKE_MOVIE
    }
}