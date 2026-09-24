package com.appointmentmanager.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.appointmentmanager.app.R
import com.appointmentmanager.app.data.AppointmentEntity
import com.appointmentmanager.app.ui.AppointmentViewModel
import com.appointmentmanager.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AppointmentViewModel,
    isDarkTheme: Boolean,
    onAddAppointment: () -> Unit,
    onOpenAppointment: (Long) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onUseSystemTheme: () -> Unit
) {
    val appointments by viewModel.appointments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.main_title))
                },
                actions = {
                    IconButton(
                        onClick = {
                            menuExpanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(
                                R.string.more_options
                            )
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.export_json))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onExport()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.import_json))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onImport()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.toggle_dark_mode))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isDarkTheme) {
                                        Icons.Default.LightMode
                                    } else {
                                        Icons.Default.DarkMode
                                    },
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleDarkMode()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.system_theme))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.BrightnessAuto,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onUseSystemTheme()
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAppointment
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(
                        R.string.add_appointment
                    )
                )
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                placeholder = {
                    Text(stringResource(R.string.search_hint))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                )
            )

            if (appointments.isEmpty()) {
                val emptyMessage = if (searchQuery.isBlank()) {
                    stringResource(R.string.no_appointments)
                } else {
                    stringResource(R.string.no_search_results)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 4.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = appointments,
                        key = { appointment -> appointment.id }
                    ) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onClick = {
                                onOpenAppointment(appointment.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: AppointmentEntity,
    onClick: () -> Unit
) {
    val daysRemaining = DateUtils.daysUntil(appointment.appointmentDate)

    val remainingText = when {
        daysRemaining < 0 -> stringResource(
            R.string.days_overdue,
            (-daysRemaining).toInt()
        )

        daysRemaining == 0L -> stringResource(R.string.today)
        daysRemaining == 1L -> stringResource(R.string.tomorrow)
        else -> stringResource(
            R.string.days_remaining,
            daysRemaining.toInt()
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = appointmentCardColor(daysRemaining)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = appointment.fullName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(
                    R.string.labeled_value,
                    stringResource(R.string.identity_number),
                    appointment.identityNumber.ifBlank {
                        stringResource(R.string.empty_value)
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.labeled_value,
                    stringResource(R.string.appointment_date),
                    DateUtils.formatDate(appointment.appointmentDate)
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = remainingText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun appointmentCardColor(daysRemaining: Long): Color {
    val colors = MaterialTheme.colorScheme

    return when {
        daysRemaining < 0 -> colors.surfaceVariant
        daysRemaining <= 1 -> colors.errorContainer
        daysRemaining == 2L -> colors.tertiaryContainer
        else -> colors.secondaryContainer
    }
}
