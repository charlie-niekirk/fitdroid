package com.fitdroid.feature.dashboard

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
class DashboardViewModel(
    private val scores: ScoreRepository,
    private val activity: ActivityRepository,
    private val settings: UserSettingsRepository,
    private val sync: ImmediateSync,
    clock: Clock,
    zoneId: ZoneId,
) : ViewModel(), OrbitContainerHost<DashboardState, DashboardState, DashboardEffect> {

    private val today: LocalDate = LocalDate.now(clock.withZone(zoneId))
    private val rangeStart: LocalDate = today.minusDays(ScoreWindowDays - 1)
    private val rangeEndExclusive: LocalDate = today.plusDays(1)

    override val container = orbitContainer<DashboardState, DashboardEffect>(
        initialState = DashboardState(today = today),
        onCreate = {
            sync.request()
            collectMirror()
        },
    )

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        sync.request()
    }

    fun onSleepClick() = intent {
        postSideEffect(DashboardEffect.OpenSleep)
    }

    fun onActivityClick() = intent {
        postSideEffect(DashboardEffect.OpenActivity)
    }

    private suspend fun collectMirror() = subIntent {
        repeatOnSubscription {
            combine(
                scores.observeInRange(rangeStart, rangeEndExclusive),
                activity.observeMetrics(rangeStart, rangeEndExclusive),
                settings.settings,
            ) { scoreList, metrics, userSettings ->
                val todayScores = scoreList.firstOrNull { it.date == today }
                DashboardState(
                    isLoading = false,
                    isRefreshing = false,
                    today = today,
                    sleepScore = todayScores?.sleep,
                    readinessScore = todayScores?.readiness,
                    activityScore = todayScores?.activity,
                    sleepTrend = scoreList
                        .sortedBy { it.date }
                        .mapNotNull { it.sleep?.score?.toFloat() },
                    steps = metrics.firstOrNull { it.date == today }?.steps,
                    stepGoal = userSettings.steps,
                )
            }.collect { next ->
                reduce { next }
            }
        }
    }
}
