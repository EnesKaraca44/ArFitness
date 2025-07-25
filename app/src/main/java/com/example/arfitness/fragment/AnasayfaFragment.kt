package com.example.arfitness.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arfitness.adapter.CardAdapter
import com.example.arfitness.adapter.CategoryAdapter
import com.example.arfitness.databinding.FragmentAnasayfaBinding
import com.example.arfitness.entitiy.Exercise
import com.example.arfitness.viewmodel.AnasayfaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnasayfaFragment : Fragment() {

    private var _binding: FragmentAnasayfaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnasayfaViewModel by viewModels()

    // İki farklı adapter'ımız var
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var cardAdapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnasayfaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()

        observeViewModel()

        // Geçmiş butonunu ayarla
        binding.btnHistory.setOnClickListener {
            val gecis = AnasayfaFragmentDirections.anadanhistorıye()
            Navigation.findNavController(it).navigate(gecis)
        }
    }

    /**
     * Üstteki yatay kategori RecyclerView'ını kurar.
     */
    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter { secilenKategoriAdi ->
            viewModel.filtrele(secilenKategoriAdi)
        }

        binding.rvCategories.adapter = categoryAdapter
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Adapter'a List<EgzersizKategori> listesini gönder
        categoryAdapter.setCategories(viewModel.tumKategoriliEgzersizler)
        cardAdapter = CardAdapter(requireContext(), emptyList())

        // 2. Adapter'ı ve LayoutManager'ı RecyclerView'a atıyoruz.
        binding.rvExercises.adapter = cardAdapter
        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())

    }

    private fun observeViewModel() {
        viewModel.filtrelenmisEgzersizler.observe(viewLifecycleOwner) { egzersizListesi ->
            cardAdapter.updateList(egzersizListesi)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}