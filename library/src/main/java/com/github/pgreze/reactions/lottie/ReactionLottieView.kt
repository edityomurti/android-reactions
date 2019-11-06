package com.github.pgreze.reactions.lottie

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.github.pgreze.reactions.ReactionLottie

/**
 * Created by edityomurti on 2019-10-28 18:16
 */
class ReactionLottieView constructor(
        context: Context,
        val reaction: ReactionLottie
) : LottieAnimationView(context) {

    val location = Point()
        get() {
            if (field.x == 0 || field.y == 0) {
                val location = IntArray(2).also(::getLocationOnScreen)
                field.set(location[0], location[1])
            }
            return field
        }

    init {
        scaleType = ScaleType.FIT_CENTER
        setAnimation(reaction.fileName)
        repeatCount = LottieDrawable.INFINITE
        playAnimation()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        location.set(0, 0)
    }

}