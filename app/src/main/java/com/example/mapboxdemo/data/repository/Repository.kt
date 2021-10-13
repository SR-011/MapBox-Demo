package com.example.mapboxdemo.data.repository

import com.example.mapboxdemo.data.api.RetrofitBuilder

class Repository {

    suspend fun getWeatherInfo(place:String) = RetrofitBuilder.weatherService.getWeatherInfo(place, "Api Key")
}