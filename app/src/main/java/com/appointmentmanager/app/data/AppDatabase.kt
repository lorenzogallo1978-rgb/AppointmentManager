package com.appointmentmanager.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AppointmentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appointmentDao(): AppointmentDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `appointments_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fullName` TEXT NOT NULL,
                        `identityNumber` TEXT NOT NULL,
                        `mhrsPassword` TEXT NOT NULL,
                        `eDevletPassword` TEXT NOT NULL,
                        `eNabizPassword` TEXT NOT NULL,
                        `birthDate` INTEGER,
                        `notes` TEXT NOT NULL,
                        `appointmentDate` INTEGER,
                        `notified` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO appointments_new (
                        id,
                        fullName,
                        identityNumber,
                        mhrsPassword,
                        eDevletPassword,
                        eNabizPassword,
                        birthDate,
                        notes,
                        appointmentDate,
                        notified
                    )
                    SELECT
                        id,
                        fullName,
                        identityNumber,
                        mhrsPassword,
                        eDevletPassword,
                        eNabizPassword,
                        birthDate,
                        notes,
                        appointmentDate,
                        notified
                    FROM appointments
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE appointments")
                db.execSQL("ALTER TABLE appointments_new RENAME TO appointments")
            }
        }

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "appointment_manager.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
