package com.adwardstark.sonarview

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

/**
 * Created by Aditya Awasthi on 17/12/20.
 * @author github.com/adwardstark
 */

class SonarView : View {

    companion object {
        private const val DEFAULT_SIZE = 100
    }

    private var defaultWidth = 0
    private var defaultHeight = 0
    private var start = 0
    private var centerX = 0
    private var centerY = 0
    private var radarRadius = 0

    private var sonarColor by Delegates.notNull<Int>()
    private var sonarTailColor by Delegates.notNull<Int>()
    private var sonarCircleColor by Delegates.notNull<Int>()
    private var sonarCircleStrokeWidth = 2f

    private lateinit var mPaintCircle: Paint
    private lateinit var mPaintRadar: Paint
    private lateinit var sonarMatrix: Matrix
    private lateinit var sonarShader: Shader

    private val sonarHandler: Handler = Handler()
    private val run: Runnable = object : Runnable {
        override fun run() {
            start += 2
            sonarMatrix = Matrix()
            sonarMatrix.postRotate(start.toFloat(), centerX.toFloat(), centerY.toFloat())
            postInvalidate()
            sonarHandler.postDelayed(this, 10)
        }
    }

    constructor(context: Context) : super(context) {
        init(null, context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(attrs, context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(attrs, context)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2
        centerY = h / 2
        radarRadius = w.coerceAtMost(h)
        sonarShader = SweepGradient(
            centerX.toFloat(),
            centerY.toFloat(),
            Color.TRANSPARENT,
            sonarTailColor
        )
    }

    private fun init(
        attrs: AttributeSet?,
        context: Context
    ) {
        sonarCircleColor = ContextCompat.getColor(context, R.color.sonarCircleColor)
        sonarColor = ContextCompat.getColor(context, R.color.sonarColor)
        sonarTailColor = ContextCompat.getColor(context, R.color.sonarTailColor)

        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.SonarView)
            sonarColor = ta.getColor(R.styleable.SonarView_sonarColor, sonarColor)
            sonarTailColor = ta.getColor(R.styleable.SonarView_sonarTailColor, sonarTailColor)
            sonarCircleColor = ta.getColor(R.styleable.SonarView_sonarCircleColor, sonarCircleColor)
            sonarCircleStrokeWidth = ta.getFloat(
                R.styleable.SonarView_sonarCircleStrokeWidth,
                sonarCircleStrokeWidth
            )
            ta.recycle()
        }

        initPaint()
        defaultWidth = dip2px(context)
        defaultHeight = dip2px(context)
        sonarMatrix = Matrix()
        sonarHandler.post(run)
    }

    private fun initPaint() {
        mPaintCircle = Paint()
        mPaintCircle.color = sonarCircleColor
        mPaintCircle.isAntiAlias = true
        mPaintCircle.style = Paint.Style.STROKE
        mPaintCircle.strokeWidth = sonarCircleStrokeWidth
        mPaintRadar = Paint()
        mPaintRadar.color = sonarColor
        mPaintRadar.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var resultWidth: Int
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        if (modeWidth == MeasureSpec.EXACTLY) {
            resultWidth = sizeWidth
        } else {
            resultWidth = defaultWidth
            if (modeWidth == MeasureSpec.AT_MOST) {
                resultWidth = resultWidth.coerceAtMost(sizeWidth)
            }
        }
        var resultHeight: Int
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (modeHeight == MeasureSpec.EXACTLY) {
            resultHeight = sizeHeight
        } else {
            resultHeight = defaultHeight
            if (modeHeight == MeasureSpec.AT_MOST) {
                resultHeight = resultHeight.coerceAtMost(sizeHeight)
            }
        }
        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            radarRadius / 7.toFloat(),
            mPaintCircle
        )
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            radarRadius / 4.toFloat(),
            mPaintCircle
        )
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            radarRadius / 3.toFloat(),
            mPaintCircle
        )
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            3 * radarRadius / 7.toFloat(),
            mPaintCircle
        )

        mPaintRadar.shader = sonarShader
        canvas.concat(sonarMatrix)
        canvas.drawCircle(
            centerX.toFloat(),
            centerY.toFloat(),
            3 * radarRadius / 7.toFloat(),
            mPaintRadar
        )
    }

    private fun dip2px(context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (DEFAULT_SIZE * scale + 0.5f).toInt()
    }
}