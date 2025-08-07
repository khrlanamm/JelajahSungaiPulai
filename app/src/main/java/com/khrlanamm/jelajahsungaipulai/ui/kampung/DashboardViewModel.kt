package com.khrlanamm.jelajahsungaipulai.ui.kampung

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is kampung Fragment"
    }
    val text: LiveData<String> = _text
}