package com.github.pgreze.reactions.lottie

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.github.pgreze.reactions.R
import kotlin.math.roundToInt

/**
 * Created by edityomurti on 2019-11-05 16:46
 */

class ReactionSnipeView constructor(
        context: Context,
        snipeActionTextList: List<SnipeAction.SnipeActionText>
) : LinearLayout(context) {

    init {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.reactions_bg_snipe)

        val snipeInfo = SnipeAction.SnipeActionInfo(context, "Geser atas untuk pilih side note")
        addView(snipeInfo)
        snipeActionTextList.forEach {
            addView(it)
        }
    }

    sealed class SnipeAction(context: Context) : TextView(context){

        class SnipeActionText(context: Context, textCaption: CharSequence, id: String): SnipeAction(context) {
            val id: String = id

            init {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(Color.parseColor("#353e4a"))
                setPadding(
                        toDp(4),
                        toDp(12),
                        toDp(4),
                        toDp(12)
                )
                text = textCaption
                gravity = Gravity.CENTER
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

            fun onTouchListener(x: Float, y: Float) {
                if (isIntersected(x, y)) {
                    setTypeface(null, Typeface.BOLD)
                } else {
                    setTypeface(null, Typeface.NORMAL)
                }
            }
        }

        class SnipeActionInfo(context: Context, textCaption: CharSequence): SnipeAction(context) {
            init {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(Color.parseColor("#8691a1"))
                setPadding(
                        toDp(4),
                        toDp(12),
                        toDp(4),
                        toDp(12)
                )
                text = textCaption
                gravity = Gravity.CENTER
            }
        }


        fun toDp(value: Int): Int {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).roundToInt()
        }

    }

}