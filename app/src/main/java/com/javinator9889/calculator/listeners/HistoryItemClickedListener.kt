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
package com.javinator9889.calculator.listeners

import com.javinator9889.calculator.containers.HistoryData

/**
 * Listener that gets notified when a history item was interacted (clicked).
 */
interface HistoryItemClickedListener {

    /**
     * A listener function that received the history item that was clicked.
     *
     * @param item the clicked item.
     */
    fun onClick(item: HistoryData)
}