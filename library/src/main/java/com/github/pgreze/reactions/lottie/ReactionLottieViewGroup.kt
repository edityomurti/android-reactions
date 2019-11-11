package com.github.pgreze.reactions.lottie

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.pgreze.reactions.*
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Created by edityomurti on 2019-10-28 19:28
 */

@SuppressLint("ViewConstructor")
class ReactionLottieViewGroup(context: Context, private val config: ReactionsLottieConfig) : ViewGroup(context) {

    private val tag = ReactionViewGroup::class.java.simpleName

    private val horizontalPadding: Int = config.horizontalMargin
    private val verticalPadding: Int = config.verticalMargin

    private var iconDivider: Int = horizontalPadding / 2

    private var smallIconSize: Float
    private var mediumIconSize: Float = config.reactionSize.toFloat()
    private var largeIconSize: Float = 1.2f * mediumIconSize

    private var firstClick = Point()
    private var parentLocation = Point()
    private var parentSize: Size = Size(0, 0)

    private var dialogWidth: Int
    private var dialogHeight: Int = (mediumIconSize + 2 * verticalPadding).toInt()

    private var isDownwardLayout = false

    init {
        val nIcons = config.reactions.size

        dialogWidth = (horizontalPadding * 2 +
                mediumIconSize * nIcons +
                iconDivider * nIcons.minus(1)).toInt()

        smallIconSize = (dialogWidth
                - horizontalPadding * 2
                - largeIconSize
                - iconDivider * nIcons.minus(1)
                ) / nIcons.minus(1)
    }

    private val background = FrameLayout(context)
            .also {
                it.layoutParams = LayoutParams(dialogWidth, dialogHeight)
                addView(it)
            }

    private val bottomInfo = LinearLayout(context)
            .also {
                it.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                it.gravity = Gravity.CENTER
                val bottomInfoText = TextView(context).also { tv ->
                    tv.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    tv.text = "Geser samping untuk vote"
                    tv.setTextColor(Color.BLACK)
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    tv.gravity = Gravity.CENTER
                }
                it.setPadding(
                        context.toDp(12),
                        context.toDp(12),
                        context.toDp(12),
                        context.toDp(12)
                        )
                it.addView(bottomInfoText)
                addView(it)
            }

    private val snipeArrow: ImageView = ImageView(context).also {
        it.layoutParams = LayoutParams(context.toDp(14), context.toDp(10))
        it.setImageResource(R.drawable.reactions_arrow_snipe)
    }

    private val snipeList = listOf(
            ReactionSnipeView.SnipeAction.SnipeActionText(context, "Bermanfaat", "bermanfaat"),
            ReactionSnipeView.SnipeAction.SnipeActionText(context, "Bernas", "bernas"),
            ReactionSnipeView.SnipeAction.SnipeActionText(context, "Pantas Dicermati", "pantas_dicermati")
    )

    private val snipeLayout: ReactionSnipeLayout = ReactionSnipeLayout(context, dialogWidth, snipeArrow, snipeList)
            .also {
                println("VotePopUp snipeLayout created")
//                addView(it)
            }

    private val reactions: List<ReactionLottieView> = config.reactions
            .map {
                ReactionLottieView(context, it).also {
                    it.layoutParams = LayoutParams(mediumIconSize.toInt(), mediumIconSize.toInt())
                    it.bringToFront()
                    addView(it)
                }
            }
            .toList()


    private val statusBarHeight: Int by lazy(LazyThreadSafetyMode.NONE)
            /*get()*/ {
        val rect = Rect(0,0,0,0)
        val window = (context as? Activity)?.window
        window?.decorView?.getWindowVisibleDisplayFrame(rect)
        rect.top
    }

    private var dialogX: Int = 0
    private var dialogY: Int = 0
    private var snipeDialogY: Int = 0

