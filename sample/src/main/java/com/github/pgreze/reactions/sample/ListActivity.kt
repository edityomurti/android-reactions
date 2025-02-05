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
import com.github.pgreze.reactions.lottie.ReactionLottieViewGroup

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

                    val isUpvote = index % 2 == 0

                    val lottieConfig = reactionLottieConfig(this@ListActivity) {
                        typeVote = if (isUpvote) ReactionLottieViewGroup.Companion.TypeVote.VOTE_UPVOTE else ReactionLottieViewGroup.Companion.TypeVote.VOTE_DOWNVOTE
                        textBackground = ColorDrawable(Color.TRANSPARENT)
                    }

                    val popupLottie = ReactionLottiePopup(this@ListActivity, lottieConfig, rv) { isUpvote, reaction, snipe -> true.also {
                        toast("vote: $isUpvote\nreaction: $reaction\nsnipe: $snipe")
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
