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
import com.example.arfitness.databinding.FragmentAnasayfaBinding
import com.example.arfitness.entitiy.Exercise
import com.example.arfitness.viewmodel.AnasayfaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnasayfaFragment : Fragment() {

    private var _binding: FragmentAnasayfaBinding? = null
    private val binding get() = _binding!!

    // Hilt ile ViewModel'i alıyoruz.
    private val viewModel: AnasayfaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnasayfaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView'ı ve Adapter'ı ayarlayan fonksiyonu çağırıyoruz.
        setupRecyclerView()

        // Geçmiş sayfasına gitmek için butonun tıklama olayını ayarlıyoruz.
        binding.btnHistory.setOnClickListener {
            val gecis = AnasayfaFragmentDirections.anadanhistorıye() // Bu action adını nav_graph'tan kontrol et.
            Navigation.findNavController(it).navigate(gecis)
        }
    }

    private fun setupRecyclerView() {
        // 1. Veriyi ViewModel'den alıyoruz. Artık Fragment içinde liste oluşturmuyoruz.
        // viewModel.egzersizListesi, bize List<EgzersizDetay> tipinde bir liste verir.
        val liste = viewModel.egzersizListesi

        // 2. Adapter'ı oluştururken, constructor'ına istediği parametreleri veriyoruz.
        // 'requireContext()' context'i, 'liste' de egzersiz listesini sağlar.
        val adapter = CardAdapter(requireContext(), liste)

        // 3. Adapter'ı ve LayoutManager'ı RecyclerView'a atıyoruz.
        binding.rvExercises.adapter = adapter
        binding.rvExercises.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


