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
 * Created by Javinator9889 on 11/07/20 - Calculator.
 */
package com.javinator9889.calculator.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import com.javinator9889.calculator.containers.HistoryData
import com.javinator9889.calculator.utils.userLocaleDate
import kotlinx.android.synthetic.main.history_item.view.*
import timber.log.Timber

class HistoryAdapter(
    context: Context,
    @LayoutRes private val resource: Int,
    historyValues: List<HistoryData>
) : ArrayAdapter<HistoryData>(context, resource, historyValues) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val historyItem = getItem(position) ?: return super.getView(position, convertView, parent)
        Timber.d("Obtained history item: $historyItem")
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        Timber.d("Created view")

        val operation = historyItem.operation.joinToString("") { Timber.d(it.value); it.value }
        Timber.d(operation)

        view.operation.text = operation
        view.date.text = historyItem.date.userLocaleDate

        return view
    }
}