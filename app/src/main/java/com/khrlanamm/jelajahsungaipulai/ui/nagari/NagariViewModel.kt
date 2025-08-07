package com.khrlanamm.jelajahsungaipulai.ui.nagari

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NagariViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is nagari Fragment"
    }
    val text: LiveData<String> = _text
}