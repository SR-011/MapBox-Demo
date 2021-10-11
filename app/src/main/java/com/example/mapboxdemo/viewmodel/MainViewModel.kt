package com.example.mapboxdemo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapboxdemo.data.model.CurrentWeatherInfo
import com.example.mapboxdemo.data.repository.Repository
import com.google.gson.Gson
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val repository = Repository()
    private val departureWeatherInfo = MutableLiveData<CurrentWeatherInfo>()
    val _departureWeatherInfo: LiveData<CurrentWeatherInfo> = departureWeatherInfo
    private val destinationWeatherInfo = MutableLiveData<CurrentWeatherInfo>()
    val _destinationWeatherInfo: LiveData<CurrentWeatherInfo> = destinationWeatherInfo

    fun getDepartureWeatherInfo(){

        viewModelScope.launch {
            repository.getWeatherInfo("Dhaka").apply {
                departureWeatherInfo.value = this
                Log.d("TAG", "getWeatherInfo: ${Gson().toJson(this)}")
            }
        }
    }

    fun getDestinationWeatherInfo(){

        viewModelScope.launch {
            repository.getWeatherInfo("22.247928,91.814863").apply {
                destinationWeatherInfo.value = this
                Log.d("TAG", "getWeatherInfo: ${Gson().toJson(this)}")
            }
        }
    }
}