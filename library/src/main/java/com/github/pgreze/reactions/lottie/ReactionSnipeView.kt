package com.github.pgreze.reactions.lottie

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.github.pgreze.reactions.R
import com.github.pgreze.reactions.dsl.toDp
import com.github.pgreze.reactions.lottie.ReactionSnipeView.Companion.TYPE_SNIPE.*
import kotlin.math.roundToInt

/**
 * Created by edityomurti on 2019-11-05 16:46
 */

class ReactionSnipeView constructor(
        context: Context,
        val snipeActionTextList: List<SnipeAction.SnipeActionText>
) : LinearLayout(context) {

    val snipeInfo = SnipeAction.SnipeActionInfo(context, "Geser atas untuk pilih side note")

    init {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundResource(R.drawable.reactions_bg_snipe)
    }

    fun show(downward: Boolean) {
        snipeInfo.setText("Geser ${if (!downward) "atas" else "bawah"}  untuk pilih side note")
        if (!downward) {
            addView(snipeInfo)
            snipeActionTextList.forEach {
                addView(it)
            }
        } else {
            snipeActionTextList.asReversed().forEach {
                addView(it)
            }
            addView(snipeInfo)
        }
    }

    fun hide() {
        removeView(snipeInfo)
        snipeActionTextList.forEach {
            removeView(it)
        }
    }

    sealed class SnipeAction(context: Context) : TextView(context){

        class SnipeActionText(context: Context, typeSnipe: TYPE_SNIPE): SnipeAction(context) {
            var id: String

            init {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(Color.parseColor("#353e4a"))
                setPadding(
                        context.toDp(4),
                        context.toDp(12),
                        context.toDp(4),
                        context.toDp(12)
                )
                id = when (typeSnipe) {
                    SNIPE_TERIMAKASIH -> ":terima_kasih:"
                    SNIPE_BERMANFAAT -> ":bermanfaat:"
                    SNIPE_BERNAS -> ":bernas:"
                    SNIPE_SALAH_KONTEKS -> ":salah_konteks:"
                    SNIPE_DISINFORMASI -> ":disinformasi:"
                    SNIPE_SPAM -> ":spam:"
                }
                text = when (typeSnipe) {
                    SNIPE_TERIMAKASIH -> "Terima Kasih"
                    SNIPE_BERMANFAAT -> "Bermanfaat"
                    SNIPE_BERNAS -> "Bernas"
                    SNIPE_SALAH_KONTEKS -> "Salah Konteks"
                    SNIPE_DISINFORMASI -> "Disinformasi"
                    SNIPE_SPAM -> "Spam"
                }
                gravity = Gravity.CENTER
            }

            fun isIntersected(x: Float, y: Float): Boolean {
                return x >= getCurrentLocation().x
                        && x < getCurrentLocation().x + width
                        && y >= getCurrentLocation().y
                        && y < getCurrentLocation().y + height
            }

            fun onTouchListener(x: Float, y: Float) {
                if (isIntersected(x, y)) {
                    setTypeface(null, Typeface.BOLD)
                } else {
                    setTypeface(null, Typeface.NORMAL)
                }
            }

            private fun getCurrentLocation(): Point {
                val location = IntArray(2).also(::getLocationOnScreen)
                return Point(location[0], location[1])
            }
        }

        class SnipeActionInfo(context: Context, textCaption: CharSequence): SnipeAction(context) {
            init {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(Color.parseColor("#8691a1"))
                setPadding(
                        context.toDp(4),
                        context.toDp(12),
                        context.toDp(4),
                        context.toDp(12)
                )
                text = textCaption
                gravity = Gravity.CENTER
            }
        }

    }

    companion object {
        enum class TYPE_SNIPE {
            SNIPE_TERIMAKASIH,
            SNIPE_BERMANFAAT,
            SNIPE_BERNAS,
            SNIPE_SALAH_KONTEKS,
            SNIPE_DISINFORMASI,
            SNIPE_SPAM
        }
    }
}