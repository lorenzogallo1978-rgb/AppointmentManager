package com.appointmentmanager.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.appointmentmanager.app.data.AppDatabase
import com.appointmentmanager.app.data.AppointmentRepository
import com.appointmentmanager.app.data.BackupManager
import com.appointmentmanager.app.data.SettingsDataStore
import com.appointmentmanager.app.notification.NotificationHelper

class AppointmentManagerApplication : Application(), Configuration.Provider {

    val database: AppDatabase by lazy {
        AppDatabase.create(this)
    }

    val appointmentRepository: AppointmentRepository by lazy {
        AppointmentRepository(database)
    }

    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(this)
    }

    val backupManager: BackupManager by lazy {
        BackupManager(
            context = this,
            repository = appointmentRepository
        )
    }

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.scheduleDailyWorker(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
