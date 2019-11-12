package com.github.pgreze.reactions.sample

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.github.pgreze.reactions.PopupGravity
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactionLottieConfig
import com.github.pgreze.reactions.dsl.reactionPopup
import com.github.pgreze.reactions.dsl.reactions
import com.github.pgreze.reactions.lottie.ReactionLottiePopup

@SuppressLint("NewApi")
fun MainActivity.setup() {
    val size = resources.getDimensionPixelSize(R.dimen.crypto_item_size)
    val margin = resources.getDimensionPixelSize(R.dimen.crypto_item_margin)

    // Popup DSL + listener via function
    val popup1 = reactionPopup(this, ::onReactionSelected) {
        reactions {
            resId    { R.drawable.ic_crypto_btc }
            resId    { R.drawable.ic_crypto_eth }
            resId    { R.drawable.ic_crypto_ltc }
            reaction { R.drawable.ic_crypto_dash scale ImageView.ScaleType.FIT_CENTER }
            reaction { R.drawable.ic_crypto_xrp scale ImageView.ScaleType.FIT_CENTER }
            drawable { getDrawable(R.drawable.ic_crypto_xmr) }
            drawable { getDrawable(R.drawable.ic_crypto_doge) }
            reaction { getDrawable(R.drawable.ic_crypto_steem) scale ImageView.ScaleType.FIT_CENTER }
            reaction { getDrawable(R.drawable.ic_crypto_kmd) scale ImageView.ScaleType.FIT_CENTER }
            drawable { getDrawable(R.drawable.ic_crypto_zec) }
        }
        reactionTexts = R.array.crypto_symbols
        popupColor = Color.LTGRAY
        reactionSize = size
        horizontalMargin = margin
        verticalMargin = horizontalMargin / 2
    }
    // Setter also available
    popup1.reactionSelectedListener = { position ->
        toast("$position selected")
        true
    }
    findViewById<View>(R.id.top_right_btn).setOnTouchListener(popup1)

    // Config DSL + listener in popup constructor
    val config = reactionConfig(this) {
        reactionsIds = intArrayOf(
            R.drawable.ic_crypto_btc,
            R.drawable.ic_crypto_eth,
            R.drawable.ic_crypto_ltc,
            R.drawable.ic_crypto_dash,
            R.drawable.ic_crypto_xrp,
            R.drawable.ic_crypto_xmr,
            R.drawable.ic_crypto_doge,
            R.drawable.ic_crypto_steem,
            R.drawable.ic_crypto_kmd,
            R.drawable.ic_crypto_zec
        )
        reactionSize = resources.getDimensionPixelSize(R.dimen.reactions_item_size)
        reactionTextProvider = { position -> "Item $position" }
        popupGravity = PopupGravity.PARENT_RIGHT
        popupMargin = resources.getDimensionPixelSize(R.dimen.crypto_item_size)
        textBackground = ColorDrawable(Color.TRANSPARENT)
        textColor = Color.BLACK
        textHorizontalPadding = 0
        textVerticalPadding = 0
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
    }

    val popup2 = ReactionPopup(this, config) { position -> true.also {
        toast("$position selected")
    } }
    findViewById<View>(R.id.right_btn).setOnTouchListener(popup2)

    findViewById<Button>(R.id.btn_lottie).setOnClickListener { startActivity(Intent(this, ListActivity::class.java)) }

}

fun MainActivity.onReactionSelected(position: Int) = true.also {
    toast("$position selected")
}

fun MainActivity.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT)
            .apply { setGravity(Gravity.CENTER, 0, 300) }
            .show()
}
