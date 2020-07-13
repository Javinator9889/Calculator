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
package com.javinator9889.calculator.models.viewmodels.calculator

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.*
import com.javinator9889.calculator.containers.ButtonAction
import com.javinator9889.calculator.models.ButtonActionList
import com.javinator9889.calculator.models.viewmodels.factory.ViewModelAssistedFactory
import com.javinator9889.calculator.utils.Calculator
import com.javinator9889.calculator.utils.removeLast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal const val ARG_OPERANDS = "args:calculator:operands"
internal const val ARG_RESULT = "args:calculator:result"
internal const val ARG_COP = "args:calculator:current_operation"
internal const val ARG_EQUALS = "args:calculator:equals_button"
internal const val ARG_OPERATOR = "args:calculator:operator_live_data"


/**
 * The calculator's view model. This class is the responsible for calculating and synchronizing
 * the user actions with the output information. By using the ViewModel, which has its own lifecycle
 * and does not change across UI updates, it is possible to have it attached to the main UI and work
 * even if the user changes orientation or swaps activities.
 * In particular, this is achieved thanks to ViewModel itself and {@link SavedStateHandle} class,
 * which allows the developer to store more complex data into device memory by using, for example,
 * the Parcelable class.
 * When started, it creates the public live data (operationResult, currentOperation, equalsResult
 * and operatorLiveData) and offers them to the rest of the application. In addition, it starts
 * observing the "operatorLiveData" for detecting changes in the user input (when the user presses
 * a button, that live data is updated with the correspondent action and value).
 * Finally, as it is observing forever the data-source, it must be notified whether it has to stop
 * observing for avoiding memory leaks (see {@link #finish()}).
 *
 * @param savedStateHandle the state handler for storing custom properties.
 *
 * @see CalculatorViewModel.finish
 * @see SavedStateHandle
 * @see ViewModel
 * @see operatorLiveData
 * @see ButtonAction
 * @see Observer
 */
class CalculatorViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val operationResult: MutableLiveData<String> = savedStateHandle.getLiveData(ARG_RESULT, "")
    val currentOperation: MutableLiveData<String> = savedStateHandle.getLiveData(ARG_COP, "")
    val equalsResult: MutableLiveData<String> = savedStateHandle.getLiveData(ARG_EQUALS, "")
    val operatorLiveData: MutableLiveData<ButtonAction> =
        savedStateHandle.getLiveData(ARG_OPERATOR, ButtonAction("", ""))
    internal var operands: ButtonActionList =
        savedStateHandle.get<ButtonActionList>(ARG_OPERANDS) ?: ButtonActionList()
    private var equalsPressed = false
    private val observer: Observer<ButtonAction> = Observer { button ->
        when (button.action) {
            "AC" -> {
                operands.clear()
                operationResult.value = ""
                currentOperation.value = ""
                equalsPressed = false
            }
            "C" -> {
                operands.removeLast()
                operate()
                equalsPressed = false
            }
            "=" -> {
                if (!validateInput())
                    equalsResult.value = "NaN"
                else {
                    operationResult.value?.let { it ->
                        equalsPressed = true
                        equalsResult.value = it
                    }
                }
            }
            else -> {
                if (button.action.isDigitsOnly() && equalsPressed)
                    operands.clear()
                equalsPressed = false
                operands.add(button)
                operate()
            }
        }
        savedStateHandle.set(ARG_OPERANDS, operands)
    }

    init {
        viewModelScope.launch {
            operatorLiveData.observeForever(observer)
        }
    }

    /**
     * Notifies the view model that it must stop observing for data changes.
     */
    fun finish() = operatorLiveData.removeObserver(observer)

    internal fun operate() = viewModelScope.launch {
        val expression = StringBuilder(operands.size)
        val operation = StringBuilder(operands.size)
        operands.forEach { operand ->
            expression.append(operand.action)
            operation.append(operand.value)
        }
        Timber.d("Operation: $operation")
        withContext(Dispatchers.Main) {
            currentOperation.value = operation.toString()
        }
        with(Calculator.evaluate(expression.toString())) {
            var calcResult = this
            if (calcResult.endsWith(".0"))
                calcResult = calcResult.removeSuffix(".0")
            withContext(Dispatchers.Main) {
                operationResult.value = calcResult
            }
            savedStateHandle.set(ARG_RESULT, calcResult)
        }
        savedStateHandle.set(ARG_COP, operation.toString())
    }

    private fun validateInput(): Boolean {
        val expression = StringBuilder(operands.size)
        operands.forEach { operand ->
            expression.append(operand.action)
        }
        Timber.d("Expression: $expression")
        return Calculator.isValid(expression.toString())
    }

    /**
     * Factory object that implements the ViewModelAssistedFactory interface for creating
     * a custom ViewModel by using the lazy initialization method.
     */
    companion object Factory : ViewModelAssistedFactory<CalculatorViewModel> {
        /**
         * @inheritDoc
         */
        override fun create(handle: SavedStateHandle) = CalculatorViewModel(handle)
    }
}