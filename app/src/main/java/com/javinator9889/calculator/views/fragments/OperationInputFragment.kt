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
package com.javinator9889.calculator.views.fragments

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.javinator9889.calculator.R
import com.javinator9889.calculator.containers.ButtonAction
import com.javinator9889.calculator.models.ButtonBinder
import com.javinator9889.calculator.models.viewmodels.calculator.CalculatorViewModel
import com.javinator9889.calculator.models.viewmodels.factory.ViewModelFactory
import com.javinator9889.calculator.models.viewmodels.history.HistoryViewModel
import com.javinator9889.calculator.utils.activityViewModels
import com.javinator9889.calculator.utils.disableKeyboard
import com.javinator9889.calculator.views.activities.ARG_CURRENT_RESULT_TEXT
import com.javinator9889.calculator.views.activities.ARG_OPERATION_TEXT
import com.javinator9889.calculator.views.widgets.CalculatorEditText
import kotlinx.android.synthetic.main.calc_layout.*
import kotlinx.android.synthetic.main.calc_layout.view.*
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class OperationInputFragment : Fragment(), CalculatorEditText.OnTextSizeChangeListener {
    private lateinit var binder: ButtonBinder
    private var currentAnimator: Animator? = null
    private val isFirstLaunch = AtomicBoolean(true)
    private val calculatorViewModel: CalculatorViewModel by activityViewModels {
        ViewModelFactory(CalculatorViewModel.Factory, requireActivity())
    }
    private val historyViewModel: HistoryViewModel by activityViewModels()

    init {
        lifecycleScope.launchWhenStarted {
            calculatorViewModel.currentOperation.observe(this@OperationInputFragment) {
                Timber.d("COP updated - $it")
                if (it != "") {
                    operation.requestFocus()
                    operation.setText(it)
                    operation.setSelection(operation.length())
                } else {
                    operation.clearFocus()
                    if (!isFirstLaunch.compareAndSet(true, false))
                        onClear()
                }
            }
            calculatorViewModel.operationResult.observe(this@OperationInputFragment) {
                Timber.d("OP result updated - $it")
                if (it != "NaN") {
                    operation.error = null
                    currentResult.setText(it)
                }
            }
            calculatorViewModel.equalsResult.observe(this@OperationInputFragment) {
                Timber.d("EQ pressed - $it")
                if (it == "NaN") {
                    operation.error = getString(R.string.error_expression)
                    operation.setSelection(operation.length())
                    return@observe
                }
                operation.error = null
                if (calculatorViewModel.operands.size == 1 &&
                    calculatorViewModel.operands[0] == ButtonAction("", "")
                )
                    return@observe
                historyViewModel.insertNewOperation(calculatorViewModel.operands)
                onResult(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.calc_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binder = ButtonBinder(
            container = view.container,
            model = calculatorViewModel,
            lifecycleOwner = this,
            vibrationService = view.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        )
        view.operation.movementMethod = ScrollingMovementMethod()
        view.operation.setSelection(0)
        view.operation.error = null
        view.operation.disableKeyboard()
        view.operation.isCursorVisible = true
        view.operation.requestFocus()
        view.operation.setOnTextSizeChangeListener(this)
        savedInstanceState?.let {
            view.operation.setText(it.getCharSequence(ARG_OPERATION_TEXT))
            view.currentResult.setText(it.getCharSequence(ARG_CURRENT_RESULT_TEXT))
            view.operation.setSelection(0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentAnimator?.end()
        Timber.d("Saving instance - data: ${calculatorViewModel.currentOperation.value}")
        outState.putCharSequence(ARG_OPERATION_TEXT, calculatorViewModel.currentOperation.value)
        outState.putCharSequence(ARG_CURRENT_RESULT_TEXT, currentResult.text)
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

    private fun onClear() {
        val sourceView = btn_reset
        val colorRes = R.color.colorAccent
        val groupOverlay =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                requireActivity().window.decorView.overlay as ViewGroupOverlay
            else {
                operation.setText("")
                currentResult.setText("")
                return
            }
        val displayRect = Rect()
        resultContainer.getGlobalVisibleRect(displayRect)

        val revealView = View(requireContext())
        revealView.top = displayRect.top
        revealView.left = displayRect.left
        revealView.right = displayRect.right
        revealView.bottom = displayRect.bottom
        revealView.setBackgroundColor(ResourcesCompat.getColor(resources, colorRes, null))
        groupOverlay.add(revealView)

        val clearLocation = IntArray(2)
        sourceView.getLocationInWindow(clearLocation)
        clearLocation[0] += sourceView.width / 2
        clearLocation[1] += sourceView.height / 2

        val revealCenterX = clearLocation[0] - revealView.left
        val revealCenterY = clearLocation[1] - revealView.top

        val x12 = (revealView.left - revealCenterX).toDouble().pow(2)
        val x22 = (revealView.right - revealCenterX).toDouble().pow(2)
        val y2 = (revealView.bottom - revealCenterY).toDouble().pow(2)
        val revealRadius = max(sqrt(x12 + y2), sqrt(x22 + y2)).toFloat()

        val revealAnimator = ViewAnimationUtils.createCircularReveal(
            revealView,
            revealCenterX,
            revealCenterY,
            .0F,
            revealRadius
        )
        revealAnimator.duration =
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        val alphaAnimator = ObjectAnimator.ofFloat(revealView, View.ALPHA, .0F)
        alphaAnimator.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        alphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                operation.setText("")
                currentResult.setText("")
            }
        })

        val animatorSet = AnimatorSet()
        animatorSet.play(revealAnimator).before(alphaAnimator)
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            @SuppressLint("NewApi")
            override fun onAnimationEnd(animation: Animator?) {
                groupOverlay.remove(revealView)
                currentAnimator = null
            }
        })
        currentAnimator = animatorSet
        animatorSet.start()
    }
}