package com.khrlanamm.jelajahsungaipulai.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Menulis ulang semua dokumen `statistik` dari data penduduk yang baru dimuat admin.
 * Dipanggil setiap kali admin memuat daftar penduduk, termasuk setelah tambah, ubah,
 * dan hapus, sehingga kelompok umur ikut mengikuti tanggal hari ini.
 */
object StatistikPublisher {

    fun publish(db: FirebaseFirestore, wargaList: List<Warga>) {
        val collection = db.collection(StatistikPenduduk.COLLECTION)
        val batch = db.batch()

        batch.set(
            collection.document(StatistikPenduduk.SEMUA),
            StatistikPenduduk.hitung(wargaList, perRt = false).toFirestore()
        )

        val kampungList = wargaList.mapNotNull { it.kampung?.takeIf { k -> k.isNotBlank() } }.distinct()
        for (kampung in kampungList) {
            batch.set(
                collection.document(kampung),
                StatistikPenduduk.hitung(wargaList.filter { it.kampung == kampung }, perRt = true).toFirestore()
            )
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("StatistikPublisher", "Statistik diperbarui untuk ${kampungList.size} kampung.")
            }
            .addOnFailureListener { e ->
                Log.w("StatistikPublisher", "Gagal memperbarui statistik", e)
            }
    }
}
