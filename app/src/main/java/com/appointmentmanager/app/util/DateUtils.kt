package com.appointmentmanager.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val formatter = DateTimeFormatter.ofPattern(
        "dd/MM/yyyy",
        Locale.US
    )

    fun toEpochMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }

    fun toLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
    }

    fun normalizeEpochMillis(epochMillis: Long): Long {
        return toEpochMillis(toLocalDate(epochMillis))
    }

    fun formatDate(epochMillis: Long): String {
        return toLocalDate(epochMillis).format(formatter)
    }

    fun daysUntil(epochMillis: Long, today: LocalDate = LocalDate.now()): Long {
        return ChronoUnit.DAYS.between(
            today,
            toLocalDate(epochMillis)
        )
    }
}
