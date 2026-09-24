package com.appointmentmanager.app.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val database: AppDatabase
) {
    private val appointmentDao: AppointmentDao
        get() = database.appointmentDao()

    fun observeAppointments(query: String): Flow<List<AppointmentEntity>> {
        return appointmentDao.observeAppointments(query.trim())
    }

    fun observeAppointmentById(id: Long): Flow<AppointmentEntity?> {
        return appointmentDao.observeAppointmentById(id)
    }

    suspend fun getAppointmentById(id: Long): AppointmentEntity? {
        return appointmentDao.getAppointmentById(id)
    }

    suspend fun insert(appointment: AppointmentEntity): Long {
        return appointmentDao.insert(appointment)
    }

    suspend fun update(appointment: AppointmentEntity) {
        appointmentDao.update(appointment)
    }

    suspend fun deleteById(id: Long) {
        appointmentDao.deleteById(id)
    }

    suspend fun getAllAppointments(): List<AppointmentEntity> {
        return appointmentDao.getAllAppointments()
    }

    suspend fun replaceAllAppointments(appointments: List<AppointmentEntity>) {
        val importedAppointments = appointments.map { appointment ->
            appointment.copy(id = 0L)
        }

        database.withTransaction {
            appointmentDao.deleteAll()

            if (importedAppointments.isNotEmpty()) {
                appointmentDao.insertAll(importedAppointments)
            }
        }
    }

    suspend fun getAppointmentsForNotification(
        fromMillis: Long,
        toMillis: Long
    ): List<AppointmentEntity> {
        return appointmentDao.getAppointmentsForNotification(
            fromMillis = fromMillis,
            toMillis = toMillis
        )
    }

    suspend fun markAsNotified(id: Long) {
        appointmentDao.updateNotified(id = id, notified = true)
    }
}
