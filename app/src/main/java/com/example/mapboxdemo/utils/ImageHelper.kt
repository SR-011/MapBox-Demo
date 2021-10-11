package com.example.mapboxdemo.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

object ImageHelper {
    fun getFlagBitMap(flag: String): Bitmap? {
        val decodedString: ByteArray = Base64.decode(flag, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
}