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
 * Created by Javinator9889 on 5/06/20 - Calculator.
 */
package com.javinator9889.calculator.logger

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Custom Timber tree for only logging both warn and error messages
 */
class CrashReportingTree : Timber.Tree() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    init {
        crashlytics.setCrashlyticsCollectionEnabled(true)
    }

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.DEBUG, Log.VERBOSE, Log.INFO -> return
            Log.WARN -> {
                Log.w(tag, message, t)
                crashlytics.log("W: $tag: $message")
                t?.let { crashlytics.recordException(it) }
            }
            Log.ERROR -> {
                Log.e(tag, message, t)
                crashlytics.log("E/$tag: $message")
                t?.let { crashlytics.recordException(it) }
            }
        }
    }
}