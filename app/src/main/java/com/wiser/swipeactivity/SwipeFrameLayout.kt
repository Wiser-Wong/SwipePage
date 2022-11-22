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
 * 用途: 滑动布局
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
     * 下滑高度百分比
     */
    private var percentHeight = 4f

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

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        val ta =
            context.obtainStyledAttributes(attrs, R.styleable.SwipeFrameLayout, defStyleAttr, 0)
        isEnableDrag = ta.getBoolean(R.styleable.SwipeFrameLayout_sf_enable_drag, isEnableDrag)
        friction = ta.getFloat(R.styleable.SwipeFrameLayout_sf_friction,friction)
        percentHeight = ta.getFloat(R.styleable.SwipeFrameLayout_sf_percent_spring, percentHeight)
        animDuration = ta.getInteger(R.styleable.SwipeFrameLayout_sf_duration, animDuration.toInt()).toLong()
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
                shouldInterceptEvent = offsetY >= touchSlop * 3 || offsetY > offsetX
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
                val offsetY = (ev.rawY - interceptY) / friction
                if (offsetY > 0) {
                    translationY = offsetY
                }
            }
            MotionEvent.ACTION_UP -> {
                if (translationY > measuredHeight / percentHeight) {
                    close()
                } else {
                    open()
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

    /**
     * 设置摩擦力
     */
    fun setFriction(friction: Float) {
        this.friction = friction
    }

    /**
     * 设置百分比高度回弹
     */
    fun setPercentHeightSpring(percentHeight: Float) {
        this.percentHeight = percentHeight
    }

    /**
     * 设置动画时间
     */
    fun setDuration(animDuration: Long) {
        this.animDuration = animDuration
    }

    /**
     * 打开页面
     */
    private fun open() {
        if (isAnimRunning) return
        isAnimRunning = true
        clearAnimation()
        val animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0f)
        animator.addListener(object: AnimatorListenerAdapter(){
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
        val animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, measuredHeight.toFloat())
        animator.duration = animDuration
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                isAnimRunning = false
                onDragCloseListener?.apply {
                    onDragClose()
                }.run {
                    if (context is AppCompatActivity) {
                        (context as? AppCompatActivity)?.finish()
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