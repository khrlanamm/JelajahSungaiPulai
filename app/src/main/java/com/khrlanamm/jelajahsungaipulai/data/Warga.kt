package com.khrlanamm.jelajahsungaipulai.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Warga(
    val agama: String? = null,
    @get:PropertyName("hub_keluarga") val hubKeluarga: String? = null,
    @get:PropertyName("jenis_kel") val jenisKel: String? = null,
    val kampung: String? = null,
    @get:PropertyName("kep_kel") val kepKel: String? = null,
    val nama: String? = null,
    val nik: String? = null,
    @get:PropertyName("no_kk") val noKk: String? = null,
    val pekerjaan: String? = null,
    val pendidikan: String? = null,
    val rt: Long? = null,
    val status: String? = null,
    @get:PropertyName("tempat_lahir") val tempatLahir: String? = null,
    @get:PropertyName("tgl_lahir") val tglLahir: Timestamp? = null
)
