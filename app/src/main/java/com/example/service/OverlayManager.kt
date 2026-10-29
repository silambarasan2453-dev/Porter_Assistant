package com.example.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.model.RangeClassification
import kotlin.math.abs

data class OverlayDisplayData(
    val destination: String,
    val distanceKm: Double?,
    val classification: RangeClassification,
    val explanation: String,
    val processingDurationMs: Long
)

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var autoDismissRunnable: Runnable? = null

    fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun showOverlay(data: OverlayDisplayData, onStopMonitoring: () -> Unit) {
        if (!canDrawOverlays()) return

        handler.post {
            dismiss()

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 120 // Positioned nicely near top to avoid covering bottom Accept/Skip buttons
            }

            val card = createOverlayView(data, onStopMonitoring, params)
            overlayView = card

            try {
                windowManager.addView(card, params)
            } catch (e: Exception) {
                overlayView = null
                return@post
            }

            // Auto dismiss after 15 seconds (matching Porter's booking timeout)
            autoDismissRunnable = Runnable {
                dismiss()
            }
            handler.postDelayed(autoDismissRunnable!!, 15000L)
        }
    }

    fun dismiss() {
        autoDismissRunnable?.let { handler.removeCallbacks(it) }
        autoDismissRunnable = null
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
            overlayView = null
        }
    }

    private fun createOverlayView(
        data: OverlayDisplayData,
        onStopMonitoring: () -> Unit,
        params: WindowManager.LayoutParams
    ): View {
        val root = FrameLayout(context).apply {
            setPadding(32, 16, 32, 16)
        }

        val cardLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)

            // Background styled with rounded corners and distinct border
            val shape = GradientDrawable().apply {
                cornerRadius = 28f
                setColor(Color.parseColor("#0F172A")) // Slate 900
                setStroke(3, getStatusColor(data.classification))
            }
            background = shape
            elevation = 16f
        }

        // Header Row (App title, badge, dismiss)
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(context).apply {
            text = "Porter Location Assistant"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val closeBtn = TextView(context).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            textSize = 16f
            setPadding(16, 8, 8, 8)
            setOnClickListener { dismiss() }
        }

        headerRow.addView(title)
        headerRow.addView(closeBtn)
        cardLayout.addView(headerRow)

        // Status Badge Row
        val statusPill = TextView(context).apply {
            text = data.classification.label
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(24, 10, 24, 10)
            val pillShape = GradientDrawable().apply {
                cornerRadius = 16f
                setColor(getStatusColor(data.classification))
            }
            background = pillShape
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
                bottomMargin = 12
            }
        }
        cardLayout.addView(statusPill)

        // Distance Text
        val distanceView = TextView(context).apply {
            text = if (data.distanceKm != null) {
                "%.1f km straight-line from Home".format(data.distanceKm)
            } else {
                "Distance: Unresolved Location"
            }
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        cardLayout.addView(distanceView)

        // Destination Text
        val destView = TextView(context).apply {
            text = "Drop: ${data.destination}"
            setTextColor(Color.parseColor("#CBD5E1"))
            textSize = 13f
            maxLines = 2
            setPadding(0, 6, 0, 6)
        }
        cardLayout.addView(destView)

        // Rule / Explanation Text
        val explainView = TextView(context).apply {
            text = "${data.explanation} • In ${data.processingDurationMs}ms"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 11f
        }
        cardLayout.addView(explainView)

        // Drag listener to allow moving overlay freely without blocking screen
        root.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (abs(dx) > 10 || abs(dy) > 10) {
                            params.x = initialX + dx
                            params.y = initialY + dy
                            try {
                                windowManager.updateViewLayout(root, params)
                            } catch (_: Exception) {}
                            return true
                        }
                    }
                }
                return false
            }
        })

        root.addView(cardLayout)
        return root
    }

    private fun getStatusColor(classification: RangeClassification): Int {
        return when (classification) {
            RangeClassification.WITHIN_RANGE -> Color.parseColor("#16A34A") // Green
            RangeClassification.REVIEW -> Color.parseColor("#D97706")       // Amber
            RangeClassification.OUTSIDE_RANGE -> Color.parseColor("#DC2626") // Red
            RangeClassification.UNKNOWN -> Color.parseColor("#64748B")       // Slate Gray
        }
    }
}
