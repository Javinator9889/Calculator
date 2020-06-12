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

import android.animation.*
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.javinator9889.calculator.R
import com.javinator9889.calculator.models.ButtonBinder
import com.javinator9889.calculator.models.viewmodels.calculator.CalculatorViewModel
import com.javinator9889.calculator.models.viewmodels.factory.ViewModelFactory
import com.javinator9889.calculator.utils.notNull
import com.javinator9889.calculator.utils.viewModels
import com.javinator9889.calculator.views.activities.base.ActionBarBase
import com.javinator9889.calculator.views.widgets.CalculatorEditText
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
class Calculator : ActionBarBase(), CalculatorEditText.OnTextSizeChangeListener {
    override val layoutId: Int = R.layout.calc_layout
    override val menuRes: Int = R.menu.app_menu
    private lateinit var binder: ButtonBinder
    private var currentAnimator: Animator? = null
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
                    if (it != "")
                        operation.requestFocus()
                    else
                        operation.clearFocus()
                })
                calculatorViewModel.operationResult.observe(this@Calculator, Observer {
                    Timber.d("OP result updated - $it")
                    if (it != "NaN") {
                        operation.error = null
                        currentResult.setText(it)
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
                    onResult(it)
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
                disableEditTextKeyboard()
                operation.isCursorVisible = true
                operation.requestFocus()
                operation.setOnTextSizeChangeListener(this@Calculator)
            }
        }
    }

    /**
     * @inheritDoc
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentAnimator.notNull {
            it.end()
        }
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
        currentResult.setText(savedInstanceState.getCharSequence(ARG_CURRENT_RESULT_TEXT))
        operation.setSelection(0)
    }

    /**
     * @inheritDoc
     */
    override fun finish() {
        super.finish()
        calculatorViewModel.finish()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        currentAnimator.notNull {
            it.end()
        }
    }

    override fun onTextSizeChanged(textView: TextView, oldSize: Float) {
        val textScale = oldSize / textView.textSize
        val translationX = (1.0f - textScale) * (textView.width / 2.0f - textView.paddingRight)
        val translationY = (1.0f - textScale) * (textView.height / 2.0f - textView.paddingBottom)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(textView, View.SCALE_X, textScale, 1.0f),
            ObjectAnimator.ofFloat(textView, View.SCALE_Y, textScale, 1.0f),
            ObjectAnimator.ofFloat(textView, View.TRANSLATION_X, translationX, 0.0f),
            ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, translationY, 0.0f)
        )
        animatorSet.duration =
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    private fun disableEditTextKeyboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            operation.showSoftInputOnFocus = false
        else with(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
            hideSoftInputFromWindow(operation.windowToken, 0)
        }
    }

    private fun onResult(result: String) {
        if (TextUtils.isEmpty(currentResult.text))
            return
        // Calculate the values needed to perform the scale and translation animations,
        // accounting for how the scale will affect the final position of the text.
        // Calculate the values needed to perform the scale and translation animations,
        // accounting for how the scale will affect the final position of the text.
        val resultScale = operation.getVariableTextSize(result) / currentResult.textSize
        val resultTranslationX = (1.0f - resultScale) *
                (currentResult.width / 2.0f - currentResult.paddingRight)
        val resultTranslationY = (1.0f - resultScale) *
                (currentResult.height / 2.0f - currentResult.paddingBottom) +
                (operation.bottom - currentResult.bottom) +
                (currentResult.paddingBottom - operation.paddingBottom)
        val formulaTranslationY = -operation.bottom.toFloat()

        // Use a value animator to fade to the final text color over the course of the animation.
        val resultTextColor = currentResult.currentTextColor
        val formulaTextColor = operation.currentTextColor
        val textColorAnimator =
            ValueAnimator.ofObject(ArgbEvaluator(), resultTextColor, formulaTextColor)
        textColorAnimator.addUpdateListener { valueAnimator ->
            currentResult.setTextColor(
                valueAnimator.animatedValue as Int
            )
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            textColorAnimator,
            ObjectAnimator.ofFloat(currentResult, View.SCALE_X, resultScale),
            ObjectAnimator.ofFloat(currentResult, View.SCALE_Y, resultScale),
            ObjectAnimator.ofFloat(currentResult, View.TRANSLATION_X, resultTranslationX),
            ObjectAnimator.ofFloat(currentResult, View.TRANSLATION_Y, resultTranslationY),
            ObjectAnimator.ofFloat(operation, View.TRANSLATION_Y, formulaTranslationY)
        )
        animatorSet.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                currentResult.setText(result)
            }

            override fun onAnimationEnd(animation: Animator) {
                // Reset all of the values modified during the animation.
                currentResult.setTextColor(resultTextColor)
                currentResult.scaleX = 1.0f
                currentResult.scaleY = 1.0f
                currentResult.translationX = 0.0f
                currentResult.translationY = 0.0f
                operation.translationY = 0.0f

                // Finally update the formula to use the current result.
                operation.setText(result)
                currentResult.text = null
                currentAnimator = null
            }
        })

        currentAnimator = animatorSet
        animatorSet.start()
    }
}