package com.fitdroid.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.fitdroid.core.designsystem.theme.FitdroidTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeNavigationBarTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent {
            FitdroidTheme {
                HomeNavigationBar(selected = HomeTab.Dashboard, onSelect = {})
            }
        }
    }

    @Test
    fun tabs_areVisible() {
        composeTestRule.onNodeWithText("Today").assertExists()
        composeTestRule.onNodeWithText("Sleep").assertExists()
        composeTestRule.onNodeWithText("Activity").assertExists()
        composeTestRule.onNodeWithText("Reports").assertExists()
        composeTestRule.onNodeWithText("Settings").assertExists()
        composeTestRule.onNodeWithText("Today").assertIsSelected()
    }
}
