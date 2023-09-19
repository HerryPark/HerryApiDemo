package com.herry.test.app.permission

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import com.herry.libs.util.AppUtil
import com.herry.libs.widget.view.dialog.AppDialog
import com.herry.test.widget.Popup

@Suppress("MemberVisibilityCanBePrivate")
object PermissionHelper {
    enum class Type(val permissions: MutableList<String>) {
        STORAGE(mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)),
        CAMERA(mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)),
        CAMCORDER(mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)),
        VOICE_RECORD(mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO));

        companion object {
            fun generate(permissions: MutableList<String>) : Type? = values().firstOrNull { it.permissions == permissions }
        }
    }

    fun hasPermission(context: Context?, mode: Type): Boolean {
        return hasPermission(context, mode.permissions.toTypedArray())
    }

    fun hasPermission(context: Context?, permissions: Array<String>): Boolean {
        context ?: return false
        return permissions.firstOrNull { context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED } == null
    }

    fun createPermissionSettingScreenPopup(context: Context?, onCancel: ((dialog: DialogInterface) -> Unit)? = null): AppDialog? {
        context ?: return null

        return Popup(context).apply {
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