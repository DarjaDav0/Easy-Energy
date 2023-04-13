package com.example.easyenergy

import android.util.Log
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.channels.consumeEach

//En vielä ole testannut, koska laiteongelmia
class Influxqueries {

    fun day(args: Array<String>) = runBlocking {

        val influxDBClient = InfluxDBClientKotlinFactory
            .create("http://localhost:8086", "LAQZ_p-mQbLi9PUAzqtIyknKjCZ6wQmYqhBSC2i2ZDJmKnIz-RYZbnkqHqOGrNOk5tHGgUL1suFiNF2TYCBLZg==".toCharArray(),
                "Easy Energy")
//Jos halutaan data valitulta päivältä, tarvitaan datepicker frontista, jotta tarvittava
// päivämäärä saadaan kyselyyn
        
        val fluxQuery = ("from(bucket: \"electricity_prices\")\n" +
                "  |> range((start: -1d)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"my_measurement\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"price\")\n" +
                "  |> aggregateWindow(every: v.windowPeriod, fn: mean, createEmpty: false)\n" +
                "  |> yield(name: \"mean\")")

        //Result is returned as a stream, might require some processing
        val results = influxDBClient.getQueryKotlinApi().query(fluxQuery)

        //print results
        results.consumeEach { println("Line: $it") }

        Log.d("test", "$results")

        influxDBClient.close()
    }

}