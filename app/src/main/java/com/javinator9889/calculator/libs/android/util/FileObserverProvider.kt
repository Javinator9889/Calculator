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
import com.javinator9889.calculator.listeners.OnFileChangedListener
import java.io.File


object FileObserverProvider {
    fun getObserver(file: File, callback: OnFileChangedListener) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            HistoryFileObserver(file, callback)
        else
            OldHistoryFileObserver(file, callback)
}