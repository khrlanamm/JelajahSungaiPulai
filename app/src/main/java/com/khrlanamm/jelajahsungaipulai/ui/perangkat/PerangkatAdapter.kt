package com.khrlanamm.jelajahsungaipulai.ui.perangkat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.khrlanamm.jelajahsungaipulai.data.Perangkat
import com.khrlanamm.jelajahsungaipulai.databinding.ItemPerangkatBinding

class PerangkatAdapter(private val listPerangkat: List<Perangkat>) :
    RecyclerView.Adapter<PerangkatAdapter.PerangkatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerangkatViewHolder {
        val binding =
            ItemPerangkatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PerangkatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PerangkatViewHolder, position: Int) {
        holder.bind(listPerangkat[position])
    }

    override fun getItemCount(): Int = listPerangkat.size

    inner class PerangkatViewHolder(private val binding: ItemPerangkatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(perangkat: Perangkat) {
            binding.tvNamaPerangkat.text = perangkat.nama
            binding.tvJabatanPerangkat.text = perangkat.jabatan

            val cornerRadius = (12 * itemView.context.resources.displayMetrics.density + 0.5f).toInt()

            Glide.with(itemView.context)
                .load(perangkat.imageResId)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .into(binding.ivFotoPerangkat)
        }
    }
}
