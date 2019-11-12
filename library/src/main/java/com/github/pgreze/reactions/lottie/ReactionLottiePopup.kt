package com.github.pgreze.reactions.lottie

/**
 * Created by edityomurti on 2019-10-28 19:25
 */

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.github.pgreze.reactions.OnClickVoteLister
import com.github.pgreze.reactions.ReactionLottieSelectedListener
import com.github.pgreze.reactions.ReactionsLottieConfig
import com.github.pgreze.reactions.dsl.reactionConfig

/**
 * Entry point for reaction popup.
 */
class ReactionLottiePopup @JvmOverloads constructor(
        context: Context,
        val reactionsConfig: ReactionsLottieConfig,
        val recyclerView: RecyclerView? = null,
        var reactionSelectedListener: ReactionLottieSelectedListener? = null
) : PopupWindow(context), View.OnTouchListener {
    private var handler: Handler

    private val rootView = FrameLayout(context).also {
        it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private val view: ReactionLottieViewGroup by lazy(LazyThreadSafetyMode.NONE) {
        // Lazily inflate content during first display
        ReactionLottieViewGroup(context, reactionsConfig).also {
            it.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER)

            it.reactionSelectedListener = reactionSelectedListener

            rootView.addView(it)
        }.also { it.dismissListener = ::dismiss }
    }

    init {
        contentView = rootView
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        handler = Handler(Looper.getMainLooper())
    }

    private var isHandlerRunning = false
    private var onTouched = false

    inner class ShowPopupRunner(val v: View, val event: MotionEvent) : Runnable {
        override fun run() {
            // Show fullscreen with button as context provider
            isHandlerRunning = false
            showAtLocation(v, Gravity.NO_GRAVITY, 0, 0)
            view.show(event, v)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!isShowing) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    onTouched = true
                    isHandlerRunning = true
                    handler.postDelayed(ShowPopupRunner(v, event), 300)
                    recyclerView?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    val parentLocation = IntArray(2)
                            .also(v::getLocationOnScreen)
                            .let { Point(it[0], it[1]) }

                    val boundaryLeft = parentLocation.x
                    val boundaryRight = parentLocation.x + v.width
                    val boundaryTop = parentLocation.y
                    val boundaryBot = parentLocation.y + v.height

                    val insideLeft = event.rawX >= boundaryLeft
                    val insideRight = event.rawX < boundaryRight
                    val insideTop = event.rawY >= boundaryTop
                    val insideBot = event.rawY < boundaryBot

                    val outsideBoundaryX = !insideLeft || !insideRight
                    val outsideBoundaryY = !insideTop || !insideBot

                    if (outsideBoundaryX || outsideBoundaryY) {
                        if (onTouched) {
                            if (isHandlerRunning) {
                                onTouched = false
                                isHandlerRunning = false
                                handler.removeCallbacksAndMessages(null)
                                recyclerView?.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                    }

                }
                MotionEvent.ACTION_UP -> {
                    if (onTouched) {
                        if (isHandlerRunning) {
                            reactionSelectedListener?.invoke(reactionsConfig.typeVote == ReactionLottieViewGroup.Companion.TypeVote.VOTE_UPVOTE, null, null)
                        }

                        onTouched = false
                        isHandlerRunning = false
                        handler.removeCallbacksAndMessages(null)
                        recyclerView?.requestDisallowInterceptTouchEvent(false)
                    }
                }
            }
        }
        return view.onTouchEvent(event)
    }

    override fun dismiss() {
        view.dismiss()
        super.dismiss()
    }
}