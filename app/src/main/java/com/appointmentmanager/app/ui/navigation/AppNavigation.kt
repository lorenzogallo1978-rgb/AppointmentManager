package com.appointmentmanager.app.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appointmentmanager.app.R
import com.appointmentmanager.app.data.AppointmentEntity
import com.appointmentmanager.app.ui.AppointmentViewModel
import com.appointmentmanager.app.ui.screens.AddEditAppointmentScreen
import com.appointmentmanager.app.ui.screens.AppointmentDetailScreen
import com.appointmentmanager.app.ui.screens.MainScreen

private const val HOME_ROUTE = "home"
private const val ADD_ROUTE = "add"
private const val DETAIL_ROUTE = "detail/{appointmentId}"
private const val EDIT_ROUTE = "edit/{appointmentId}"
private const val APPOINTMENT_ID_ARGUMENT = "appointmentId"

@Composable
fun AppNavigation(
    viewModel: AppointmentViewModel,
    systemIsDark: Boolean,
    isDarkTheme: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val saveAppointment: (AppointmentEntity) -> Unit = { appointment ->
        viewModel.saveAppointment(
            appointment = appointment,
            onSaved = {
                navController.popBackStack()
            },
            onError = {
                Toast.makeText(
                    context,
                    context.getString(R.string.save_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE
    ) {
        composable(HOME_ROUTE) {
            MainScreen(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                onAddAppointment = {
                    navController.navigate(ADD_ROUTE)
                },
                onOpenAppointment = { id ->
                    navController.navigate("detail/$id")
                },
                onExport = onExport,
                onImport = onImport,
                onToggleDarkMode = {
                    viewModel.toggleDarkMode(systemIsDark)
                },
                onUseSystemTheme = {
                    viewModel.useSystemTheme()
                }
            )
        }

        composable(ADD_ROUTE) {
            AddEditAppointmentScreen(
                appointmentId = null,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onSave = saveAppointment
            )
        }

        composable(
            route = EDIT_ROUTE,
            arguments = listOf(
                navArgument(APPOINTMENT_ID_ARGUMENT) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments
                ?.getLong(APPOINTMENT_ID_ARGUMENT)
                ?: -1L

            AddEditAppointmentScreen(
                appointmentId = appointmentId,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onSave = saveAppointment
            )
        }

        composable(
            route = DETAIL_ROUTE,
            arguments = listOf(
                navArgument(APPOINTMENT_ID_ARGUMENT) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments
                ?.getLong(APPOINTMENT_ID_ARGUMENT)
                ?: -1L

            AppointmentDetailScreen(
                appointmentId = appointmentId,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                onEdit = {
                    navController.navigate("edit/$appointmentId")
                },
                onDelete = { id ->
                    viewModel.deleteAppointment(
                        id = id,
                        onDeleted = {
                            navController.popBackStack()
                        },
                        onError = {
                            Toast.makeText(
                                context,
                                context.getString(R.string.delete_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            )
        }
    }
}
