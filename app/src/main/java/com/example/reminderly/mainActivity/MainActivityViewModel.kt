package com.example.reminderly.mainActivity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {

      var navigateToCalendarFragment: MutableLiveData<Boolean> = MutableLiveData(false)

    fun navigateToCalendarFragment() {
        navigateToCalendarFragment.value=true
    }

    fun doneNavigateToCalendarFragment() {
        navigateToCalendarFragment.value=false
    }

}