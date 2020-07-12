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
package com.javinator9889.calculator.containers

import android.os.Parcel
import android.os.Parcelable
import com.javinator9889.calculator.models.ButtonActionList
import java.io.Serializable
import java.util.*

/**
 * Helper class for storing data inside a file for later recovering it.
 */
data class HistoryData(val operation: ButtonActionList, val date: Date) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable<ButtonActionList>(ButtonActionList::class.java.classLoader)!!,
        Date(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(operation, flags)
        parcel.writeLong(date.time)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<HistoryData> {
        override fun createFromParcel(parcel: Parcel) = HistoryData(parcel)

        override fun newArray(size: Int) = arrayOfNulls<HistoryData>(size)
    }
}