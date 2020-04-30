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
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.reminderly.Utils.AD_CLICK_PER_SESSION
import com.example.reminderly.Utils.MyUtils
import com.example.reminderly.database.Reminder
import com.example.reminderly.databinding.HeaderItemBinding
import com.example.reminderly.databinding.NativeAdBinding
import com.example.reminderly.databinding.ReminderItemBinding
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest


class ReminderAdapter(
    private val context: Context,
    private val clickListener: ReminderClickListener
) :
    ListAdapter<Reminder, RecyclerView.ViewHolder>(DiffCallbackReminders()) {


    companion object {
        private const val TYPE_REMINDER = 0
        private const val TYPE_HEADER = 1
        private const val TYPE_AD = 2
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {


        return when (viewType) {
            TYPE_REMINDER -> {
                ReminderViewHolder.from(parent)
            }
            TYPE_HEADER -> {
                HeaderViewHolder.from(parent)
            }
            TYPE_AD -> {
                AdViewHolder.from(parent)
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
            is AdViewHolder -> {
                holder.bind(context)
            }
            else -> throw IllegalArgumentException("Invalid ViewHolder type")
        }
    }

    override fun getItemViewType(position: Int): Int {

        val currentReminder = getItem(position)

        return when (currentReminder.header) {
            0->TYPE_REMINDER
                1->TYPE_HEADER
                2->TYPE_HEADER
                3->TYPE_HEADER
                4->TYPE_AD
            else -> throw Exception("unknown itemView type")
        }


    }


    //----------------ReminderViewHolder------------
    class ReminderViewHolder private constructor(val binding: ReminderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ReminderClickListener, item: Reminder) {
            binding.reminder = item
            binding.clickListener=clickListener
            binding.adapterPosition=adapterPosition
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


    //----------------AdViewHolder------------
    class AdViewHolder private constructor(val binding: NativeAdBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(context: Context) {
            //download ad for each ad item in recycler
            passAdToTemplate(binding,context)
        }


        private fun passAdToTemplate(binding: NativeAdBinding,context: Context) {
            val adLoader: AdLoader = AdLoader.Builder(
                context,
                "ca-app-pub-3940256099942544/2247696110"
            ) //todo replace with real native ad id from keys.xml
                .forUnifiedNativeAd { unifiedNativeAd ->
                    binding.loadingGroup.visibility=View.GONE //hide loading layout
                    binding.smallNativeAdTemplate.visibility= View.VISIBLE//show ad layout since load was successful
                    val styles =
                        NativeTemplateStyle.Builder().withMainBackgroundColor(ColorDrawable(0xfff))
                            .build()
                    binding.smallNativeAdTemplate.setStyles(styles)
                    binding.smallNativeAdTemplate.setNativeAd(unifiedNativeAd)
                }
                .withAdListener(object : AdListener(){
                    override fun onAdFailedToLoad(errorCode: Int) {
                        binding.smallNativeAdTemplate.visibility= View.GONE//hide ad layout since load failed
                        binding.loadingGroup.visibility=View.GONE //hide loading layout
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        //add to ad click counter so we can block user if he clicks multiple ads
                        var adClicks = MyUtils.getInt(context, AD_CLICK_PER_SESSION)
                        adClicks++
                        MyUtils.putInt(context, AD_CLICK_PER_SESSION,adClicks)

                    }
                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }


        companion object {
            fun from(parent: ViewGroup): AdViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = NativeAdBinding.inflate(layoutInflater, parent, false)

                return AdViewHolder(binding)
            }



        }

    }




}


interface ReminderClickListener {
    fun onReminderClick(reminder: Reminder)
    fun onFavoriteClick(reminder: Reminder,position: Int)
    fun onMenuClick(reminder: Reminder,position: Int)
}


class DiffCallbackReminders : DiffUtil.ItemCallback<Reminder>() {
    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem == newItem
    }
}







