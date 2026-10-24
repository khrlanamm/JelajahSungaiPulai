package com.khrlanamm.jelajahsungaipulai.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import java.util.Calendar
import java.util.Date

/**
 * Ringkasan demografi tanpa data pribadi (nama, NIK, No KK, tanggal lahir).
 *
 * Disimpan di koleksi `statistik` supaya warga biasa bisa melihat Demografi
 * tanpa membaca koleksi `penduduk`, yang hanya boleh dibaca admin.
 * Dokumen `semua` berisi seluruh nagari, dan satu dokumen per kampung
 * (ID dokumen = nilai field `kampung`, misalnya "TANAH NAGO").
 */
data class StatistikPenduduk(
    val total: Int,
    val kepalaKeluarga: Int,
    val lakiLaki: Int,
    val perempuan: Int,
    val umur: Map<String, Int>,
    val pendidikan: Map<String, Int>,
    val pekerjaan: Map<String, Int>,
    val status: Map<String, Int>,
    val agama: Map<String, Int>,
    val wilayah: Map<String, Int>
) {

    // Peta disimpan sebagai array {label, jumlah} agar urutan tetap dan label kosong tetap boleh.
    fun toFirestore(): Map<String, Any> = mapOf(
        "total" to total,
        "kepala_keluarga" to kepalaKeluarga,
        "laki_laki" to lakiLaki,
        "perempuan" to perempuan,
        "umur" to umur.toEntries(),
        "pendidikan" to pendidikan.toEntries(),
        "pekerjaan" to pekerjaan.toEntries(),
        "status" to status.toEntries(),
        "agama" to agama.toEntries(),
        "wilayah" to wilayah.toEntries(),
        "diperbarui" to FieldValue.serverTimestamp()
    )

    companion object {
        const val COLLECTION = "statistik"
        const val SEMUA = "semua"

        val KELOMPOK_UMUR = listOf(
            "0-5 Tahun", "6-12 Tahun", "13-18 Tahun",
            "19-30 Tahun", "31-50 Tahun", "51+ Tahun"
        )

        /**
         * Menghitung statistik dari data mentah. [perRt] true untuk satu kampung
         * (wilayah dihitung per RT), false untuk seluruh nagari (per kampung).
         */
        fun hitung(
            wargaList: List<Warga>,
            perRt: Boolean,
            today: Calendar = Calendar.getInstance()
        ): StatistikPenduduk {
            val umur = KELOMPOK_UMUR.associateWith { 0 }.toMutableMap()
            for (w in wargaList) {
                val birthDate = w.tglLahir?.toDate() ?: continue
                val label = kelompokUmur(getAge(birthDate, today)) ?: continue
                umur[label] = umur.getValue(label) + 1
            }

            val wilayah = mutableMapOf<String, Int>()
            if (perRt) {
                for (w in wargaList) {
                    w.rt?.let { rt ->
                        val rtLabel = "RT $rt"
                        wilayah[rtLabel] = wilayah.getOrDefault(rtLabel, 0) + 1
                    }
                }
            } else {
                for (w in wargaList) {
                    w.kampung?.trim()?.takeIf { it.isNotEmpty() }?.let { kampung ->
                        wilayah[kampung] = wilayah.getOrDefault(kampung, 0) + 1
                    }
                }
            }

            return StatistikPenduduk(
                total = wargaList.size,
                kepalaKeluarga = wargaList.count { w ->
                    w.hubKeluarga?.trim()?.equals("Kepala Keluarga", ignoreCase = true) == true
                },
                lakiLaki = wargaList.count { w -> w.jenisKel?.trim()?.uppercase() == "L" },
                perempuan = wargaList.count { w -> w.jenisKel?.trim()?.uppercase() == "P" },
                umur = umur,
                pendidikan = wargaList.groupingBy { it.pendidikan.orEmpty().trim() }.eachCount().toSortedMap(),
                pekerjaan = wargaList.groupingBy { it.pekerjaan.orEmpty().trim() }.eachCount().toSortedMap(),
                status = wargaList.groupingBy { it.status.orEmpty().trim() }.eachCount(),
                agama = wargaList.groupingBy { it.agama.orEmpty().trim() }.eachCount(),
                wilayah = wilayah.toSortedMap()
            )
        }

        fun fromFirestore(doc: DocumentSnapshot): StatistikPenduduk? {
            if (!doc.exists()) return null
            return StatistikPenduduk(
                total = doc.getLong("total")?.toInt() ?: 0,
                kepalaKeluarga = doc.getLong("kepala_keluarga")?.toInt() ?: 0,
                lakiLaki = doc.getLong("laki_laki")?.toInt() ?: 0,
                perempuan = doc.getLong("perempuan")?.toInt() ?: 0,
                umur = entries(doc, "umur"),
                pendidikan = entries(doc, "pendidikan"),
                pekerjaan = entries(doc, "pekerjaan"),
                status = entries(doc, "status"),
                agama = entries(doc, "agama"),
                wilayah = entries(doc, "wilayah")
            )
        }

        private fun getAge(birthDate: Date, today: Calendar): Int {
            val birth = Calendar.getInstance()
            birth.time = birthDate

            var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            return age
        }

        private fun kelompokUmur(age: Int): String? = when {
            age in 0..5 -> "0-5 Tahun"
            age in 6..12 -> "6-12 Tahun"
            age in 13..18 -> "13-18 Tahun"
            age in 19..30 -> "19-30 Tahun"
            age in 31..50 -> "31-50 Tahun"
            age > 50 -> "51+ Tahun"
            else -> null
        }

        private fun Map<String, Int>.toEntries(): List<Map<String, Any>> =
            map { (label, jumlah) -> mapOf("label" to label, "jumlah" to jumlah) }

        private fun entries(doc: DocumentSnapshot, field: String): Map<String, Int> {
            val list = doc.get(field) as? List<*> ?: return emptyMap()
            val result = LinkedHashMap<String, Int>()
            for (item in list) {
                val entry = item as? Map<*, *> ?: continue
                val label = entry["label"] as? String ?: continue
                result[label] = (entry["jumlah"] as? Number)?.toInt() ?: 0
            }
            return result
        }
    }
}
