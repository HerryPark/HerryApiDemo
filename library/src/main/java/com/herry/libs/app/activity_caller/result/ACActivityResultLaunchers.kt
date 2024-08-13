package com.herry.libs.app.activity_caller.result

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.herry.libs.app.activity_caller.module.ACNavigation
import com.herry.libs.app.nav.NavMovement
import com.herry.libs.helper.ApiHelper
import kotlin.jvm.Throws

class ACActivityResultLaunchers(var activity: ComponentActivity) {

    internal class ActivityResultViewModel : ViewModel() {
        val launchIntentResult = LaunchIntentResults()
        val requestPermissionResults = RequestPermissionResults()
        val launchPickerResult = LaunchPickerResults()
        val launchTakeResult = LaunchTakeResults()
    }

    private val activityResultViewModel: ActivityResultViewModel = ViewModelProvider(activity)[ActivityResultViewModel::class.java]

    private val activityLauncher: ActivityResultLauncher<Intent> = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        onLaunchIntentResult(activityResult)
    }

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantResults: Map<String, Boolean> ->
        onRequestPermissionResult(grantResults)
    }

    private val pickerLauncher: ActivityResultLauncher<PickVisualMediaRequest> = activity.registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onLaunchPickerResult(mutableListOf<Uri>().apply {
            if (uri != null) {
                this.add(uri)
            }
        })
    }

    private val takeLauncher: ActivityResultLauncher<TakeMediaRequest> = activity.registerForActivityResult(
        ACActivityResultContracts.TakeMedia()
    ) { result ->
        onLaunchTakeResult(result)
    }

    fun processRequestPermission(
        permissions: Array<String>,
        onGranted: ((permission: Array<String>) -> Unit)? = null,
        onDenied: ((permission: Array<String>) -> Unit)? = null,
        onBlocked: ((permission: Array<String>) -> Unit)? = null
    ) {
        if (permissions.isEmpty()) {
            onGranted?.let { it(permissions) }
            return
        }

        activityResultViewModel.requestPermissionResults.onResult = { granted: Array<String>, denied: Array<String>, blocked: Array<String> ->
            when {
                denied.isNotEmpty() -> onDenied?.let { it(denied) }
                blocked.isNotEmpty() -> onBlocked?.let { it(blocked) }
                else -> onGranted?.let { it(granted) }
            }
        }

        val grantedPermissions = mutableListOf<String>()
        val requestPermissions = mutableListOf<String>()
        for (permission in permissions) {
            if (TextUtils.isEmpty(permission)) {
                continue
            }

            val checkedPermission = activity.checkSelfPermission(permission)
            if (checkedPermission == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permission)
            } else {
                requestPermissions.add(permission)
            }
        }

        if (requestPermissions.isNotEmpty()) {
            activityResultViewModel.requestPermissionResults.apply {
                this.grantedPermissions.clear()
                this.grantedPermissions.addAll(grantedPermissions)
            }
            requestPermissionLauncher.launch(requestPermissions.toTypedArray())
        } else {
            activityResultViewModel.requestPermissionResults.onResult?.invoke(
                grantedPermissions.toTypedArray(),
                arrayOf(),
                arrayOf()
            )

            activityResultViewModel.requestPermissionResults.onResult = null
        }
    }

    private fun onRequestPermissionResult(grantResults: Map<String, Boolean>) {
        val permissionResults = activityResultViewModel.requestPermissionResults

        if (grantResults.isEmpty()) {
            return
        }

        val grantedPermissions = mutableListOf<String>().apply {
            addAll(permissionResults.grantedPermissions)
        }
        val deniedPermissions = mutableListOf<String>()
        val blockedPermissions = mutableListOf<String>()

        grantResults.keys.forEach { permission ->
            val isGranted = grantResults[permission] == true
            if (isGranted) {
                grantedPermissions.add(permission)
            } else {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    // denied
                    deniedPermissions.add(permission)
                } else {
                    // blocked
                    blockedPermissions.add(permission)
                }
            }
        }

        if (ApiHelper.hasAPI34()) {
            // checks has partial permission for the visual media (READ_MEDIA_IMAGES or READ_MEDIA_VIDEO)
            if (grantedPermissions.contains(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                deniedPermissions.remove(Manifest.permission.READ_MEDIA_IMAGES)
                deniedPermissions.remove(Manifest.permission.READ_MEDIA_VIDEO)
                blockedPermissions.remove(Manifest.permission.READ_MEDIA_IMAGES)
                blockedPermissions.remove(Manifest.permission.READ_MEDIA_VIDEO)
            }
        }

        permissionResults.onResult?.invoke(
            grantedPermissions.toTypedArray(),
            deniedPermissions.toTypedArray(),
            blockedPermissions.toTypedArray()
        )

        activityResultViewModel.requestPermissionResults.onResult = null
    }

    @Throws(ActivityNotFoundException::class)
    fun processLaunchActivity(intent: Intent, options: ActivityOptionsCompat?, onResult: ((result: ACNavigation.Result) -> Unit)?) {
        activityResultViewModel.launchIntentResult.onResult = onResult

        activityLauncher.launch(intent, options)
    }

    private fun onLaunchIntentResult(activityResult: ActivityResult) {
        activityResultViewModel.launchIntentResult.onResult?.invoke(
            ACNavigation.Result(
                activity,
                activityResult.resultCode,
                activityResult.data,
                activityResult.data?.getBundleExtra(NavMovement.NAV_BUNDLE)
            )
        )

        activityResultViewModel.launchIntentResult.onResult = null
    }

    @Throws(ActivityNotFoundException::class)
    fun processLaunchPicker(request: PickVisualMediaRequest, onResult: ((uris: List<Uri>) -> Unit)?) {
        activityResultViewModel.launchPickerResult.onResult = onResult

        pickerLauncher.launch(request)
    }

    private fun onLaunchPickerResult(uris: List<Uri>) {
        activityResultViewModel.launchPickerResult.onResult?.invoke(uris)
    }

    @Throws(ActivityNotFoundException::class)
    fun processLaunchTake(request: TakeMediaRequest, onResult: ((success: Boolean) -> Unit)?) {
        activityResultViewModel.launchTakeResult.onResult = onResult

        takeLauncher.launch(request)
    }

    private fun onLaunchTakeResult(success: Boolean) {
        activityResultViewModel.launchTakeResult.onResult?.invoke(success)
    }

    fun unregisterAll() {
        requestPermissionLauncher.unregister()
        activityLauncher.unregister()
        pickerLauncher.unregister()
        takeLauncher.unregister()
    }
}