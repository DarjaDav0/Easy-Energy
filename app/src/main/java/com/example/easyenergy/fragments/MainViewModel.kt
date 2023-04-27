package com.example.easyenergy.fragments

import android.R
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.easyenergy.BuildConfig
import com.example.easyenergy.datatypes.ElectricityPrice
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import com.google.gson.GsonBuilder
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.domain.Authorization
import com.influxdb.client.domain.Permission
import com.influxdb.client.domain.PermissionResource
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.channels.consumeEach
import kotlin.collections.ArrayList
import kotlin.math.log


class MainViewModel : ViewModel(){
    private val url = "http://localhost:8086"
    //token pitäisi saada local.properties tiedostoon ja sitten hakea sieltä
    private val token = "wRvSgV9igGmzZnHpB-pJ-l-oZQ2LtRwSoZgeazPmWTKLaP5RJhEYVhGgoh5bYVbPl8H0HSrV41KWBj94rztSkw=="
    private val org = "easyenergy"
    private val bucket = "electricity_prices"
    private val client: InfluxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket)

    private var hoursList = mutableListOf<Double>()
    var allDataList = mutableListOf<ElectricityPrice>()
    var dayDataList = mutableListOf<Double>()
    var dayClassList = mutableListOf<ElectricityPrice>()
    private lateinit var testChart : AAChartModel
    val getChart get() = testChart
    var chosenYear: String = "2022"
    val timeFormat = ""
    var currentTime = ""
    val displayDayPrices: List<Double> = listOf(8.70, 8.61, 8.59, 9.99, 11.69, 14.28, 13.24, 10.54, 9.63, 9.37, 9.06, 8.99, 9.03, 8.89, 8.88, 9.78, 10.63, 10.24, 9.64, 9.68, 9.73, 9.70)
    val displayMonthPrices: List<Double> = listOf(4.26, 5.39, 10.33, 11.59, 10.65, 9.13, 5.55, 5.46, 3.72, 2.6, 3.33, 3.63, 1.87, 3.38, 4.39, 7.38, 10.0, 8.5, 5.47, 5.85, 5.96, 3.63, 3.67, 6.8, 3.77, 6.15)
    val  displayYearPrice: List<Double> = listOf(13.23, 14.03, 18.39, 26.15, 21.55, 11.32, 19.56, 24.56, 7.89, 8.0, 7.41, 5.96,)
    private var _currentHourPrice: String= ""
    val currentHourPrice get() = _currentHourPrice
    private var _yearArray = mutableListOf<String>()
    private var selectedYear = "2023"



    //tällä hetkellä hakee getAllDatan kautta kaikki vuodet, _yearArrayhyn tallennettu getAllDatan data -> tulee vaihtaa sitten lopulliseen vuoden datan hakuun
    fun createSpinner(spinner: Spinner, context: Context)
    {
        val spinnerAdapter= ArrayAdapter(context, R.layout.simple_spinner_item, _yearArray)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

                selectedYear = spinner.getSelectedItem().toString()
                Log.d("Spinner", selectedYear)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    fun createDayChart()
    {
        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            //.title("Hintaseuranta")
            .titleStyle(AAStyle.Companion.style("#494949", 32, AAChartFontWeightType.Bold))
            .axesTextColor("#494949")
            .backgroundColor("#f9bf05")

            .axesTextColor("#FFFF000")
            .yAxisTitle("c/kwh")
            .xAxisVisible(true)
            .borderRadius(3)
            .title("Day price")
            .animationDuration(650)
            .dataLabelsEnabled(false)
            .animationType(AAChartAnimationType.SwingTo)
            .categories(arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23"))
            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .color("#494949")
                    .name("c/kwh")
                    .data(displayDayPrices.toTypedArray())
            )
            )
    }

    fun createMonthChart()
    {
        hoursList.clear()
        //for loop on puhtaasti tehty testausta varten
        for (i in 0 until 7)
        {
            hoursList.add(i.toDouble())
        }

        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            //.title("Hintaseuranta")
            .titleStyle(AAStyle.Companion.style("#494949", 28, AAChartFontWeightType.Bold))
            .axesTextColor("#494949")
            .backgroundColor("#f9bf05")

            .axesTextColor("#FFFF000")
            .yAxisTitle("c/kwh")
            .borderRadius(3)
            .title("Month price")
            .animationDuration(650)
            .animationType(AAChartAnimationType.SwingTo)
            .categories(arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9",
                "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                "30", "31"))
            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .color("#494949")
                    .name("c/kwh")
                    .data(displayMonthPrices.toTypedArray())
            )
            )
    }

    fun createYearChart()
    {
        hoursList.clear()

        //for loop on puhtaasti tehty testausta varten
        for (i in 0 until 4)
        {
            hoursList.add(i.toDouble())
        }

        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            //.title("Hintaseuranta")
            .titleStyle(AAStyle.Companion.style("#494949", 32, AAChartFontWeightType.Bold))
            .axesTextColor("#494949")
            .backgroundColor("#f9bf05")


            .axesTextColor("#FFFF000")
            .yAxisTitle("c/kwh")
            .borderRadius(3)
            .title("Year price")
            .animationDuration(650)
            .animationType(AAChartAnimationType.SwingTo)
            .categories(arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9",
                "10", "11", "12"))
            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .color("#494949")
                    .name("c/kwh")
                    .data(displayYearPrice.toTypedArray())
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
                        if (_yearArray.contains(timeValSub))
                        {
                            true
                        }
                        else
                        {
                            _yearArray.add(timeValSub!!)
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
    fun getDayData(context: Context) {
        viewModelScope.launch {
            val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()
            currentTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            //periaatteessa toimii kun vaihtaa 2023-03-21 kohdan currenttimeen
            //mutta backendillä oli itsellään ongelmia hakea viime aikaista dataa byday
            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices"

            val gson = GsonBuilder().setPrettyPrinting().create()
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    var result : List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()



                    for (item in result)
                    {
                        val time = item.Time?.substring(12,13)
                        dayDataList.add(item.value!!)
                        hoursList.add(time!!.toDouble())
                        dayClassList.add(item)
                    }
                    // Store the list of ElectricityPrice objects in allDataList

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

    fun getMonthData(context: Context) {
        viewModelScope.launch {
            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices"

            val gson = GsonBuilder().setPrettyPrinting().create()
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    val result: List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()
                    val dailyData: MutableMap<String, MutableList<Double>> = mutableMapOf()

                    // Calculate average price for each day
                    for (item in result) {
                        val date = item.Time?.substring(0, 10) ?: continue
                        val price = item.value ?: continue

                        if (!dailyData.containsKey(date)) {
                            dailyData[date] = mutableListOf(price)
                        } else {
                            dailyData[date]?.add(price)
                        }
                    }

                    // Calculate average price for each month
                    val monthlyData: MutableMap<String, MutableList<Double>> = mutableMapOf()
                    for ((date, prices) in dailyData) {
                        val month = date.substring(0, 7)
                        if (!monthlyData.containsKey(month)) {
                            monthlyData[month] = mutableListOf(prices.average())
                        } else {
                            monthlyData[month]?.add(prices.average())
                        }
                    }

                    // Print averages for each month
                    for ((month, prices) in monthlyData) {
                        val average = prices.average()
                        Log.d("month:", average.toString())
                    }

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

            // Add the request to the RequestQueue.
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(stringRequest)
        }
    }

    fun getLatestHourData(context: Context) {
        viewModelScope.launch {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices/byday/$currentDate" + "T00:00:00Z"

            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    val result: List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()

                    val calendar = Calendar.getInstance()
                    calendar.time = Date()

                    // Calculate the latest full hour
                    val latestFullHour = calendar.get(Calendar.HOUR_OF_DAY) - 1
                    val filteredList = result.filter { it.Time?.substring(11, 13)?.toInt() == latestFullHour }

                    val hourDataList = mutableListOf<Double>()

                    for (item in filteredList) {
                        hourDataList.add(item.value!!)
                        _currentHourPrice = "${item.value!!} c/kwh"
                    }

                    if (hourDataList.isEmpty()) {
                        Log.d("ADVTECH", "No data for the latest full hour.")
                    } else {
                        Log.d("ADVTECH", "Values for the latest full hour:")
                        for (item in hourDataList) {
                            Log.d("ADVTECH", "$item c/kwh")
                        }
                    }
                },
                Response.ErrorListener {
                    Log.d("ADVTECH", it.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    headers["Content-Type"] = "application/json; charset=utf-8"
                    return headers
                }
            }

            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(stringRequest)
        }
    }


    fun getTodayAverage(context: Context) {
        viewModelScope.launch {
            val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices/byday/$currentDate" + "T00:00:00Z"

            val gson = GsonBuilder().setPrettyPrinting().create()
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    var result : List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()
                    val dayDataList = mutableListOf<Double>()

                    for (item in result)
                    {
                        dayDataList.add(item.value!!)
                        _currentHourPrice = "${item.value!!} c/kwh"
                    }

                    val average = dayDataList.average()
                    Log.d("ADVTECH", "Päivän keskiarvo: $average")
                },
                Response.ErrorListener {
                    Log.d("ADVTECH", it.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    headers["Content-Type"] = "application/json; charset=utf-8"
                    return headers
                }
            }

            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(stringRequest)
        }
    }


    fun getNowAverage(context: Context) {
        val currentDay = SimpleDateFormat("yyyy-MM-ddThh:", Locale.getDefault()).format(Date())
        val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
        val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices/byday/$currentTime" + "T$currentHour:00:00Z"
        val gson = GsonBuilder().setPrettyPrinting().create()

        val stringRequest = object : StringRequest(
            Request.Method.GET, JSON_URL,
            Response.Listener { response ->
                val result = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()
                var sum = 0.0
                var count = 0

                for (item in result) {
                    sum += item.value ?: 0.0
                    count++
                }

                val average = if (count > 0) sum / count else 0.0

                Log.d("ADVTECH", "Average for $currentDay: $average")
            },
            Response.ErrorListener {
                Log.d("ADVTECH", it.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(stringRequest)
    }

 //deprekoitu functio ↓
//    fun getDataFromInflux(context: Context) {
//
//        val influxDBClient = InfluxDBClientKotlinFactory
//            .create("http://localhost:8086", "wRvSgV9igGmzZnHpB-pJ-l-oZQ2LtRwSoZgeazPmWTKLaP5RJhEYVhGgoh5bYVbPl8H0HSrV41KWBj94rztSkw==".toCharArray(), org, bucket)
//        // hard coded ajat toistaiseksi
//        val fluxQuery = ("from(bucket: \"electricity_prices\")\n" +
//                "  |> range(start: 2023-04-20T04:00:00Z, stop: 2023-04-21T04:00:00Z)\n" +
//                "  |> filter(fn: (r) => r[\"_measurement\"] == \"my_measurement\")")
//
//
//        val results = influxDBClient.getQueryKotlinApi().queryRaw(fluxQuery, org)
//
//
//        Log.d("InfluxDB", results.toString())
//
//        influxDBClient.close()
//
//    }

    //deprekoitu functio ↓
    fun getThisHourData(context: Context) {
        viewModelScope.launch {
            currentTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()

            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices/byday/2023-03-21T${currentHour}:00:00Z"

            val gson = GsonBuilder().setPrettyPrinting().create()
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    var result: List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()

                    val formatter = DecimalFormat("0.0")
                    val thisHourItem = result[currentHour]
                    val formatted = formatter.format(thisHourItem.value)
                    _currentHourPrice = "$formatted c/kwh"
                    Log.d("ThisHour", thisHourItem.value.toString())



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

    //deprekoitu functio ↓
//    fun testDataFromInflux() {
//
//        //TODO: ongelmana on authorization, influxd serveri vaatii edelleen tokenia pyöriessään
//        runBlocking {
//            val token = "nv-jvEE-tt0Ofh7fm9kaTtyMoGHB_DaQRmj1D3POJwrNbYvNBK1BwX1B0trBMMEiUcsPp5Nh1XHx12qY0Ya8bQ==".toCharArray()
//            /*
//            val resource = PermissionResource()
//            resource.org("Easy Energy")
//            resource.type(PermissionResource.TYPE_BUCKETS)
//            val read = Permission()
//            read.resource(resource)
//            read.action(Permission.ActionEnum.READ)
//             */
//            val influxDBClient = InfluxDBClientKotlinFactory
//                .create(
//                    "http://10.0.2.2:8086?readTimeout=5000&connectTimeout=5000&logLevel=BASIC", token
//                )
//
//            // hard coded ajat toistaiseksi
//            val fluxQuery = ("from(bucket: \"electricity_prices\")\n" +
//                    "  |> range(start: 2023-04-20T04:00:00Z, stop: 2023-04-21T04:00:00Z)\n" +
//                    "  |> filter(fn: (r) => r[\"_measurement\"] == \"my_measurement\")")
//
//
//            val list = mutableListOf<String>()
//            val result = influxDBClient.getQueryKotlinApi().query(fluxQuery, "Easy Energy")
//
//            Log.d("InfluxDB", result.toString())
//
//
//
//            influxDBClient.close()
//        }
//    }

}