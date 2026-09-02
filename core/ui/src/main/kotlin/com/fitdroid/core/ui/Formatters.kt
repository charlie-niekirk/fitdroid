package com.fitdroid.core.ui

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object Formatters {
    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    fun localDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(dateFormatter.withLocale(locale))

    fun timeOfDay(
        instant: Instant,
        zoneId: ZoneId,
        locale: Locale = Locale.getDefault(),
    ): String =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(zoneId)
            .format(instant)

    fun dateTime(
        instant: Instant,
        zoneId: ZoneId,
        locale: Locale = Locale.getDefault(),
    ): String =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(zoneId)
            .format(instant)

    fun duration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }
    }
}
