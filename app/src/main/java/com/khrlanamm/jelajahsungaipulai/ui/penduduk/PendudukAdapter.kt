package com.khrlanamm.jelajahsungaipulai.ui.penduduk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.data.Penduduk

class PendudukAdapter(private val onItemClick: (Penduduk) -> Unit) :
    RecyclerView.Adapter<PendudukAdapter.PendudukViewHolder>() {

    private var pendudukList: List<Penduduk> = emptyList()

    fun submitList(newList: List<Penduduk>) {
        pendudukList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendudukViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_penduduk, parent, false)
        return PendudukViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendudukViewHolder, position: Int) {
        val penduduk = pendudukList[position]
        holder.bind(penduduk)
        holder.itemView.setOnClickListener { onItemClick(penduduk) }
    }

    override fun getItemCount(): Int = pendudukList.size

    class PendudukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tv_nama)
        private val tvNik: TextView = itemView.findViewById(R.id.tv_nik)
        private val tvRtKampung: TextView = itemView.findViewById(R.id.tv_rt_kampung)
        private val context = itemView.context

        fun bind(penduduk: Penduduk) {
            tvNama.text = context.getString(R.string.nama_format, penduduk.nama ?: "-")
            tvNik.text = context.getString(R.string.nik_format, penduduk.nik ?: "-")
            tvRtKampung.text = context.getString(
                R.string.rt_kampung_format,
                penduduk.rt?.toString() ?: "-",
                penduduk.kampung ?: "-"
            )
        }
    }
}
