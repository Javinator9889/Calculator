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
 * Created by Javinator9889 on 6/06/20 - Calculator.
 */
package com.javinator9889.calculator.utils

/**
 * If the running variable is not null, runs the code specified in 'f'
 * @param f the code to be run if 'T' is not null.
 */
fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}
