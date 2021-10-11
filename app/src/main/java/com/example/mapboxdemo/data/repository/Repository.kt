package com.example.mapboxdemo.data.repository

import com.example.mapboxdemo.data.api.RetrofitBuilder

class Repository {

    suspend fun getWeatherInfo(place:String) = RetrofitBuilder.weatherService.getWeatherInfo(place, "e724aa55752f45f4a9084258210410")
}