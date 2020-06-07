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
package com.javinator9889.calculator.views.activities

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.text.method.ScrollingMovementMethod
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.javinator9889.calculator.R
import com.javinator9889.calculator.models.ButtonBinder
import com.javinator9889.calculator.models.viewmodels.calculator.CalculatorViewModel
import com.javinator9889.calculator.models.viewmodels.factory.ViewModelFactory
import kotlinx.android.synthetic.main.calc_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Bundle key for saving the current displayed operation text
 */
internal const val ARG_OPERATION_TEXT = "args:calculator:operation_text"

/**
 * Bundle key for saving the current displayed operation result
 */
internal const val ARG_CURRENT_RESULT_TEXT = "args:calculator:operation_result"


/**
 * Main application activity. This class encapsulates the behaviour that must be shown
 * to the users. It consists on some different parts:
 *  - The {@link com.javinator9889.calculator.models.viewmodels.calculator.CalculatorViewModel CalculatorViewModel}
 *  which contains the application logic: it has relationships between buttons and actions that must
 *  take place.
 *  - The LifecycleScope part, responsible for launching coroutines for managing the input data and
 *  elements of the UI.
 *
 * When the activity is created, it waits until the lifecycle status has reached the CREATED one
 * before initializing some variables (such as {@literal lateinit var binder}) and start observing
 * the view models' live data.
 *
 * @see com.javinator9889.calculator.models.viewmodels.calculator.CalculatorViewModel
 */
class Calculator : AppCompatActivity() {
    private lateinit var binder: ButtonBinder
    private val calculatorViewModel: CalculatorViewModel by viewModels {
        ViewModelFactory(CalculatorViewModel.Factory, this)
    }

    init {
        lifecycleScope.launch(context = Dispatchers.Main) {
            whenCreated {
                calculatorViewModel.currentOperation.observe(this@Calculator, Observer {
                    Timber.d("COP updated - $it")
                    operation.setText(it)
                    operation.setSelection(operation.length())
                })
                calculatorViewModel.operationResult.observe(this@Calculator, Observer {
                    Timber.d("OP result updated - $it")
                    if (it != "NaN") {
                        operation.error = null
                        currentResult.text = it
                    }
                })
                calculatorViewModel.equalsResult.observe(this@Calculator, Observer {
                    Timber.d("EQ pressed - $it")
                    if (it == "NaN") {
                        operation.error = getString(R.string.error_expression)
                        operation.setSelection(operation.length())
                        return@Observer
                    }
                    operation.error = null
                    val fadeOutAnimation =
                        AnimationUtils.loadAnimation(this@Calculator, android.R.anim.fade_out)
                    val fadeInAnimation =
                        AnimationUtils.loadAnimation(this@Calculator, android.R.anim.fade_in)
                    operation.clearFocus()
                    operation.setText(currentResult.text)
                    operation.setSelection(0)
                    operation.startAnimation(fadeInAnimation)

                    currentResult.startAnimation(fadeOutAnimation)
                    currentResult.text = ""
                })
                Timber.d("Setting values")
                binder = ButtonBinder(
                    container = container,
                    model = calculatorViewModel,
                    lifecycleOwner = this@Calculator,
                    vibrationService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                )
                operation.movementMethod = ScrollingMovementMethod()
                operation.setSelection(0)
                operation.error = null
            }
        }
    }

    /**
     * @inheritDoc
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calc_layout)
    }

    /**
     * @inheritDoc
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.d("Saving instance - data: ${calculatorViewModel.currentOperation.value}")
        outState.putCharSequence(ARG_OPERATION_TEXT, calculatorViewModel.currentOperation.value)
        outState.putCharSequence(ARG_CURRENT_RESULT_TEXT, currentResult.text)
    }

    /**
     * @inheritDoc
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Timber.d("Restoring instance - op: ${savedInstanceState.getCharSequence(ARG_OPERATION_TEXT)}")
        operation.setText(savedInstanceState.getCharSequence(ARG_OPERATION_TEXT))
        currentResult.text = savedInstanceState.getCharSequence(ARG_CURRENT_RESULT_TEXT)
        operation.setSelection(0)
    }

    /**
     * @inheritDoc
     */
    override fun finish() {
        super.finish()
        calculatorViewModel.finish()
    }
}