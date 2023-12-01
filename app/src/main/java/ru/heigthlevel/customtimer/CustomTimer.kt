package ru.heigthlevel.customtimer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.cos
import java.lang.Math.min
import java.sql.Time
import kotlin.math.sin

class CustomTimer: View {
    constructor(context: Context?) : super(context) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initPaint()
    }

    private var cHeight = 0
    private var cWidth: Int = 0
    private var tClockHours = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private var tPadding = 0
    private var cNumeralSpacing = 0
    private var cHandTruncation = 0
    private var mHourHandTruncation: Int = 0
    private var tRadius = 0
    private var cPaint: Paint? = null
    private var tRect: Rect = Rect()

    private var isInit = false
    private var timeState = TimeState(0, false)
        set(value) {
            if (value == field)
                return
            field = value
            timeStateListeners.forEach { it(value) }
        }
    private var timeStateListeners = mutableSetOf<(TimeState) -> Unit>()
    private var currentTime = 0L
    private val mainScope = CoroutineScope(Dispatchers.Main)

    private fun initPaint() {
        cPaint = Paint()
    }

    override fun onDraw(canvas: Canvas?) {
        if (!isInit) {
            cHeight = height
            cWidth = width
            tPadding = cNumeralSpacing + 50
            val minAttr = cHeight.coerceAtMost(cWidth)
            tRadius = minAttr / 2 - tPadding
            cHandTruncation = minAttr / 20
            mHourHandTruncation = minAttr / 17
            isInit = true
        }

        canvas?.drawColor(resources.getColor(R.color.simple))

        cPaint?.reset()
        cPaint?.color = Color.WHITE
        cPaint?.style = Paint.Style.STROKE
        cPaint?.strokeWidth = 4F
        cPaint?.isAntiAlias = true
        cPaint?.let {
            canvas?.drawCircle(cWidth / 2F, cHeight / 2F, tRadius + tPadding - 10F, cPaint!!)
        }
        cPaint?.style = Paint.Style.FILL_AND_STROKE
        cPaint?.let {
            canvas?.drawCircle(cWidth / 2F, cHeight / 2F, 12F, cPaint!!)
        }

        val fontSize = 30f
        cPaint!!.textSize = fontSize.toFloat()


        for (hour in tClockHours) {
            val tmp = hour.toString()
            cPaint!!.getTextBounds(tmp, 0, tmp.length, tRect)
            val angle = Math.PI / 6 * (hour - 3)
            val x = (cWidth / 2 + kotlin.math.cos(angle) * tRadius - tRect.width() / 2).toInt()
            val y = (cHeight / 2 + kotlin.math.sin(angle) * tRadius + tRect.height() / 2).toInt()
            canvas?.drawText(hour.toString(), x.toFloat(), y.toFloat(), cPaint!!)
        }

        val time =  Time(currentTime())

        drawHandLine(canvas!!, time.hours.toDouble(), isHour = true, isSecond = false)
        drawHandLine(
            canvas,
            time.minutes.toDouble(),
            isHour = false,
            isSecond = false
        )
        drawHandLine(
            canvas,
            time.seconds.toDouble(),
            isHour = false,
            isSecond = true
        )
        postInvalidateDelayed(500)
        invalidate()
    }

    private fun drawHandLine(canvas: Canvas, moment: Double, isHour: Boolean, isSecond: Boolean) {
        val angle = Math.PI * moment / 30 - Math.PI / 2
        val handRadius =
            if (isHour) tRadius - cHandTruncation - mHourHandTruncation else tRadius - cHandTruncation
        if (isSecond) cPaint!!.color = Color.RED
        canvas.drawLine(
            (cWidth / 2).toFloat(),
            (cHeight / 2).toFloat(),
            (cWidth / 2 + cos(angle) * handRadius).toFloat(),
            (cHeight / 2 + sin(angle) * handRadius).toFloat(),
            cPaint!!
        )
    }

    fun addListener(listener: (TimeState) -> Unit) {
        timeStateListeners.add(listener)
        listener(timeState)
    }

    fun currentTime(): Long = timeState.time

    fun start(time: Long) {
        addSeconds(isStart = true, time)
        mainScope.launch {
            while (timeState.isPlayed) {
                addSeconds()
                delay(1000)
            }
        }
    }

    fun stop() {
        timeState = TimeState(currentTime(), false)
    }


    fun reset() {
        timeState = TimeState(0, false)
        currentTime = 0
    }

    private fun addSeconds(isStart: Boolean = false, time: Long = 0L) {
        if (isStart.not())
            currentTime += 1000
        if (time != 0L)
            currentTime = time
        timeState = TimeState(currentTime, isPlayed = true)
    }
}
