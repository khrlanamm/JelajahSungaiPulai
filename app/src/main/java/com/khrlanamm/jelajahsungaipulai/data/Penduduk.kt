package com.khrlanamm.jelajahsungaipulai.data

import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

data class Penduduk(
    val documentId: String? = null,
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
    val rt: Long? = null,
    val status: String? = null,
    val tempat_lahir: String? = null,
    val tgl_lahir: String? = null
) {
    companion object {
        fun fromFirestore(doc: QueryDocumentSnapshot): Penduduk {
            val tglLahirTimestamp = doc.getTimestamp("tgl_lahir")
            val tglLahirString = tglLahirTimestamp?.toDate()?.let { date ->
                // Format tanggal sesuai kebutuhan, contoh: "dd MMMM yyyy" untuk lokal Indonesia
                SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(date)
            }
            return Penduduk(
                documentId = doc.id,
                agama = doc.getString("agama"),
                hub_keluarga = doc.getString("hub_keluarga"),
                jenis_kel = doc.getString("jenis_kel"),
                kampung = doc.getString("kampung"),
                kep_kel = doc.getString("kep_kel"),
                nama = doc.getString("nama"),
                nik = doc.getString("nik"),
                no_kk = doc.getString("no_kk"),
                pekerjaan = doc.getString("pekerjaan"),
                pendidikan = doc.getString("pendidikan"),
                rt = doc.getLong("rt"),
                status = doc.getString("status"),
                tempat_lahir = doc.getString("tempat_lahir"),
                tgl_lahir = tglLahirString
            )
        }
    }
}
