package com.appointmentmanager.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "appointments")
@Serializable
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val fullName: String,
    val identityNumber: String = "",
    val mhrsPassword: String = "",
    val eDevletPassword: String = "",
    val eNabizPassword: String = "",
    val birthDate: Long? = null,
    val notes: String = "",
    val appointmentDate: Long? = null,
    val notified: Boolean = false
)
