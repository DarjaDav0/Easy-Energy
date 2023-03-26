package com.example.easyenergy.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.easyenergy.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // Inflate the layout for this fragment
        //TODO: ei osaa oikein inflatea fragmenttia
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        val root: View = binding.root

        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}