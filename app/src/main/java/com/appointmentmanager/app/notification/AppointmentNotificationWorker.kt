package com.appointmentmanager.app.notification

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appointmentmanager.app.AppointmentManagerApplication
import com.appointmentmanager.app.util.DateUtils
import java.time.LocalDate
import java.util.concurrent.CancellationException

class AppointmentNotificationWorker(
    appContext: android.content.Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val application = applicationContext as? AppointmentManagerApplication
            ?: return Result.failure()

        return try {
            NotificationHelper.createNotificationChannel(application)

            val today = LocalDate.now()
            val fromMillis = DateUtils.toEpochMillis(today)
            val toMillis = DateUtils.toEpochMillis(today.plusDays(2))

            val appointments =
                application.appointmentRepository.getAppointmentsForNotification(
                    fromMillis = fromMillis,
                    toMillis = toMillis
                )

            appointments.forEach { appointment ->
                val notificationPosted =
                    NotificationHelper.showAppointmentNotification(
                        context = application,
                        appointment = appointment
                    )

                if (notificationPosted) {
                    application.appointmentRepository.markAsNotified(
                        appointment.id
                    )
                }
            }

            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
