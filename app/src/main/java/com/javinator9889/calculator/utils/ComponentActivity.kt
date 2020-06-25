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

import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider

/**
 * {@inheritDoc}
 *
 *
 * The extras of [.getIntent] when this is first called will be used as
 * the defaults to any [androidx.lifecycle.SavedStateHandle] passed to a view model
 * created using this factory.
 */
val ComponentActivity.defaultViewModelProviderFactory: ViewModelProvider.Factory
    get() {
        checkNotNull(application) {
            ("Your activity is not yet attached to the "
                    + "Application instance. You can't request ViewModel before onCreate call.")
        }
        return SavedStateViewModelFactory(
            application,
            this,
            if (intent != null) intent.extras else null
        )
    }
