package com.khrlanamm.jelajahsungaipulai.data

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class StatistikPendudukTest {

    private val today: Calendar = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 25) }

    private fun lahir(year: Int, month: Int, day: Int): Timestamp =
        Timestamp(Calendar.getInstance().apply { set(year, month, day) }.time)

    private val warga = listOf(
        Warga(
            jenisKel = "L", hubKeluarga = "Kepala Keluarga", kampung = "TANAH NAGO", rt = 1,
            agama = "Islam", status = "Kawin", pendidikan = "SMA", pekerjaan = "Petani",
            tglLahir = lahir(1980, Calendar.JANUARY, 10)
        ),
        Warga(
            jenisKel = "P", hubKeluarga = "Istri", kampung = "TANAH NAGO", rt = 2,
            agama = "Islam", status = "Kawin", pendidikan = "SD", pekerjaan = "Petani",
            tglLahir = lahir(2021, Calendar.DECEMBER, 1)
        ),
        Warga(
            jenisKel = " p ", kampung = "SILAUT III", rt = 1,
            agama = "Islam", status = "Belum Kawin",
            tglLahir = lahir(2008, Calendar.NOVEMBER, 1)
        )
    )

    @Test
    fun hitungSeluruhNagari() {
        val s = StatistikPenduduk.hitung(warga, perRt = false, today = today)

        assertEquals(3, s.total)
        assertEquals(1, s.kepalaKeluarga)
        assertEquals(1, s.lakiLaki)
        assertEquals(2, s.perempuan)
        assertEquals(1, s.umur["0-5 Tahun"])
        // Ulang tahun ke-18 belum lewat pada 25 Oktober 2026
        assertEquals(1, s.umur["13-18 Tahun"])
        assertEquals(1, s.umur["31-50 Tahun"])
        assertEquals(mapOf("" to 1, "SD" to 1, "SMA" to 1), s.pendidikan)
        assertEquals(mapOf("SILAUT III" to 1, "TANAH NAGO" to 2), s.wilayah)
    }

    @Test
    fun hitungPerRt() {
        val s = StatistikPenduduk.hitung(warga.filter { it.kampung == "TANAH NAGO" }, perRt = true, today = today)

        assertEquals(2, s.total)
        assertEquals(mapOf("RT 1" to 1, "RT 2" to 1), s.wilayah)
        assertEquals(mapOf("Kawin" to 2), s.status)
    }
}
