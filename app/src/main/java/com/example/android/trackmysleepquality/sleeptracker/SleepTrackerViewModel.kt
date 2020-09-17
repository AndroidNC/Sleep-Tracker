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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _tonight = MutableLiveData<SleepNight?>()
    val tonight : LiveData<SleepNight?>
    get() {
        return _tonight
    }

    private val nights = database.getAllNights()

    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    private var _shouldNavigate = MutableLiveData<Boolean>()

    val shouldNavigate : LiveData<Boolean>
        get() {
            return _shouldNavigate
        }

    init {
        _shouldNavigate.value = false
        initializeTonight()
    }

    private fun initializeTonight() {
        uiScope.launch {
            _tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {

        return withContext(Dispatchers.IO) {

               var night = database.getTonight()

               if(night?.endTimeMilli != night?.startTimeMilli) {
                   night = null
               }

               night
           }
    }

    fun onStartTracking() {
        uiScope.launch {
            val newNight = SleepNight()
            insert(newNight)

            _tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(newNight: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(newNight)
        }
    }

    fun onStopTracking() {
        uiScope.launch {
            val oldNight = _tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
            _shouldNavigate.value = true
        }
    }

    private suspend fun update(newNight: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(newNight)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
           _tonight.value = null
        }
    }

    private suspend fun clear() {

        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    fun navigationFinished() {
        _shouldNavigate.value = false
    }

}

