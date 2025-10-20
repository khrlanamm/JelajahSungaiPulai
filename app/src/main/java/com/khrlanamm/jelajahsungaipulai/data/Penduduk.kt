package com.khrlanamm.jelajahsungaipulai.data

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

data class Penduduk(
    val documentId: String? = null,
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

    val tglLahir: String? = null
) {
    companion object {
        fun fromFirestore(doc: QueryDocumentSnapshot): Penduduk {
            val tglLahirTimestamp = doc.getTimestamp("tgl_lahir")
            val tglLahirString = tglLahirTimestamp?.toDate()?.let { date ->
                SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(date)
            }
            return Penduduk(
                documentId = doc.id,
                agama = doc.getString("agama"),
                hubKeluarga = doc.getString("hub_keluarga"),
                jenisKel = doc.getString("jenis_kel"),
                kampung = doc.getString("kampung"),
                kepKel = doc.getString("kep_kel"),
                nama = doc.getString("nama"),
                nik = doc.getString("nik"),
                noKk = doc.getString("no_kk"),
                pekerjaan = doc.getString("pekerjaan"),
                pendidikan = doc.getString("pendidikan"),
                rt = doc.getLong("rt"),
                status = doc.getString("status"),
                tempatLahir = doc.getString("tempat_lahir"),
                tglLahir = tglLahirString
            )
        }
    }
}
