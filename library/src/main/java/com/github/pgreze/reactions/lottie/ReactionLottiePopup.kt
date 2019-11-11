package com.github.pgreze.reactions.lottie

/**
 * Created by edityomurti on 2019-10-28 19:25
 */

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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

/**
 * Entry point for reaction popup.
 */
class ReactionLottiePopup @JvmOverloads constructor(
        context: Context,
        reactionsConfig: ReactionsLottieConfig,
        val recyclerView: RecyclerView? = null,
        var onClickListener: OnClickVoteLister,
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
//                    CAUSING BUG ON LARGE PIXELED PHONE
//                    println("VotePopUp ACTION_MOVE")
//                    if (onTouched) {
//                        if (isHandlerRunning) {
//                            onTouched = false
//                            isHandlerRunning = false
//                            handler.removeCallbacksAndMessages(null)
//                            recyclerView?.requestDisallowInterceptTouchEvent(false)
//                        }
//                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (onTouched) {
                        if (isHandlerRunning) {
                            onClickListener()
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