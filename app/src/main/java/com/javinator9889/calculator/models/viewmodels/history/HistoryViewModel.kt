/*
 * Copyright © 2020 - present | Calculator by Javinator9889
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 * Created by Javinator9889 on 12/07/20 - Calculator.
 */
package com.javinator9889.calculator.models.viewmodels.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.javinator9889.calculator.containers.HistoryData
import com.javinator9889.calculator.models.ButtonActionList
import com.javinator9889.calculator.models.data.HistoryLiveData
import com.javinator9889.calculator.views.activities.HISTORY_FILE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val context = app.applicationContext
    val historyData = HistoryLiveData(app.applicationContext, viewModelScope)

    fun insertNewOperation(operation: ButtonActionList) = viewModelScope.launch(Dispatchers.IO) {
        val file = File(context.cacheDir, HISTORY_FILE)
        val writtenValues = mutableListOf<HistoryData>()
        if (file.exists()) {
            ObjectInputStream(FileInputStream(file)).use {
                val savedValues = it.readObject() as MutableList<HistoryData>
                writtenValues.addAll(savedValues)
            }
        }
        val now = Calendar.getInstance().time
        writtenValues.add(HistoryData(operation, now))
        ObjectOutputStream(FileOutputStream(file)).use {
            it.writeObject(writtenValues)
            try {
                it.writeFields()
            } catch (_: NotActiveException) {
            }
        }
    }
}