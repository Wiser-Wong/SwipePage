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
class SwipeFrameLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

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
    private var friction = 2

    /**
     * 下滑高度百分比
     */
    private var percentHeight = 4

    /**
     * 动画时长
     */
    private var animDuration = 200L

    /**
     * 拖拽监听
     */
    private var onDragCloseListener: OnDragCloseListener? = null

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return shouldInterceptEvent(ev)
    }

    /**
     * 是否拦截事件
     */
    private fun shouldInterceptEvent(ev: MotionEvent?): Boolean {
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
     * 打开页面
     */
    private fun open() {
        clearAnimation()
        ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0f).setDuration(animDuration).start()
    }

    /**
     * 关闭页面
     */
    private fun close() {
        clearAnimation()
        val animator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, measuredHeight.toFloat())
        animator.duration = animDuration
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
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