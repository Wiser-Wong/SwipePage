package com.wiser.swipeactivity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

/**
 ***************************************
 * 项目名称:SwipeActivity
 * @Author wangxy
 * 创建时间: 2022/11/21     4:14 PM
 * 用途: 滑动关闭布局
 ***************************************
 */
class SwipeFrameLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private var touchSlop = ViewConfiguration.get(context)?.scaledTouchSlop ?: 20

    /**
     * 按下X点
     */
    private var interceptX = 0f

    /**
     * 按下Y点
     */
    private var interceptY = 0f

    /**
     * 摩擦力
     */
    private var friction = 2f

    /**
     * 滑动百分比距离关闭 4/height 默认是高度的1/4
     */
    private var percentDistance = 4f

    /**
     * 动画时长
     */
    private var animDuration = 200L

    /**
     * 拖拽监听
     */
    private var onDragCloseListener: OnDragCloseListener? = null

    /**
     * 是否能拖拽
     */
    private var isEnableDrag = true

    /**
     * 是否动画正在运行
     */
    private var isAnimRunning = false

    /**
     * 方向
     */
    private var orientation: Int = VERTICAL

    companion object {
        const val HORIZONTAL = 1
        const val VERTICAL = 0
    }

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        val ta =
            context.obtainStyledAttributes(attrs, R.styleable.SwipeFrameLayout, defStyleAttr, 0)
        isEnableDrag = ta.getBoolean(R.styleable.SwipeFrameLayout_sf_enable_drag, isEnableDrag)
        friction = ta.getFloat(R.styleable.SwipeFrameLayout_sf_friction, friction)
        percentDistance =
            ta.getFloat(R.styleable.SwipeFrameLayout_sf_percent_spring, percentDistance)
        animDuration =
            ta.getInteger(R.styleable.SwipeFrameLayout_sf_duration, animDuration.toInt()).toLong()
        orientation = ta.getInteger(R.styleable.SwipeFrameLayout_sf_orientation, orientation)
        ta.recycle()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return shouldInterceptEvent(ev)
    }

    /**
     * 是否拦截事件
     */
    private fun shouldInterceptEvent(ev: MotionEvent?): Boolean {
        if (!isEnableDrag) return false
        var shouldInterceptEvent = false
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                interceptX = ev.rawX
                interceptY = ev.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = abs(ev.rawX - interceptX)
                val offsetY = abs(ev.rawY - interceptY)
                when (orientation) {
                    VERTICAL -> {
                        shouldInterceptEvent = offsetY >= touchSlop * 3 || offsetY > offsetX
                    }
                    HORIZONTAL -> {
                        shouldInterceptEvent = offsetX >= touchSlop * 3 || offsetX > offsetY
                    }
                    else -> {
                        isEnableDrag = false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                shouldInterceptEvent = false
            }
        }
        return shouldInterceptEvent
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnableDrag) return false
        handleTouchEvent(event)
        return true
    }

    /**
     * 处理滑动事件
     */
    private fun handleTouchEvent(ev: MotionEvent?) {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                when (orientation) {
                    VERTICAL -> {
                        val offsetY = (ev.rawY - interceptY) / friction
                        if (offsetY > 0) {
                            translationY = offsetY
                        }
                    }
                    HORIZONTAL -> {
                        val offsetX = (ev.rawX - interceptX) / friction
                        if (offsetX > 0) {
                            translationX = offsetX
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                when (orientation) {
                    VERTICAL -> {
                        if (translationY > measuredHeight / percentDistance) {
                            close()
                        } else {
                            open()
                        }
                    }
                    HORIZONTAL -> {
                        if (translationX > measuredWidth / percentDistance) {
                            close()
                        } else {
                            open()
                        }
                    }
                }
            }
        }
    }

    /**
     * 设置拖拽关闭监听
     */
    fun setOnDragCloseListener(onDragCloseListener: OnDragCloseListener?) {
        this.onDragCloseListener = onDragCloseListener
    }

    /**
     * 设置是否能拖拽
     */
    fun setEnableDrag(isEnableDrag: Boolean) {
        this.isEnableDrag = isEnableDrag
    }

    fun getEnableDrag(): Boolean = isEnableDrag

    /**
     * 设置摩擦力
     */
    fun setFriction(friction: Float) {
        this.friction = friction
    }

    fun getFriction(): Float = friction

    /**
     * 设置百分比高度回弹
     */
    fun setPercentHeightSpring(percentHeight: Float) {
        this.percentDistance = percentHeight
    }

    fun getPercentDistance(): Float = percentDistance

    /**
     * 设置动画时间
     */
    fun setDuration(animDuration: Long) {
        this.animDuration = animDuration
    }

    fun getDuration(): Long = animDuration

    /**
     * 设置方向
     */
    fun setOrientation(orientation: Int) {
        this.orientation = orientation
    }

    fun getOrientation(): Int = orientation

    /**
     * 打开页面
     */
    private fun open() {
        if (isAnimRunning) return
        isAnimRunning = true
        clearAnimation()
        val property = when (orientation) {
            VERTICAL -> View.TRANSLATION_Y
            HORIZONTAL -> View.TRANSLATION_X
            else -> View.TRANSLATION_Y
        }
        val animator = ObjectAnimator.ofFloat(this, property, 0f)
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                isAnimRunning = false
            }
        })
        animator.setDuration(animDuration).start()
    }

    /**
     * 关闭页面
     */
    private fun close() {
        if (isAnimRunning) return
        isAnimRunning = true
        clearAnimation()
        val property = when (orientation) {
            VERTICAL -> View.TRANSLATION_Y
            HORIZONTAL -> View.TRANSLATION_X
            else -> View.TRANSLATION_Y
        }
        val value = when (orientation) {
            VERTICAL -> measuredHeight.toFloat()
            HORIZONTAL -> measuredWidth.toFloat()
            else -> measuredHeight.toFloat()
        }
        val animator = ObjectAnimator.ofFloat(this, property, value)
        animator.duration = animDuration
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                isAnimRunning = false
                if (onDragCloseListener != null) {
                    onDragCloseListener?.onDragClose()
                }
                if (context is AppCompatActivity) {
                    (context as? AppCompatActivity)?.apply {
                        finish()
                        overridePendingTransition(
                            R.anim.activity_bottom_silent,
                            R.anim.activity_bottom_silent
                        )
                    }
                }
            }
        })
        animator.start()
    }
}

interface OnDragCloseListener {
    fun onDragClose()
}