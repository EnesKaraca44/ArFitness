package com.example.arfitness.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.arfitness.data.WorkoutSession
import com.example.arfitness.R
import com.example.arfitness.analysis.ExerciseType
import com.example.arfitness.databinding.ItemWorkoutHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : ListAdapter<WorkoutSession, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding =
            ItemWorkoutHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }
    //Bu iç sınıf, RecyclerView'daki her bir satırın görünümünü (view) tutar ve
    // bu görünüme nasıl veri bağlanacağını bilir.
    class HistoryViewHolder(private val binding: ItemWorkoutHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: WorkoutSession) {
            binding.apply {
                // Header Section
                tvExerciseName.text = session.exerciseName
                tvWorkoutDate.text = formatDate(session.date) // Tarihi formatlayarak yazdır

                // Stats Row
                tvDuration.text = formatDuration(session.durationInSeconds)
                // Plank süreli bir harekettir; tekrar sütununda 0 yerine tire göster
                val isTimed = ExerciseType.fromName(session.exerciseName) == ExerciseType.PLANK
                tvReps.text = if (isTimed) "–" else session.repCount.toString()

                // TODO: Bu alanlar için veritabanına yeni sütunlar eklemen gerekecek.
                // Şimdilik varsayılan değerler koyalım.
                tvOverallScore.text = "${session.formScore}%"
                tvCalories.text = session.caloriesBurned.toString()
                        tvFormFeedback.text = binding.root.context.getString(R.string.good_form) // Varsayılan
        tvFormFeedback.text = when {
            session.formScore > 90 -> binding.root.context.getString(R.string.perfect_form)
            session.formScore > 75 -> binding.root.context.getString(R.string.good_form)
            else -> binding.root.context.getString(R.string.improvable)
        }
            }
        }

        // Zaman ve tarih formatlama fonksiyonları (bunlar aynı kalabilir)
        private fun formatDuration(seconds: Long): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }

        private fun formatDate(timestamp: Long): String {
            // Yeni tasarımda saat de var, formatı güncelleyelim.
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
            return sdf.format(Date(timestamp))
        }
    }
    //DiffCallback, ListAdapter'ın iki liste arasındaki farkı akıllıca bulmasını sağlar.
    // Bu, "RecyclerView'ı tamamen yenilemek" yerine sadece değişen elemanları güncelleyerek
    // performansı artırır.
    class DiffCallback : DiffUtil.ItemCallback<WorkoutSession>() {
        override fun areItemsTheSame(oldItem: WorkoutSession, newItem: WorkoutSession) =
            oldItem.id == newItem.id // İki elemanın aynı olup olmadığını ID'lerine bakarak anlarız.

        override fun areContentsTheSame(oldItem: WorkoutSession, newItem: WorkoutSession) =
            oldItem == newItem // İki elemanın içeriğinin aynı olup olmadığını kontrol ederiz.
    }
}
