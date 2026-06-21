package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AgeCalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("AgeCalcPrefs", Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    private val _ageResult = MutableStateFlow<AgeResult?>(null)
    val ageResult: StateFlow<AgeResult?> = _ageResult.asStateFlow()

    init {
        // Load the last saved birth date, if any
        val savedDateStr = sharedPrefs.getString("last_birth_date", null)
        if (savedDateStr != null) {
            try {
                val parsedDate = LocalDate.parse(savedDateStr, dateFormatter)
                _selectedDate.value = parsedDate
                // Auto compute if we stored a valid date previously
                _ageResult.value = AgeCalculator.calculateAge(parsedDate)
            } catch (e: Exception) {
                // Ignore parse errors from invalid saved data
            }
        }
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun calculateAge() {
        val date = _selectedDate.value ?: return
        val result = AgeCalculator.calculateAge(date)
        _ageResult.value = result

        // Save last calculated birthday to preferences
        sharedPrefs.edit()
            .putString("last_birth_date", date.format(dateFormatter))
            .apply()
    }

    fun reset() {
        _selectedDate.value = null
        _ageResult.value = null
        sharedPrefs.edit().remove("last_birth_date").apply()
    }
}
