package com.herry.test.app.gif.list

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.data.GifMediaFileInfoData
import com.herry.test.rx.RxCursorIterable
import io.reactivex.Observable


/**
 * Created by herry.park on 2020/06/11.
 **/
class GifListPresenter : GifListContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
    override fun onAttach(view: GifListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onLaunch(view: GifListContract.View, recreated: Boolean) {
        if (recreated) {
            return
        }

        // sets list items
        loadGifList()
    }

    private fun loadGifList() {
        subscribeObservable(
            getGifContentsFromMediaStore()
            , {
                launch {
                    updateGifList(it)
                }
            }
        )
    }

    private fun updateGifList(list: MutableList<GifMediaFileInfoData>) {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *list.toTypedArray())

        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    @SuppressLint("Range")
    @Suppress("DEPRECATION")
    private fun getGifContentsFromMediaStore(): Observable<MutableList<GifMediaFileInfoData>> {
        val context: Context? = view?.getViewContext()
        context ?: return Observable.empty()

        return Observable.fromCallable {
            val photos = mutableListOf<GifMediaFileInfoData>()
            val selection = MediaStore.Images.Media.MIME_TYPE + "=?"
            val selectionArgs = arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("gif"))
            val cursor = context.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.DATE_ADDED
                ),
                selection,
                selectionArgs,
                MediaStore.MediaColumns.DATE_ADDED + " DESC"
            )

            cursor?.let {
                RxCursorIterable.from(cursor).forEach { c ->
                    val id = c.getString(c.getColumnIndex(MediaStore.Images.Media._ID))
                    val path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA))
                    val mimeType = c.getString(c.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                    val displayName =
                        c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                    val size = c.getInt(c.getColumnIndex(MediaStore.Images.Media.SIZE))
                    val width = c.getInt(c.getColumnIndex(MediaStore.Video.Media.WIDTH))
                    val height = c.getInt(c.getColumnIndex(MediaStore.Video.Media.HEIGHT))
                    val date = c.getLong(c.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))
                    photos.add(
                        GifMediaFileInfoData(
                            id = id,
                            mimeType = mimeType,
                            path = path,
                            name = displayName,
                            size = size,
                            width = width,
                            height = height,
                            date = date
                        )
                    )
                }
            }
            photos
        }
    }

    override fun decode(content: GifMediaFileInfoData) {
        view?.onDetail(content)
    }
}