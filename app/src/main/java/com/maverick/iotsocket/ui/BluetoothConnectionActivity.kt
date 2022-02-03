package com.maverick.iotsocket.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.maverick.iotsocket.DeviceItemAdapter
import com.maverick.iotsocket.R
import com.maverick.iotsocket.util.showToast
import java.util.*

class BluetoothConnectionActivity: AppCompatActivity(), BluetoothService.OnBluetoothScanCallback,
BluetoothService.OnBluetoothEventCallback, DeviceItemAdapter.OnAdapterItemClickListener {
    val TAG = "BluetoothConnectionActivity"

    private var pgBar: ProgressBar? = null
    private var mMenu: Menu? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: DeviceItemAdapter? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mService: BluetoothService? = null
    private var mScanning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_connection)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        mRecyclerView = findViewById<View>(R.id.bluetoothListRecyclerView) as RecyclerView
        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.layoutManager = lm

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mAdapter = DeviceItemAdapter(this, mBluetoothAdapter!!.bondedDevices)
        mAdapter!!.setOnAdapterItemClickListener(this)
        mRecyclerView!!.adapter = mAdapter

        mService = BluetoothService.getDefaultInstance()

        mService!!.setOnScanCallback(this)
        mService!!.setOnEventCallback(this)

        pgBar = findViewById(R.id.bluetoothScanProgressBar)
    }

    override fun onResume() {
        super.onResume()
        mService!!.setOnEventCallback(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.bluetoothScan) {
            startStopScan()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun startStopScan() {
        if (!mScanning) {
            mService!!.startScan()
        } else {
            mService!!.stopScan()
        }
    }

    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
        if (
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        Log.d(
            TAG,
            "onDeviceDiscovered: ${device.name} - ${device.address} - " +
                    Arrays.toString(device.uuids)
        )
        val dv = BluetoothDeviceDecorator(device, rssi)
        val index = mAdapter!!.devices.indexOf(dv)
        if (index < 0) {
            mAdapter!!.devices.plus(dv)
            mAdapter!!.notifyItemInserted(mAdapter!!.devices.size - 1)
        } else {
            mAdapter!!.devices[index].device = device
            mAdapter!!.devices[index].rssi = rssi
            mAdapter!!.notifyItemChanged(index)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bluetooth_connection_menu, menu)
        mMenu = menu
        return true
    }

    override fun onStartScan() {
        Log.d(TAG, "onStartScan")
        mScanning = true
        pgBar!!.visibility = View.VISIBLE
        mMenu!!.findItem(R.id.bluetoothScan).title = "Stop"
    }

    override fun onStopScan() {
        Log.d(TAG, "onStopScan")
        mScanning = false
        pgBar!!.visibility = View.GONE
        mMenu!!.findItem(R.id.bluetoothScan).title = "Start"
    }

    override fun onDataRead(buffer: ByteArray, length: Int) {
        Log.d(TAG, "onDataRead${String(buffer)}")
    }

    override fun onStatusChange(status: BluetoothStatus) {
        Log.d(TAG, "onStatusChange: $status")
        Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show()

        if (status == BluetoothStatus.CONNECTED) {
            "Connected".showToast(this)
            finish()
        } else {
            "Something wrong".showToast(this)
        }
    }

    override fun onDeviceName(deviceName: String) {
        Log.d(TAG, "onDeviceName: $deviceName")
    }

    override fun onToast(message: String) {
        Log.d(TAG, "onToast:$message")
    }

    override fun onDataWrite(buffer: ByteArray) {
        Log.d(TAG, "onDataWrite:${String(buffer)}")
    }

    override fun onItemClick(device: BluetoothDeviceDecorator?, position: Int) {
        if (device != null) {
            mService!!.connect(device.device)
        }
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.i(TAG, "bt req $result")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                //granted
            } else {
                //deny
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }
}