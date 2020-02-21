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
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reminderly.R
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.ReminderItemBinding
import com.example.reminderly.databinding.ReminderWithHeaderItemBinding
import java.util.*


class ReminderAdapter(
    private val context: Context,
    private val clickListener: ReminderClickListener
) :
    ListAdapter<Reminder, RecyclerView.ViewHolder>(DiffCallbackReminders()) {


    companion object {
        private const val TYPE_REMINDER = 0
        private const val TYPE_REMINDER_WITH_HEADER = 1


        private var overdueHeader = false
        private var todayHeader = false
        private var upcomingHeader = false
        private var headerText = "init"

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        return when (viewType) {
            TYPE_REMINDER -> {
                ReminderViewHolder.from(parent)
            }
            TYPE_REMINDER_WITH_HEADER -> {
                ReminderWithHeaderViewHolder.from(parent)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ReminderViewHolder -> {
                holder.bind(clickListener, getItem(position))
            }
            is ReminderWithHeaderViewHolder -> {
                holder.bind(clickListener, getItem(position))
            }
            else -> throw IllegalArgumentException("Invalid ViewHolder type")
        }
    }

    override fun getItemViewType(position: Int): Int {

        val currentReminder = getItem(position)

        when {
            DateUtils.isToday(currentReminder.createdAt.timeInMillis) -> {
                //current calendar is today
                return when (todayHeader) {
                    //add header on top of first reminder of today reminders only so it work as separator
                    false ->{
                        todayHeader=true
                        headerText=context.getString(R.string.today)
                        TYPE_REMINDER_WITH_HEADER
                    }
                    true -> TYPE_REMINDER
                }
            }
            Calendar.getInstance().timeInMillis > currentReminder.createdAt.timeInMillis -> {
                //calendar is older that today
                return when (overdueHeader) {
                    false ->{
                        overdueHeader=true
                        headerText=context.getString(R.string.overdue)
                        TYPE_REMINDER_WITH_HEADER
                    }
                    true -> TYPE_REMINDER
                }
            }
            else -> {
                //calendar is in the future
                return when (upcomingHeader) {
                    false ->{
                        upcomingHeader=true
                        headerText=context.getString(R.string.upcoming)
                        TYPE_REMINDER_WITH_HEADER
                    }
                    true -> TYPE_REMINDER
                }
            }
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


    //----------------ReminderWithHeaderViewHolder------------
    class ReminderWithHeaderViewHolder private constructor(val binding: ReminderWithHeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ReminderClickListener, item: Reminder) {
            binding.reminderItem.reminder = item
            binding.reminderItem.clickListener=clickListener
            binding.headerText.text= headerText
        }

        companion object {
            fun from(parent: ViewGroup): ReminderWithHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ReminderWithHeaderItemBinding.inflate(layoutInflater, parent, false)

                return ReminderWithHeaderViewHolder(binding)
            }
        }

    }

}


interface ReminderClickListener {
    fun onReminderClick(reminder: Reminder)
}


class DiffCallbackReminders : DiffUtil.ItemCallback<Reminder>() {
    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem == newItem
    }
}







