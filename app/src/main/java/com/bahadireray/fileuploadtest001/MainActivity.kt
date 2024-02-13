package com.bahadireray.fileuploadtest001

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
      openFilePicker()
    } else {
      Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      Surface(color = MaterialTheme.colorScheme.background) {
        FilePickerScreen()
      }
    }
  }

  @Composable
  fun FilePickerScreen() {
    var permissionGranted by remember { mutableStateOf(false) }

    Column(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Button(onClick = {
        if (checkPermission()) {
          permissionGranted = true
          openFilePicker()
        } else {
          requestPermission()
        }
      }) {
        Text(text = "Select File")
      }
    }
  }

  private fun checkPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.READ_EXTERNAL_STORAGE
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
  }

  private fun requestPermission() {
    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
  }

  private fun openFilePicker() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "application/pdf"
    }
    startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
      data?.data?.let { uri ->
        val filePath = getRealPathFromURI(uri)
        Toast.makeText(this, "Selected file path: $filePath", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun getRealPathFromURI(uri: Uri): String? {
    val cursor = contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
      it.moveToFirst()
      val columnIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
      it.getString(columnIndex)
    } ?: uri.path
  }

  companion object {
    private const val REQUEST_CODE_PICK_FILE = 1001
  }
}