    private var currentState: ReactionLottieViewState? = null
        set(value) {
            if (field == value) return

            val oldValue = field
            field = value
            Log.i(tag, "State: $oldValue -> $value")
            when (value) {
                is ReactionLottieViewState.Boundary -> animTranslationY(value)
                is ReactionLottieViewState.WaitingSelection -> animSize(null)
                is ReactionLottieViewState.Selected -> animSize(value)
            }
        }

    private var currentAnimator: ValueAnimator? = null
        set(value) {
            field?.cancel()

            field = value

            removeView(snipeLayout)
            snipeLayout.visibility = View.GONE
            snipeLayout.hide()

            field?.duration = 100
            field?.start()
        }

    private var isFirstTouchAlwaysInsideButton = true
//    private var isIgnoringFirstReaction: Boolean = false

    var reactionSelectedListener: ReactionLottieSelectedListener? = null

    var dismissListener: (() -> Unit)? = null

    // onLayout/onMeasure https://newfivefour.com/android-custom-views-onlayout-onmeasure.html
    // Detailed  https://proandroiddev.com/android-draw-a-custom-view-ef79fe2ff54b
    // Advanced sample: https://github.com/frogermcs/LikeAnimation/tree/master/app/src/main/java/frogermcs/io/likeanimation

    override fun onSizeChanged(width: Int, height: Int, oldW: Int, oldH: Int) {
        println("VotePopUp onSizeChanged()")
        super.onSizeChanged(width, height, oldW, oldH)

        dialogX = when (config.popupGravity) {
            PopupGravity.DEFAULT -> // Slightly on right of parent's left position
                (firstClick.x - horizontalPadding - mediumIconSize / 2).toInt()
            PopupGravity.PARENT_LEFT -> // Fallback to SCREEN_RIGHT
                parentLocation.x
                        .takeUnless { it + dialogWidth > width }
                        ?: width - dialogWidth - config.popupMargin
            PopupGravity.PARENT_RIGHT -> // Fallback to SCREEN_LEFT
                (parentLocation.x + parentSize.width - dialogWidth)
                        .takeUnless { it < 0 }
                        ?: config.popupMargin
            PopupGravity.SCREEN_LEFT ->
                config.popupMargin
            PopupGravity.SCREEN_RIGHT ->
                width - dialogWidth - config.popupMargin
            PopupGravity.CENTER ->
                (width - dialogWidth) / 2
        }
        // Fallback to center if invalid position
        if (dialogX < 0 || dialogX + dialogWidth >= width) {
            dialogX = max(0, (width - dialogWidth) / 2)
        }

        // Y position will be slightly on top of parent view
//        println("VotePopUp onSizeChanged parentLocation.y : ${parentLocation.y}")
//        println("VotePopUp onSizeChanged dialogHeight : ${dialogHeight}")
//        println("VotePopUp onSizeChanged bottomInfo.height : ${bottomInfo.layoutParams.height}")

        val pixelGap = statusBarHeight + statusBarHeight / 2

        dialogY = parentLocation.y - dialogHeight - bottomInfo.layoutParams.height * 2 + pixelGap
        snipeDialogY = dialogY - snipeLayout.layoutParams.height * 2
//        println("VotePopUp onSizeChanged dialogY : $dialogY")
//        println("VotePopUp onSizeChanged snipeLayout.layoutParams.height : ${snipeLayout.layoutParams.height}")
//        println("VotePopUp onSizeChanged snipeDialogY : $snipeDialogY")
//        println("VotePopUp onSizeChanged statusBarHeight : $statusBarHeight")
        isDownwardLayout = snipeDialogY < statusBarHeight
        if (isDownwardLayout) {
            // Below parent view
//            println("VotePopUp onSizeChanged dialogY changed parentLocation.y : ${parentLocation.y}")
//            println("VotePopUp onSizeChanged dialogY changed parentSize.height : ${parentSize.height}")
//            println("VotePopUp onSizeChanged dialogY changed snipeLayout.layoutParams.height : ${snipeLayout.layoutParams.height}")
            dialogY = parentLocation.y + parentSize.height + bottomInfo.layoutParams.height - pixelGap
            snipeDialogY = dialogY - snipeLayout.layoutParams.height * 2
//            println("VotePopUp onSizeChanged dialogY changedTo : $dialogY")
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        println("VotePopUp onLayout")
        background.also { view ->
            val translationX = view.translationX.toInt()
            val translationY = 0 //view.translationY.toInt()

            view.setBackgroundResource(if (!isDownwardLayout) R.drawable.reactions_bg_reaction_lottie_view_group else R.drawable.reactions_bg_reaction_lottie_view_group_downward)

            view.layout(
                    dialogX + translationX,
                    (dialogY + mediumIconSize - view.layoutParams.height + translationY).toInt(),
//                    (dialogY + mediumIconSize - view.layoutParams.height + translationY).toInt(),
                    dialogX + dialogWidth + translationX,
                    dialogY + dialogHeight + translationY
            )

//            println("VotePopUp onLayout background.top = ${view.top}")
//            println("VotePopUp onLayout dialogY + mediumIconSize - view.layoutParams.height + translationY")
//            println("VotePopUp onLayout $dialogY + $mediumIconSize - ${view.layoutParams.height} + $translationY")
        }

        var prevX = 0
        reactions.forEach { view ->
            val translationX = view.translationX.toInt()
            val translationY = 0//view.translationY.toInt()

            val bottom = dialogY + dialogHeight - verticalPadding + translationY
            val top = bottom - view.layoutParams.height + translationY
            val left = dialogX + horizontalPadding + prevX + translationX
            val right = left + view.layoutParams.width + translationX
            view.layout(left, top, right, bottom)

            prevX += view.width + iconDivider
        }

        if (snipeLayout.visibility == View.VISIBLE) {
            snipeLayout.also { view ->
                val translationX = view.translationX.toInt()
                val translationY = 0//view.translationY.toInt()

                view.measure(0, 0)

                val top =  if (!isDownwardLayout) {
                    dialogY + mediumIconSize - background.layoutParams.height + translationY - view.measuredHeight
                } else {
                    (dialogY + dialogHeight).toFloat()
                }

                val bottom = top + view.measuredHeight
                val left = dialogX + translationX
                val right = dialogX + dialogWidth + translationX

                view.layout(
                        left,
                        top.toInt(),
                        right,
                        bottom.toInt()
                )
            }
            snipeArrow.also { view ->
                val selectedReactionView = (currentState as? ReactionLottieViewState.Selected)?.viewReaction ?: return
                val left = (selectedReactionView.left + (selectedReactionView.right - selectedReactionView.left) / 2f - view.measuredWidth / 2f) - dialogX
                val right = left + view.measuredWidth

                if (!isDownwardLayout) {
                    view.rotation = 180f
                }

                view.layout(left.toInt(), view.top,  right.toInt(), view.bottom)
            }
        }

        bottomInfo.also { view ->
            val translationX = view.translationX.toInt()
            val translationY = 0//view.translationY.toInt()
            view.measure(0, 0)
            var clippedMargin = context.toDp(2)

            var top = dialogY + dialogHeight + translationY - clippedMargin
            var bottom = top + view.measuredHeight
            var left = dialogX + translationX
            var right = dialogX + dialogWidth + translationX

            if (isDownwardLayout) {
                top = dialogY - view.measuredHeight + translationY + clippedMargin
                bottom = top + view.measuredHeight
            }

            view.setBackgroundResource(if (!isDownwardLayout) R.drawable.reactions_bg_bottom_info else R.drawable.reactions_bg_bottom_info_downward)

            view.layout(
                    left,
                    top,
                    right,
                    bottom
            )
        }
    }

    fun show(event: MotionEvent, parent: View) {
        println("VotePopUp show")
        this.firstClick = Point(event.rawX.roundToInt(), event.rawY.roundToInt())
        this.parentLocation = IntArray(2)
                .also(parent::getLocationOnScreen)
                .let { Point(it[0], it[1]) }
        parentSize = Size(parent.width, parent.height)
        isFirstTouchAlwaysInsideButton = true
//        isIgnoringFirstReaction = true

        // Resize, could be fixed with later resolved width/height
        onSizeChanged(width, height, width, height)

        // Appear effect
        visibility = View.VISIBLE
        currentState = ReactionLottieViewState.Boundary.Appear(path = dialogHeight to 0)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

            isFirstTouchAlwaysInsideButton = isFirstTouchAlwaysInsideButton && inInsideParentView(event)

            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    // Ignores when appearing
                    if (currentState is ReactionLottieViewState.Boundary.Appear || currentState == null) return true

                    val selectedReactionView = (currentState as? ReactionLottieViewState.Selected)?.viewReaction
                    val viewReaction = getIntersectedIcon(event.rawX, event.rawY)
                    val viewSnipe = getIntersectedSnipe(event.rawX, event.rawY)
                    setSnipeOnTouchListener(event.rawX, event.rawY)
                    val onSnipeLayout = snipeLayout.isIntersected(event.rawX, event.rawY) && snipeLayout.visibility == View.VISIBLE

                    if (onSnipeLayout) {
                    } else if (viewReaction == null) {
                        currentState = ReactionLottieViewState.WaitingSelection
                    } else if ((currentState as? ReactionLottieViewState.Selected)?.viewReaction != viewReaction) {
                        currentState = ReactionLottieViewState.Selected(viewReaction, viewSnipe)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (currentState == null) {
                        dismiss()
                        return true
                    }

                    val snipe = getIntersectedSnipe(event.rawX, event.rawY)?.text?.toString()
                    (currentState as? ReactionLottieViewState.Selected)?.also {
                        reactionSelectedListener?.invoke(config.reactions.indexOf(it.viewReaction.reaction), snipe)
                    }

                    dismiss()
                }
                MotionEvent.ACTION_CANCEL -> {
                    currentState = ReactionLottieViewState.WaitingSelection
                }
            }

        return true
    }

