package com.screen.autolocker.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AccessibilityLockService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        super.onDestroy()
    }

    fun performLockScreen(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
    }

    companion object {
        @Volatile
        private var instance: AccessibilityLockService? = null

        val isEnabled: Boolean
            get() = instance != null

        fun lockScreen(): Boolean {
            return instance?.performLockScreen() == true
        }
    }
}
