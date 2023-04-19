package com.example.easyenergy.fragments

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

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    //private val recyclerView = binding.dayPriceList
    //private val adapter = ElectricityPriceAdapter(viewModel.dayDataList)

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

        //asettaa main fragmentin textviewin viewmodelin muuttujan mukaan
        binding.currentHourPriceText.text = viewModel.currentHourPrice


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
            viewModel.getAllData(this.requireContext())
        }




        //recyclerView.adapter = adapter
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}