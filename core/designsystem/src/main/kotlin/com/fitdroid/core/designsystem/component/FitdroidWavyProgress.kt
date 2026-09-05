package com.fitdroid.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fitdroid.core.designsystem.theme.FitdroidTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FitdroidWavyProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    LinearWavyProgressIndicator(
        progress = progress,
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun FitdroidWavyProgressPreview() {
    FitdroidTheme(dynamicColor = false) {
        FitdroidWavyProgress(progress = { 0.68f })
    }
}
