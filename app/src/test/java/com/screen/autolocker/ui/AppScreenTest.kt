package com.screen.autolocker.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppScreenTest {

    @Test
    fun fromRoute_returnsMatchingScreen() {
        assertEquals(AppScreen.TIMER, AppScreen.fromRoute("timer"))
        assertEquals(AppScreen.HISTORY, AppScreen.fromRoute("history"))
        assertEquals(AppScreen.SETTINGS, AppScreen.fromRoute("settings"))
    }

    @Test
    fun fromRoute_fallsBackToTimer() {
        assertEquals(AppScreen.TIMER, AppScreen.fromRoute(null))
        assertEquals(AppScreen.TIMER, AppScreen.fromRoute("unknown"))
    }
}
