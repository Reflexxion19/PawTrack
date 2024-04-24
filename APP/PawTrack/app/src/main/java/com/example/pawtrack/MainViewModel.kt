package com.example.pawtrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel(){
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()
    //galima ideti logino checka
    init{
        viewModelScope.launch {
            delay(1300L)
            _isReady.value = true
        }
    }
}