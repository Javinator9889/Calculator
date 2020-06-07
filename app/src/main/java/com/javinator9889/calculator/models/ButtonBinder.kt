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
package com.javinator9889.calculator.models

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.set
import androidx.core.view.forEach
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.javinator9889.calculator.R
import com.javinator9889.calculator.containers.ButtonAction
import com.javinator9889.calculator.models.viewmodels.calculator.CalculatorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


class ButtonBinder(
    container: ConstraintLayout,
    model: CalculatorViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val vibrationService: Vibrator
) : View.OnClickListener {
    private val buttonActions: SparseArray<ButtonAction> = SparseArray(30)
    private val operatorLiveData = model.operatorLiveData

    init {
        lifecycleOwner.lifecycleScope.launch {
            recursivelyFindChilds(container)
        }
    }

    private fun recursivelyFindChilds(container: ViewGroup) {
        container.forEach { child ->
            if (child is ViewGroup)
                recursivelyFindChilds(child)
            else
                matchView(child)
        }
    }

    private fun matchView(view: View) =
        when (view.id) {
            R.id.btn_percent,
            R.id.btn_power,
            R.id.btn_clear,
            R.id.btn_reset,
            R.id.btn_fact,
            R.id.btn_obrkt,
            R.id.btn_cbrkt,
            R.id.btn_e,
            R.id.btn_9,
            R.id.btn_8,
            R.id.btn_7,
            R.id.btn_6,
            R.id.btn_5,
            R.id.btn_4,
            R.id.btn_3,
            R.id.btn_2,
            R.id.btn_1,
            R.id.btn_minus,
            R.id.btn_0,
            R.id.btn_decimal,
            R.id.btn_plus,
            R.id.btn_equals -> bindButton(view as Button, view.text, view.text)
            R.id.btn_ln,
            R.id.btn_sin,
            R.id.btn_cos,
            R.id.btn_tan -> bindButton(view as Button, "${view.text}(", "${view.text}(")
            R.id.btn_root -> bindButton(view as Button, "sqrt(", "${view.text}(")
            R.id.btn_log -> bindButton(view as Button, "${view.text}10(", "${view.text}(")
            R.id.btn_divide -> bindButton(view as Button, "/", view.text)
            R.id.btn_multiply -> bindButton(view as Button, "*", view.text)
            R.id.btn_pi -> bindButton(view as Button, "pi", view.text)
            else -> Timber.d("Unknown ID")
        }

    override fun onClick(v: View) {
        lifecycleOwner.lifecycleScope.launch(context = Dispatchers.Main) {
            Timber.d("View clicked: $v")
            buttonActions[v.id, null]?.let {
                operatorLiveData.value = it
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    vibrationService.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                else
                    vibrationService.vibrate(100)
            }
        }
    }

    private fun bindButton(view: Button, action: CharSequence, value: CharSequence) {
        buttonActions[view.id] = ButtonAction(action, value)
        view.setOnClickListener(this)
    }
}