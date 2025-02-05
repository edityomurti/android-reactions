package com.github.pgreze.reactions.lottie

import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * Created by edityomurti on 2019-11-06 16:50
 */

class ReactionSnipeLayout(
        context: Context, dialogWidth: Int,
        val snipeArrow: ImageView,
        snipeList: List<ReactionSnipeView.SnipeAction.SnipeActionText>
) : LinearLayout(context) {

    val reactionSnipeView = ReactionSnipeView(context, snipeList)
            .also { snipeView ->
                snipeView.layoutParams = LinearLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

    init {
        layoutParams = LinearLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        orientation = LinearLayout.VERTICAL
        visibility = View.GONE
    }

    fun show(downward: Boolean) {
        if (!downward) {
            addView(reactionSnipeView)
            addView(snipeArrow)
        } else {
            addView(snipeArrow)
            addView(reactionSnipeView)
        }
        reactionSnipeView.show(downward)
        println("getInitialHeight in show : $height")
    }

    fun hide() {
        removeView(reactionSnipeView)
        removeView(snipeArrow)
        reactionSnipeView.hide()
    }

    fun isIntersected(x: Float, y: Float): Boolean {
        return x >= getCurrentLocation().x
                && x < getCurrentLocation().x + width
                && y >= getCurrentLocation().y
                && y < getCurrentLocation().y + height
    }

    private fun getCurrentLocation(): Point {
        val location = IntArray(2).also(::getLocationOnScreen)
        return Point(location[0], location[1])
    }
}