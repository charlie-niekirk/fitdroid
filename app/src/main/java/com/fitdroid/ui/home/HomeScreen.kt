package com.fitdroid.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.fitdroid.R
import com.fitdroid.feature.activity.ActivityScreen
import com.fitdroid.feature.dashboard.DashboardScreen
import com.fitdroid.feature.sleep.SleepScreen

enum class HomeTab {
    Dashboard,
    Sleep,
    Activity,
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var tab by rememberSaveable { mutableStateOf(HomeTab.Dashboard) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            HomeNavigationBar(selected = tab, onSelect = { tab = it })
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                HomeTab.Dashboard -> DashboardScreen(
                    onOpenSleep = { tab = HomeTab.Sleep },
                    onOpenActivity = { tab = HomeTab.Activity },
                )

                HomeTab.Sleep -> SleepScreen()

                HomeTab.Activity -> ActivityScreen()
            }
        }
    }
}

@Composable
internal fun HomeNavigationBar(
    selected: HomeTab,
    onSelect: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = NavigationBarDefaults.windowInsets,
    ) {
        HomeTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.labelRes),
                    )
                },
                label = { Text(stringResource(tab.labelRes)) },
            )
        }
    }
}

private val HomeTab.icon: ImageVector
    get() = when (this) {
        HomeTab.Dashboard -> Icons.Filled.Home
        HomeTab.Sleep -> Icons.Filled.Hotel
        HomeTab.Activity -> Icons.AutoMirrored.Filled.DirectionsWalk
    }

private val HomeTab.labelRes: Int
    get() = when (this) {
        HomeTab.Dashboard -> R.string.home_tab_dashboard
        HomeTab.Sleep -> R.string.home_tab_sleep
        HomeTab.Activity -> R.string.home_tab_activity
    }
