package com.fitdroid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitdroid.core.designsystem.theme.AwakeColor
import com.fitdroid.core.designsystem.theme.DeepSleepColor
import com.fitdroid.core.designsystem.theme.FitdroidTheme
import com.fitdroid.core.designsystem.theme.LightSleepColor
import com.fitdroid.core.designsystem.theme.RemSleepColor
import com.fitdroid.core.designsystem.theme.UnknownStageColor
import com.fitdroid.core.model.SleepStageType
import java.time.Duration

data class HypnogramSegment(
    val type: SleepStageType,
    val duration: Duration,
)

@Composable
fun Hypnogram(
    segments: List<HypnogramSegment>,
    modifier: Modifier = Modifier,
) {
    val totalMillis = segments.sumOf { it.duration.toMillis() }.coerceAtLeast(1L)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp)),
    ) {
        segments.forEach { segment ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(segment.duration.toMillis().coerceAtLeast(1L) / totalMillis.toFloat())
                    .background(segment.type.hypnogramColor()),
            )
        }
    }
}

private fun SleepStageType.hypnogramColor(): Color = when (this) {
    SleepStageType.Awake -> AwakeColor
    SleepStageType.Light -> LightSleepColor
    SleepStageType.Deep -> DeepSleepColor
    SleepStageType.Rem -> RemSleepColor
    SleepStageType.Unknown -> UnknownStageColor
}

@Preview(showBackground = true)
@Composable
private fun HypnogramPreview() {
    FitdroidTheme(dynamicColor = false) {
        Hypnogram(
            segments = listOf(
                HypnogramSegment(SleepStageType.Awake, Duration.ofMinutes(12)),
                HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(90)),
                HypnogramSegment(SleepStageType.Deep, Duration.ofMinutes(70)),
                HypnogramSegment(SleepStageType.Rem, Duration.ofMinutes(45)),
                HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(40)),
            ),
        )
    }
}
