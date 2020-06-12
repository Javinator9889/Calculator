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
 * Created by Javinator9889 on 12/06/20 - Calculator.
 */
package com.javinator9889.calculator.utils

import org.javia.arity.Symbols
import org.javia.arity.SyntaxException
import org.javia.arity.Util

internal const val MAX_DIGITS = 12;
internal const val ROUNDING_DIGITS = 5;

object Calculator {
    fun evaluate(input: String): String {
        val symbols = Symbols()
        var expression = input
        while (expression.isNotEmpty() && "+-/*".indexOf(expression[expression.lastIndex]) != -1)
            expression = expression.substring(0, expression.lastIndex)
        try {
            val result = symbols.eval(expression)
            if (result.isNaN())
                return "NaN"
            return Util.doubleToString(result, MAX_DIGITS, ROUNDING_DIGITS);
        } catch (e: SyntaxException) {
            return "NaN"
        }
    }

    fun isValid(input: String): Boolean {
        val symbols = Symbols()
        return try {
            symbols.eval(input)
            true
        } catch (_: SyntaxException) {
            false
        }
    }
}