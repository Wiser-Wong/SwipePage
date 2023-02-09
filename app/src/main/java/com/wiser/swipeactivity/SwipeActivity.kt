package com.wiser.swipeactivity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 ***************************************
 * 项目名称:SwipeActivity
 * @Author wangxy
 * 邮箱：wangxiangyu@ksjgs.com
 * 创建时间: 2022/11/21     3:56 PM
 * 用途: 更新说明
 ***************************************
 */
class SwipeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe)
        findViewById<SwipeFrameLayout>(R.id.fl_swipe)?.setOnDragCloseListener(onDragCloseListener = object : OnDragCloseListener{
            override fun onDragClose() {
                Toast.makeText(this@SwipeActivity, "关闭了", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        overridePendingTransition(
            R.anim.activity_bottom_enter,
            R.anim.activity_bottom_silent
        )
    }

    override fun finish() {
        super.finish()
    }
}