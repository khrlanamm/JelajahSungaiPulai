package com.khrlanamm.jelajahsungaipulai.ui.perangkat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.khrlanamm.jelajahsungaipulai.R
import com.khrlanamm.jelajahsungaipulai.data.Perangkat
import com.khrlanamm.jelajahsungaipulai.databinding.ItemPerangkatBinding
import com.khrlanamm.jelajahsungaipulai.databinding.ItemPerangkatFeaturedBinding

class PerangkatAdapter(private val listPerangkat: List<Perangkat>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_FEATURED = 0
        const val VIEW_TYPE_GRID = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_FEATURED else VIEW_TYPE_GRID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FEATURED) {
            val binding = ItemPerangkatFeaturedBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FeaturedViewHolder(binding)
        } else {
            val binding =
                ItemPerangkatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            GridViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val perangkat = listPerangkat[position]
        if (holder is FeaturedViewHolder) {
            holder.bind(perangkat)
        } else if (holder is GridViewHolder) {
            holder.bind(perangkat)
        }
    }

    override fun getItemCount(): Int = listPerangkat.size

    inner class FeaturedViewHolder(private val binding: ItemPerangkatFeaturedBinding) :
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

    inner class GridViewHolder(private val binding: ItemPerangkatBinding) :
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