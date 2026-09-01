package com.fitdroid.core.network.model

import java.time.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class IdentityResponse(
    val name: String? = null,
    val legacyUserId: String? = null,
    val healthUserId: String? = null,
)

@Serializable
data class DataPointsResponse(
    val dataPoints: List<DataPoint> = emptyList(),
    val nextPageToken: String? = null,
)

@Serializable
data class DataPoint(
    val name: String? = null,
    val dailyHeartRateVariability: DailyHeartRateVariability? = null,
    val dailyOxygenSaturation: DailyOxygenSaturation? = null,
    val dailyRespiratoryRate: DailyRespiratoryRate? = null,
    val dailyRestingHeartRate: DailyRestingHeartRate? = null,
    val dailySleepTemperatureDerivations: DailySleepTemperatureDerivations? = null,
    val respiratoryRateSleepSummary: RespiratoryRateSleepSummary? = null,
)

@Serializable
data class GoogleDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    fun toLocalDate(): LocalDate = LocalDate.of(year, month, day)
}

@Serializable
data class DailyHeartRateVariability(
    val date: GoogleDate,
    val averageHeartRateVariabilityMilliseconds: Double? = null,
    @Serializable(with = Int64AsStringSerializer::class)
    val nonRemHeartRateBeatsPerMinute: Long? = null,
    val entropy: Double? = null,
    val deepSleepRootMeanSquareOfSuccessiveDifferencesMilliseconds: Double? = null,
)

@Serializable
data class DailyOxygenSaturation(
    val date: GoogleDate,
    val averagePercentage: Double? = null,
    val lowerBoundPercentage: Double? = null,
    val upperBoundPercentage: Double? = null,
    val standardDeviationPercentage: Double? = null,
)

@Serializable
data class DailyRespiratoryRate(
    val date: GoogleDate,
    val breathsPerMinute: Double? = null,
)

@Serializable
data class DailyRestingHeartRate(
    val date: GoogleDate,
    @Serializable(with = Int64AsStringSerializer::class)
    val beatsPerMinute: Long? = null,
)

@Serializable
data class DailySleepTemperatureDerivations(
    val date: GoogleDate,
    val nightlyTemperatureCelsius: Double? = null,
    val baselineTemperatureCelsius: Double? = null,
    val relativeNightlyStddev30dCelsius: Double? = null,
) {
    val deviationCelsius: Double?
        get() {
            val nightly = nightlyTemperatureCelsius ?: return null
            val baseline = baselineTemperatureCelsius ?: return null
            return nightly - baseline
        }
}

@Serializable
data class RespiratoryRateSleepSummary(
    val fullSleepStats: RespiratoryRateSleepSummaryStatistics? = null,
)

@Serializable
data class RespiratoryRateSleepSummaryStatistics(
    val averageBreathsPerMinute: Double? = null,
)

internal object Int64AsStringSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Int64AsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Long {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeString().toLong()
        val primitive = jsonDecoder.decodeJsonElement() as JsonPrimitive
        return primitive.content.toLong()
    }
}
