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

class ReactionSnipeLayout(context: Context, dialogWidth: Int, snipeArrow: ImageView, snipeList: List<ReactionSnipeView.SnipeAction.SnipeActionText>) : LinearLayout(context) {
    init {
        layoutParams = LinearLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        orientation = LinearLayout.VERTICAL
        visibility = View.GONE

        val reactionSnipeView = ReactionSnipeView(context, snipeList)
                .also { snipeView ->
                    snipeView.layoutParams = LinearLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
        addView(reactionSnipeView)
        addView(snipeArrow)

    }

    private val location = Point()
        get() {
            if (field.x == 0 || field.y == 0) {
                val location = IntArray(2).also(::getLocationOnScreen)
                field.set(location[0], location[1])
            }
            return field
        }

    fun isIntersected(x: Float, y: Float): Boolean {
        return x >= location.x
                && x < location.x + width
                && y >= location.y
                && y < location.y + height
    }
}