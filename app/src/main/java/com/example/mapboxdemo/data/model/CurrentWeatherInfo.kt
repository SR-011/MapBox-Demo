package com.example.mapboxdemo.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class CurrentWeatherInfo(
    @SerializedName("location")
    @Expose
    val location: Location,
    @SerializedName("current")
    @Expose
    val current: Current
)

data class Location(
    @SerializedName("name")
    @Expose
    val name: String,
    @SerializedName("region")
    @Expose
    val region: String,
    @SerializedName("country")
    @Expose
    val country: String,
    @SerializedName("lat")
    @Expose
    val lat: Double,
    @SerializedName("lon")
    @Expose
    val lon: Double,
    @SerializedName("tz_id")
    @Expose
    val tzId: String,
    @SerializedName("localtime_epoch")
    @Expose
    val localtimeEpoch: Int,
    @SerializedName("localtime")
    @Expose
    val localtime: String
)

data class Current(
    @SerializedName("last_updated_epoch")
    @Expose
    val lastUpdatedEpoch: Int,
    @SerializedName("last_updated")
    @Expose
    val lastUpdated: String,
    @SerializedName("temp_c")
    @Expose
    val tempC: Double,
    @SerializedName("temp_f")
    @Expose
    val tempF: Double,
    @SerializedName("is_day")
    @Expose
    val isDay: Int,
    @SerializedName("condition")
    @Expose
    val condition: Condition,
    @SerializedName("wind_mph")
    @Expose
    val windMph: Double,
    @SerializedName("wind_kph")
    @Expose
    val windKph: Double,
    @SerializedName("wind_degree")
    @Expose
    val windDegree: Int,
    @SerializedName("wind_dir")
    @Expose
    val windDir: String,
    @SerializedName("pressure_mb")
    @Expose
    val pressureMb: Double,
    @SerializedName("pressure_in")
    @Expose
    val pressureIn: Double,
    @SerializedName("precip_mm")
    @Expose
    val preCipMm: Double,
    @SerializedName("precip_in")
    @Expose
    val preCipIn: Double,
    @SerializedName("humidity")
    @Expose
    val humidity: Int,
    @SerializedName("cloud")
    @Expose
    val cloud: Int,
    @SerializedName("feelslike_c")
    @Expose
    val feelsLikeC: Double,
    @SerializedName("feelslike_f")
    @Expose
    val feelsLikeF: Double,
    @SerializedName("vis_km")
    @Expose
    val visKm: Double,
    @SerializedName("vis_miles")
    @Expose
    val visMiles: Double,
    @SerializedName("uv")
    @Expose
    val uv: Double,
    @SerializedName("gust_mph")
    @Expose
    val gustMph: Double,
    @SerializedName("gust_kph")
    @Expose
    val gustKph: Double
)

data class Condition(
    @SerializedName("text")
    @Expose
    val text: String,
    @SerializedName("icon")
    @Expose
    val icon: String,
    @SerializedName("code")
    @Expose
    val code: Int
)
