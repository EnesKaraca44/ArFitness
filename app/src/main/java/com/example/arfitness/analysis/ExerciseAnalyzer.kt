package com.example.arfitness.analysis

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs

abstract class ExerciseAnalyzer {

    enum class Side { LEFT, RIGHT }

    /** Tekrar yerine süre ölçülen hareketlerde (Plank) true. */
    open val isTimed: Boolean = false

    /**
     * Son karede sayaca giden sinyal ve sayaç durumu. Debug ekranı ve sinyal kaydı (CSV)
     * bunu kullanır; eşikler gerçek kayıtlarla buradan ayarlanır.
     */
    data class DebugState(val values: List<Double?>, val phase: String)

    open val debugState: DebugState? get() = null

    /** Seansın form puanı (0–100); henüz puanlanacak bir şey yoksa null. */
    open val sessionFormScore: Int? get() = null

    /**
     * @param timestampMs karenin kamera zaman damgası (ms). Sayım kare sayısına değil
     * bu zamana göre yapılır; böylece yavaş ve hızlı telefonlarda aynı davranır.
     */
    abstract fun analyze(pose: Pose, timestampMs: Long): Feedback

    private val smoothedValues = mutableMapOf<String, Double>()
    private val lastSides = mutableMapOf<String, Side>()

    /**
     * ML Kit, kişi bulduğunda 33 noktanın hepsini döndürür; bu yüzden null kontrolü yetmez.
     * `inFrameLikelihood` noktanın kadrajda olma olasılığıdır (başka bir uzvun arkasında
     * kalıp kalmadığını söylemez), o yüzden yan çekimde taraf seçimi [pickSide] ile yapılır.
     */
    protected fun isVisible(vararg landmarks: PoseLandmark?): Boolean =
        landmarks.all { it != null && it.inFrameLikelihood >= MIN_IN_FRAME_LIKELIHOOD }

    /** Üstel hareketli ortalama: eklem titremesini bastırır. */
    protected fun smooth(key: String, raw: Double, alpha: Double = SMOOTHING_ALPHA): Double {
        val previous = smoothedValues[key]
        val value = if (previous == null) raw else alpha * raw + (1.0 - alpha) * previous
        smoothedValues[key] = value
        return value
    }

    /** Sinyal güvenilmez olduğunda eski değerin sonraki karelere sızmasını engeller. */
    protected fun clearSmoothing(key: String) {
        smoothedValues.remove(key)
    }

    /**
     * İki taraftan hangisinin kullanılacağını seçer.
     * - Yalnızca bir taraf kadrajdaysa o taraf.
     * - İkisi de kadrajdaysa kameraya yakın olan (ML Kit'te z küçükse yakındır). Önden çekimde
     *   iki tarafın z'si birbirine yakın olduğundan, fark [SIDE_SWITCH_MARGIN_Z]'den küçükse
     *   önceki seçim korunur; böylece taraf kare kare zıplamaz.
     */
    protected fun pickSide(key: String, left: Array<PoseLandmark?>, right: Array<PoseLandmark?>): Side? {
        val leftOk = isVisible(*left)
        val rightOk = isVisible(*right)
        val side = when {
            leftOk && rightOk -> {
                val dz = meanZ(left) - meanZ(right)
                when {
                    abs(dz) < SIDE_SWITCH_MARGIN_Z -> lastSides[key] ?: Side.LEFT
                    dz < 0 -> Side.LEFT
                    else -> Side.RIGHT
                }
            }
            leftOk -> Side.LEFT
            rightOk -> Side.RIGHT
            else -> null
        }
        if (side != null) lastSides[key] = side
        return side
    }

    private fun meanZ(landmarks: Array<PoseLandmark?>): Float =
        landmarks.filterNotNull().map { it.position3D.z }.average().toFloat()

    protected fun Pose.lm(type: Int): PoseLandmark? = getPoseLandmark(type)

    companion object {
        const val MIN_IN_FRAME_LIKELIHOOD = 0.6f
        const val SMOOTHING_ALPHA = 0.4
        const val SIDE_SWITCH_MARGIN_Z = 25f
    }
}
