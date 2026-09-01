package com.fitdroid.feature.sleep

import androidx.lifecycle.ViewModel
import com.fitdroid.core.database.ScoreRepository
import com.fitdroid.core.database.SleepRepository
import com.fitdroid.core.sync.ImmediateSync
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
class SleepViewModel(
    private val sleep: SleepRepository,
    private val scores: ScoreRepository,
    private val sync: ImmediateSync,
    clock: Clock,
    private val zoneId: ZoneId,
) : ViewModel(), OrbitContainerHost<SleepState, SleepUiState, Nothing> {

    private val today: LocalDate = LocalDate.now(clock.withZone(zoneId))
    private val rangeStart: LocalDate = today.minusDays(ScoreWindowDays - 1)
    private val rangeEndExclusive: LocalDate = today.plusDays(1)
    private val sessionStart = rangeStart.minusDays(1).atStartOfDay(zoneId).toInstant()
    private val sessionEnd = rangeEndExclusive.atStartOfDay(zoneId).toInstant()

    override val container = orbitContainer<SleepState, SleepUiState, Nothing>(
        initialState = SleepState(selectedDate = today, today = today),
        transformState = ::toUiState,
        onCreate = { collectMirror() },
    )

    fun refresh() = intent {
        reduce { state.copy(isRefreshing = true) }
        sync.request()
    }

    fun selectPreviousNight() = intent {
        if (state.selectedDate > today.minusDays(ScoreWindowDays - 1)) {
            reduce { state.copy(selectedDate = state.selectedDate.minusDays(1)) }
        }
    }

    fun selectNextNight() = intent {
        if (state.selectedDate < today) {
            reduce { state.copy(selectedDate = state.selectedDate.plusDays(1)) }
        }
    }

    private fun toUiState(state: SleepState): SleepUiState = state.toUiState(zoneId)

    private suspend fun collectMirror() = subIntent {
        repeatOnSubscription {
            combine(
                sleep.observeInRange(sessionStart, sessionEnd),
                scores.observeInRange(rangeStart, rangeEndExclusive),
            ) { sessions, scoreList ->
                sessions to scoreList.mapNotNull { it.sleep }
            }.collect { (sessions, sleepScores) ->
                reduce {
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        sessions = sessions,
                        scores = sleepScores,
                    )
                }
            }
        }
    }
}
