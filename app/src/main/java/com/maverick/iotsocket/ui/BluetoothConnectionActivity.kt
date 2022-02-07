package com.maverick.iotsocket.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.maverick.iotsocket.DeviceItemAdapter
import com.maverick.iotsocket.R
import com.maverick.iotsocket.connection.ConnectionManager
import com.maverick.iotsocket.util.showToast
import java.util.*


class BluetoothConnectionActivity : AppCompatActivity(), BluetoothService.OnBluetoothScanCallback,
    BluetoothService.OnBluetoothEventCallback, DeviceItemAdapter.OnAdapterItemClickListener {
    val TAG = BluetoothConnectionActivity::class.simpleName

    private val REQUEST_BT_CONNECT = 1
    private val REQUEST_BT_SCAN = 2
    private val REQUEST_ENABLE_BT = 3
    private var pgBar: ProgressBar? = null
    private var mMenu: Menu? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: DeviceItemAdapter? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mService: BluetoothService? = null
    private var mScanning: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_connection)

        mRecyclerView = findViewById<View>(R.id.bluetoothListRecyclerView) as RecyclerView
        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.layoutManager = lm
        pgBar = findViewById(R.id.bluetoothScanProgressBar)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mService = BluetoothService.getDefaultInstance()
            mService?.setOnScanCallback(this)
            mService?.setOnEventCallback(this)

            setupAdapter()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ), REQUEST_BT_CONNECT
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun setupAdapter() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        if (mBluetoothAdapter?.isEnabled != true) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            mAdapter = DeviceItemAdapter(this, mBluetoothAdapter!!.bondedDevices)
            mAdapter!!.setOnAdapterItemClickListener(this)
            mRecyclerView!!.adapter = mAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        mService?.setOnEventCallback(this)
    }

    override fun onPause() {
        super.onPause()
        mService?.setOnEventCallback(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> when (resultCode) {
                RESULT_OK -> setupAdapter()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BT_CONNECT -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mService = BluetoothService.getDefaultInstance()
                    mService?.setOnScanCallback(this)
                    mService?.setOnEventCallback(this)

                    setupAdapter()
                }
            }
            REQUEST_BT_SCAN -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startStopScan()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.bluetoothScan) {
            Log.i(TAG, "onOptionsItemSelected: ")
            startStopScan()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startStopScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_BT_SCAN)
        } else {
            if (!mScanning) {
                mService?.startScan()
            } else {
                mService?.stopScan()
            }
        }
    }

    override fun onDeviceDiscovered(device: BluetoothDevice, rssi: Int) {
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
    }

    override fun onDeviceName(deviceName: String) {
        Log.d(TAG, "onDeviceName: $deviceName")
    }

    override fun onToast(message: String) {
        Log.w(TAG, "onToast:$message")
    }

    override fun onDataWrite(buffer: ByteArray) {
        Log.d(TAG, "onDataWrite:${String(buffer)}")
    }

    override fun onItemClick(device: BluetoothDeviceDecorator?, position: Int) {
        if (device != null) {
            // TODO Store mac address rather than the device itself
            ConnectionManager.changeBluetoothDevice(device.device)
            Log.i(TAG, "new bluetooth connection ${device.name}")
            "\"${device.name}\" ${getString(R.string.text_added)}".showToast(this)
            finish()
        }
    }
}