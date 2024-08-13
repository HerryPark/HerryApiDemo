package com.herry.test.app.intent.share

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.herry.libs.helper.ApiHelper
import com.herry.libs.nodeview.model.Node
import com.herry.libs.nodeview.model.NodeHelper
import com.herry.libs.nodeview.model.NodeModelGroup
import com.herry.libs.permission.PermissionHelper
import com.herry.test.data.MediaFileInfoData
import com.herry.test.widget.MediaAccessPermissionNoticeModel
import com.herry.test.widget.MediaAccessPermissionNoticeType
import com.herry.test.widget.MediaSelectionForm
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicBoolean
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
        // update access permission notice
        if (ApiHelper.hasAPI33()) updateAccessPermissionNotice(state)

        setMediaType(mediaType.get() ?: MediaSelectionForm.MediaType.IMAGES_ONLY)
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
        if (mediaType.get() != type) {
            currentAccessPermission.set(null)
            mediaType.set(type)
            view?.onUpdatedMediaSelectionType(MediaSelectionForm.Model(currentType = type))
        }

        requestPermissionType = getRequestMediaPermissionType(type)
        loadMediaList(type)
    }

    override fun selectLimitedVisualMedia() {
        if (ApiHelper.hasAPI34()) {
            choiceLimitedVisualMedia(
                onDone = {
                    loadMediaList(mediaType.get())
                }
            )
        }
    }

    private fun loadMediaList(mediaType: MediaSelectionForm.MediaType) {
        checkAccessPermissions(isNoticeEnabled = ApiHelper.hasAPI33()) {
            subscribeObservable(
                observable = getMediaContentsFromMediaStore(mediaType = mediaType),
                onNext = {
                    updateMediaList(it)
                }
            )
        }
    }

    private fun updateMediaList(list: MutableList<MediaFileInfoData>) {
        view?.getViewContext() ?: return

        this.nodes.beginTransition()

        val nodes: Node<NodeModelGroup> = NodeHelper.createNodeGroup()
        NodeHelper.addModels(nodes, *list.toTypedArray())

        NodeHelper.upsert(this.nodes, nodes)
        this.nodes.endTransition()

        view?.onLoadedList(count = list.size)
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

    private val currentAccessPermission: AtomicReference<PermissionHelper.Access?> = AtomicReference(null)
    private val isAccessPermissionNoticeVisible: AtomicBoolean = AtomicBoolean(false)

    private var requestPermissionType: PermissionHelper.Type? = null

    @RequiresApi(api = ApiHelper.API33)
    private fun isCheckedAccessPermission(): Boolean = currentAccessPermission.get() != null

    @RequiresApi(api = ApiHelper.API33)
    private fun isChangedAccessPermission(): Boolean {
        val context = view?.getViewContext() ?: return false

        val requestPermissionType = requestPermissionType ?: return false

        val currentAccessPermission = currentAccessPermission.get()
        return when {
            currentAccessPermission == null -> false
            else -> currentAccessPermission != PermissionHelper.getAccessPermission(context, requestPermissionType)
        }
    }

    @RequiresApi(api = ApiHelper.API34)
    private fun choiceLimitedVisualMedia(onDone: () -> Unit, onFailure: (() -> Unit)? = null) {
        val context = view?.getViewContext()
        if (context == null) {
            onFailure?.invoke()
            return
        }

        val requestPermissionType = requestPermissionType
        if (requestPermissionType == null || !requestPermissionType.permissions.contains(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
            onFailure?.invoke()
            return
        }

        if (PermissionHelper.getAccessPermission(context, requestPermissionType) == PermissionHelper.Access.PARTIAL) {
            // request permission
            view?.onRequestPermission(
                type = requestPermissionType,
                onGranted = {
                    launch(LaunchWhenPresenter.LAUNCHED) {
                        updateAccessPermissionNotice(true)
                        onDone()
                    }
                }
            )
        }
    }

    private fun checkAccessPermissions(isNoticeEnabled: Boolean, onDone: (permitted: PermissionHelper.Permitted) -> Unit) {
        val context = view?.getViewContext() ?: return

        val requestPermissionType = requestPermissionType
        if (requestPermissionType == null) {
            onDone(PermissionHelper.Permitted.GRANTED)
            return
        }

        val hasPermission = PermissionHelper.hasPermission(context, requestPermissionType.permissions.toTypedArray())
        // check permission
        if (!hasPermission && currentAccessPermission.get() == null) {
            val onRespondedPermissions: (permitted: PermissionHelper.Permitted) -> Unit = { permitted ->
                launch(LaunchWhenPresenter.LAUNCHED) {
                    if (ApiHelper.hasAPI33() && isNoticeEnabled) {
                        updateAccessPermissionNotice()
                    }
                    onDone(permitted)
                }
            }

            // gets current permission for the "requestPermissionType"
            val currentAccessPermission = PermissionHelper.getAccessPermission(context, requestPermissionType)
            when (currentAccessPermission) {
                PermissionHelper.Access.FULL -> { onRespondedPermissions(PermissionHelper.Permitted.GRANTED) }
                PermissionHelper.Access.PARTIAL -> {
                    onRespondedPermissions(PermissionHelper.Permitted.GRANTED)
                }
                PermissionHelper.Access.DENIED -> {
                    // request permission
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
            }
        } else {
            onDone(PermissionHelper.Permitted.GRANTED)
        }
    }

    @RequiresApi(api = ApiHelper.API33)
    private fun updateAccessPermissionNotice(state: ResumeState) {
        // checks changed permission
        if (isCheckedAccessPermission()) {
            if (isChangedAccessPermission()) {
                updateAccessPermissionNotice(reCheckAccessPermission = true)
            } else if (state.isLaunch()) {
                updateAccessPermissionNotice(reCheckAccessPermission = false)
            }
        }
    }

    @RequiresApi(api = ApiHelper.API33)
    private fun updateAccessPermissionNotice(reCheckAccessPermission: Boolean = true) {
        val context = view?.getViewContext() ?: return

        val requestPermissionType = requestPermissionType ?: return
        val accessNoticeType: MediaAccessPermissionNoticeType = if (requestPermissionType == PermissionHelper.Type.STORAGE_AUDIO_ONLY) {
            MediaAccessPermissionNoticeType.AUDIO
        } else {
            MediaAccessPermissionNoticeType.VISUAL_MEDIA
        }

        if (reCheckAccessPermission) {
            val accessPermission = PermissionHelper.getAccessPermission(context, requestPermissionType)
            currentAccessPermission.set(accessPermission)

            val isShowNotice = when (accessPermission) {
                PermissionHelper.Access.FULL -> false
                PermissionHelper.Access.PARTIAL,
                PermissionHelper.Access.DENIED -> true
            }

            isAccessPermissionNoticeVisible.set(isShowNotice)

            // sets permission
            view?.onUpdatedAccessPermission(
                model = MediaAccessPermissionNoticeModel(
                    type = accessNoticeType,
                    access = accessPermission
                ),
                isShow = isShowNotice
            )
        } else {
            currentAccessPermission.get()?.let { currentAccessPermission ->
                view?.onUpdatedAccessPermission(
                    model = MediaAccessPermissionNoticeModel(
                        type = accessNoticeType,
                        access = currentAccessPermission
                    ),
                    isShow = isAccessPermissionNoticeVisible.get()
                )
            }
        }
    }

    override fun setAccessPermissionNoticeVisible(visible: Boolean) {
        isAccessPermissionNoticeVisible.set(visible)
    }
}