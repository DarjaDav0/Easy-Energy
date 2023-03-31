package com.example.easyenergy.fragments

import androidx.lifecycle.ViewModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement

class MainViewModel : ViewModel(){
    private val hoursList = mutableListOf<Double>()


    private lateinit var testChart : AAChartModel
    val getChart get() = testChart



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
}