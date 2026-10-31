package com.dev.secretmemo.util

import android.content.Context
import android.graphics.Color

data class ThemePalette(
    val primaryColor: Int = Color.parseColor("#09090B"),
    val primaryDarkColor: Int = Color.parseColor("#000000"),
    val accentColor: Int = Color.parseColor("#09090B"),
    val backgroundColor: Int = Color.parseColor("#FFFFFF")
)

object ThemeManager {
    fun getThemePalette(context: Context): ThemePalette {
        return ThemePalette()
    }
}
