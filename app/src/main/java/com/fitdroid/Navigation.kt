package com.fitdroid

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.fitdroid.feature.onboarding.OnboardingScreen
import com.fitdroid.ui.main.MainScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Onboarding)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<Onboarding> {
                    OnboardingScreen(
                        onFinished = { backStack.navigateToMain() },
                        modifier = Modifier.safeDrawingPadding(),
                    )
                }
                entry<Main> {
                    MainScreen(
                        onItemClick = { navKey -> backStack.add(navKey) },
                        modifier = Modifier.safeDrawingPadding().padding(16.dp),
                    )
                }
            },
    )
}

private fun NavBackStack<NavKey>.navigateToMain() {
    if (lastOrNull() == Main) return
    clear()
    add(Main)
}
