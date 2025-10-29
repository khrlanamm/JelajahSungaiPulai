package com.khrlanamm.jelajahsungaipulai.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Warga(
    var agama: String? = null,

    @get:PropertyName("hub_keluarga")
    @set:PropertyName("hub_keluarga")
    var hubKeluarga: String? = null,

    @get:PropertyName("jenis_kel")
    @set:PropertyName("jenis_kel")
    var jenisKel: String? = null,

    var kampung: String? = null,

    @get:PropertyName("kep_kel")
    @set:PropertyName("kep_kel")
    var kepKel: String? = null,

    var nama: String? = null,
    var nik: String? = null,

    @get:PropertyName("no_kk")
    @set:PropertyName("no_kk")
    var noKk: String? = null,

    var pekerjaan: String? = null,
    var pendidikan: String? = null,
    var rt: Long? = null,
    var status: String? = null,

    @get:PropertyName("tempat_lahir")
    @set:PropertyName("tempat_lahir")
    var tempatLahir: String? = null,

    @get:PropertyName("tgl_lahir")
    @set:PropertyName("tgl_lahir")
    var tglLahir: Timestamp? = null
)