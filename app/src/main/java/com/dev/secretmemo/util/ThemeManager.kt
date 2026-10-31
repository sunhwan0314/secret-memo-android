package com.dev.secretmemo.util

import android.content.Context
import android.graphics.Color
import androidx.core.content.edit
import com.dev.secretmemo.R
import java.util.Calendar

enum class ThemeMode(val key: String, val displayName: String) {
    AUTO("AUTO", "⚙️ 계절 자동 감지"),
    WINTER("WINTER", "🎄 크리스마스 & 겨울"),
    SPRING("SPRING", "🌸 봄 벚꽃"),
    SUMMER("SUMMER", "☀️ 여름 바다"),
    AUTUMN("AUTUMN", "🍁 가을 단풍");

    companion object {
        fun fromKey(key: String): ThemeMode {
            return entries.firstOrNull { it.key == key } ?: AUTO
        }
    }
}

data class ThemePalette(
    val primaryColor: Int,
    val primaryDarkColor: Int,
    val accentColor: Int,
    val backgroundColor: Int,
    val backgroundResId: Int,
    val emoji: String,
    val name: String
)

object ThemeManager {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "KEY_THEME_MODE"

    fun getSavedThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = prefs.getString(KEY_THEME_MODE, ThemeMode.AUTO.key) ?: ThemeMode.AUTO.key
        return ThemeMode.fromKey(key)
    }

    fun saveThemeMode(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_THEME_MODE, mode.key)
        }
    }

    fun getThemePalette(context: Context): ThemePalette {
        var mode = getSavedThemeMode(context)
        if (mode == ThemeMode.AUTO) {
            mode = getAutoSeasonThemeMode()
        }

        return when (mode) {
            ThemeMode.WINTER -> ThemePalette(
                primaryColor = Color.parseColor("#1E293B"),    // Slate Charcoal
                primaryDarkColor = Color.parseColor("#0F172A"),
                accentColor = Color.parseColor("#DC2626"),     // Crimson Accent
                backgroundColor = Color.parseColor("#F8FAFC"),
                backgroundResId = R.drawable.bg_theme_winter,
                emoji = "🎄",
                name = "크리스마스 & 겨울"
            )
            ThemeMode.SPRING -> ThemePalette(
                primaryColor = Color.parseColor("#1E293B"),    // Slate Charcoal
                primaryDarkColor = Color.parseColor("#0F172A"),
                accentColor = Color.parseColor("#E11D48"),     // Rose Accent
                backgroundColor = Color.parseColor("#F8FAFC"),
                backgroundResId = R.drawable.bg_theme_spring,
                emoji = "🌸",
                name = "봄 벚꽃"
            )
            ThemeMode.SUMMER -> ThemePalette(
                primaryColor = Color.parseColor("#1E293B"),    // Slate Charcoal
                primaryDarkColor = Color.parseColor("#0F172A"),
                accentColor = Color.parseColor("#2563EB"),     // Royal Blue Accent
                backgroundColor = Color.parseColor("#F8FAFC"),
                backgroundResId = R.drawable.bg_theme_summer,
                emoji = "☀️",
                name = "여름 바다"
            )
            ThemeMode.AUTUMN -> ThemePalette(
                primaryColor = Color.parseColor("#1E293B"),    // Slate Charcoal
                primaryDarkColor = Color.parseColor("#0F172A"),
                accentColor = Color.parseColor("#D97706"),     // Amber Accent
                backgroundColor = Color.parseColor("#F8FAFC"),
                backgroundResId = R.drawable.bg_theme_autumn,
                emoji = "🍁",
                name = "가을 단풍"
            )
            ThemeMode.AUTO -> getThemePaletteForMode(getAutoSeasonThemeMode())
        }
    }

    private fun getThemePaletteForMode(mode: ThemeMode): ThemePalette {
        return when (mode) {
            ThemeMode.WINTER -> ThemePalette(
                primaryColor = Color.parseColor("#1E293B"),
                primaryDarkColor = Color.parseColor("#0F172A"),
                accentColor = Color.parseColor("#DC2626"),
                backgroundColor = Color.parseColor("#F8FAFC"),
                backgroundResId = R.drawable.bg_theme_winter,
                emoji = "🎄",
                name = "크리스마스 & 겨울 (자동)"
            )
            ThemeMode.SPRING -> ThemePalette(
                primaryColor = Color.parseColor("#1E293B"),
                primaryDarkColor = Color.parseColor("#0F172A"),
                accentColor = Color.parseColor("#E11D48"),
                backgroundColor = Color.parseColor("#F8FAFC"),
                backgroundResId = R.drawable.bg_theme_spring,
                emoji = "🌸",
                name = "봄 벚꽃 (자동)"
            )
            ThemeMode.SUMMER -> ThemePalette(
                primaryColor = Color.parseColor("#1E293B"),
                primaryDarkColor = Color.parseColor("#0F172A"),
                accentColor = Color.parseColor("#2563EB"),
                backgroundColor = Color.parseColor("#F8FAFC"),
                backgroundResId = R.drawable.bg_theme_summer,
                emoji = "☀️",
                name = "여름 바다 (자동)"
            )
            ThemeMode.AUTUMN -> ThemePalette(
                primaryColor = Color.parseColor("#1E293B"),
                primaryDarkColor = Color.parseColor("#0F172A"),
                accentColor = Color.parseColor("#D97706"),
                backgroundColor = Color.parseColor("#F8FAFC"),
                backgroundResId = R.drawable.bg_theme_autumn,
                emoji = "🍁",
                name = "가을 단풍 (자동)"
            )
            ThemeMode.AUTO -> getThemePaletteForMode(ThemeMode.WINTER)
        }
    }

    private fun getAutoSeasonThemeMode(): ThemeMode {
        val month = Calendar.getInstance().get(Calendar.MONTH) // 0-based
        return when (month) {
            Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> ThemeMode.WINTER
            Calendar.MARCH, Calendar.APRIL, Calendar.MAY -> ThemeMode.SPRING
            Calendar.JUNE, Calendar.JULY, Calendar.AUGUST -> ThemeMode.SUMMER
            else -> ThemeMode.AUTUMN
        }
    }
}
