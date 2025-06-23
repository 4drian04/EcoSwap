package com.example.ecoswap

import android.app.Activity
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout


class SoftInputAssist(activity: Activity) {
    private var contentContainer: ViewGroup?
    private var rootView: View?
    private val rootViewLayout: FrameLayout.LayoutParams?

    private val contentAreaOfWindowBounds: Rect = Rect()
    private var viewTreeObserver: ViewTreeObserver? = null
    private var usableHeightPrevious = 0

    private val listener = OnGlobalLayoutListener {
        possiblyResizeChildOfContent()
    }

    init {
        contentContainer = activity.contentRootView as? ViewGroup
        rootView = contentContainer?.getChildAt(0)
        rootViewLayout = rootView?.layoutParams as? FrameLayout.LayoutParams
    }

    fun onPause() {
        if (viewTreeObserver != null && viewTreeObserver!!.isAlive) {
            viewTreeObserver?.removeOnGlobalLayoutListener(listener)
        }
    }

    fun onResume() {
        viewTreeObserver = rootView?.viewTreeObserver

        if (viewTreeObserver != null) {
            viewTreeObserver?.addOnGlobalLayoutListener(listener)
        }
    }

    fun onDestroy() {
        contentContainer = null
        rootView = null
        viewTreeObserver = null
    }

    private fun possiblyResizeChildOfContent() {
        runOnMainThread {
            contentContainer?.getWindowVisibleDisplayFrame(contentAreaOfWindowBounds)
            val usableHeightNow: Int = contentAreaOfWindowBounds.bottom

            if (usableHeightNow != usableHeightPrevious) {
                rootViewLayout?.height = usableHeightNow

                rootView?.layout(
                    contentAreaOfWindowBounds.left - (contentContainer?.x?.toInt() ?: 0),
                    contentAreaOfWindowBounds.top - (contentContainer?.y?.toInt() ?: 0),
                    contentAreaOfWindowBounds.right - (contentContainer?.x?.toInt() ?: 0),
                    contentAreaOfWindowBounds.bottom - (contentContainer?.y?.toInt() ?: 0)
                )

                rootView?.requestLayout()
                usableHeightPrevious = usableHeightNow
            }
        }
    }
}

val Activity.contentRootView: View?
    get() = window?.decorView?.findViewById(android.R.id.content)
        ?: findViewById(android.R.id.content)

private object ContextHandler {
    val handler = Handler(Looper.getMainLooper())
}

fun runOnMainThread(action: () -> Unit) {
    ContextHandler.handler.post {
        action()
    }
}