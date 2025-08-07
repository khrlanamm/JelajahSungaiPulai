package com.khrlanamm.jelajahsungaipulai.data

import com.google.firebase.Timestamp

// Data class untuk merepresentasikan data warga/penduduk dari Firestore
data class Warga(
    val agama: String? = null,
    val hub_keluarga: String? = null,
    val jenis_kel: String? = null,
    val kampung: String? = null,
    val kep_kel: String? = null,
    val nama: String? = null,
    val nik: String? = null,
    val no_kk: String? = null,
    val pekerjaan: String? = null,
    val pendidikan: String? = null,
    val rt: Long? = null, // Menggunakan Long karena Firestore menyimpan angka sebagai Long
    val status: String? = null,
    val tempat_lahir: String? = null,
    val tgl_lahir: Timestamp? = null // Menggunakan Timestamp untuk tanggal dari Firestore
)

