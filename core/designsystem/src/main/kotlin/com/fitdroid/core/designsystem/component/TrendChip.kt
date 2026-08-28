package com.fitdroid.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitdroid.core.designsystem.theme.FitdroidTheme

enum class TrendDirection {
    Up,
    Down,
    Flat,
}

@Composable
fun TrendChip(
    text: String,
    direction: TrendDirection,
    modifier: Modifier = Modifier,
) {
    val arrow = when (direction) {
        TrendDirection.Up -> "▲"
        TrendDirection.Down -> "▼"
        TrendDirection.Flat -> "●"
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "$arrow $text",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrendChipPreview() {
    FitdroidTheme(dynamicColor = false) {
        TrendChip(text = "+4 vs avg", direction = TrendDirection.Up)
    }
}
