package com.example.wifiscanner

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item_wifi.view.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION = 120
    private var wifiReceiverRegistered: Boolean = false

    private var dataWifi =  arrayListOf<Model_WifiStation>()
    private val adapterWifi = AdapterWifi(dataWifi) { partItem : Model_WifiStation -> ChooseWifi(partItem) }

    private val wifiManager: WifiManager get() = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val results = wifiManager.scanResults
            `dataWifi`.clear()
            if (results != null) {
                for (i in 0 until results.size) {
                    dataWifi.add(
                        Model_WifiStation(results[i].SSID,results[i].BSSID,results[i].frequency,results[i].level)
                    )
                }
            }

            adapterWifi.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_wifi.setHasFixedSize(true)
        recycler_wifi.layoutManager = LinearLayoutManager(this)
        recycler_wifi.adapter = adapterWifi

        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, R.string.prompt_enabling_wifi, Toast.LENGTH_SHORT).show()
            wifiManager.isWifiEnabled = true
        }

        button.setOnClickListener {
            wifiManager.startScan()
        }
    }

    override fun onStart() {
        super.onStart()
        startScanning()
    }

    private fun startScanning() {
        if (checkPermissions()) {
            registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            wifiReceiverRegistered = true
        }
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION
            )
            false
        } else {
            true
        }
    }

    private fun stopScanning() {
        if (wifiReceiverRegistered) {
            unregisterReceiver(wifiReceiver)
            wifiReceiverRegistered = false
        }
    }

    override fun onStop() {
        super.onStop()
        stopScanning()
    }

    private fun ChooseWifi(data:Model_WifiStation){
    }

    class AdapterWifi(private val listData: ArrayList<Model_WifiStation>, val clickListener: (Model_WifiStation) -> Unit) : RecyclerView.Adapter<AdapterWifi.CardViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CardViewHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item_wifi, viewGroup, false)
            return CardViewHolder(view)
        }

        override fun getItemCount(): Int = listData.size

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            holder.bind(listData[position])
        }

        inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(item: Model_WifiStation) {
                val txt_ssid: TextView = itemView.findViewById(R.id.txt_ssid)
                val txt_bssid: TextView = itemView.findViewById(R.id.txt_bssid)
                val txt_frequency: TextView = itemView.findViewById(R.id.txt_frequency)
                val txt_level: TextView = itemView.findViewById(R.id.txt_level)

                txt_ssid.text = item.ssid
                txt_bssid.text = item.bssid
                txt_frequency.text = itemView.context.getString(R.string.station_frequency, item.frequency.toString())
                txt_level.text = itemView.context.getString(R.string.station_level, item.level.toString())
            }
        }
    }

}
