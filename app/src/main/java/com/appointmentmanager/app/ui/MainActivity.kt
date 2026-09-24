package com.appointmentmanager.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.appointmentmanager.app.AppointmentManagerApplication
import com.appointmentmanager.app.R
import com.appointmentmanager.app.data.InvalidBackupFormatException
import com.appointmentmanager.app.ui.navigation.AppNavigation
import com.appointmentmanager.app.ui.theme.AppointmentManagerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: AppointmentViewModel

    private var selectedImportUri by mutableStateOf<Uri?>(null)

    private val exportDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            exportBackup(uri)
        }
    }

    private val importDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        selectedImportUri = uri
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // إذا رفض المستخدم الإذن، يستمر التطبيق بالعمل دون إشعارات.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application as AppointmentManagerApplication
        viewModel = ViewModelProvider(
            this,
            AppointmentViewModelFactory(
                repository = application.appointmentRepository,
                settingsDataStore = application.settingsDataStore
            )
        )[AppointmentViewModel::class.java]

        setContent {
            val savedDarkMode by viewModel.darkModeEnabled.collectAsState()
            val systemIsDark = isSystemInDarkTheme()
            val effectiveDarkMode = savedDarkMode ?: systemIsDark

            AppointmentManagerTheme(darkTheme = effectiveDarkMode) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl
                ) {
                    AppNavigation(
                        viewModel = viewModel,
                        systemIsDark = systemIsDark,
                        isDarkTheme = effectiveDarkMode,
                        onExport = {
                            exportDocumentLauncher.launch(
                                getString(R.string.export_file_name)
                            )
                        },
                        onImport = {
                            importDocumentLauncher.launch(
                                arrayOf(
                                    "application/json",
                                    "text/plain",
                                    "application/octet-stream"
                                )
                            )
                        }
                    )

                    val uriToConfirm = selectedImportUri
                    if (uriToConfirm != null) {
                        AlertDialog(
                            onDismissRequest = {
                                selectedImportUri = null
                            },
                            title = {
                                Text(
                                    stringResource(
                                        R.string.import_confirmation_title
                                    )
                                )
                            },
                            text = {
                                Text(
                                    stringResource(
                                        R.string.import_confirmation_message
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        selectedImportUri = null
                                        importBackup(uriToConfirm)
                                    }
                                ) {
                                    Text(stringResource(R.string.confirm))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        selectedImportUri = null
                                    }
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        )
                    }

                    LaunchedEffect(Unit) {
                        if (
                            Build.VERSION.SDK_INT >=
                            Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                }
            }
        }
    }

    private fun exportBackup(uri: Uri) {
        val application = application as AppointmentManagerApplication

        lifecycleScope.launch {
            try {
                application.backupManager.exportTo(uri)
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.export_success),
                    Toast.LENGTH_LONG
                ).show()
            } catch (_: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.export_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun importBackup(uri: Uri) {
        val application = application as AppointmentManagerApplication

        lifecycleScope.launch {
            try {
                application.backupManager.importFrom(uri)
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.import_success),
                    Toast.LENGTH_LONG
                ).show()
            } catch (_: InvalidBackupFormatException) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.invalid_backup_format),
                    Toast.LENGTH_LONG
                ).show()
            } catch (_: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.import_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