    fun dismiss() {
        if (currentState == null) return

        currentState = ReactionLottieViewState.Boundary.Disappear(
                (currentState as? ReactionLottieViewState.Selected)?.viewReaction,
                0 to dialogHeight)
    }

    private fun inInsideParentView(event: MotionEvent): Boolean =
            event.rawX >= parentLocation.x
                    && event.rawX <= parentLocation.x + parentSize.width
                    && event.rawY >= parentLocation.y
                    && event.rawY <= parentLocation.y + parentSize.height

    private fun getIntersectedIcon(x: Float, y: Float): ReactionLottieView? =
            reactions.firstOrNull {
                x >= it.location.x - horizontalPadding
                        && x < it.location.x + it.width + iconDivider
                        && (if (!isDownwardLayout) {y >= it.location.y - horizontalPadding} else {y >= it.location.y + horizontalPadding - dialogHeight - iconDivider}) // top boundary
                        && (if (!isDownwardLayout) {y < it.location.y + it.height + dialogHeight + iconDivider} else {y < it.location.y + it.height + verticalPadding}) // bot boundary
            }

    private fun getIntersectedSnipe(x: Float, y: Float): ReactionSnipeView.SnipeAction.SnipeActionText? =
            snipeList.firstOrNull {
                it.isIntersected(x, y)
            }

