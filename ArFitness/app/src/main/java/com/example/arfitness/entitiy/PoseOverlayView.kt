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
    private var isFrontCamera: Boolean = true

    fun setPose(newPose: Pose, originalImageWidth: Int, originalImageHeight: Int, isFrontCamera: Boolean = true) {
        this.pose = newPose
        this.imageWidth = if (originalImageWidth > 0) originalImageWidth else 1
        this.imageHeight = if (originalImageHeight > 0) originalImageHeight else 1
        this.isFrontCamera = isFrontCamera
        invalidate()
    }

    private fun translateX(x: Float): Float {
        val scaled = x * scaleX
        return if (isFrontCamera) {
            width.toFloat() - scaled
        } else {
            scaled
        }
    }

    private fun translateY(y: Float): Float {
        return y * scaleY
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentPose = pose ?: return

        scaleX = width.toFloat() / imageWidth.toFloat()
        scaleY = height.toFloat() / imageHeight.toFloat()

        val landmarks = currentPose.allPoseLandmarks

        // Noktaları çiz
        for (landmark in landmarks) {
            val point = landmark.position
            val px = translateX(point.x)
            val py = translateY(point.y)
            canvas.drawCircle(px, py, 10f, pointPaint)
        }

        // Çizgileri çiz
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

    private fun drawLine(canvas: Canvas, landmarks: List<PoseLandmark>, startType: Int, endType: Int) {
        val startLandmark = landmarks.find { it.landmarkType == startType }
        val endLandmark = landmarks.find { it.landmarkType == endType }

        if (startLandmark != null && endLandmark != null) {
            val startX = translateX(startLandmark.position.x)
            val startY = translateY(startLandmark.position.y)
            val endX = translateX(endLandmark.position.x)
            val endY = translateY(endLandmark.position.y)
            canvas.drawLine(startX, startY, endX, endY, linePaint)
        }
    }
}