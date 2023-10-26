package io.noone.app_nist

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged

class MainActivity : AppCompatActivity() {

    private lateinit var generateRandom: GenerateRandom
    private lateinit var toast: Toast

    private companion object {
        private const val PERMISSION_REQUEST_CODE = 42
        private const val FILE_IS_SAVED = "The file is saved in"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        generateRandom = GenerateRandom(PermissionHelper(this))
        toast = Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG)

        findViewById<Button>(R.id.btGenerate).setOnClickListener {
            currentFocus?.hideKeyboard()
            generateRandom(findViewById<EditText>(R.id.etLength).text.toString(),
                onGenerate = { path ->
                    showMessage("$FILE_IS_SAVED \"$path\"")
                },
                onDenied = {
                    requestFilesystemPermission()
                })
        }

        findViewById<EditText>(R.id.etLength).doAfterTextChanged {
            findViewById<Button>(R.id.btGenerate).isEnabled =
                it.toString().isNotBlank() && it.toString() != "0"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            generateRandom(findViewById<EditText>(R.id.etLength).text.toString(),
                onGenerate = { path ->
                    showMessage("$FILE_IS_SAVED \"$path\"")
                },
                onDenied = {
                    requestFilesystemPermission()
                })
        } else {
            showMessage("permission denied!")
        }
    }

    private fun requestFilesystemPermission() {
        requestPermissions(
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun showMessage(string: String?) {
        string?.let {
            toast.setText(it)
        } ?: toast.setText(R.string.default_error_message)
        toast.show()
    }

    private fun View.hideKeyboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowInsetsController?.hide(WindowInsets.Type.ime())
        } else {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            findFocus()?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
        }
        clearFocus()
    }
}