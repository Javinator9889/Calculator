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
 * Created by Javinator9889 on 25/06/20 - Calculator.
 */
package com.javinator9889.calculator.utils

import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.disableKeyboard() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        this.showSoftInputOnFocus = false
    else with(context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
        hideSoftInputFromWindow(this@disableKeyboard.windowToken, 0)
    }
}