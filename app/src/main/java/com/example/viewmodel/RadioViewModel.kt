package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RadioRepository
import com.example.data.model.RadioDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RadioViewModel : ViewModel() {
    private val _radios = MutableStateFlow<List<RadioDto>>(emptyList())
    val radios = _radios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchRadios()
    }

    fun fetchRadios() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _radios.value = RadioRepository.getRadios()
            } catch (e: Exception) {
                // Keep existing or handle cleanly
            } finally {
                _isLoading.value = false
            }
        }
    }
}
