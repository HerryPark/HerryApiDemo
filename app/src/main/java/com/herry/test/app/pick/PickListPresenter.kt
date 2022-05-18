package com.herry.test.app.pick

import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.BuildConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PickListPresenter : PickListContract.Presenter() {
    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    override fun onAttach(view: PickListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onLaunch(view: PickListContract.View, recreated: Boolean) {
        if (recreated) {
            return
        }

        loadList()
    }

    private fun loadList() {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        PickListContract.PickType.values().forEach {
            NodeHelper.addModel(nodes, it)
        }
        NodeHelper.upSert(this.nodes, nodes)

        this.nodes.endTransition()
    }

    override fun pick(type: PickListContract.PickType) {
        view?.onScreen(type)
//        when (type) {
//            PickListContract.PickType.PICK_MOVIE -> {}
//            PickListContract.PickType.PICK_PHOTO -> TODO()
//            PickListContract.PickType.TAKE_PHOTO -> TODO()
//            PickListContract.PickType.TAKE_MOVIE -> TODO()
//        }
    }

    @Throws(IOException::class)
    override fun getToTakeTempFile(type: PickListContract.PickType): File? {
        val context = view?.getViewContext() ?: return null

        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File? = when (type) {
            PickListContract.PickType.TAKE_PHOTO -> context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            PickListContract.PickType.TAKE_MOVIE -> context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            else -> null
        }
        storageDir ?: return null

        return when (type) {
            PickListContract.PickType.TAKE_PHOTO ->
                File.createTempFile(
                    "PHOTO_${timeStamp}_", /* prefix */
                    ".jpg", /* suffix */
                    storageDir /* directory */
                )
            PickListContract.PickType.TAKE_MOVIE ->
                File.createTempFile(
                    "VIDEO_${timeStamp}_", /* prefix */
                    ".mp4", /* suffix */
                    storageDir /* directory */
                )
            else -> null
        }
    }

    override fun getUriForFileProvider(file: File?): Uri? {
        file ?: return null
        val context = view?.getViewContext() ?: return null

        return FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    override fun picked(file: File, receivedUri: Uri?, type: PickListContract.PickType, success: Boolean) {

    }


    private fun deleteTempFile(file: File?) {
        if (file?.exists() == true) {
            file.delete()
        }
    }
}