    private fun setSnipeOnTouchListener(x: Float, y: Float)  {
        snipeList.forEach {
            it.onTouchListener(x, y)
        }
    }

    private fun animTranslationY(boundary: ReactionLottieViewState.Boundary) {
        // Init views
        val initialAlpha = if (boundary is ReactionLottieViewState.Boundary.Appear) 0f else 1f
        forEach {
            it.alpha = initialAlpha
            it.translationY = boundary.path.first.toFloat()
            if (boundary is ReactionLottieViewState.Boundary.Appear) {
                it.layoutParams.size = mediumIconSize.toInt()
            }
        }
        requestLayout()

        // TODO: animate selected index if boundary == Disappear
        currentAnimator = ValueAnimator.ofFloat(0f, 1f)
                .apply {
                    addUpdateListener {
                        val progress = it.animatedValue as Float
                        val translationY = boundary.path.progressMove(progress).toFloat()

                        forEach {
                            it.translationY = translationY
                            it.alpha = if (boundary is ReactionLottieViewState.Boundary.Appear) {
                                progress
                            } else {
                                1 - progress
                            }
                        }

                        // Invalidate children positions
                        requestLayout()
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {
                            when (boundary) {
                                is ReactionLottieViewState.Boundary.Appear -> {
                                    currentState = ReactionLottieViewState.WaitingSelection
                                }
                                is ReactionLottieViewState.Boundary.Disappear -> {
                                    visibility = View.GONE
                                    currentState = null
                                    // Notify listener
                                    dismissListener?.invoke()
                                }
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {}
                    })
                }
    }

    private fun animSize(state: ReactionLottieViewState.Selected?) {
        val paths = reactions.map {
            it.layoutParams.size to if (state == null) {
                mediumIconSize.toInt()
            } else if (state.viewReaction == it) {
                largeIconSize.toInt()
            } else {
                smallIconSize.toInt()
            }
        }

        currentAnimator = ValueAnimator.ofFloat(0f, 1f)
                .apply {
                    addUpdateListener {
                        val progress = it.animatedValue as Float

                        reactions.forEachIndexed { index, view ->
                            val size = paths[index].progressMove(progress)
                            view.layoutParams.size = size
                        }

                        // Invalidate children positions
                        requestLayout()
                    }
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {
                            val index = state?.viewReaction ?: return
                            if (reactions.indexOf(index) != 0) {
                                println("VotePopUp snipelayout VISIBLE")
                                addView(snipeLayout)
                                snipeLayout.visibility = View.VISIBLE
                                snipeLayout.show(isDownwardLayout)
                            }
                            requestLayout()
                        }

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationStart(animation: Animator?) {}
                    })
                }
    }
}

