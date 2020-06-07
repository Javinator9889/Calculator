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
import com.javinator9889.calculator.models.viewmodels.factory.ViewModelAssistedFactory
import com.javinator9889.calculator.utils.removeLast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mariuszgromada.math.mxparser.Expression
import timber.log.Timber

internal const val ARG_OPERANDS = "args:calculator:operands"
internal const val ARG_RESULT = "args:calculator:result"
internal const val ARG_COP = "args:calculator:current_operation"
internal const val ARG_EQUALS = "args:calculator:equals_button"
internal const val ARG_OPERATOR = "args:calculator:operator_live_data"


class CalculatorViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val operationResult: MutableLiveData<String> = savedStateHandle.getLiveData(ARG_RESULT, "")
    val currentOperation: MutableLiveData<String> = savedStateHandle.getLiveData(ARG_COP, "")
    val equalsResult: MutableLiveData<String> = savedStateHandle.getLiveData(ARG_EQUALS, "")
    val operatorLiveData: MutableLiveData<ButtonAction> =
        savedStateHandle.getLiveData(ARG_OPERATOR, ButtonAction("", ""))
    private var operands: MutableList<ButtonAction> =
        savedStateHandle.get<MutableList<ButtonAction>>(ARG_OPERANDS)
            ?: mutableListOf()
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

    fun finish() {
        operatorLiveData.removeObserver(observer)
    }

    private fun operate() {
        viewModelScope.launch {
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
            with(Expression(expression.toString())) {
                var calcResult = calculate().toString()
                if (calcResult.endsWith(".0"))
                    calcResult = calcResult.removeSuffix(".0")
                withContext(Dispatchers.Main) {
                    operationResult.value = calcResult
                }
                savedStateHandle.set(ARG_RESULT, calcResult)
            }
            savedStateHandle.set(ARG_COP, operation.toString())
        }
    }

    private fun validateInput(): Boolean {
        val expression = StringBuilder(operands.size)
        operands.forEach { operand ->
            expression.append(operand.action)
        }
        Timber.d("Expression: $expression")
        return with(Expression(expression.toString())) {
            Timber.d("Is valid?: ${checkSyntax()}")
            checkSyntax()
        }
    }

    companion object Factory : ViewModelAssistedFactory<CalculatorViewModel> {
        override fun create(handle: SavedStateHandle) = CalculatorViewModel(handle)
    }
}