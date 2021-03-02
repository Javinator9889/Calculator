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
import android.os.FileObserver
import androidx.lifecycle.LiveData
import com.javinator9889.calculator.containers.HISTORY_FILE
import com.javinator9889.calculator.containers.HistoryData
import com.javinator9889.calculator.libs.android.util.FileObserverProvider
import com.javinator9889.calculator.listeners.FileChangedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.*


/**
 * LiveData class that listens for changes in the history file, for notifying the
 * observers about that change.
 */
class HistoryLiveData(context: Context, private val scope: CoroutineScope) :
    LiveData<List<HistoryData>>(), FileChangedListener {
    private val file = File(context.cacheDir, HISTORY_FILE)
    private val observer = FileObserverProvider.getObserver(file, this, FileObserver.MODIFY)

    /**
     * {@inheritDoc}
     */
    override fun onActive() {
        observer.startWatching(); onFileChanged(file, 0)
    }

    /**
     * {@inheritDoc}
     */
    override fun onInactive() = observer.stopWatching()

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    override fun onFileChanged(file: File, mask: Int) {
        Timber.d("File has changed! - ${this.file == file} ; $mask")
        if (file == this.file && (mask == FileObserver.MODIFY || mask == 0) && file.exists()) {
            Timber.d("The file was updated")
            scope.launch(Dispatchers.IO) {
                ObjectInputStream(FileInputStream(file)).use {
                    try {
                        val historyData = it.readObject() as MutableList<HistoryData>
                        postValue(historyData)
                    } catch (_: EOFException) {
                    } catch (_: StreamCorruptedException) {
                    } catch (err: Throwable) {
                        Timber.w(err, "Error while recovering history data from file")
                    }
                }
            }
        }
    }
}