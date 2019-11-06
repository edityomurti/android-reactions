package com.github.pgreze.reactions.sample

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.datasource.emptyDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.github.pgreze.reactions.PopupGravity
import com.github.pgreze.reactions.dsl.reactionLottieConfig
import com.github.pgreze.reactions.lottie.ReactionLottiePopup

class ListActivity : AppCompatActivity() {

    val dataSource = emptyDataSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)



        val rv = findViewById<RecyclerView>(R.id.recycler_view)

        rv.setup {
            withDataSource(dataSource)
            withItem<String, VoteClassHolder>(R.layout.item_vote) {
                onBind(::VoteClassHolder) { index, item ->

                    val lottieConfig = reactionLottieConfig(this@ListActivity) {
                        reactionFileNames = arrayOf(
                                "lottie_reaction_thumbsdown.json",
                                "lottie_reaction_thumbsup.json",
                                "lottie_reaction_grinning.json",
                                "lottie_reaction_neutral_face.json"
                        )
                        reactionTextProvider = { position -> "Item $position" }
                        popupGravity = PopupGravity.SCREEN_LEFT
                        popupMargin = resources.getDimensionPixelSize(R.dimen.popup_margin)
                        textBackground = ColorDrawable(Color.TRANSPARENT)
                        textColor = Color.BLACK
                        textHorizontalPadding = 0
                        textVerticalPadding = 0
                        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
                    }

                    val popupLottie = ReactionLottiePopup(this@ListActivity, lottieConfig, rv, {
                        toast("VOTE!")
                    }) { position, snipe -> true.also {
                        toast("reaction: $position\nsnipe: $snipe")
                    } }

                    btnVote.text = "Vote $index"
                    btnVote.setOnTouchListener(popupLottie)
                }
            }
        }

        addData()
    }

    private fun addData() {
        for (i in 0 until 20) {
            dataSource.add("item ke $i")
        }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 300) }
                .show()
    }

    inner class VoteClassHolder(view: View) : ViewHolder(view) {
        val btnVote = view.findViewById<Button>(R.id.btn_vote)
    }
}
