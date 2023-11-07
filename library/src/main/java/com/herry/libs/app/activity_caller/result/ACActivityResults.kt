package com.herry.libs.app.activity_caller.result

import android.net.Uri
import com.herry.libs.app.activity_caller.module.ACNavigation

internal class LaunchIntentResults {
    var onResult: ((result: ACNavigation.Result) -> Unit)? = null
}

@Suppress("unused")
internal class RequestPermissionResults {
    var onResult: ((granted: Array<String>, denied: Array<String>, blocked: Array<String>) -> Unit)? = null
    val grantedPermissions = mutableListOf<String>()
    val deniedPermissions = mutableListOf<String>()
    val blockedPermissions = mutableListOf<String>()
}

internal class LaunchPickerResults {
    var onResult: ((uris: List<Uri>) -> Unit)? = null
}

internal class LaunchTakeResults {
    var onResult: ((success: Boolean) -> Unit)? = null
}
