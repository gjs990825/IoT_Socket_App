package com.maverick.iotsocket

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CommandAdapter(activity: Activity, private val resourceId: Int, data: List<Command>) :
    ArrayAdapter<Command>(activity, resourceId, data) {

    inner class ViewHolder(val commandName: TextView, val commandContent: TextView)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(resourceId, parent, false)
            viewHolder = ViewHolder(
                view.findViewById(R.id.commandName),
                view.findViewById(R.id.commandContent)
            )
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val command = getItem(position)
        command?.let {
            viewHolder.commandName.text = command.commandName
            viewHolder.commandContent.text = command.commandContent
        }
        return view
    }
}