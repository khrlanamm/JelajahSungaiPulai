package com.khrlanamm.jelajahsungaipulai.data

import androidx.annotation.DrawableRes

data class Perangkat(
    val nama: String,
    val jabatan: String,
    @DrawableRes val imageResId: Int
)
