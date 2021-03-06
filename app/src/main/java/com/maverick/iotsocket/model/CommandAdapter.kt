package com.maverick.iotsocket.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maverick.iotsocket.R

class CommandAdapter(
    private val commandList: List<Command>,
    private val onClickListener: CommandOnClickListener
) :
    RecyclerView.Adapter<CommandAdapter.ViewHolder>() {
    interface CommandOnClickListener {
        fun onClick(command: Command)
        fun onLongClick(command: Command)
    }

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
        viewHolder.itemView.setOnLongClickListener {
            val position = viewHolder.adapterPosition
            val command = commandList[position]
            onClickListener.onLongClick(command)
            return@setOnLongClickListener true
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val command = commandList[position]
        holder.commandContent.text = command.content
        holder.commandName.text = command.name
    }

    override fun getItemCount() = commandList.size
}