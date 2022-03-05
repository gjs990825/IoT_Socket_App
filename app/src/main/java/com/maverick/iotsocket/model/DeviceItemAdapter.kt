package com.maverick.iotsocket.model

/*
* MIT License
*
* Copyright (c) 2015 Douglas Nassif Roma Junior
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator
import com.maverick.iotsocket.MyApplication.Companion.context
import com.maverick.iotsocket.R

/**
 * Created by douglas on 10/04/2017.
 */
class DeviceItemAdapter(mContext: Context, devices: List<BluetoothDevice>) :
    RecyclerView.Adapter<DeviceItemAdapter.ViewHolder>() {
    var devices: List<BluetoothDeviceDecorator> = decorateDevices(devices)
    private val mInflater: LayoutInflater =
        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mOnItemClickListener: OnAdapterItemClickListener? =
        null

    constructor(context: Context, devices: Set<BluetoothDevice>?) : this(
        context,
        ArrayList<BluetoothDevice>(devices?.toMutableList() ?: setOf())
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            mInflater.inflate(R.layout.device_item, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val device: BluetoothDeviceDecorator = devices[position]
        holder.tvName.text = if (TextUtils.isEmpty(device.name)) "---" else device.name
        holder.tvAddress.text = device.address
        holder.tvRSSI.text = if (device.rssi != 0) {
            device.rssi.toString()
        } else {
            context.getString(R.string.text_device_paired)
        }
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(
                device,
                position
            )
        }
    }

    override fun getItemCount(): Int = devices.size

//    val devices: List<BluetoothDeviceDecorator>
//        get() = mDevices

    fun setOnAdapterItemClickListener(onItemClickListener: OnAdapterItemClickListener?) {
        mOnItemClickListener = onItemClickListener
    }

    interface OnAdapterItemClickListener {
        fun onItemClick(device: BluetoothDeviceDecorator?, position: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById<View>(R.id.tv_name) as TextView
        val tvAddress: TextView = itemView.findViewById<View>(R.id.tv_address) as TextView
        val tvRSSI: TextView = itemView.findViewById<View>(R.id.tv_rssi) as TextView
    }

    companion object {
        fun decorateDevices(btDevices: Collection<BluetoothDevice>): List<BluetoothDeviceDecorator> {
            val devices: MutableList<BluetoothDeviceDecorator> = ArrayList()
            for (dev in btDevices) {
                devices.add(BluetoothDeviceDecorator(dev, 0))
            }
            return devices
        }
    }
}