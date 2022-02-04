package com.maverick.iotsocket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maverick.iotsocket.util.showToast

interface CommandOnClickListener {
    fun onClick(command: Command)
}

class CommandAdapter(private val commandList: List<Command>, private val onClickListener: CommandOnClickListener) :
    RecyclerView.Adapter<CommandAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commandName: TextView = view.findViewById(R.id.commandName)
        val commandContent: TextView = view.findViewById(R.id.commandContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.command_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val command = commandList[position]
            onClickListener.onClick(command)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val command = commandList[position]
        holder.commandContent.text = command.commandContent
        holder.commandName.text = command.commandName
    }

    override fun getItemCount() = commandList.size
}