package com.khrlanamm.jelajahsungaipulai.ui.nagari

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NagariViewModel : ViewModel() {

    // MutableLiveData to hold the loading state
    private val _isLoading = MutableLiveData<Boolean>()
    // Expose LiveData as immutable to the Fragment
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        // Initialize the ViewModel by simulating a data loading process
        // Set isLoading to true to show the progress bar initially
        _isLoading.value = true
        viewModelScope.launch {
            delay(2000) // Simulate a 2-second loading time
            // After the simulated loading, set isLoading to false to hide the progress bar
            _isLoading.value = false
        }
    }
}
