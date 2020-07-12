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
package com.javinator9889.calculator.libs.android.util

import android.os.Build
import android.os.FileObserver
import com.javinator9889.calculator.listeners.FileChangedListener
import java.io.File


/**
 * Since Android Q, the FileObserver constructor changed and deprecated the other constructors
 * not based on File {@link java.io.File}. This provider allows APIs below Android Q use the
 * old constructor (@link android.os.FileObserver#FileObserver(String, int)} and the newer ones
 * the constructor based on {@link java.io.File}.
 */
object FileObserverProvider {
    fun getObserver(
        file: File,
        callback: FileChangedListener,
        mask: Int = FileObserver.ALL_EVENTS
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        HistoryFileObserver(file, callback, mask)
    else
        OldHistoryFileObserver(file, callback, mask)
}