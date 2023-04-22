package com.example.easyenergy.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.easyenergy.ElectricityPriceAdapter
import com.example.easyenergy.databinding.FragmentMainBinding
import com.example.easyenergy.datatypes.ElectricityPrice
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory

class MainFragment : Fragment() {
    private val bucket = "electricity_prices"
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        viewModel.getThisHourData(this.requireContext())

        viewModel.getDayData(this.requireContext())





        createCharts()



        //daily
        binding.dayButton.setOnClickListener()
        {
            viewModel.createDayChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
        }

        //monthly
        binding.monthButton.setOnClickListener()
        {
            viewModel.createWeekChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
        }

        //year
        binding.yearButton.setOnClickListener()
        {
            viewModel.createMonthChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
            viewModel.createSpinner(binding.spinnerDropdown, this.requireContext())
            binding.spinnerDropdown.visibility = View.VISIBLE
        }

        //testaukseen, saa poistaa lopullisesta versiosta
        binding.buttonForTesting.setOnClickListener()
        {
            //viewModel.getAllData(this.requireContext())
            //viewModel.getDataFromInflux(this.requireContext())
            getDataFromInflux()
            viewModel.getDayData(this.requireContext())
            viewModel.createDayChart()
        }




        val recyclerView = binding.dayPriceList
        val adapter = ElectricityPriceAdapter(viewModel.dayClassList)
        recyclerView.adapter = adapter
        return binding.root
    }

    fun createCharts()
    {
        //käyttää ensin viewmodelissa sijaitsevaa funktiota jonka jälkeen asettaa kuvaajaan viewmodelin muuttujan kautta
        viewModel.createDayChart()
        binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)

        //asettaa main fragmentin textviewin viewmodelin muuttujan mukaan
        binding.currentHourPriceText.text = viewModel.currentHourPrice

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // testinappia varten täällä tämä funktio
    fun getDataFromInflux(){

        val influxDBClient = InfluxDBClientKotlinFactory
            .create("http://localhost:8086", "wRvSgV9igGmzZnHpB-pJ-l-oZQ2LtRwSoZgeazPmWTKLaP5RJhEYVhGgoh5bYVbPl8H0HSrV41KWBj94rztSkw==".toCharArray(), "easyenergy", bucket)
        // hard coded ajat toistaiseksi
        val fluxQuery = ("from(bucket: \"electricity_prices\")\n" +
                "  |> range(start: 2023-04-20T04:00:00Z, stop: 2023-04-21T04:00:00Z)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"my_measurement\")")


        val results = influxDBClient.getQueryKotlinApi().queryRaw(fluxQuery, "easyenergy")
        binding.textView5.text = results.toString()

        Log.d("InfluxDB", results.toString())

        influxDBClient.close()
    }
}