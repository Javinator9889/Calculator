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

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.javinator9889.calculator.R
import com.javinator9889.calculator.containers.HistoryData
import com.javinator9889.calculator.listeners.HistoryItemClickedListener
import com.javinator9889.calculator.models.viewmodels.calculator.CalculatorViewModel
import com.javinator9889.calculator.models.viewmodels.factory.ViewModelFactory
import com.javinator9889.calculator.utils.viewModels
import com.javinator9889.calculator.views.activities.base.ActionBarBase
import com.javinator9889.calculator.views.fragments.HistoryFragment
import com.javinator9889.calculator.views.fragments.OperationInputFragment


/**
 * Bundle key for saving the current displayed operation text
 */
internal const val ARG_OPERATION_TEXT = "args:calculator:operation_text"

/**
 * Bundle key for saving the current displayed operation result
 */
internal const val ARG_CURRENT_RESULT_TEXT = "args:calculator:operation_result"

/**
 * Fragment key for saving / restoring it from instance state
 */
internal const val ARG_FRAGMENT_KEY = "args:calculator:fragment:operation"


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
class Calculator : ActionBarBase(), HistoryItemClickedListener {
    override val layoutId: Int = R.layout.main_layout
    override val menuRes: Int = R.menu.app_menu
    private val calculatorViewModel: CalculatorViewModel by viewModels {
        ViewModelFactory(CalculatorViewModel.Factory, this)
    }
    private lateinit var calculatorFragment: Fragment

    @LayoutRes
    private var activeFragmentId: Int = R.layout.calc_layout
        get() = synchronized(this) {
            return@synchronized field
        }
        set(value) = synchronized(this) {
            field = value
        }

    /**
     * @inheritDoc
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calculatorFragment =
            if (savedInstanceState == null) OperationInputFragment()
            else supportFragmentManager.getFragment(savedInstanceState, ARG_FRAGMENT_KEY)
                ?: OperationInputFragment()
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.frameLayout, calculatorFragment)
            show(calculatorFragment)
            disallowAddToBackStack()
            commit()
        }
    }

    /**
     * @inheritDoc
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::calculatorFragment.isInitialized)
            supportFragmentManager.putFragment(outState, ARG_FRAGMENT_KEY, calculatorFragment)
    }

    /**
     * @inheritDoc
     */
    override fun finish() {
        super.finish()
        calculatorViewModel.finish()
    }

    override fun onHistoryPressed() {
        val historyFragment = HistoryFragment()
        with(supportFragmentManager.beginTransaction()) {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            hide(calculatorFragment)
            add(R.id.frameLayout, historyFragment)
            disallowAddToBackStack()
            activeFragmentId = R.layout.history_layout
            toolbar?.menu?.findItem(R.id.history)?.isVisible = false
            show(historyFragment)
            commit()
        }
    }

    override fun onBackPressed() {
        if (activeFragmentId == R.layout.calc_layout)
            super.onBackPressed()
        else {
            with(supportFragmentManager.beginTransaction()) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                replace(R.id.frameLayout, calculatorFragment)
                disallowAddToBackStack()
                activeFragmentId = R.layout.calc_layout
                toolbar?.menu?.findItem(R.id.history)?.isVisible = true
                show(calculatorFragment)
                commit()
            }
        }
    }

    override fun onClick(item: HistoryData) {
        if (activeFragmentId == R.layout.history_layout) {
            calculatorViewModel.operands = item.operation
            onBackPressed()
            calculatorViewModel.operate()
        }
    }
}