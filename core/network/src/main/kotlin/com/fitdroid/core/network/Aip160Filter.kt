package com.fitdroid.core.network

import java.time.Instant
import java.time.LocalDate

object Aip160Filter {
    fun dateRange(
        type: HealthDataType,
        startInclusive: LocalDate,
        endExclusive: LocalDate,
    ): String {
        require(type.kind == HealthDataKind.Daily) {
            "${type.path} is ${type.kind}; Daily types must be filtered on date"
        }
        return range("${type.path}.date", startInclusive.toString(), endExclusive.toString())
    }

    fun timeRange(
        type: HealthDataType,
        startInclusive: Instant,
        endExclusive: Instant,
    ): String {
        require(type.kind != HealthDataKind.Daily) {
            "${type.path} is Daily; filter with dateRange() using LocalDate"
        }
        val field = when (type.kind) {
            HealthDataKind.Session -> "${type.path}.interval.end_time"
            HealthDataKind.Sample -> "${type.path}.sample_time.physical_time"
            HealthDataKind.Interval -> "${type.path}.interval.start_time"
            HealthDataKind.Daily -> error("unreachable")
        }
        return range(field, startInclusive.toString(), endExclusive.toString())
    }

    private fun range(field: String, startInclusive: String, endExclusive: String): String =
        """$field >= "$startInclusive" AND $field < "$endExclusive""""
}
