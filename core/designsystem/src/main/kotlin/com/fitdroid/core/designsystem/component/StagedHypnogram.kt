package com.fitdroid.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitdroid.core.designsystem.theme.FitdroidTheme
import com.fitdroid.core.model.SleepStageType
import java.time.Duration
import kotlin.math.abs
import kotlin.math.max

private val ConnectedStageOrder = listOf(
    SleepStageType.Awake,
    SleepStageType.Rem,
    SleepStageType.Light,
    SleepStageType.Deep,
)

private val LabelHeight = 20.dp
private val LabelTrackGap = 4.dp
private val TrackHeight = 24.dp
private val LaneGap = 12.dp
private val AwakeMinimumWidth = 4.dp
private val EventMinimumWidth = 1.dp
private val ConnectorWidth = 3.dp
private const val ConnectorAlpha = 0.28f
private const val ConnectedSegmentFillAlpha = 0.82f
private const val ConnectedSegmentBorderAlpha = 0.96f

@Composable
fun StagedHypnogram(
    segments: List<HypnogramSegment>,
    awakeLabel: String,
    awakeDuration: String,
    restlessnessLabel: String,
    restlessnessDuration: String,
    remLabel: String,
    remDuration: String,
    lightLabel: String,
    lightDuration: String,
    deepLabel: String,
    deepDuration: String,
    startTimeLabel: String,
    midpointTimeLabel: String,
    endTimeLabel: String,
    modifier: Modifier = Modifier,
) {
    val showRestlessness = segments.any { it.type == SleepStageType.AwakeInBed }
    val stageOrder = buildList {
        add(SleepStageType.Awake)
        if (showRestlessness) add(SleepStageType.AwakeInBed)
        addAll(ConnectedStageOrder.drop(1))
    }
    val summaries = mapOf(
        SleepStageType.Awake to StageSummary(awakeLabel, awakeDuration),
        SleepStageType.AwakeInBed to StageSummary(restlessnessLabel, restlessnessDuration),
        SleepStageType.Rem to StageSummary(remLabel, remDuration),
        SleepStageType.Light to StageSummary(lightLabel, lightDuration),
        SleepStageType.Deep to StageSummary(deepLabel, deepDuration),
    )
    val description = buildString {
        append("Sleep stages. ")
        stageOrder.joinTo(this, separator = ", ") { type ->
            val summary = summaries.getValue(type)
            "${summary.label}: ${summary.duration}"
        }
        append(". $startTimeLabel to $endTimeLabel")
    }
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val chartHeight =
        (LabelHeight + LabelTrackGap + TrackHeight) * stageOrder.size +
            LaneGap * (stageOrder.size - 1) +
            4.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight),
            ) {
                val trackHeight = TrackHeight.toPx()
                val laneGap = LaneGap.toPx()
                val labelHeight = LabelHeight.toPx()
                val labelTrackGap = LabelTrackGap.toPx()
                val trackTopByStage = stageOrder.mapIndexed { index, type ->
                    type to index * (labelHeight + labelTrackGap + trackHeight + laneGap) +
                        labelHeight + labelTrackGap
                }.toMap()
                trackTopByStage.values.forEach { top ->
                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset(0f, top),
                        size = Size(size.width, trackHeight),
                        cornerRadius = CornerRadius(trackHeight / 2),
                    )
                }

                val drawableSegments = segments
                    .filter { it.type in stageOrder }
                    .sortedBy { it.startFraction }
                val transitionSegments = drawableSegments
                    .filter { it.type in ConnectedStageOrder }
                val connections = transitionSegments.zipWithNext()
                    .mapNotNull { (previous, current) ->
                        if (
                            previous.type != current.type &&
                            abs(previous.endFraction - current.startFraction) <= 0.002f
                        ) {
                            StageConnection(previous, current)
                        } else {
                            null
                        }
                    }

                val borderInset = ConnectorWidth.toPx() / 2
                connections.forEach { connection ->
                    val previous = connection.from
                    val current = connection.to
                    val x = previous.endFraction * size.width
                    val previousTop = trackTopByStage.getValue(previous.type)
                    val currentTop = trackTopByStage.getValue(current.type)
                    val previousColor = previous.type.hypnogramColor().copy(alpha = ConnectorAlpha)
                    val currentColor = current.type.hypnogramColor().copy(alpha = ConnectorAlpha)
                    val (top, bottom, colors) = if (previousTop < currentTop) {
                        Triple(
                            previousTop + trackHeight - borderInset,
                            currentTop + borderInset,
                            listOf(previousColor, currentColor),
                        )
                    } else {
                        Triple(
                            currentTop + trackHeight - borderInset,
                            previousTop + borderInset,
                            listOf(currentColor, previousColor),
                        )
                    }
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = colors,
                            startY = top,
                            endY = bottom,
                        ),
                        topLeft = Offset(x - ConnectorWidth.toPx() / 2, top),
                        size = Size(ConnectorWidth.toPx(), bottom - top),
                    )
                }

                drawableSegments.forEach { segment ->
                    val trackTop = trackTopByStage.getValue(segment.type)
                    val rawWidth = (segment.endFraction - segment.startFraction) * size.width
                    if (rawWidth <= 0f) return@forEach
                    val minimumWidth = when (segment.type) {
                        SleepStageType.Awake -> AwakeMinimumWidth.toPx()
                        SleepStageType.AwakeInBed -> EventMinimumWidth.toPx()
                        else -> 0f
                    }
                    val segmentWidth = max(rawWidth, minimumWidth)
                    val rawX = segment.startFraction * size.width
                    val x = if (minimumWidth > 0f) {
                        (rawX - (segmentWidth - rawWidth) / 2)
                            .coerceIn(0f, size.width - segmentWidth)
                    } else {
                        rawX
                    }
                    val incoming = connections.firstOrNull { it.to === segment }
                    val outgoing = connections.firstOrNull { it.from === segment }
                    val incomingTop = incoming
                        ?.from
                        ?.type
                        ?.let(trackTopByStage::getValue)
                        ?.let { it < trackTop }
                        ?: false
                    val incomingBottom = incoming
                        ?.from
                        ?.type
                        ?.let(trackTopByStage::getValue)
                        ?.let { it > trackTop }
                        ?: false
                    val outgoingTop = outgoing
                        ?.to
                        ?.type
                        ?.let(trackTopByStage::getValue)
                        ?.let { it < trackTop }
                        ?: false
                    val outgoingBottom = outgoing
                        ?.to
                        ?.type
                        ?.let(trackTopByStage::getValue)
                        ?.let { it > trackTop }
                        ?: false
                    val rounded = CornerRadius(trackHeight / 2)
                    val square = CornerRadius(0f)
                    val left = (x - if (incoming != null) borderInset else 0f).coerceAtLeast(0f)
                    val right = (x + segmentWidth + if (outgoing != null) borderInset else 0f)
                        .coerceAtMost(size.width)
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = left,
                                top = trackTop,
                                right = right,
                                bottom = trackTop + trackHeight,
                                topLeftCornerRadius = if (incomingTop) square else rounded,
                                topRightCornerRadius = if (outgoingTop) square else rounded,
                                bottomRightCornerRadius = if (outgoingBottom) square else rounded,
                                bottomLeftCornerRadius = if (incomingBottom) square else rounded,
                            ),
                        )
                    }
                    val hasConnection = incoming != null || outgoing != null
                    drawPath(
                        path = path,
                        color = segment.type.hypnogramColor().copy(
                            alpha = if (hasConnection) ConnectedSegmentFillAlpha else 1f,
                        ),
                    )
                    if (hasConnection) {
                        val borderRounded = CornerRadius(trackHeight / 2 - borderInset)
                        val borderPath = Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left = left + borderInset,
                                    top = trackTop + borderInset,
                                    right = right - borderInset,
                                    bottom = trackTop + trackHeight - borderInset,
                                    topLeftCornerRadius = if (incomingTop) square else borderRounded,
                                    topRightCornerRadius = if (outgoingTop) square else borderRounded,
                                    bottomRightCornerRadius =
                                        if (outgoingBottom) square else borderRounded,
                                    bottomLeftCornerRadius =
                                        if (incomingBottom) square else borderRounded,
                                ),
                            )
                        }
                        drawPath(
                            path = borderPath,
                            color = segment.type.hypnogramColor()
                                .copy(alpha = ConnectedSegmentBorderAlpha),
                            style = Stroke(
                                width = ConnectorWidth.toPx(),
                                join = StrokeJoin.Round,
                            ),
                        )
                    }
                }
            }

            Column {
                stageOrder.forEachIndexed { index, type ->
                    val summary = summaries.getValue(type)
                    StageHeader(summary)
                    Spacer(Modifier.height(LabelTrackGap))
                    Spacer(Modifier.height(TrackHeight))
                    if (index != stageOrder.lastIndex) {
                        Spacer(Modifier.height(LaneGap))
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        TimelineAxis(
            startTimeLabel = startTimeLabel,
            midpointTimeLabel = midpointTimeLabel,
            endTimeLabel = endTimeLabel,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StageHeader(summary: StageSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(LabelHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = summary.label, style = MaterialTheme.typography.titleSmallEmphasized)
        Spacer(Modifier.width(4.dp))
        Text(
            text = "• ${summary.duration}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimelineAxis(
    startTimeLabel: String,
    midpointTimeLabel: String,
    endTimeLabel: String,
) {
    val tickColor = MaterialTheme.colorScheme.outlineVariant
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        ) {
            listOf(0f, size.width / 2, size.width).forEach { x ->
                drawLine(
                    color = tickColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = startTimeLabel,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = midpointTimeLabel,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = endTimeLabel,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
            )
        }
    }
}

private data class StageSummary(
    val label: String,
    val duration: String,
)

private data class StageConnection(
    val from: HypnogramSegment,
    val to: HypnogramSegment,
)

@Preview(showBackground = true)
@Composable
private fun StagedHypnogramPreview() {
    FitdroidTheme(dynamicColor = false) {
        StagedHypnogram(
            segments = listOf(
                HypnogramSegment(SleepStageType.Awake, Duration.ofMinutes(6), 0f, 0.015f),
                HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(70), 0.015f, 0.18f),
                HypnogramSegment(SleepStageType.AwakeInBed, Duration.ofMinutes(1), 0.08f, 0.082f),
                HypnogramSegment(SleepStageType.AwakeInBed, Duration.ofMinutes(2), 0.12f, 0.126f),
                HypnogramSegment(SleepStageType.Deep, Duration.ofMinutes(80), 0.18f, 0.37f),
                HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(45), 0.37f, 0.48f),
                HypnogramSegment(SleepStageType.Rem, Duration.ofMinutes(42), 0.48f, 0.58f),
                HypnogramSegment(SleepStageType.Light, Duration.ofMinutes(60), 0.58f, 0.72f),
                HypnogramSegment(SleepStageType.Deep, Duration.ofMinutes(35), 0.72f, 0.8f),
                HypnogramSegment(SleepStageType.Rem, Duration.ofMinutes(45), 0.8f, 0.91f),
                HypnogramSegment(SleepStageType.Awake, Duration.ofMinutes(7), 0.985f, 1f),
            ),
            awakeLabel = "Awake",
            awakeDuration = "13m",
            restlessnessLabel = "Restlessness",
            restlessnessDuration = "19m",
            remLabel = "REM",
            remDuration = "1h 27m",
            lightLabel = "Light",
            lightDuration = "2h 55m",
            deepLabel = "Deep",
            deepDuration = "1h 55m",
            startTimeLabel = "10:48 PM",
            midpointTimeLabel = "2:48 AM",
            endTimeLabel = "6:48 AM",
        )
    }
}
