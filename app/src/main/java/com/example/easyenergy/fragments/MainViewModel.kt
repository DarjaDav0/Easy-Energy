package com.example.easyenergy.fragments

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.easyenergy.datatypes.ElectricityPrice
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel : ViewModel(){
    private val hoursList = mutableListOf<Double>()
    var allDataList = mutableListOf<ElectricityPrice>()
    var dayDataList = mutableListOf<ElectricityPrice>()
    private lateinit var testChart : AAChartModel
    val getChart get() = testChart
    var chosenYear: String = "2022"
    val currentTime = Date()
    private var _currentHourPrice: String= "test price 123"
    val currentHourPrice get() = _currentHourPrice

    private var _yearArray = mutableListOf<String>()
    val yearArray get() = _yearArray



    fun createDayChart()
    {
        hoursList.clear()
        //for loop on puhtaasti tehty testausta varten
       for (i in 0 until 24)
        {
           hoursList.add(i.toDouble())
        }

        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            .title("Hintaseuranta")
            .backgroundColor("#4b2b7f")
            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .name("c/kwh")
                    .data(hoursList.toTypedArray())
            )
            )
    }

    fun createWeekChart()
    {
        hoursList.clear()
        //for loop on puhtaasti tehty testausta varten
        for (i in 0 until 7)
        {
            hoursList.add(i.toDouble())
        }

        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            .title("Hintaseuranta")
            .backgroundColor("#4b2b7f")
            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .name("c/kwh")
                    .data(hoursList.toTypedArray())
            )
            )
    }

    fun createMonthChart()
    {
        hoursList.clear()

        //for loop on puhtaasti tehty testausta varten
        for (i in 0 until 4)
        {
            hoursList.add(i.toDouble())
        }

        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            .title("Hintaseuranta")
            .backgroundColor("#4b2b7f")
            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .name("c/kwh")
                    .data(hoursList.toTypedArray())
            )
            )
    }

    fun getAllData(context: Context) {
        viewModelScope.launch {
            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices"

            val gson = GsonBuilder().setPrettyPrinting().create()
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    var result : List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()

                    //lisää atm valitun vuoden datan listaan, kevyempi myös testata vuoden datalla
                    for (item: ElectricityPrice in result)
                    {
                        val timeValSub = item.Time?.substring(0,4)


                        if (timeValSub == chosenYear)
                        {
                            allDataList.add(item)
                        }

                        //lisää vuosi arrayhyn vuoden jos sitä ei ole jo listassa, tällä tavalla välttyi duplikaateilta
                        //TODO: muokata vuoden lisäys oman tietokannan datan mukaan
                        if (yearArray.contains(timeValSub))
                        {
                            true
                        }
                        else
                        {
                            yearArray.add(timeValSub!!)
                        }

                    }
                    Log.d("ADVTECH", allDataList.size.toString())

                },
                Response.ErrorListener {
                    // typically this is a connection error
                    Log.d("ADVTECH", it.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {

                    // basic headers for the data
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    headers["Content-Type"] = "application/json; charset=utf-8"
                    return headers
                }
            }

            // Add the request to the RequestQueue. This has to be done in both getting and sending new data.
            // if using this in an activity, use "this" instead of "context"
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(stringRequest)
        }
    }

    //TODO: viewmodelin muuttuja _currentHourPrice muuttaa nykyisen tunnin hinnan mukaiseksi ja lisätä sen stringin perään c/kwh (ei atm erillistä tekstikentää sille kun voi laittaa tätäkin kautta)
    fun getDayData(context: Context) {
        viewModelScope.launch {
            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices/${currentTime}"

            val gson = GsonBuilder().setPrettyPrinting().create()
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    var result : List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()

                    // Store the list of ElectricityPrice objects in allDataList
                    dayDataList = result.toMutableList()

                    Log.d("ADVTECH", dayDataList.toString())

                },
                Response.ErrorListener {
                    // typically this is a connection error
                    Log.d("ADVTECH", it.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {

                    // basic headers for the data
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    headers["Content-Type"] = "application/json; charset=utf-8"
                    return headers
                }
            }

            // Add the request to the RequestQueue. This has to be done in both getting and sending new data.
            // if using this in an activity, use "this" instead of "context"
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(stringRequest)
        }
    }

}