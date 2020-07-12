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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.javinator9889.calculator.R
import com.javinator9889.calculator.containers.HistoryData
import com.javinator9889.calculator.models.viewmodels.history.HistoryViewModel
import com.javinator9889.calculator.utils.viewModels
import com.javinator9889.calculator.views.adapters.HistoryAdapter
import kotlinx.android.synthetic.main.history_layout.*
import kotlinx.coroutines.CompletableDeferred

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
    private val historyViewModel: HistoryViewModel by viewModels()
    private lateinit var historyData: List<HistoryData>

    init {
        lifecycleScope.launchWhenCreated {
            historyViewModel.historyData.observe(this@HistoryActivity) {
//                for (action in it)
                val adapter = HistoryAdapter(this@HistoryActivity, R.layout.history_item, it)
                historyContainer.adapter = adapter
                historyContainer.setOnItemClickListener { _, _, position, _ ->
                    val item = it[position]
                    val intent = Intent()
                        .putExtra(ARG_OPERATION, item.operation as Parcelable)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
        /*lifecycleScope.launchWhenCreated {
            val data: List<HistoryData>?
            if (isDataRecoverNeeded.await()) {
                val historyData: MutableList<HistoryData> = mutableListOf()
                try {
                    val historyFile = File(cacheDir, HISTORY_FILE)
                    if (historyFile.exists()) {
                        withContext(Dispatchers.IO) {
                            ObjectInputStream(FileInputStream(historyFile)).use {
                                try {
                                    while (true) {
                                        val item = it.readObject() as HistoryData
                                        Timber.d("Recovered $item")
                                        historyData.add(item)
                                    }
                                } catch (_: EOFException) {
                                }
                            }
                        }
                    }
                } catch (e: Throwable) {
                    Timber.w(e, "Error while recovering history data")
                } finally {
                    data = if (historyData.isNullOrEmpty()) null else historyData
                    Timber.d(data.toString())
                }
            } else data = historyData
            data?.let {
                /*val operandsList = mutableListOf<CharSequence>()
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
                }*/
                val adapter = HistoryAdapter(this@HistoryActivity, R.layout.history_item, it)
                historyContainer.adapter = adapter
                historyContainer.setOnItemClickListener { _, _, position, _ ->
                    val item = it[position]
                    val intent = Intent()
                        .putExtra(ARG_OPERATION, item.operation as Parcelable)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)
        if (savedInstanceState != null) {
            val data = savedInstanceState.getParcelableArrayList<HistoryData>(ARG_HISTORY_KEY)
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