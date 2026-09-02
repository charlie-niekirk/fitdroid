package com.fitdroid.feature.reports

import androidx.lifecycle.ViewModel
import com.fitdroid.core.database.ScoreRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ReportsViewModel(
    private val scores: ScoreRepository,
    clock: Clock,
    zoneId: ZoneId,
) : ViewModel(), OrbitContainerHost<ReportsState, ReportsUiState, Nothing> {

    private val today: LocalDate = LocalDate.now(clock.withZone(zoneId))
    private val historyStart: LocalDate = today.minusMonths(MaxMonthOffset.toLong())
    private val historyEndExclusive: LocalDate = today.plusDays(1)

    override val container = orbitContainer<ReportsState, ReportsUiState, Nothing>(
        initialState = ReportsState(today = today),
        transformState = ReportsState::toUiState,
        onCreate = { collectScores() },
    )

    fun selectPeriod(period: ReportPeriod) = intent {
        if (state.period != period) {
            reduce { state.copy(period = period, offset = 0) }
        }
    }

    fun selectPreviousPeriod() = intent {
        if (state.offset < maxOffset(state.period)) {
            reduce { state.copy(offset = state.offset + 1) }
        }
    }

    fun selectNextPeriod() = intent {
        if (state.offset > 0) {
            reduce { state.copy(offset = state.offset - 1) }
        }
    }

    private suspend fun collectScores() = subIntent {
        repeatOnSubscription {
            scores.observeInRange(historyStart, historyEndExclusive).collect { scoreList ->
                reduce {
                    state.copy(
                        isLoading = false,
                        scores = scoreList,
                    )
                }
            }
        }
    }
}
