/*
 * Author: John Rowan
 * Description: Adapter for recyclerviews from PowerballFragment and MegaMillionsFragment it does comparason
 * of users tickets with winning tickets and adds results to a view.
 * Anyone may use this file or anything contained in this project for their own personal use.
 */
package com.picker.powerball

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class RVAdapter internal constructor(
    var tickets: List<WinningTicket?>?,
    recyclerView: RecyclerView,
    private val type: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private var onLoadMoreListener: OnLoadMoreListener? = null
    private val visibleThreshold = 1
    private var lastVisibleItem = 0
    private var totalItemCount = 0
    private var isLoading = false
    fun setOnLoadMoreListener(mOnLoadMoreListener: OnLoadMoreListener?) {
        onLoadMoreListener = mOnLoadMoreListener
    }

    override fun getItemViewType(position: Int): Int {
        return if (tickets!![position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_ITEM) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.cardview, parent, false)
            return PersonViewHolder(v, parent.context)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            return LoadingViewHolder(view, parent.context)
        }

    }

    override fun onBindViewHolder(holder1: RecyclerView.ViewHolder, position: Int) {
        if (holder1 is PersonViewHolder) {
            val holder = holder1
            val view: View
            val inflater = holder.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val ball1: TextView
            val ball2: TextView
            val ball3: TextView
            val ball4: TextView
            val ball5: TextView
            val ball6: TextView

            val date = TextView(holder.context)
            date.textSize = 20f
            date.text = "Date: " + tickets!![holder1.getAdapterPosition()]!!.date
            date.text = date.text.substring(0,date.text.indexOf("T"))
            date.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            val winning = TextView(holder.context)
            winning.text = "Winning Numbers"
            winning.textSize = 20f

                view = inflater.inflate(R.layout.balls1, null)
                ball1 = view.findViewById<View>(R.id.ball1m) as TextView
                ball2 = view.findViewById<View>(R.id.ball2m) as TextView
                ball3 = view.findViewById<View>(R.id.ball3m) as TextView
                ball4 = view.findViewById<View>(R.id.ball4m) as TextView
                ball5 = view.findViewById<View>(R.id.ball5m) as TextView
                ball6 = view.findViewById<View>(R.id.ball6m) as TextView

            val images = arrayOf(ball1, ball2, ball3, ball4, ball5, ball6)
            val num = tickets!![position]!!.winningNumber
            val split = num?.split(" ")?.toTypedArray()
            for (j in split?.indices!!) {
                images[j].text = split?.get(j)
            }
            holder.linear.addView(date)
            //holder.linear.addView(winning)
            holder.linear.addView(view)
            //TextView end = (TextView) view1.findViewById(R.id.win);

        } else if (holder1 is LoadingViewHolder) {
            holder1.progressBar.isIndeterminate = true
        }


        //holder.date.setText("Date: " + tickets.get(position).date);
        //holder.ticket.setText("Winning Numbers: " + tickets.get(position).winningNumber);
        // holder.multi.setText("Multiplier: " + tickets.get(position).multiplier);
        //holder.yourtickets.setText("ticket1: " + tickets.get(position).ticket1 + " := " + tickets.get(position).calculateWin(tickets.get(position).ticket1) + "\n"
        //+ "ticket2: " + tickets.get(position).ticket2 + " := " + tickets.get(position).calculateWin(tickets.get(position).ticket2) + "\n"
        // + "ticket3: " + tickets.get(position).ticket3 + " := " + tickets.get(position).calculateWin(tickets.get(position).ticket3) + "\n");
    }

    fun setLoaded() {
        isLoading = false
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return if (tickets != null) {
            tickets!!.size
        } else 0
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    inner class PersonViewHolder internal constructor(itemView: View, context: Context) :
        RecyclerView.ViewHolder(itemView) {
        var linear: LinearLayout
        var context: Context

        init {
            itemView.setHasTransientState(true)
            linear = itemView.findViewById<View>(R.id.linear) as LinearLayout
            this.context = context
        }
    }

    inner class LoadingViewHolder(view: View, var context: Context) :
        RecyclerView.ViewHolder(view) {
        var progressBar: ProgressBar

        init {
            progressBar = view.findViewById<View>(R.id.progressBar1) as ProgressBar
        }
    }

    init {
        //setHasStableIds(true);
        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = linearLayoutManager!!.itemCount
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener!!.onLoadMore()
                    }
                    isLoading = true
                }
            }
        })
    }
}