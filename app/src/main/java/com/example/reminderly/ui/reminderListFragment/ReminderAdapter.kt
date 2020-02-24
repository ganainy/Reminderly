/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ourchat.ui.chat


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.HeaderItemBinding
import com.example.reminderly.databinding.ReminderItemBinding


class ReminderAdapter(
    private val context: Context,
    private val clickListener: ReminderClickListener
) :
    ListAdapter<Reminder, RecyclerView.ViewHolder>(DiffCallbackReminders()) {


    companion object {
        private const val TYPE_REMINDER = 0
        private const val TYPE_HEADER = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        return when (viewType) {
            TYPE_REMINDER -> {
                ReminderViewHolder.from(parent)
            }
            TYPE_HEADER -> {
                HeaderViewHolder.from(parent)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ReminderViewHolder -> {
                holder.bind(clickListener, getItem(position))
            }
            is HeaderViewHolder -> {
                holder.bind(clickListener, getItem(position))
            }
            else -> throw IllegalArgumentException("Invalid ViewHolder type")
        }
    }

    override fun getItemViewType(position: Int): Int {

        val currentReminder = getItem(position)

        return when (currentReminder.header) {
            0->TYPE_REMINDER
                else->TYPE_HEADER
        }


    }


    //----------------ReminderViewHolder------------
    class ReminderViewHolder private constructor(val binding: ReminderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ReminderClickListener, item: Reminder) {
            binding.reminder = item
            binding.clickListener=clickListener
        }

        companion object {
            fun from(parent: ViewGroup): ReminderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ReminderItemBinding.inflate(layoutInflater, parent, false)

                return ReminderViewHolder(binding)
            }
        }

    }


    //----------------HeaderViewHolder------------
    class HeaderViewHolder private constructor(val binding: HeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ReminderClickListener, item: Reminder) {
            binding.header=item.header
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HeaderItemBinding.inflate(layoutInflater, parent, false)

                return HeaderViewHolder(binding)
            }
        }

    }

}


interface ReminderClickListener {
    fun onReminderClick(reminder: Reminder)
    fun onFavoriteClick(reminder: Reminder)
    fun onMenuClick(reminder: Reminder)
}


class DiffCallbackReminders : DiffUtil.ItemCallback<Reminder>() {
    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem == newItem
    }
}







