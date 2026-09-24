package com.appointmentmanager.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.appointmentmanager.app.R
import com.appointmentmanager.app.data.AppointmentEntity
import com.appointmentmanager.app.ui.AppointmentViewModel
import com.appointmentmanager.app.util.DateUtils
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    appointmentId: Long?,
    viewModel: AppointmentViewModel,
    onBack: () -> Unit,
    onSave: (AppointmentEntity) -> Unit
) {
    val appointmentFlow = remember(appointmentId) {
        if (appointmentId == null) {
            flowOf<AppointmentEntity?>(null)
        } else {
            viewModel.observeAppointment(appointmentId)
        }
    }

    val appointment by appointmentFlow.collectAsState(initial = null)

    if (appointmentId != null && appointment == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.edit_appointment))
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.loading))
            }
        }
        return
    }

    AppointmentForm(
        appointment = appointment,
        onBack = onBack,
        onSave = onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentForm(
    appointment: AppointmentEntity?,
    onBack: () -> Unit,
    onSave: (AppointmentEntity) -> Unit
) {
    var fullName by remember(appointment?.id) {
        mutableStateOf(appointment?.fullName.orEmpty())
    }
    var identityNumber by remember(appointment?.id) {
        mutableStateOf(appointment?.identityNumber.orEmpty())
    }
    var mhrsPassword by remember(appointment?.id) {
        mutableStateOf(appointment?.mhrsPassword.orEmpty())
    }
    var eDevletPassword by remember(appointment?.id) {
        mutableStateOf(appointment?.eDevletPassword.orEmpty())
    }
    var eNabizPassword by remember(appointment?.id) {
        mutableStateOf(appointment?.eNabizPassword.orEmpty())
    }
    var birthDate by remember(appointment?.id) {
        mutableStateOf(appointment?.birthDate)
    }
    var notes by remember(appointment?.id) {
        mutableStateOf(appointment?.notes.orEmpty())
    }
    var appointmentDate by remember(appointment?.id) {
        mutableStateOf(appointment?.appointmentDate)
    }

    var showBirthDatePicker by remember {
        mutableStateOf(false)
    }
    var showAppointmentDatePicker by remember {
        mutableStateOf(false)
    }
    var validationAttempted by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (appointment == null) {
                                R.string.add_appointment
                            } else {
                                R.string.edit_appointment
                            }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(stringResource(R.string.full_name))
                },
                isError = validationAttempted && fullName.isBlank()
            )

            if (validationAttempted && fullName.isBlank()) {
                Text(
                    text = stringResource(R.string.required_name_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = identityNumber,
                onValueChange = {
                    identityNumber = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text(stringResource(R.string.identity_number))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            PasswordInput(
                label = stringResource(R.string.mhrs_password),
                value = mhrsPassword,
                onValueChange = {
                    mhrsPassword = it
                }
            )

            PasswordInput(
                label = stringResource(R.string.edevlet_password),
                value = eDevletPassword,
                onValueChange = {
                    eDevletPassword = it
                }
            )

            PasswordInput(
                label = stringResource(R.string.enabiz_password),
                value = eNabizPassword,
                onValueChange = {
                    eNabizPassword = it
                }
            )

            DateSelectionButton(
                label = stringResource(R.string.birth_date),
                selectedDateMillis = birthDate,
                notSetText = stringResource(R.string.birth_date_not_set),
                onClick = {
                    showBirthDatePicker = true
                }
            )

            DateSelectionButton(
                label = stringResource(R.string.appointment_date),
                selectedDateMillis = appointmentDate,
                notSetText = stringResource(R.string.appointment_date_not_set),
                onClick = {
                    showAppointmentDatePicker = true
                }
            )

            if (validationAttempted && appointmentDate == null) {
                Text(
                    text = stringResource(
                        R.string.required_appointment_date_error
                    ),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = {
                    Text(stringResource(R.string.notes))
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    validationAttempted = true

                    if (
                        fullName.isNotBlank() &&
                        appointmentDate != null
                    ) {
                        val selectedAppointmentDate = appointmentDate
                            ?: return@Button

                        val originalAppointment = appointment
                        val shouldKeepNotified =
                            originalAppointment?.notified == true &&
                                originalAppointment.appointmentDate ==
                                selectedAppointmentDate

                        onSave(
                            AppointmentEntity(
                                id = originalAppointment?.id ?: 0L,
                                fullName = fullName.trim(),
                                identityNumber = identityNumber.trim(),
                                mhrsPassword = mhrsPassword,
                                eDevletPassword = eDevletPassword,
                                eNabizPassword = eNabizPassword,
                                birthDate = birthDate,
                                notes = notes,
                                appointmentDate = selectedAppointmentDate,
                                notified = shouldKeepNotified
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showBirthDatePicker) {
        AppointmentDatePickerDialog(
            initialDateMillis = birthDate,
            onDismiss = {
                showBirthDatePicker = false
            },
            onDateSelected = { selectedMillis ->
                birthDate = selectedMillis
                showBirthDatePicker = false
            }
        )
    }

    if (showAppointmentDatePicker) {
        AppointmentDatePickerDialog(
            initialDateMillis = appointmentDate,
            onDismiss = {
                showAppointmentDatePicker = false
            },
            onDateSelected = { selectedMillis ->
                appointmentDate = selectedMillis
                showAppointmentDatePicker = false
            }
        )
    }
}

@Composable
private fun PasswordInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(label)
        },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        )
    )
}

@Composable
private fun DateSelectionButton(
    label: String,
    selectedDateMillis: Long?,
    notSetText: String,
    onClick: () -> Unit
) {
    val displayedDate = selectedDateMillis?.let {
        DateUtils.formatDate(it)
    } ?: notSetText

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null
            )

            Spacer(modifier = Modifier.padding(horizontal = 4.dp))

            Text(
                stringResource(
                    R.string.labeled_value,
                    label,
                    displayedDate
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppointmentDatePickerDialog(
    initialDateMillis: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMillis ->
                        onDateSelected(
                            DateUtils.normalizeEpochMillis(selectedMillis)
                        )
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(stringResource(R.string.select_date))
            }
        )
    }
}
