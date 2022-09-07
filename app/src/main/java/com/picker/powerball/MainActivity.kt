package com.picker.powerball

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import powerball.apps.jacs.powerball.data.mega.MegamillionsData
import powerball.apps.jacs.powerball.data.power.PowerballData
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "powerball"
    var mContext: Context? = null
    val url = "https://data.ny.gov/resource/d6yy-54nr.json"

    var tickets = ArrayList<WinningTicket?>()
    var counter = 0

    lateinit var now: Calendar
    lateinit var past: Calendar
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()
        mContext = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        display();
        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
    }


    private fun display() {
        counter = 0
        tickets = ArrayList()
        val rv: RecyclerView = findViewById(R.id.recycle1)
        rv.removeAllViews()
        rv.setHasFixedSize(true)
        rv.addItemDecoration(DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
        val llm = LinearLayoutManager(mContext)
        rv.layoutManager = llm
        val rvAdapter = RVAdapter(tickets, rv, Constants.POWERBALL)
        rv.adapter = rvAdapter
        requestQueue = Volley.newRequestQueue(mContext)
        now = Calendar.getInstance()
        past = Calendar.getInstance()
        past.add(Calendar.MONTH, -1)


        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "setRecyclerView: ")
                    val json = URL(url).readText()
                    //Log.d(TAG, json)
                    val gson = Gson()

                    val powerballData = gson.fromJson(json, PowerballData::class.java)
                    for(ticket in powerballData){
                        val person = WinningTicket()
                        person.date = ticket.draw_date//jsonObject.getString("field_draw_date")
                        person.winningNumber = ticket.winning_numbers
                        person.multiplier = ticket.multiplier
                        tickets.add(counter, person)
                        rv.post{
                            rvAdapter.notifyItemInserted(counter)
                        }
                        counter++
                        if (counter > 33)
                            break;
                    }
                } catch (e: Exception) {
                    Log.e("exception coroutine", "exception ${e.toString()}")
                }

            }
        }
    }
}