package com.appointmentmanager.app.data

import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val appointmentDao: AppointmentDao
) {

    fun observeAppointments(query: String): Flow<List<AppointmentEntity>> {
        return appointmentDao.observeAppointments(query.trim())
    }

    fun observeAppointmentById(id: Long): Flow<AppointmentEntity?> {
        return appointmentDao.observeAppointmentById(id)
    }

    suspend fun insert(appointment: AppointmentEntity): Long {
        return appointmentDao.insert(appointment)
    }

    suspend fun update(appointment: AppointmentEntity) {
        appointmentDao.update(appointment)
    }

    suspend fun delete(appointment: AppointmentEntity) {
        appointmentDao.delete(appointment)
    }

    suspend fun getAllAppointments(): List<AppointmentEntity> {
        return appointmentDao.getAllAppointments()
    }

    suspend fun replaceAllAppointments(appointments: List<AppointmentEntity>) {
        appointmentDao.deleteAll()

        val normalizedAppointments = appointments.map {
            it.copy(id = 0L)
        }

        appointmentDao.insertAll(normalizedAppointments)
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
