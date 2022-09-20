package com.picker.powerball

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import powerball.apps.jacs.powerball.data.mega.MegamillionsData
import java.net.URL
import java.util.*

class PredictActivity : Activity() {
    var predictTickets = ArrayList<WinningTicket?>()

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_predict)

        // AD values
        loadAd()

        predictNumber()

    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,getString(R.string.interstitial_ad_unit_no_1), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(Utils.TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(Utils.TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun predictNumber() {
        val score = IntArray(76)

        // Load whole data from site
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                var count = 0
                try {
                    Log.d(Utils.TAG, "onPredict start")
                    val json = URL(MainActivity.url).readText()
                    val gson = Gson()
                    val predictData = gson.fromJson(json, MegamillionsData::class.java)
                    for (ticket in predictData) {
                        val predict = WinningTicket()
                        predict.date = ticket.draw_date
                        predict.winningNumber = ticket.winning_numbers// + " " + ticket.mega_ball
                        predict.multiplier = ticket.multiplier
                        predictTickets.add(count, predict)
                        if (Utils.DBG) Log.d(Utils.TAG, "raw data " + predict.winningNumber)
                        count++
                        if (count > 50)
                            break
                    }
                } catch (e: Exception) {
                    Log.e(Utils.TAG, "exception ${e.toString()}")
                }
            }
            Log.d(Utils.TAG, "onPredict end")

            // Calculate prediction - save score
            for (ticket in predictTickets) {
                val winNumbers = ticket?.winningNumber?.split(" ")?.toTypedArray()
                if (winNumbers != null) {
                    for (winNumber in winNumbers) {
                        score[winNumber.toInt()]++
                    }
                }
            }

            // Calculate prediction - apply latest winning number for the better effect.
            val latestTicket = predictTickets[0]
            val latestWinNumbers = latestTicket?.winningNumber?.split(" ")?.toTypedArray()
            if (latestWinNumbers != null) {
                for (latestWinNumber in latestWinNumbers) {
                    score[latestWinNumber.toInt()]++
                    if (Utils.DBG) Log.d(Utils.TAG, "onPredict: latest " + latestWinNumber)
                }
            }

            // Make a two prediction.
            val luckyNum = IntArray(76)
            var count = 0
            for (j in 0..75) {
                var temp = 0
                var tempLoc = 0
                for (i in 0..75) {
                    if (score[i] > temp) {
                        temp = score[i]
                        tempLoc = i
                    }
                }
                luckyNum[count++] = tempLoc
                score[tempLoc] = 0
            }

            for (i in 0..75) {
                if (Utils.DBG) Log.d(Utils.TAG, "[LuckNum" + i + "=" + luckyNum[i])
            }

            val result = StringBuilder()
            val result2 = StringBuilder()
            for (i in 0..5) {
                if (Utils.DBG) Log.d(Utils.TAG, "[analyzed]" + i + " = " + luckyNum[i])
                result.append(luckyNum[i]).append(", ")
            }
            for (i in 64..69) {
                if (Utils.DBG) Log.d(Utils.TAG, "[analyzed2]" + i + " = " + luckyNum[i])
                result2.append(luckyNum[i]).append(", ")
            }

            findViewById<TextView>(R.id.win_num1).text = luckyNum[0].toString()
            findViewById<TextView>(R.id.win_num2).text = luckyNum[1].toString()
            findViewById<TextView>(R.id.win_num3).text = luckyNum[2].toString()
            findViewById<TextView>(R.id.win_num4).text = luckyNum[3].toString()
            findViewById<TextView>(R.id.win_num5).text = luckyNum[4].toString()
            findViewById<TextView>(R.id.win_num6).text = luckyNum[5].toString()

            findViewById<TextView>(R.id.win_num21).text = luckyNum[64].toString()
            findViewById<TextView>(R.id.win_num22).text = luckyNum[65].toString()
            findViewById<TextView>(R.id.win_num23).text = luckyNum[66].toString()
            findViewById<TextView>(R.id.win_num24).text = luckyNum[67].toString()
            findViewById<TextView>(R.id.win_num25).text = luckyNum[68].toString()
            findViewById<TextView>(R.id.win_num26).text = luckyNum[69].toString()

        }
    }

    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    fun onBack(view: View) {
        // add advertisement.
        showInterstitial()
        finish()
    }
}