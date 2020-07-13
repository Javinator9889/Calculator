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
package com.javinator9889.calculator.containers

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * Data class that stores a button action and its value. In particular, this is how it works:
 *  - the "action" represents the mathematical operation that can be understood by mXparser lib.
 *  For example, the root is translated from '√' to "sqrt(".
 *  - the "value" represents how the operation will be displayed on screen so the user is able
 *  to understand it. For example, root is translated from '√' to '√(', as it needs parenthesis.
 *  In addition, this class implements the Parcelable implementation so it can be written and
 *  recovered from both disk and memory.
 *
 *  @param action how the operation is represented in mXparser.
 *  @param value how the operation is represented on screen.
 */
data class ButtonAction(val action: String, val value: String) : Parcelable, Serializable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(action)
        parcel.writeString(value)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ButtonAction> {
        override fun createFromParcel(parcel: Parcel) = ButtonAction(parcel)

        override fun newArray(size: Int) = arrayOfNulls<ButtonAction>(size)
    }
}