package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PenduduklViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is penduduk Fragment"
    }
    val text: LiveData<String> = _text
}