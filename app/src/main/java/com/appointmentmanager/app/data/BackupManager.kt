package com.appointmentmanager.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

class InvalidBackupFormatException(
    cause: Throwable? = null
) : Exception("The selected file is not a valid appointment backup.", cause)

class BackupManager(
    private val context: Context,
    private val repository: AppointmentRepository
) {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = false
    }

    suspend fun exportTo(uri: Uri) {
        withContext(Dispatchers.IO) {
            val appointments = repository.getAllAppointments()
            val jsonText = json.encodeToString(appointments)

            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: throw IOException("Could not open the selected destination.")

            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(jsonText)
                writer.flush()
            }
        }
    }

    suspend fun importFrom(uri: Uri) {
        withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Could not open the selected backup file.")

            val jsonText = inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                reader.readText()
            }

            val appointments = try {
                json.decodeFromString<List<AppointmentEntity>>(jsonText)
            } catch (exception: SerializationException) {
                throw InvalidBackupFormatException(exception)
            } catch (exception: IllegalArgumentException) {
                throw InvalidBackupFormatException(exception)
            }

            if (appointments.any { appointment ->
                    appointment.fullName.isBlank()
                }
            ) {
                throw InvalidBackupFormatException()
            }

            repository.replaceAllAppointments(appointments)
        }
    }
}
