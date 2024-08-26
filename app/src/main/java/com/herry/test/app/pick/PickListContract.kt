package com.herry.test.app.pick

import android.net.Uri
import com.herry.libs.mvp.MVPView
import com.herry.libs.nodeview.INodeRoot
import com.herry.test.app.base.mvp.BaseMVPPresenter
import java.io.File

interface PickListContract {
    interface View : MVPView<Presenter>, INodeRoot {
        fun onScreen(type: PickType)
        fun onPicked(message: String)
    }

    abstract class Presenter : BaseMVPPresenter<View>() {
        abstract fun pick(type: PickType)
        abstract fun getToTakeTempFile(type: PickType): File?
        abstract fun getUriForFileProvider(file: File?): Uri?
        abstract fun picked(tempFile: File, picked: Uri?, type: PickType, success: Boolean)
    }

    enum class PickType {
        PICK_PHOTO,
        PICK_VIDEO,
        PICK_PHOTO_AND_VIDEO,
        TAKE_PHOTO,
        TAKE_VIDEO
    }
}