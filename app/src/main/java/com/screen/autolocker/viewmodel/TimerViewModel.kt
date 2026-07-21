package com.screen.autolocker.viewmodel

import android.os.Build
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screen.autolocker.data.SettingsState
import com.screen.autolocker.data.SystemAccessRepository
import com.screen.autolocker.data.TimerState
import com.screen.autolocker.data.SettingsRepository
import com.screen.autolocker.data.TimerRepository
import com.screen.autolocker.service.TimerInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StartValidation(
    val canStart: Boolean,
    val missingPermissions: List<String> = emptyList()
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val timerRepo: TimerRepository,
    private val settingsRepo: SettingsRepository,
    private val timerInteractor: TimerInteractor,
    private val systemAccessRepository: SystemAccessRepository
) : ViewModel() {

    private var countdownTimer: CountDownTimer? = null
    private val _remaining = MutableStateFlow(0L)
    private val _selectedMinutes = MutableStateFlow(15f)

    val remaining: StateFlow<Long> = _remaining
    val selectedMinutes: StateFlow<Float> = _selectedMinutes

    val settings: StateFlow<SettingsState> = settingsRepo.state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SettingsState()
    )

    val state: StateFlow<TimerState> =
        timerRepo.state.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            TimerState()
        )

    val uiState = combine(state, settings, selectedMinutes) { timer, prefs, minutes ->
        Triple(timer, prefs, minutes)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Triple(TimerState(), SettingsState(), 15f)
    )

    init {
        viewModelScope.launch {
            settings.collect { prefs ->
                if (!state.value.isActive) {
                    _selectedMinutes.value = prefs.lastMinutes.toFloat()
                }
            }
        }
        viewModelScope.launch {
            state.collect { timerState ->
                syncCountdown(timerState)
            }
        }
    }

    fun updateSelectedMinutes(value: Float) {
        _selectedMinutes.value = value.coerceIn(1f, 120f)
    }

    fun applyPreset(minutes: Int) {
        if (state.value.isActive) {
            extend(minutes.toLong())
        } else {
            updateSelectedMinutes(minutes.toFloat())
        }
    }

    fun reuseMinutes(minutes: Int) {
        updateSelectedMinutes(minutes.toFloat())
        viewModelScope.launch { settingsRepo.setLastMinutes(minutes) }
    }

    fun validateBeforeStart(
        notificationsGranted: Boolean,
        accessibilityGranted: Boolean,
        deviceAdminGranted: Boolean
    ): StartValidation {
        val missing = buildList {
            if (!notificationsGranted) add("Notifications")
            if (!systemAccessRepository.isBatteryOptimizationIgnored()) {
                add("Battery optimization access")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !systemAccessRepository.canScheduleExactAlarms()) {
                add("Exact alarms")
            }
            if (!accessibilityGranted) add("Accessibility Lock")
            if (!deviceAdminGranted) add("Device Admin (fallback)")
        }.distinct()
        return StartValidation(missing.isEmpty(), missing)
    }

    fun startTimer() = viewModelScope.launch {
        val minutes = selectedMinutes.value.toLong()
        timerInteractor.start(minutes)
    }

    fun stopTimer() = viewModelScope.launch {
        timerInteractor.stop()
    }

    fun togglePause() = viewModelScope.launch {
        timerInteractor.togglePause()
    }

    fun extend(minutes: Long = 10L) = viewModelScope.launch {
        timerRepo.extend(minutes)
    }

    private fun syncCountdown(timerState: TimerState) {
        countdownTimer?.cancel()
        when {
            !timerState.isActive -> _remaining.value = 0L
            timerState.isPaused -> _remaining.value = timerState.remainingMs
            else -> {
                val initial = (timerState.endTime - System.currentTimeMillis()).coerceAtLeast(0L)
                _remaining.value = initial
                countdownTimer = object : CountDownTimer(initial, 1000L) {
                    override fun onTick(millisUntilFinished: Long) {
                        _remaining.value = millisUntilFinished.coerceAtLeast(0L)
                    }

                    override fun onFinish() {
                        _remaining.value = 0L
                    }
                }.start()
            }
        }
    }

    override fun onCleared() {
        countdownTimer?.cancel()
        super.onCleared()
    }
}