private var ViewGroup.LayoutParams.size: Int
    get() = width
    set(value) {
        width = value
        height = value
    }

/** Replace with [android.util.Size] when minSdkVersion = 21 */
private class Size(val width: Int, val height: Int)

private inline fun ViewGroup.forEach(action: (View) -> Unit) {
    for (child in 0 until childCount) {
        action(getChildAt(child))
    }
}

private fun progressMove(from: Int, to: Int, progress: Float): Int =
        from + ((to - from) * progress).toInt()

private fun Pair<Int, Int>.progressMove(progress: Float): Int =
        progressMove(first, second, progress)

private fun Context.toDp(value: Int): Int {
    return Math.round(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics))
}

sealed class ReactionLottieViewState {

    sealed class Boundary(val path: Pair<Int, Int>) : ReactionLottieViewState() {

        /** All views are moving from +translationY to 0 with normal size */
        class Appear(path: Pair<Int, Int>) : Boundary(path)

        /**
         * Different behaviour considering [selectedView]:
         * - if no [selectedView], going down with normal size
         * - otherwise going down
         *   while [selectedView] is going (idx=0=up, other=up/left) and decreasing size
         */
        class Disappear(val selectedView: ReactionLottieView?, path: Pair<Int, Int>) : Boundary(path)
    }

    object WaitingSelection : ReactionLottieViewState()

    /**
     * Increase size of selected [view] while others are decreasing.
     */
    class Selected(val viewReaction: ReactionLottieView, val viewSnipe: ReactionSnipeView.SnipeAction.SnipeActionText?) : ReactionLottieViewState()
}
