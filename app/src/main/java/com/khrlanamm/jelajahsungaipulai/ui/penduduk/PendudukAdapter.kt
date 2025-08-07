package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.data.Penduduk // Import kelas Penduduk dari folder data

// RecyclerView Adapter untuk data Penduduk
class PendudukAdapter(private val onItemClick: (Penduduk) -> Unit) :
    RecyclerView.Adapter<PendudukAdapter.PendudukViewHolder>() {

    private var pendudukList: List<Penduduk> = emptyList()

    // Fungsi untuk memperbarui data di adapter
    fun submitList(newList: List<Penduduk>) {
        pendudukList = newList
        notifyDataSetChanged() // Beri tahu RecyclerView bahwa data telah berubah
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendudukViewHolder {
        // Inflate layout item_penduduk.xml untuk setiap item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_penduduk, parent, false)
        return PendudukViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendudukViewHolder, position: Int) {
        val penduduk = pendudukList[position]
        holder.bind(penduduk)
        // Atur pendengar klik untuk seluruh tampilan item
        holder.itemView.setOnClickListener { onItemClick(penduduk) }
    }

    override fun getItemCount(): Int = pendudukList.size

    // Kelas ViewHolder untuk menampung referensi ke tampilan di item_penduduk.xml
    class PendudukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tv_nama)
        private val tvNik: TextView = itemView.findViewById(R.id.tv_nik)
        private val tvRtKampung: TextView = itemView.findViewById(R.id.tv_rt_kampung)

        fun bind(penduduk: Penduduk) {
            // Ikat data ke TextViews
            tvNama.text = "Nama: ${penduduk.nama ?: "-"}"
            tvNik.text = "NIK: ${penduduk.nik ?: "-"}"
            tvRtKampung.text = "RT: ${penduduk.rt ?: "-"}, Kampung: ${penduduk.kampung ?: "-"}"
        }
    }
}
