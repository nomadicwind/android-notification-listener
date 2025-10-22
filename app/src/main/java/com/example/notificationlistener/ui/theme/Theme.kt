package com.example.notificationlistener.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 浅色主题颜色方案
 * 基于Material 3设计规范定义的颜色组合
 */
private val LightColors: ColorScheme = lightColorScheme(
    primary = Primary, // 主要颜色
    onPrimary = OnPrimary, // 在主要颜色上的文字颜色
    primaryContainer = PrimaryContainer, // 主要颜色容器背景色
    onPrimaryContainer = OnPrimaryContainer, // 在主要颜色容器上的文字颜色
    secondary = Secondary, // 次要颜色
    onSecondary = OnSecondary, // 在次要颜色上的文字颜色
    background = BackgroundLight, // 背景色
    onBackground = OnBackgroundLight, // 在背景色上的文字颜色
    surface = SurfaceLight, // 表面色（卡片、对话框等）
    onSurface = OnSurfaceLight // 在表面色上的文字颜色
)

/**
 * 深色主题颜色方案
 * 基于Material 3设计规范定义的颜色组合
 */
private val DarkColors: ColorScheme = darkColorScheme(
    primary = PrimaryDark, // 深色主题主要颜色
    onPrimary = OnPrimaryDark, // 在深色主题主要颜色上的文字颜色
    secondary = SecondaryDark, // 深色主题次要颜色
    onSecondary = OnSecondaryDark, // 在深色主题次要颜色上的文字颜色
    background = BackgroundDark, // 深色主题背景色
    onBackground = OnBackgroundDark, // 在深色主题背景色上的文字颜色
    surface = SurfaceDark, // 深色主题表面色
    onSurface = OnSurfaceDark // 在深色主题表面色上的文字颜色
)

@Composable
/**
 * 通知监听器主题组件
 * 提供应用的主题配置，支持动态颜色和深色/浅色主题
 * @param useDarkTheme 是否使用深色主题，默认根据系统设置决定
 * @param dynamicColor 是否使用动态颜色（Android 12+），默认为true
 * @param content 应用内容的Composable函数
 */
fun NotificationListenerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(), // 是否使用深色主题
    dynamicColor: Boolean = true, // 是否使用动态颜色
    content: @Composable () -> Unit // 应用内容
) {
    // 根据条件选择颜色方案
    val colorScheme = when {
        // 如果启用动态颜色且系统版本为Android 12及以上，使用系统动态颜色
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 如果使用深色主题，使用预定义的深色颜色方案
        useDarkTheme -> DarkColors
        // 否则使用预定义的浅色颜色方案
        else -> LightColors
    }

    // 应用Material 3主题
    MaterialTheme(
        colorScheme = colorScheme, // 颜色方案
        typography = Typography, // 字体排版方案
        content = content // 应用内容
    )
}
