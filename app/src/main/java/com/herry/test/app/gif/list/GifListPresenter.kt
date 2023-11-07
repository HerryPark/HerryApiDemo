package com.herry.test.app.gif.list

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.herry.libs.helper.ApiHelper
import com.herry.libs.log.Trace
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.libs.permission.PermissionHelper
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

    override fun onResume(view: GifListContract.View, state: ResumeState) {
        checkPermission(
            onGranted = {
                // sets list items
                loadGifList()
            },
            onDenied = {
                updateGifList(mutableListOf())
            }
        )
    }

    private fun checkPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        view?.onCheckPermission(
            type = if (ApiHelper.hasAPI33()) PermissionHelper.Type.STORAGE_IMAGE_ONLY else PermissionHelper.Type.STORAGE_MEDIA_ALL,
            onGranted = {
                launch(LaunchWhenPresenter.LAUNCHED) {
                    onGranted()
                }
            },
            onDenied = {
                launch(LaunchWhenPresenter.LAUNCHED) {
                    onDenied()
                }
            })
    }

    private fun loadGifList() {
        subscribeObservable(
            observable = getGifContentsFromMediaStore(),
            onNext = {
                updateGifList(it)
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

        view?.onLoadedList(count = list.size)
    }

    @SuppressLint("Range")
    private fun getGifContentsFromMediaStore(): Observable<MutableList<GifMediaFileInfoData>> {
        val context: Context? = view?.getViewContext()
        context ?: return Observable.empty()

        return Observable.fromCallable {
            val photos = mutableListOf<GifMediaFileInfoData>()
            val collectionUri = if (ApiHelper.hasAPI29()) {
                // Query all the device storage volumes instead of the primary only
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.DATE_ADDED
            )
            val selection = MediaStore.Images.Media.MIME_TYPE + "=?"
            val selectionArgs = arrayOf(MimeTypeMap.getSingleton().getMimeTypeFromExtension("gif"))

            context.contentResolver?.query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                MediaStore.MediaColumns.DATE_ADDED + " DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val addedDateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                RxCursorIterable.from(cursor).forEach { c ->
                    val id = c.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(collectionUri, id)
                    val displayName = c.getString(displayNameColumn)
                    val size = c.getLong(sizeColumn)
                    val mimeType = c.getString(mimeTypeColumn)
                    val width = c.getInt(widthColumn)
                    val height = c.getInt(heightColumn)
                    val date = c.getLong(addedDateColumn)

                    val path = uri.path ?: ""

                    photos.add(
                        GifMediaFileInfoData(
                            id = id.toString(),
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