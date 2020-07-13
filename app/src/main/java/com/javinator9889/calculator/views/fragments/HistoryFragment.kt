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
package com.javinator9889.calculator.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.javinator9889.calculator.R
import com.javinator9889.calculator.listeners.HistoryItemClickedListener
import com.javinator9889.calculator.models.viewmodels.history.HistoryViewModel
import com.javinator9889.calculator.utils.activityViewModels
import com.javinator9889.calculator.views.adapters.HistoryAdapter
import kotlinx.android.synthetic.main.history_layout.*

class HistoryFragment : Fragment() {
    private val historyViewModel: HistoryViewModel by activityViewModels()

    init {
        lifecycleScope.launchWhenStarted {
            historyViewModel.historyData.observe(this@HistoryFragment) {
                context?.let { ctx ->
                    val adapter = HistoryAdapter(ctx, R.layout.history_item, it)
                    historyContainer.adapter = adapter
                    historyContainer.setOnItemClickListener { _, _, position, _ ->
                        val activity = requireActivity() as HistoryItemClickedListener
                        activity.onClick(it[position])
                    }
                    historyContainer.post {
                        historyContainer.setSelection(adapter.count - 1)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.history_layout, container, false)
}