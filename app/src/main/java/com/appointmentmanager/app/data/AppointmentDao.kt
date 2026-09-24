package com.appointmentmanager.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query(
        """
        SELECT * FROM appointments
        WHERE fullName LIKE '%' || :query || '%'
           OR identityNumber LIKE '%' || :query || '%'
        ORDER BY appointmentDate ASC, fullName COLLATE NOCASE ASC
        """
    )
    fun observeAppointments(query: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    fun observeAppointmentById(id: Long): Flow<AppointmentEntity?>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: Long): AppointmentEntity?

    @Query("SELECT * FROM appointments ORDER BY appointmentDate ASC")
    suspend fun getAllAppointments(): List<AppointmentEntity>

    @Query(
        """
        SELECT * FROM appointments
        WHERE notified = 0
          AND appointmentDate >= :fromMillis
          AND appointmentDate <= :toMillis
        ORDER BY appointmentDate ASC
        """
    )
    suspend fun getAppointmentsForNotification(
        fromMillis: Long,
        toMillis: Long
    ): List<AppointmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentEntity>)

    @Update
    suspend fun update(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE appointments SET notified = :notified WHERE id = :id")
    suspend fun updateNotified(id: Long, notified: Boolean)

    @Query("DELETE FROM appointments")
    suspend fun deleteAll()
}
