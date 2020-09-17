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

package com.example.android.trackmysleepquality.sleepquality

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

class SleepQualityViewModel(val database: SleepDatabaseDao, application: Application) : AndroidViewModel(application) {

    var viewModelJob = Job()
    var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob )

    var _shoudNavigate = MutableLiveData<Boolean>()
    val shouldNavigate : LiveData<Boolean>
    get() {
        return _shoudNavigate
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    init {
        _shoudNavigate.value = false
    }


    fun onSleepQualitySelection(quality: Int) {
        uiScope.launch {
            updateSleep(quality)
            _shoudNavigate.value = true
        }
    }

    private suspend fun updateSleep(quality: Int) {
        withContext(Dispatchers.IO) {
            var tonight = database.getTonight()
            tonight!!.sleepQuality = quality
            database.update(tonight!!)
        }
    }

    fun NavigationComplted() {
        _shoudNavigate.value = false
    }
}