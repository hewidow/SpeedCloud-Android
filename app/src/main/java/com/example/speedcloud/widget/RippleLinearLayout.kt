package com.example.speedcloud.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.speedcloud.R
import java.util.*

// 背景带波纹的LinearLayout
class RippleLinearLayout : LinearLayout {
    private var mContext: Context
    private var timer: Timer // 定时器

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView()
        mContext = context
        timer = Timer()
        timer.schedule(drawTask, Date(), 1000 / fps) // 启动定时任务
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer.cancel() // 销毁时取消定时任务
    }

    lateinit var mPaint: Paint
    lateinit var mPath: Path

    /**
     * 初始化绘制工具
     */
    private fun initView() {
        mPaint = Paint() // 初始化画笔
        mPaint.isAntiAlias = true // 去除锯齿
        mPaint.style = Paint.Style.FILL // 设置实线
        mPath = Path() // 初始化路径
    }

    override fun dispatchDraw(canvas: Canvas) {
        initDraw(canvas) // 绘制在子View的后面
        super.dispatchDraw(canvas)
    }

    companion object {
        private const val fps: Long = 10 // 每秒的fps
    }

    private var startX: Float = 0F
    private var startX2: Float = startX - 100 // 两条波纹的起始位置

    /**
     * 绘制一条波纹
     */
    private fun drawOneRipple(
        canvas: Canvas,
        startX: Float, // 起始位置
        color: Int, // 颜色
        amplitude: Float,// 振幅
        halfCycle: Float,// 半周期
        _dlt: Float // 每秒移动距离
    ): Float {
        val dlt = _dlt / fps // 根据fps算出每次移动距离
        val mWidth = width // 获取窗口宽度
        val mHeight: Float = height.toFloat() // 获取窗口宽度
        var startXTemp = startX // 备份起始位置，以便此函数末尾重新计算新位置
        var x = startX // 开始位置
        var flag = 1 // 1代表刚开始在正半轴绘制，-1代表在负半轴绘制
        while (x <= -halfCycle) {
            x += halfCycle
            flag = -flag
        }// 根据起始位置使绘制的x坐标限定在(-halfCycle,halfCycle)之间（可能canvas不会绘制超出显示区域的东西，所以此步骤可能多余）
        mPath.reset() // 重置画笔
        mPath.moveTo(x, mHeight) // 移动到左下角
        mPath.lineTo(x, mHeight * 3.5F / 13F) // 根据顶部logo的比例高度计算
        while (x < mWidth) {
            mPath.rQuadTo(halfCycle / 2, amplitude * flag, halfCycle, 0F)
            x += halfCycle
            flag = -flag
        } // 绘制波纹
        mPath.lineTo(x, mHeight) // 移动到右下角
        mPath.close() // 闭合路径
        mPaint.color = color // 设置填充颜色
        canvas.drawPath(mPath, mPaint) // 在canvas上画下路径

        // 计算下一次的startX
        startXTemp -= dlt
        while (startXTemp <= -halfCycle * 2) startXTemp += halfCycle * 2
        return startXTemp
    }

    /**
     * 绘制动态波纹
     */
    private fun initDraw(canvas: Canvas) {
        startX = drawOneRipple(
            canvas,
            startX,
            ContextCompat.getColor(mContext, R.color.ripple),
            30F,
            300F,
            30F
        )
        startX2 = drawOneRipple(
            canvas,
            startX2,
            ContextCompat.getColor(mContext, R.color.ripple2),
            20F,
            200F,
            20F
        )
        // Log.d("paint", "$startX | $startX2")
    }

    // 定义一个绘制任务
    private val drawTask: TimerTask = object : TimerTask() {
        override fun run() {
            postInvalidate() // 通知绘制
        }
    }
}