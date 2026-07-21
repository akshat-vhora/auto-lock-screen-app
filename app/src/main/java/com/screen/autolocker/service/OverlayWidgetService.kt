package com.screen.autolocker.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.CountDownTimer
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.screen.autolocker.R
import com.screen.autolocker.data.SettingsRepository
import com.screen.autolocker.data.TimerRepository
import com.screen.autolocker.ui.theme.paletteFor
import com.screen.autolocker.utils.formatTime
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OverlayWidgetService : Service() {
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var timerRepository: TimerRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observerJob: Job? = null
    private var countdownTimer: CountDownTimer? = null

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var overlayText: TextView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!settingsRepository.snapshot().overlayEnabled || !Settings.canDrawOverlays(this)) {
            removeOverlay()
            stopSelf()
            return START_NOT_STICKY
        }

        observerJob?.cancel()
        observerJob = scope.launch {
            timerRepository.state.collectLatest { state ->
                val overlayEnabled = settingsRepository.snapshot().overlayEnabled
                if (!overlayEnabled ||
                    !Settings.canDrawOverlays(this@OverlayWidgetService) ||
                    !state.isActive
                ) {
                    removeOverlay()
                    if (!state.isActive) {
                        stopSelf()
                    }
                    return@collectLatest
                }

                ensureOverlay()
                updateOverlayCountdown(state)
            }
        }

        return START_STICKY
    }

    private fun ensureOverlay() {
        if (overlayView != null) return

        val theme = settingsRepository.snapshot().theme
        val palette = paletteFor(theme, false)

        val textView = TextView(this).apply {
            text = "00:00"
            textSize = 16f
            setTextColor(palette.buttonText.toArgb())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(34, 20, 34, 20)
            background = ContextCompat.getDrawable(this@OverlayWidgetService, R.drawable.overlay_widget_bg)
        }

        val container = FrameLayout(this).apply {
            addView(textView)
            setOnTouchListener(DragTouchListener())
        }

        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 220
        }

        windowManager.addView(container, params)
        overlayView = container
        overlayText = textView
        layoutParams = params
    }

    private fun removeOverlay() {
        countdownTimer?.cancel()
        countdownTimer = null
        overlayView?.let {
            runCatching { windowManager.removeView(it) }
        }
        overlayView = null
        overlayText = null
        layoutParams = null
    }

    override fun onDestroy() {
        observerJob?.cancel()
        countdownTimer?.cancel()
        removeOverlay()
        scope.cancel()
        super.onDestroy()
    }

    private fun updateOverlayCountdown(state: com.screen.autolocker.data.TimerState) {
        countdownTimer?.cancel()
        val remaining = if (state.isPaused) {
            state.remainingMs
        } else {
            (state.endTime - System.currentTimeMillis()).coerceAtLeast(0L)
        }
        overlayText?.text = formatTime(remaining)
        if (state.isPaused) return

        countdownTimer = object : CountDownTimer(remaining, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                overlayText?.text = formatTime(millisUntilFinished.coerceAtLeast(0L))
            }

            override fun onFinish() {
                overlayText?.text = formatTime(0L)
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private inner class DragTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val params = layoutParams ?: return false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(v, params)
                    return true
                }
            }
            return false
        }
    }
}
