package io.noone.app_nist

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class PermissionHelper(
    private val context: Context
) {

    fun checkFilesystemPermission(onGranted: () -> Unit, onDenied: () -> Unit) =
        if (checkFilesystemPermission()) onGranted() else onDenied()

    private fun checkFilesystemPermission(): Boolean {
        return isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> {
                context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            }

            else -> true
        }
    }
}