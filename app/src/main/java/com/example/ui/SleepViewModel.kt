package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SleepLog
import com.example.data.SleepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormState(
    val id: Long = 0,
    val sleepTime: Long = System.currentTimeMillis() - 8 * 3600 * 1000, // Default: 8 hours ago
    val wakeTime: Long = System.currentTimeMillis(),
    val sleepQuality: Int = 3,
    val mood: Int = 3,
    val note: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class SleepViewModel(private val repository: SleepRepository) : ViewModel() {

    // Expose all logs reactively from Room
    val allLogs: StateFlow<List<SleepLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    fun resetForm() {
        _formState.update {
            FormState(
                sleepTime = System.currentTimeMillis() - 8 * 3600 * 1000,
                wakeTime = System.currentTimeMillis()
            )
        }
    }

    fun loadLogForEdit(id: Long) {
        viewModelScope.launch {
            val log = repository.getLogById(id)
            if (log != null) {
                _formState.update {
                    FormState(
                        id = log.id,
                        sleepTime = log.sleepTime,
                        wakeTime = log.wakeTime,
                        sleepQuality = log.sleepQuality,
                        mood = log.mood,
                        note = log.note ?: "",
                        isSaving = false,
                        error = null,
                        isSuccess = false
                    )
                }
            } else {
                _formState.update { it.copy(error = "Data log tidak ditemukan.") }
            }
        }
    }

    fun updateSleepTime(time: Long) {
        _formState.update { it.copy(sleepTime = time) }
    }

    fun updateWakeTime(time: Long) {
        _formState.update { it.copy(wakeTime = time) }
    }

    fun updateSleepQuality(quality: Int) {
        _formState.update { it.copy(sleepQuality = quality.coerceIn(1, 5)) }
    }

    fun updateMood(mood: Int) {
        _formState.update { it.copy(mood = mood.coerceIn(1, 5)) }
    }

    fun updateNote(note: String) {
        if (note.length <= 280) {
            _formState.update { it.copy(note = note) }
        }
    }

    fun saveLog(onSuccess: () -> Unit) {
        val current = _formState.value
        if (current.isSaving) return // Prevent double submission

        // Quick client-side validation for immediate feedback
        if (current.wakeTime <= current.sleepTime) {
            _formState.update { it.copy(error = "Waktu bangun harus setelah waktu tidur.") }
            return
        }
        if (current.note.length > 280) {
            _formState.update { it.copy(error = "Catatan maksimal 280 karakter.") }
            return
        }

        _formState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val log = SleepLog(
                    id = current.id,
                    sleepTime = current.sleepTime,
                    wakeTime = current.wakeTime,
                    sleepQuality = current.sleepQuality,
                    mood = current.mood,
                    note = current.note.trim().ifEmpty { null }
                )

                if (current.id == 0L) {
                    repository.insertLog(log)
                } else {
                    repository.updateLog(log)
                }

                _formState.update { it.copy(isSaving = false, isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _formState.update { it.copy(isSaving = false, error = e.localizedMessage ?: "Terjadi kesalahan sistem.") }
            }
        }
    }

    fun deleteLog(log: SleepLog) {
        viewModelScope.launch {
            try {
                repository.deleteLog(log)
            } catch (e: Exception) {
                // handle error silently or log
            }
        }
    }
}

class SleepViewModelFactory(private val repository: SleepRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SleepViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
