package com.screen.autolocker.ui

enum class AppScreen {
    TIMER,
    HISTORY,
    SETTINGS;

    val route: String
        get() = name.lowercase()

    companion object {
        fun fromRoute(route: String?): AppScreen {
            return entries.firstOrNull { it.route == route } ?: TIMER
        }
    }
}
