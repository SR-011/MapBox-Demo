package com.example.mapboxdemo.data.api

import com.example.mapboxdemo.data.model.CurrentWeatherInfo
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherService {
    @GET("current.json")
    suspend fun getWeatherInfo(
        @Query("q") query: String?,
        @Query("key") apiKey: String?
    ): CurrentWeatherInfo
}