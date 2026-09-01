package com.fitdroid.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

private val DarkColorScheme =
    darkColorScheme(
        primary = SleepTeal80,
        secondary = SleepTealGrey80,
        tertiary = RestorativeBlue80,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = SleepTeal40,
        secondary = SleepTealGrey40,
        tertiary = RestorativeBlue40,
    )

@Composable
fun FitdroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .build()
    }

    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme

            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitdroidTypography,
        content = content,
    )
}
