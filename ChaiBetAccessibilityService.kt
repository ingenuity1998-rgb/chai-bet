package com.chaibet.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import java.util.Calendar

class ChaiBetAccessibilityService : AccessibilityService() {

    private lateinit var prefs: AppPreferences
    private val blacklistedApps = setOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.twitter.android",
        "com.reddit.frontpage"
    )

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return

        if (blacklistedApps.contains(pkgName)) {
            // Check if active session is already unlocked
            if (prefs.isGraceSessionActive()) {
                return
            }

            // Check if within focus hours (e.g. 09:00 - 18:00)
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (currentHour in 9..18) {
                launchInterlockOverlay(pkgName)
            }
        }
    }

    private fun launchInterlockOverlay(triggerPackage: String) {
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("EXTRA_PACKAGE_NAME", triggerPackage)
        }
        startService(intent)
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }
}
