package com.herry.libs.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.herry.libs.helper.ApiHelper
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.view.dialog.AppDialog

@Suppress("MemberVisibilityCanBePrivate")
object PermissionHelper {
    enum class Type(val permissions: MutableList<String>) {
        // API 34 (OS v14) partial permission
        // Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        STORAGE_MEDIA_ALL(mutableListOf<String>().apply {
            if (!ApiHelper.hasAPI33()) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                // When the 'android:maxSdkVersion="28"' of 'Manifest.permission.WRITE_EXTERNAL_STORAGE'
                // is set on the  Huawei OS 10 device, the media content to external storage can't despite that
                // the maxSdkVersion setting is the Android official guide. So, it is not set.
                // Also, if the 'File Open Error No.' is a 13, it means that the file accessing permission does not have.
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                if (ApiHelper.hasAPI34()) {
                    add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                }
                add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }),
        @RequiresApi(api = ApiHelper.API33)
        STORAGE_VISUAL_MEDIA(mutableListOf<String>().apply {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
            if (ApiHelper.hasAPI34()) {
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        }),
        @RequiresApi(api = ApiHelper.API33)
        STORAGE_IMAGE_ONLY(mutableListOf<String>().apply {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            if (ApiHelper.hasAPI34()) {
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        }),
        @RequiresApi(api = ApiHelper.API33)
        STORAGE_VIDEO_ONLY(mutableListOf<String>().apply {
            add(Manifest.permission.READ_MEDIA_VIDEO)
            if (ApiHelper.hasAPI34()) {
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        }),
        @RequiresApi(api = ApiHelper.API33)
        STORAGE_AUDIO_ONLY(mutableListOf<String>().apply {
            add(Manifest.permission.READ_MEDIA_AUDIO)
        }),
        CAMERA(mutableListOf<String>().apply {
            add(Manifest.permission.CAMERA)
        }),
        CAMCORDER(mutableListOf<String>().apply {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
        }),
        VOICE_RECORD(mutableListOf<String>().apply {
            add(Manifest.permission.RECORD_AUDIO)
        }),
        @RequiresApi(api = ApiHelper.API33)
        NOTIFICATION(mutableListOf<String>().apply {
            add(Manifest.permission.POST_NOTIFICATIONS)
        });

        companion object {
            fun generate(permissions: MutableList<String>) : Type? = entries.firstOrNull { it.permissions == permissions }
        }
    }

    fun hasPermission(context: Context?, type: Type): Boolean {
        return hasPermission(context, type.permissions.toTypedArray())
    }

    fun hasPermission(context: Context?, permissions: Array<String>): Boolean {
        context ?: return false

        val grantedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()
        permissions.forEach { checkPermission ->
            if (ContextCompat.checkSelfPermission(context, checkPermission) == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(checkPermission)
            } else {
                deniedPermissions.add(checkPermission)
            }
        }

        return deniedPermissions.isEmpty()
    }

    enum class Permitted {
        GRANTED,
        DENIED,
        BLOCKED
    }

    enum class Access {
        FULL,
        PARTIAL,
        DENIED
    }

    fun getAccessPermission(context: Context?, type: Type): Access {
        context ?: return Access.DENIED
        if (type.permissions.isEmpty()) return Access.DENIED

        val accesses = HashMap<String, Access>()
        type.permissions.forEach { permission ->
            accesses[permission] = getAccessPermission(context, permission)
        }

        return if (accesses.values.contains(Access.DENIED)) {
            Access.DENIED
        } else if (accesses.values.contains(Access.PARTIAL)) {
            Access.PARTIAL
        } else {
            Access.FULL
        }
    }

    private fun getAccessPermission(context: Context?, permission: String): Access {
        context ?: return Access.DENIED

        val access = ContextCompat.checkSelfPermission(context, permission)
        when (permission) {
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES -> {
                return if (access == PackageManager.PERMISSION_GRANTED) {
                    // Full access on Android 13+
                    Access.FULL
                } else if (ApiHelper.hasAPI34() &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    // Partial access on Android 14+
                    Access.PARTIAL
                }
                else {
                    Access.DENIED
                }
            }
        }

        return if (access == PackageManager.PERMISSION_GRANTED) {
            Access.FULL
        } else {
            Access.DENIED
        }
    }

    fun createPermissionSettingScreenPopup(activity: Activity?, onCancel: ((dialog: DialogInterface) -> Unit)? = null): AppDialog? {
        activity ?: return null

        return AppDialog(activity).apply {
            setCancelable(false)
            setTitle("Setting permissions")
            setMessage("Permission settings are turned off and can not access those services.\n\nPlease turn in [Settings] > [authority].")
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                AppUtil.showAppInfoSettingScreen(activity)
            }
            setNegativeButton(android.R.string.cancel,
                if (onCancel != null) {
                    DialogInterface.OnClickListener { dialog, _ ->
                        onCancel.invoke(dialog)
                    }
                } else null
            )
        }
    }
}