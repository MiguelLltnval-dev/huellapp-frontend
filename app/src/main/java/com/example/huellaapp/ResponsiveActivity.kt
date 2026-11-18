package com.example.huellaapp

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/** One owner for system/keyboard insets: layouts never guess the navigation bar height. */
abstract class ResponsiveActivity : AppCompatActivity() {
    override fun setContentView(layoutResID: Int) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.setContentView(layoutResID)
        val content = findViewById<ViewGroup>(android.R.id.content)
        val root = content.getChildAt(0)
        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(left + safe.left, top + safe.top, right + safe.right,
                bottom + maxOf(safe.bottom, keyboard.bottom))
            onViewportChanged(((view.height - view.paddingTop - view.paddingBottom) / resources.displayMetrics.density).toInt(), keyboard.bottom > 0)
            insets
        }
        root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) { ViewCompat.requestApplyInsets(v) }
            override fun onViewDetachedFromWindow(v: View) = Unit
        })
        ViewCompat.requestApplyInsets(root)
    }

    protected open fun onViewportChanged(heightDp: Int, keyboardVisible: Boolean) = Unit
}
