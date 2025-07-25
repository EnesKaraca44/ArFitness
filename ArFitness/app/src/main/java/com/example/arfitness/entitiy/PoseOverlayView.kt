package com.example.arfitness.entitiy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PoseOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Çizim için kullanacağımız "fırçaların" (Paint) hazırlıyoruz.
    // Bunlar aynı kalıyor, sadece renkleri ve kalınlıkları daha belirgin yaptım.
    private val pointPaint = Paint().apply {
        color = Color.CYAN // Noktaların rengi
        style = Paint.Style.FILL
        strokeWidth = 15f // Noktaların kalınlığı (daha görünür)
    }

    private val linePaint = Paint().apply {
        color = Color.WHITE // Çizgilerin rengi
        style = Paint.Style.STROKE
        strokeWidth = 10f // Çizgilerin kalınlığı (daha görünür)
    }

    // Dışarıdan gelen 'Pose' (iskelet) verisini tutacağımız değişken.
    private var pose: Pose? = null

    // --- YENİ DEĞİŞKENLER ---
    // Bu değişkenler, koordinat sistemlerini birbirine çevirmek için gerekli.
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f


    // --- GÜNCELLENEN FONKSİYON ---
    // Dışarıdan (ExerciseFragment'tan) artık hem iskeleti hem de
    // o iskeletin bulunduğu orijinal görüntünün boyutlarını alacağız.
    fun setPose(newPose: Pose, originalImageWidth: Int, originalImageHeight: Int) {
        pose = newPose
        imageWidth = originalImageWidth
        imageHeight = originalImageHeight
        // 'invalidate()' komutu, Android sistemine "Bu görünümü yeniden çiz!" der.
        invalidate()
    }

    // Bu fonksiyon, Android tarafından bu görünümün ekrana çizilmesi gerektiğinde
    // otomatik olarak çağrılır.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Eğer çizecek bir iskelet verimiz yoksa, hiçbir şey yapma.
        val currentPose = pose ?: return

        // --- YENİ HESAPLAMA ---
        // Her çizimden önce, ölçeklendirme faktörlerini hesaplıyoruz.
        // Bu View'ın (OverlayView) genişliğini, analiz edilen görüntünün genişliğine bölüyoruz.
        scaleX = width.toFloat() / imageWidth
        scaleY = height.toFloat() / imageHeight

        // Tüm iskelet noktalarını (landmark) alıyoruz.
        val landmarks = currentPose.allPoseLandmarks

        // Her bir noktayı ekrana çiziyoruz.
        for (landmark in landmarks) {
            // Noktanın ekrandaki (x, y) koordinatlarını al.
            val point = landmark.position

            // --- GÜNCELLENEN ÇİZİM ---
            // Ham koordinatları doğrudan kullanmak yerine, ölçeklendirilmiş hallerini kullanıyoruz.
            val scaledX = point.x * scaleX
            val scaledY = point.y * scaleY
            canvas.drawCircle(scaledX, scaledY, 8f, pointPaint)
        }

        // Şimdi noktaları birleştiren çizgileri çizelim.
        // Sol kol
        drawLine(canvas, landmarks, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)

        // Sağ kol
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)

        // Gövde
        drawLine(canvas, landmarks, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP)
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP)

        // Sol bacak
        drawLine(canvas, landmarks, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)

        // Sağ bacak
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE)
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
    }

    // İki nokta arasında çizgi çizen yardımcı bir fonksiyon.
    private fun drawLine(canvas: Canvas, landmarks: List<PoseLandmark>, startType: Int, endType: Int) {
        val startLandmark = landmarks.find { it.landmarkType == startType }
        val endLandmark = landmarks.find { it.landmarkType == endType }

        if (startLandmark != null && endLandmark != null) {
            val startPoint = startLandmark.position
            val endPoint = endLandmark.position

            // --- GÜNCELLENEN ÇİZİM ---
            // Çizgi çizerken de ölçeklendirilmiş koordinatları kullanıyoruz.
            val startX = startPoint.x * scaleX
            val startY = startPoint.y * scaleY
            val endX = endPoint.x * scaleX
            val endY = endPoint.y * scaleY
            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }
    }
}