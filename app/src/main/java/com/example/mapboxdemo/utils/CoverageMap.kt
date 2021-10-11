package com.example.mapboxdemo.utils

enum class CoverageMap(val layerName: String, val index: Int) {
    NONE("None", 0),
    FLEXXEC("FlexExec", 1),
    VIASAT_KU("ViaSat Ku", 2)
}