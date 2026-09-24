package com.appointmentmanager.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appointmentmanager.app.R
import com.appointmentmanager.app.ui.AppointmentViewModel
import com.appointmentmanager.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: Long,
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (Long) -> Unit
) {
    val appointmentFlow = remember(appointmentId) {
        viewModel.observeAppointment(appointmentId)
    }
    val appointment by appointmentFlow.collectAsState(initial = null)
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appointment_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (appointment != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Text(
                            text = stringResource(R.string.edit),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Button(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text(
                            text = stringResource(R.string.delete),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        val currentAppointment = appointment
        if (currentAppointment == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.person_not_found))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = currentAppointment.fullName,
                    style = MaterialTheme.typography.headlineSmall
                )
                CopyableDetailRow(
                    label = stringResource(R.string.identity_number),
                    value = currentAppointment.identityNumber
                )
                DetailValueRow(
                    label = stringResource(R.string.birth_date),
                    value = currentAppointment.birthDate?.let(DateUtils::formatDate)
                        ?: stringResource(R.string.birth_date_not_set)
                )
                DetailValueRow(
                    label = stringResource(R.string.appointment_date),
                    value = DateUtils.formatDate(currentAppointment.appointmentDate)
                )
                CopyableDetailRow(
                    label = stringResource(R.string.mhrs_password),
                    value = currentAppointment.mhrsPassword
                )
                CopyableDetailRow(
                    label = stringResource(R.string.edevlet_password),
                    value = currentAppointment.eDevletPassword
                )
                CopyableDetailRow(
                    label = stringResource(R.string.enabiz_password),
                    value = currentAppointment.eNabizPassword
                )
                DetailValueRow(
                    label = stringResource(R.string.notes),
                    value = currentAppointment.notes.ifBlank {
                        stringResource(R.string.no_notes)
                    }
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_confirmation_title)) },
            text = { Text(stringResource(R.string.delete_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete(appointmentId)
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DetailValueRow(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun CopyableDetailRow(label: String, value: String) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(
                    text = value.ifBlank { stringResource(R.string.empty_value) },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            IconButton(
                onClick = {
                    copyToClipboard(context, label, value)
                }
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(
        context,
        context.getString(R.string.copied),
        Toast.LENGTH_SHORT
    ).show()
}
