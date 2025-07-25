package com.example.arfitness.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.arfitness.R
import com.example.arfitness.databinding.ItemCategoryBinding
import com.example.arfitness.databinding.ItemCategoryCardBinding
import com.example.arfitness.entitiy.EgzersizKategorisi


class CategoryAdapter(
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categories: List<EgzersizKategorisi> = emptyList()
    private var selectedPosition = 0


    inner class CategoryViewHolder( val binding: ItemCategoryCardBinding) : RecyclerView.ViewHolder(binding.root) {

        // Bu fonksiyon, gelen veriyle kartın görünümünü günceller.
        fun bind(kategori: EgzersizKategorisi, isSelected: Boolean) {
            // binding nesnesi üzerinden XML'deki elemanlara ulaşıyoruz.
            binding.tvCategoryName.text = kategori.kategoriAdi
            binding.ivCategoryIcon.setImageResource(kategori.kategoriIkonId)

            // Seçili kartın görünümünü değiştir
            if (isSelected) {
                binding.cardCategory.strokeWidth = 4
                binding.cardCategory.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
            } else {
                binding.cardCategory.strokeWidth = 0
                binding.cardCategory.strokeColor = Color.TRANSPARENT
            }

            // Tıklama olayını da burada, güvenli pozisyon ile ayarlıyoruz.
            itemView.setOnClickListener {
                val guncelPozisyon = adapterPosition
                if (guncelPozisyon != RecyclerView.NO_POSITION && selectedPosition != guncelPozisyon) {
                    notifyItemChanged(selectedPosition)
                    selectedPosition = guncelPozisyon
                    notifyItemChanged(selectedPosition)
                    onCategoryClick(kategori.kategoriAdi)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val kategori = categories[position]
        // ViewHolder'a hem veriyi hem de "sen seçili misin?" bilgisini gönderiyoruz.
        // Geri kalan tüm işi ViewHolder'ın kendisi yapıyor.
        holder.bind(kategori, position == selectedPosition)
    }


    override fun getItemCount() = categories.size

    fun setCategories(newCategories: List<EgzersizKategorisi>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}