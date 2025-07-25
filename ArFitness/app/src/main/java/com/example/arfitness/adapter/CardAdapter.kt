package com.example.arfitness.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.findNavController

import androidx.recyclerview.widget.RecyclerView
import com.example.arfitness.databinding.ItemExerciseBinding
import com.example.arfitness.entitiy.EgzersizDetay
import com.example.arfitness.entitiy.Exercise
import com.example.arfitness.fragment.AnasayfaFragmentDirections

class CardAdapter(var mContext: Context, var exercisesList: List<EgzersizDetay>) :
    RecyclerView.Adapter<CardAdapter.CardTasarimTutucu>() {

    inner class CardTasarimTutucu(var tasarim: ItemExerciseBinding) : RecyclerView.ViewHolder(tasarim.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTasarimTutucu {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return CardTasarimTutucu(binding)
    }

    override fun onBindViewHolder(holder: CardTasarimTutucu, position: Int) {
        // O anki egzersiz verisini listeden alıyoruz. Tipi artık EgzersizDetay.
        val egzersiz = exercisesList.get(position)
        val t = holder.tasarim

        // Verileri kartın üzerine yazdırıyoruz.
        t.tvExerciseName.text = egzersiz.gorunenAd
        t.tvExerciseDescription.text = egzersiz.hedefKaslar // Açıklama veya hedef kaslar

        t.btnStart.setOnClickListener {
            val geciss= AnasayfaFragmentDirections.anadanexercise(egzersiz.gorunenAd)
            Navigation.findNavController(it).navigate(geciss)
        }

        // Kartın tamamına tıklandığında çalışacak olan YENİ listener.
        holder.itemView.setOnClickListener { view ->

            val gecisAction = AnasayfaFragmentDirections
                .anadandetaya(egzersiz) //

            Navigation.findNavController(view).navigate(gecisAction)
        }
    }

    override fun getItemCount(): Int {
        return exercisesList.size
    }
}