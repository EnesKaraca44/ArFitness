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

    private val pointPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
        strokeWidth = 15f
    }

    private val linePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private var pose: Pose? = null
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var scaleX: Float = 1f
    private var scaleY: Float = 1f

    fun setPose(newPose: Pose, originalImageWidth: Int, originalImageHeight: Int) {
        pose = newPose
        imageWidth = originalImageWidth
        imageHeight = originalImageHeight
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentPose = pose ?: return

        scaleX = width.toFloat() / imageWidth
        scaleY = height.toFloat() / imageHeight

        drawLandmarks(canvas, currentPose.allPoseLandmarks)
        drawConnections(canvas, currentPose.allPoseLandmarks)
    }

    private fun drawLandmarks(canvas: Canvas, landmarks: List<PoseLandmark>) {
        for (landmark in landmarks) {
            val point = landmark.position

            // --- AYNALAMA ---
            // Orijinal X koordinatını ölçeklendir ve sonra ekranın genişliğinden çıkararak yansıt.
            val mirroredX = width - (point.x * scaleX)
            val scaledY = point.y * scaleY

            canvas.drawCircle(mirroredX, scaledY, 8f, pointPaint)
        }
    }

    private fun drawConnections(canvas: Canvas, landmarks: List<PoseLandmark>) {
        drawLine(canvas, landmarks, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP)
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE)
        drawLine(canvas, landmarks, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE)
        drawLine(canvas, landmarks, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
    }

    private fun drawLine(canvas: Canvas, landmarks: List<PoseLandmark>, startType: Int, endType: Int) {
        val startLandmark = landmarks.find { it.landmarkType == startType }
        val endLandmark = landmarks.find { it.landmarkType == endType }

        if (startLandmark != null && endLandmark != null) {
            val startPoint = startLandmark.position
            val endPoint = endLandmark.position

            // --- AYNALAMA ---
            // Hem başlangıç hem de bitiş noktalarının X koordinatlarını yansıt.
            val startX = width - (startPoint.x * scaleX)
            val startY = startPoint.y * scaleY
            val endX = width - (endPoint.x * scaleX)
            val endY = endPoint.y * scaleY

            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }
    }
}