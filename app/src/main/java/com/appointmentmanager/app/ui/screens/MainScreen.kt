package com.appointmentmanager.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.appointmentmanager.app.R
import com.appointmentmanager.app.data.AppointmentEntity
import com.appointmentmanager.app.ui.AppointmentViewModel
import com.appointmentmanager.app.ui.UiMessage
import com.appointmentmanager.app.ui.theme.StatusFarBgDark
import com.appointmentmanager.app.ui.theme.StatusFarBgLight
import com.appointmentmanager.app.ui.theme.StatusFarTextDark
import com.appointmentmanager.app.ui.theme.StatusFarTextLight
import com.appointmentmanager.app.ui.theme.StatusPassedBgDark
import com.appointmentmanager.app.ui.theme.StatusPassedBgLight
import com.appointmentmanager.app.ui.theme.StatusPassedTextDark
import com.appointmentmanager.app.ui.theme.StatusPassedTextLight
import com.appointmentmanager.app.ui.theme.StatusUrgentBgDark
import com.appointmentmanager.app.ui.theme.StatusUrgentBgLight
import com.appointmentmanager.app.ui.theme.StatusUrgentTextDark
import com.appointmentmanager.app.ui.theme.StatusUrgentTextLight
import com.appointmentmanager.app.util.DateUtils
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AppointmentViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val appointments by viewModel.appointments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val darkModePref by viewModel.darkModePreference.collectAsState()
    val isSystemDark = isSystemInDarkTheme()

    var showMenu by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportErrorMsg = stringResource(R.string.export_error)
    val importSuccessMsg = stringResource(R.string.import_success)
    val importErrorMsg = stringResource(R.string.import_error)
    val defaultBackupFileName = stringResource(R.string.export_file_name)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportBackup(it, exportSuccessMsg, exportErrorMsg)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportConfirmDialog = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collectLatest { message ->
            when (message) {
                is UiMessage.Success -> snackbarHostState.showSnackbar(message.message)
                is UiMessage.Error -> snackbarHostState.showSnackbar(message.message)
            }
        }
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportUri = null
            },
            title = { Text(text = stringResource(R.string.import_confirmation_title)) },
            text = { Text(text = stringResource(R.string.import_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        pendingImportUri?.let { uri ->
                            viewModel.importBackup(uri, importSuccessMsg, importErrorMsg)
                        }
                        pendingImportUri = null
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        pendingImportUri = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.main_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDarkMode(isSystemDark) }) {
                        val isDark = darkModePref ?: isSystemDark
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = stringResource(R.string.toggle_dark_mode)
                        )
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_json)) },
                            leadingIcon = {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                exportLauncher.launch(defaultBackupFileName)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.import_json)) },
                            leadingIcon = {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_appointment)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(text = stringResource(R.string.search_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) {
                            stringResource(R.string.no_appointments)
                        } else {
                            stringResource(R.string.no_search_results)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = appointments,
                        key = { it.id }
                    ) { appointment ->
                        AppointmentItemCard(
                            appointment = appointment,
                            onClick = { onNavigateToDetail(appointment.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItemCard(
    appointment: AppointmentEntity,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val daysRemaining = DateUtils.calculateDaysRemaining(appointment.appointmentDate)

    val (badgeBgColor, badgeTextColor, statusLabel) = when {
        daysRemaining < 0 -> Triple(
            if (isDark) StatusPassedBgDark else StatusPassedBgLight,
            if (isDark) StatusPassedTextDark else StatusPassedTextLight,
            stringResource(R.string.days_overdue, -daysRemaining)
        )
        daysRemaining == 0L -> Triple(
            if (isDark) StatusUrgentBgDark else StatusUrgentBgLight,
            if (isDark) StatusUrgentTextDark else StatusUrgentTextLight,
            stringResource(R.string.today)
        )
        daysRemaining == 1L -> Triple(
            if (isDark) StatusUrgentBgDark else StatusUrgentBgLight,
            if (isDark) StatusUrgentTextDark else StatusUrgentTextLight,
            stringResource(R.string.tomorrow)
        )
        daysRemaining <= 2L -> Triple(
            if (isDark) StatusUrgentBgDark else StatusUrgentBgLight,
            if (isDark) StatusUrgentTextDark else StatusUrgentTextLight,
            stringResource(R.string.days_remaining, daysRemaining)
        )
        else -> Triple(
            if (isDark) StatusFarBgDark else StatusFarBgLight,
            if (isDark) StatusFarTextDark else StatusFarTextLight,
            stringResource(R.string.days_remaining, daysRemaining)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.65f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = appointment.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = badgeBgColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = badgeTextColor,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (appointment.identityNumber.isNotBlank()) {
                Text(
                    text = "${stringResource(R.string.identity_number)}: ${appointment.identityNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "${stringResource(R.string.appointment_date)}: ${DateUtils.formatDate(appointment.appointmentDate)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
