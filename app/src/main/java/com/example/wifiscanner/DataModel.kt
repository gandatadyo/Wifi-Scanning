package com.example.wifiscanner

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Model_WifiStation(
    val ssid:String?,
    val bssid:String?,
    val frequency: Int?,
    val level: Int?
): Parcelable
