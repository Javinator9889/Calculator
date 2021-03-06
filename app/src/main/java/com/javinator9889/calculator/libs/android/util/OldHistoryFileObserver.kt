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

import android.os.FileObserver
import com.javinator9889.calculator.listeners.FileChangedListener
import java.io.File


/**
 * FileObserver for Android versions lower than Q
 */
class OldHistoryFileObserver(
    private val file: File,
    private val callback: FileChangedListener,
    mask: Int = ALL_EVENTS
) : FileObserver(file.path, mask) {
    override fun onEvent(event: Int, path: String?) {
        callback.onFileChanged(file, event)
    }
}