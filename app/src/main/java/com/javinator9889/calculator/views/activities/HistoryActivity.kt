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
 * Created by Javinator9889 on 5/07/20 - Calculator.
 */
package com.javinator9889.calculator.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.javinator9889.calculator.R
import com.javinator9889.calculator.models.ButtonActionList
import kotlinx.android.synthetic.main.history_layout.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream

/**
 * Key for referencing / accessing the history file
 */
internal const val HISTORY_FILE = "ops.history.txt"

/**
 * Key for obtaining an operation, if any, when the user interacts with the list view
 */
internal const val ARG_OPERATION = "args:history:operation:key"

/**
 * Key for synchronizing activities that depends on this one to get the results
 */
internal const val ARG_HISTORY_CODE = 4

/**
 * Key for saving / recovering the history array data from Parcel
 */
internal const val ARG_HISTORY_KEY = "args:history:key"


class HistoryActivity : AppCompatActivity() {
    private val isDataRecoverNeeded = CompletableDeferred<Boolean>()
    private lateinit var historyData: List<ButtonActionList>

    init {
        lifecycleScope.launchWhenCreated {
            val data = if (isDataRecoverNeeded.await()) {
                runCatching {
                    val historyFile = File(cacheDir, HISTORY_FILE)
                    val historyData: MutableList<ButtonActionList> = mutableListOf()
                    if (!historyFile.exists())
                        return@runCatching null
                    withContext(Dispatchers.IO) {
                        ObjectInputStream(FileInputStream(historyFile)).use {
                            try {
                                while (true) {
                                    val item = it.readObject() as ButtonActionList
                                    historyData.add(item)
                                }
                            } catch (_: EOFException) {
                            }
                        }
                    }
                    if (historyData.isNullOrEmpty()) null else historyData
                }.getOrElse { Timber.w(it, "Error while recovering history"); null }
            } else historyData
            data?.let {
                val operandsList = mutableListOf<CharSequence>()
                val outputList = mutableListOf<CharSequence>()
                for (actionList in it) {
//                    val listBuilder = StringBuilder(actionList.size)
//                    withContext(Dispatchers.IO) {
//                        for (item in actionList) {
//                            listBuilder.append(item.value)
//                        }
//                    }
//                    operandsList.add(listBuilder.toString())
                    operandsList.add(
                        actionList.joinToString("") { buttonAction -> buttonAction.value }
//                        actionList.joinToString("")
                    )
//                    operandsList.add(action.action)
//                    outputList.add(action.value)
                }
                val adapter = ArrayAdapter<CharSequence>(
                    this@HistoryActivity, android.R.layout.simple_list_item_1, operandsList
                )
                historyContainer.adapter = adapter
                historyContainer.setOnItemClickListener { _, _, position, _ ->
                    val item = it[position]
                    val intent = Intent()
                        .putExtra(ARG_OPERATION, item as Parcelable)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)
        if (savedInstanceState != null) {
            val data = savedInstanceState.getParcelableArrayList<ButtonActionList>(ARG_HISTORY_KEY)
            if (data == null)
                isDataRecoverNeeded.complete(true)
            else {
                historyData = data
                isDataRecoverNeeded.complete(false)
            }
        } else isDataRecoverNeeded.complete(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::historyData.isInitialized) {
            outState.putParcelableArrayList(
                ARG_HISTORY_KEY,
                historyData as ArrayList<out Parcelable>
            )
        }
    }
}