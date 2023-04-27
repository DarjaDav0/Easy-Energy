package com.example.easyenergy.fragments

import android.R
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.easyenergy.ElectricityPriceAdapter
import com.example.easyenergy.databinding.FragmentMainBinding
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.easyenergy.datatypes.ElectricityPrice
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {
    private val bucket = "electricity_prices"
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var _currentHourPrice: String= ""
    val currentHourPrice get() = _currentHourPrice
    private var hoursList = mutableListOf<Double>()
    var allDataList = mutableListOf<ElectricityPrice>()
    var dayDataList = mutableListOf<Double>()
    var dayClassList = mutableListOf<ElectricityPrice>()
    private lateinit var testChart : AAChartModel
    val getChart get() = testChart
    var chosenYear: String = "2022"
    val timeFormat = ""
    private lateinit var listView: ListView
    private lateinit var linearLayoutManager: LinearLayoutManager
    var currentTime = ""
    //TODO: optimoi viewin luonti
    // view lataa nopeammin kuin data eikä suostu refreshaamaan muuta kuin puhelinta flippaamalla
    // testattu: viewgroupin removeAllViews ja refreshDrawableState(), asetettu layoutista näkyvyys GONE ja täällä VISIBLE, activityn käynnistystä uudelleen, fragment transactionia jne


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.currentHourPriceText.text = _currentHourPrice
        //viewModel.getThisHourData(this.requireContext())
        //viewModel.getDayData(this.requireContext())

        //binding.dayPriceList.adapter = ElectricityPriceAdapter()
        getLatestHourData()
        //getTodayPrices(requireContext(), listView)
        //val recyclerView = binding.dayPricesList
        //daily
        binding.dayButton.setOnClickListener()
        {
            viewModel.createDayChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
        }
        getDayData()
        //monthly
        binding.monthButton.setOnClickListener()
        {
            viewModel.createMonthChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
        }

        //year
        binding.yearButton.setOnClickListener()
        {
            viewModel.getAllData(this.requireContext())
            viewModel.createYearChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
            //viewModel.createSpinner(binding.spinnerDropdown, this.requireContext())
            //binding.spinnerDropdown.visibility = View.VISIBLE

        }

        //testaukseen, saa poistaa lopullisesta versiosta
        binding.buttonForTesting.setOnClickListener()
        {
            //viewModel.getAllData(this.requireContext())
            //viewModel.getDataFromInflux(this.requireContext())
            viewModel.getTodayAverage(this.requireContext())
            //testattu eri tapoja influxiin
            //viewModel.testDataFromInflux()
        }



        linearLayoutManager = LinearLayoutManager(this.requireContext())
        binding.dayPriceList.layoutManager = linearLayoutManager




        return binding.root
    }

    fun getLatestHourData() {

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
                    val latestFullHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val filteredList = result.filter { it.Time?.substring(11, 13)?.toInt() == latestFullHour }

                    val hourDataList = mutableListOf<Double>()

                    for (item in filteredList) {
                        hourDataList.add(item.value!!)
                        _currentHourPrice = "${item.value!!} c/kwh"

                    }

                    if (hourDataList.isEmpty()) {
                        Log.d("getLatestHourData()", "No data for the latest full hour.")

                    } else {
                        Log.d("getLatestHourData()", "Values for the latest full hour:")
                        for (item in hourDataList) {
                            Log.d("getLatestHourData()", "$item c/kwh")
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
    fun getDayData() {
            val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()
            val currentTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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


                    Log.d("getDayData()", dayDataList.toString())

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

//    fun getTodayPrices(context: Context, listView: ListView) {
//
//        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//        val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices/byday/$currentDate" + "T00:00:00Z"
//        val gson = GsonBuilder().setPrettyPrinting().create()
//
//        val stringRequest: StringRequest = object : StringRequest(
//            Request.Method.GET, JSON_URL,
//            Response.Listener { response ->
//                var result : List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()
//                val dayDataList = mutableListOf<String>()
//
//                for (item in result) {
//                    val price = "${item.Time?.substring(11,13)}: ${item.value} c/kwh"
//                    dayDataList.add(price)
//                }
//
//                val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, dayDataList)
//                listView.adapter = adapter
//            },
//            Response.ErrorListener {
//                Log.d("ADVTECH", it.toString())
//            }
//        ) {
//            @Throws(AuthFailureError::class)
//            override fun getHeaders(): Map<String, String> {
//                val headers = HashMap<String, String>()
//                headers["Accept"] = "application/json"
//                headers["Content-Type"] = "application/json; charset=utf-8"
//                return headers
//            }
//        }
//
//        val requestQueue = Volley.newRequestQueue(context)
//        requestQueue.add(stringRequest)
//
//    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        activity?.runOnUiThread()
        {
            Handler(Looper.getMainLooper()).postDelayed({
                //käyttää ensin viewmodelissa sijaitsevaa funktiota jonka jälkeen asettaa kuvaajaan viewmodelin muuttujan kautta
                viewModel.createDayChart()
                binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)

                //asettaa main fragmentin textviewin viewmodelin muuttujan mukaan
                binding.currentHourPriceText.text = _currentHourPrice

                binding.currentHourPriceText.visibility = View.VISIBLE
                binding.aaChartView.visibility = View.VISIBLE
            }, 200)

        }
    }

    // testinappia varten täällä tämä funktio
//    fun getDataFromInflux(){
//
//        runBlocking {
//            val influxDBClient = InfluxDBClientKotlinFactory
//                .create(
//                    "http://localhost:8086",
//                    "wRvSgV9igGmzZnHpB-pJ-l-oZQ2LtRwSoZgeazPmWTKLaP5RJhEYVhGgoh5bYVbPl8H0HSrV41KWBj94rztSkw==".toCharArray(),
//                    "easyenergy",
//                    bucket
//                )
//            // hard coded ajat toistaiseksi
//            val fluxQuery = ("from(bucket: \"electricity_prices\")\n" +
//                    "  |> range(start: 2023-04-20T04:00:00Z, stop: 2023-04-21T04:00:00Z)\n" +
//                    "  |> filter(fn: (r) => r[\"_measurement\"] == \"my_measurement\")")
//
//
//            val results = influxDBClient.getQueryKotlinApi().queryRaw(fluxQuery, "easyenergy")
//            binding.textView5.text = results.toString()
//
//            Log.d("InfluxDB", results.toString())
//
//            influxDBClient.close()
//        }
//    }
}