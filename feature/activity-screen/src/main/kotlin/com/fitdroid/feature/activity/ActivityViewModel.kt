package com.fitdroid.feature.activity

import androidx.lifecycle.ViewModel
import com.fitdroid.core.database.ActivityRepository
import com.fitdroid.core.database.ScoreRepository
import com.fitdroid.core.sync.ImmediateSync
import com.fitdroid.core.sync.UserSettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.combine
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ActivityViewModel(
    private val activity: ActivityRepository,
    private val scores: ScoreRepository,
    private val settings: UserSettingsRepository,
    private val sync: ImmediateSync,
    clock: Clock,
    private val zoneId: ZoneId,
) : ViewModel(), OrbitContainerHost<ActivityState, ActivityState, Nothing> {

    private val today: LocalDate = LocalDate.now(clock.withZone(zoneId))
    private val rangeStart: LocalDate = today.minusDays(ScoreWindowDays - 1)
    private val rangeEndExclusive: LocalDate = today.plusDays(1)
    private val exerciseStart = rangeStart.atStartOfDay(zoneId).toInstant()
    private val exerciseEnd = rangeEndExclusive.atStartOfDay(zoneId).toInstant()

    override val container = orbitContainer<ActivityState, Nothing>(
        initialState = ActivityState(selectedDate = today, today = today),
        onCreate = { collectMirror() },
    )

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        sync.request()
    }

    fun selectPreviousDay() = intent {
        if (state.canGoPrevious) {
            reduce { state.copy(selectedDate = state.selectedDate.minusDays(1)) }
        }
    }

    fun selectNextDay() = intent {
        if (state.canGoNext) {
            reduce { state.copy(selectedDate = state.selectedDate.plusDays(1)) }
        }
    }

    private suspend fun collectMirror() = subIntent {
        repeatOnSubscription {
            combine(
                scores.observeInRange(rangeStart, rangeEndExclusive),
                activity.observeMetrics(rangeStart, rangeEndExclusive),
                activity.observeExercise(exerciseStart, exerciseEnd),
                settings.settings,
            ) { scoreList, metrics, exercises, userSettings ->
                ActivityState(
                    isLoading = false,
                    isRefreshing = false,
                    selectedDate = today,
                    today = today,
                    scoresByDate = scoreList.mapNotNull { row ->
                        row.activity?.let { row.date to it }
                    }.toMap(),
                    metricsByDate = metrics.associateBy { it.date },
                    exercisesByDate = exercises.groupBy { it.start.atZone(zoneId).toLocalDate() },
                    recentScores = scoreList.sortedBy { it.date }.mapNotNull { it.activity?.score?.toFloat() },
                    stepGoal = userSettings.steps,
                )
            }.collect { next ->
                reduce { next.copy(selectedDate = state.selectedDate) }
            }
        }
    }
}
