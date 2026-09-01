package com.fitdroid

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.fitdroid.feature.onboarding.OnboardingScreen
import com.fitdroid.ui.home.HomeScreen

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Onboarding)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<Onboarding> {
                    OnboardingScreen(
                        onComplete = { backStack.navigateToHome() },
                        modifier = Modifier.safeDrawingPadding(),
                    )
                }
                entry<Home> {
                    HomeScreen()
                }
            },
    )
}

private fun NavBackStack<NavKey>.navigateToHome() {
    if (lastOrNull() == Home) return
    clear()
    add(Home)
}
