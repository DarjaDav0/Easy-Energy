package com.example.easyenergy.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.easyenergy.databinding.FragmentMainBinding

class MainFragment : Fragment() {

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

        //käyttää ensin viewmodelissa sijaitsevaa funktiota jonka jälkeen asettaa kuvaajaan viewmodelin muuttujan kautta
        viewModel.createDayChart()
        binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)

        binding.dayButton.setOnClickListener()
        {
            viewModel.createDayChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
        }

        binding.weekButton.setOnClickListener()
        {
            viewModel.createWeekChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
        }

        binding.monthButton.setOnClickListener()
        {
            viewModel.createMonthChart()
            binding.aaChartView.aa_drawChartWithChartModel(viewModel.getChart)
        }

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun getElectrictyData() {
        val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices"

        // Request a string response from the provided URL.
        val stringRequest: StringRequest = object : StringRequest(
            Request.Method.GET, JSON_URL,
            Response.Listener { response ->
                // print the response as a whole
                // we can use GSON to modify this response into something more usable
                Log.d("ADVTECH", response)

            },
            Response.ErrorListener {
                // typically this is a connection error
                Log.d("ADVTECH", it.toString())
            })
        {
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