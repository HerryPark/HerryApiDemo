package com.herry.test.app.intent.share

import android.content.Context
import android.provider.MediaStore
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.test.data.MediaFileInfoData
import com.herry.test.rx.RxCursorIterable
import io.reactivex.Observable


/**
 * Created by herry.park on 2020/06/11.
 **/
class ShareMediaListPresenter : ShareMediaListContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
    override fun onAttach(view: ShareMediaListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onLaunch(view: ShareMediaListContract.View, recreated: Boolean) {
        if (recreated) {
            return
        }

        // sets list items
        loadGifList()
    }

    private fun loadGifList() {
        subscribeObservable(
            getMediaContentsFromMediaStore()
            , {
                launch {
                    updateMediaList(it)
                }
            }
        )
    }

    private fun updateMediaList(list: MutableList<MediaFileInfoData>) {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *list.toTypedArray())

        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()
    }

    @Suppress("DEPRECATION")
    private fun getMediaContentsFromMediaStore(): Observable<MutableList<MediaFileInfoData>> {
        val context: Context? = view?.getViewContext()
        context ?: return Observable.empty()

        return Observable.fromCallable {
            val medias = mutableListOf<MediaFileInfoData>()

            val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)

            val selectionArgs = null
            val cursor = context.contentResolver?.query(
                MediaStore.Files.getContentUri("external"),
                arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_ADDED
                ),
                selection,
                selectionArgs,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
            )

            cursor?.let {
                RxCursorIterable.from(cursor).forEach { c ->
                    val id = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)) ?: ""
                    val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)) ?: ""
                    val mimeType = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)) ?: ""
                    val displayName =
                        c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)) ?: ""
                    val size = c.getInt(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
                    val date = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED))
                    medias.add(
                        MediaFileInfoData(
                            id = id,
                            mimeType = mimeType,
                            path = path,
                            name = displayName,
                            size = size,
                            date = date
                        )
                    )
                }

                it.close()
            }
            medias
        }
    }
}