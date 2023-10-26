package io.noone.app_nist

import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.SecureRandom

class GenerateRandom constructor(
    private val permissionHelper: PermissionHelper
) {

    private companion object {
        const val DIRECTORY_TYPE = "download"
        const val DIRECTORY_NAME = "Test NIST"
        const val FILE_NAME = "bytes.pi"
    }

    operator fun invoke(length: String, onGenerate: (String) -> Unit, onDenied: () -> Unit) {
        permissionHelper.checkFilesystemPermission(
            onGranted = {
                val bytestreamLength = length.toInt()
                if (bytestreamLength <= 0) throw Exception("wrong length")
                val random = SecureRandom.getInstanceStrong()
                val bytes = ByteArray(bytestreamLength)
                random.nextBytes(bytes)
                val file = createFileInAppDirectory(FILE_NAME)
                FileOutputStream(file).apply {
                    write(bytes)
                    close()
                }
                onGenerate.invoke(
                    file.path.substring(
                        file.path.indexOf(
                            DIRECTORY_TYPE,
                            ignoreCase = true
                        ), file.path.length
                    )
                )
            },
            onDenied = {
                onDenied.invoke()
            })
    }

    private fun createAppDirectoryInDownloads(): File {
        val downloadsDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDirectory = File(downloadsDirectory, DIRECTORY_NAME)

        if (!appDirectory.exists()) {
            val directoryCreated = appDirectory.mkdir()
            if (!directoryCreated) {
                throw Exception("Failed to create directory")
            }
        }

        return appDirectory
    }

    private fun createFileInAppDirectory(fileName: String): File {
        val file = File(createAppDirectoryInDownloads(), fileName)
        try {
            if (!file.exists()) {
                val fileCreated = file.createNewFile()
                if (!fileCreated) {
                    throw Exception("Failed to create file")
                }
            }
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        throw Exception("Failed to create file")
    }
}