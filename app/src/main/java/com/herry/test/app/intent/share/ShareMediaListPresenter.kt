package com.herry.test.app.intent.share

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.herry.libs.helper.ApiHelper
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.libs.permission.PermissionHelper
import com.herry.test.data.MediaFileInfoData
import com.herry.test.widget.MediaSelectionForm
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference


/**
 * Created by herry.park on 2020/06/11.
 **/
class ShareMediaListPresenter : ShareMediaListContract.Presenter() {

    private val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()

    private val mediaType: AtomicReference<MediaSelectionForm.MediaType> = AtomicReference()
    override fun onAttach(view: ShareMediaListContract.View) {
        super.onAttach(view)

        view.root.beginTransition()
        NodeHelper.addNode(view.root, nodes)
        view.root.endTransition()
    }

    override fun onResume(view: ShareMediaListContract.View, state: ResumeState) {
        setMediaType(mediaType.get() ?: MediaSelectionForm.MediaType.IMAGES_ONLY)
    }

    private fun displayMediaPermission(type: MediaSelectionForm.MediaType, accessPermission: PermissionHelper.Access) {
        val model = MediaSelectionForm.Model(
            currentType = type,
            permissionType = when (accessPermission) {
                PermissionHelper.Access.FULL -> MediaSelectionForm.PermissionType.FULL
                PermissionHelper.Access.PARTIAL -> MediaSelectionForm.PermissionType.PARTIAL
                PermissionHelper.Access.DENIED -> MediaSelectionForm.PermissionType.DENIED
            })

        view?.onUpdatedMediaPermission(model)
    }

    private fun getRequestMediaPermissionType(type: MediaSelectionForm.MediaType): PermissionHelper.Type {
        return if (ApiHelper.hasAPI33()) {
            when (type) {
                MediaSelectionForm.MediaType.IMAGES_ONLY -> PermissionHelper.Type.STORAGE_IMAGE_ONLY
                MediaSelectionForm.MediaType.VIDEOS_ONLY -> PermissionHelper.Type.STORAGE_VIDEO_ONLY
                MediaSelectionForm.MediaType.VISUAL_ALL -> PermissionHelper.Type.STORAGE_VISUAL_MEDIA
                MediaSelectionForm.MediaType.AUDIOS_ONLY -> PermissionHelper.Type.STORAGE_AUDIO_ONLY
                MediaSelectionForm.MediaType.MEDIA_ALL -> PermissionHelper.Type.STORAGE_MEDIA_ALL
            }
        } else {
            PermissionHelper.Type.STORAGE_MEDIA_ALL
        }
    }

    override fun setMediaType(type: MediaSelectionForm.MediaType) {
        mediaType.set(type)

        val onRespondedPermissions: (permitted: PermissionHelper.Permitted) -> Unit = { permitted ->
            launch(LaunchWhenPresenter.LAUNCHED) {
                val context = view?.getViewContext() ?: return@launch
                val accessPermission = PermissionHelper.getAccessPermission(context, getRequestMediaPermissionType(type))
                displayMediaPermission(type, accessPermission)
                if (permitted == PermissionHelper.Permitted.GRANTED) {
                    loadMediaList(type, permitted)
                } else {
                    updateMediaList(mutableListOf(), permitted)
                }
            }
        }

        val requestPermissionType = getRequestMediaPermissionType(type)

        view?.onRequestPermission(
            type = requestPermissionType,
            onGranted = {
                onRespondedPermissions(PermissionHelper.Permitted.GRANTED)
            },
            onDenied = {
                onRespondedPermissions(PermissionHelper.Permitted.DENIED)
            },
            onBlocked = {
                onRespondedPermissions(PermissionHelper.Permitted.BLOCKED)
            }
        )
    }

    private fun loadMediaList(mediaType: MediaSelectionForm.MediaType, permitted: PermissionHelper.Permitted) {
        subscribeObservable(
            observable = getMediaContentsFromMediaStore(mediaType = mediaType),
            onNext = {
                updateMediaList(it, permitted)
            }
        )
    }

    private fun updateMediaList(list: MutableList<MediaFileInfoData>, permitted: PermissionHelper.Permitted) {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *list.toTypedArray())

        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()

        view?.onLoadedList(count = list.size, permitted = permitted)
    }

    private fun getMediaContentsFromMediaStore(mediaType: MediaSelectionForm.MediaType): Observable<MutableList<MediaFileInfoData>> {
        val context: Context? = view?.getViewContext()
        context ?: return Observable.empty()

        return Observable.fromCallable {
            val medias = mutableListOf<MediaFileInfoData>()

            // List of columns we want to fetch
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
            )

            val collectionUri = if (ApiHelper.hasAPI29()) {
                // This allows us to query all the device storage volumes instead of the primary only
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Files.getContentUri("external")
            }

            val selection = when (mediaType) {
                MediaSelectionForm.MediaType.IMAGES_ONLY -> {
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
                }
                MediaSelectionForm.MediaType.VIDEOS_ONLY -> {
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}"
                }
                MediaSelectionForm.MediaType.VISUAL_ALL -> {
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
                        .plus(" OR ")
                        .plus("${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}")
                }
                MediaSelectionForm.MediaType.AUDIOS_ONLY -> {
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO}"
                }
                MediaSelectionForm.MediaType.MEDIA_ALL -> {
                    "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"
                        .plus(" OR ")
                        .plus("${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}")
                        .plus(" OR ")
                        .plus("${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO}")
                }
            }

            val selectionArgs = null
            context.contentResolver?.query(
                // Queried collection
                collectionUri,
                // List of columns we want to fetch
                projection,
                // Filtering parameters (in this case [MEDIA_TYPE] column)
                selection,
                selectionArgs,
                // Sorting order (recent -> older files)
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(collectionUri, cursor.getLong(idColumn))
                    val name = cursor.getString(displayNameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    medias.add(MediaFileInfoData(
                        id = id.toString(),
                        mimeType = mimeType,
                        path = uri.toString(),
                        name = name,
                        size = size,
                        date = dateAdded
                    ))
                }
            }

            medias
        }
    }
}