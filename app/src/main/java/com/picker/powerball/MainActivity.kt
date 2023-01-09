package com.picker.powerball

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import powerball.apps.jacs.powerball.data.mega.MegamillionsData
import powerball.apps.jacs.powerball.data.power.PowerballData
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var mContext: Context? = null
    companion object {
        val url = "https://data.ny.gov/resource/d6yy-54nr.json"
    }

    var tickets = ArrayList<WinningTicket?>()
    var counter = 0

    lateinit var now: Calendar
    lateinit var past: Calendar
    private lateinit var requestQueue: RequestQueue

    val SPBadActivity = "preventBadActivity"
    var bAdmob = true


    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()
        mContext = this
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        display()
        Toast.makeText(this, "Loading...", Toast.LENGTH_LONG).show()
        initAdmobView()
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
                    Log.d(Utils.TAG, "setRecyclerView: ")
                    val json = URL(url).readText()
                    //Log.d(Utils.TAG, json)
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
                            break
                    }
                } catch (e: Exception) {
                    Log.e("exception coroutine", "exception ${e.toString()}")
                }

            }
        }
    }



    // admob +
    private fun initAdmobView() {
        MobileAds.initialize(this) {}

        val admobView = findViewById<AdView>(R.id.admobView)
        val adRequest = AdRequest.Builder().build()
        admobView.loadAd(adRequest)

        admobView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(Utils.TAG, "onAdLoaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                Log.d(Utils.TAG, "onAdFailedToLoad$adError")
                bAdmob = false
                if (admobView != null) {
                    admobView.pause()
                    admobView.visibility = View.INVISIBLE
                }
                Log.d(Utils.TAG, "Pause adview and init mopub.")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d(Utils.TAG, "onAdOpened")

                // Today date
                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val date = Date()
                val strClickedDate = dateFormat.format(date)

                // Prevent bad activity ver2.
                // Remove AD immediately when user click AD.
                val sharedPreferences = getSharedPreferences(SPBadActivity, MODE_PRIVATE)
                val spValue = sharedPreferences.getString(SPBadActivity, "")
                if (spValue != strClickedDate) {
                    // First time to click AD. Save it in SP.
                    val editor = sharedPreferences.edit()
                    editor.putString(SPBadActivity, strClickedDate)
                    editor.apply()
                    Log.d(Utils.TAG, "saveDate value = $strClickedDate")
                }
                // Already clicked advertisement today. Remove AD.
                if (admobView != null) {
                    admobView.destroy()
                    admobView.visibility = View.GONE
                    val frameLay = findViewById<FrameLayout>(R.id.frameAd)
                    frameLay.visibility = View.GONE
                }
                Log.d(Utils.TAG, "Pause adview and save today.")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.d(Utils.TAG, "onAdClicked")
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Log.d(Utils.TAG, "onAdClosed")
            }
        }
    }
    // admob -

    fun onPredict(view: View) {
        if (counter == 0) {
            Log.d(Utils.TAG, "onPredict counter == 0")
            display()
        } else {
            // Predict number +
            val intentPredict = Intent(this, PredictActivity::class.java)
            startActivity(intentPredict)
            // Predict number -
        }
    }
}