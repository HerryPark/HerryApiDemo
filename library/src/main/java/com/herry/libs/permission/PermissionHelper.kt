package com.herry.libs.permission

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
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
                if (!ApiHelper.hasAPI29()) {
                    // max sdk is 28
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                if (ApiHelper.hasAPI34()) {
                    add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                }
                add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }),
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        STORAGE_VISUAL_MEDIA(mutableListOf<String>().apply {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
            if (ApiHelper.hasAPI34()) {
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        }),
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        STORAGE_IMAGE_ONLY(mutableListOf<String>().apply {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            if (ApiHelper.hasAPI34()) {
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        }),
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        STORAGE_VIDEO_ONLY(mutableListOf<String>().apply {
            add(Manifest.permission.READ_MEDIA_VIDEO)
            if (ApiHelper.hasAPI34()) {
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }
        }),
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
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
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        NOTIFICATION(mutableListOf<String>().apply {
            add(Manifest.permission.POST_NOTIFICATIONS)
        });

        companion object {
            fun generate(permissions: MutableList<String>) : Type? = values().firstOrNull { it.permissions == permissions }
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

        if (ApiHelper.hasAPI34()) {
            // checks has partial permission for the visual media (READ_MEDIA_IMAGES or READ_MEDIA_VIDEO)
            if (grantedPermissions.contains(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                deniedPermissions.remove(Manifest.permission.READ_MEDIA_IMAGES)
                deniedPermissions.remove(Manifest.permission.READ_MEDIA_VIDEO)
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

    fun getAccessPermission(context: Context?, permission: String): Access {
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
                    == PackageManager.PERMISSION_GRANTED) {
                    // Partial access on Android 14+
                    Access.PARTIAL
                } else {
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

    fun createPermissionSettingScreenPopup(context: Context?, onCancel: ((dialog: DialogInterface) -> Unit)? = null): AppDialog? {
        context ?: return null

        return AppDialog(context).apply {
            setCancelable(false)
            setTitle("Setting permissions")
            setMessage("Permission settings are turned off and can not access those services.\n\nPlease turn in [Settings] > [authority].")
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                AppUtil.showAppInfoSettingScreen(context)
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