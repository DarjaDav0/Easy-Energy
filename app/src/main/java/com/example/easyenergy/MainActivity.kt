package com.example.easyenergy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.easyenergy.databinding.ActivityMainBinding
//import androidx.navigation.ui.AppBarConfiguration

class MainActivity : AppCompatActivity() {

    //TODO: vaatii muutaman gradle lisäyksen, lisäksi vaaditaan navigation
    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}