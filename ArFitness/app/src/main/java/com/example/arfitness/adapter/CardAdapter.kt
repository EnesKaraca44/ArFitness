package com.example.arfitness.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.arfitness.databinding.ItemExerciseBinding
import com.example.arfitness.entitiy.EgzersizDetay
import com.example.arfitness.fragment.AnasayfaFragmentDirections

class CardAdapter(var mContext: Context, var exercisesList: List<EgzersizDetay>) :
    RecyclerView.Adapter<CardAdapter.CardTasarimTutucu>() {

    inner class CardTasarimTutucu(var tasarim: ItemExerciseBinding) : RecyclerView.ViewHolder(tasarim.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTasarimTutucu {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return CardTasarimTutucu(binding)
    }

    override fun onBindViewHolder(holder: CardTasarimTutucu, position: Int) {
        val egzersiz = exercisesList[position]
        val t = holder.tasarim

        t.tvExerciseName.text = egzersiz.gorunenAd
        t.tvExerciseDescription.text = egzersiz.hedefKaslar

        t.btnStart.setOnClickListener {
            val gecis = AnasayfaFragmentDirections.anadanexercise(egzersiz.kodAdi)
            Navigation.findNavController(it).navigate(gecis)
        }

        holder.itemView.setOnClickListener { view ->
            val gecisAction = AnasayfaFragmentDirections.anadandetaya(egzersiz)
            Navigation.findNavController(view).navigate(gecisAction)
        }
    }

    override fun getItemCount(): Int {
        return exercisesList.size
    }
}