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
package com.javinator9889.calculator.models.data

import android.content.Context
import android.os.FileObserver.CLOSE_WRITE
import androidx.lifecycle.LiveData
import com.javinator9889.calculator.containers.HistoryData
import com.javinator9889.calculator.libs.android.util.FileObserverProvider
import com.javinator9889.calculator.listeners.OnFileChangedListener
import com.javinator9889.calculator.views.activities.HISTORY_FILE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.*

class HistoryLiveData(context: Context, private val scope: CoroutineScope) :
    LiveData<List<HistoryData>>(), OnFileChangedListener {
    private val file = File(context.cacheDir, HISTORY_FILE)
    private val observer = FileObserverProvider.getObserver(file, this)

    override fun onActive() = observer.startWatching()

    override fun onInactive() = observer.stopWatching()

    override fun onFileChanged(file: File, mask: Int) {
        Timber.d("File has changed! - ${this.file == file} ; ${mask == CLOSE_WRITE}")
        if (file == this.file && mask == CLOSE_WRITE) {
            Timber.d("The file was updated")
            scope.launch(Dispatchers.IO) {
                val historyData = mutableListOf<HistoryData>()
                ObjectInputStream(FileInputStream(file)).use {
                    try {
                        while (true) {
                            val item = it.readObject() as HistoryData
                            historyData.add(item)
                        }
                    } catch (_: EOFException) {
                    } catch (_: StreamCorruptedException) {
                    }
                }
                postValue(historyData)
            }
        }
    }
}