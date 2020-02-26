package com.example.reminderly

import com.example.reminderly.database.Reminder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class ReminderPublishSubject {

    companion object{
        private val subject: PublishSubject<MutableList<Reminder>> = PublishSubject.create()
        fun setRemindersList(reminderList: MutableList<Reminder>) {
            subject.onNext(reminderList)
        }

        val reminderList: Observable<MutableList<Reminder>>
            get() = subject
    }
    }
