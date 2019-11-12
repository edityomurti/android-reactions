package com.github.pgreze.reactions.lottie

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.github.pgreze.reactions.ReactionLottie
import com.github.pgreze.reactions.lottie.ReactionLottieView.Companion.TYPE_REACTION.*

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
        setAnimation(
                when (reaction.typeReaction) {
                    REACTION_UPVOTE -> "lottie_reaction_upvote.json"
                    REACTION_SLIGHT_SMILE -> "lottie_reaction_slight_smile.json"
                    REACTION_GRINNING -> "lottie_reaction_grinning.json"
                    REACTION_THUMBSUP -> "lottie_reaction_thumbsup.json"
                    REACTION_DOWNVOTE -> "lottie_reaction_downvote.json"
                    REACTION_NEUTRAL_FACE -> "lottie_reaction_neutral_face.json"
                    REACTION_SLIGHT_FROWN -> "lottie_reaction_slight_frown.json"
                    REACTION_THUMBSDOWN -> "lottie_reaction_thumbsdown.json"
                }
        )
        repeatCount = LottieDrawable.INFINITE
        playAnimation()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        location.set(0, 0)
    }

    companion object {
        enum class TYPE_REACTION {
            REACTION_UPVOTE,
            REACTION_SLIGHT_SMILE,
            REACTION_GRINNING,
            REACTION_THUMBSUP,
            REACTION_DOWNVOTE,
            REACTION_NEUTRAL_FACE,
            REACTION_SLIGHT_FROWN,
            REACTION_THUMBSDOWN
        }
    }

}