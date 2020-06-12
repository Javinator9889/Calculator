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
package com.javinator9889.calculator.views.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.os.Parcelable
import android.text.TextPaint
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import com.javinator9889.calculator.R


class CalculatorEditText(context: Context, attrs: AttributeSet?, defStyle: Int) :
    AppCompatEditText(context, attrs, defStyle) {
    private val maximumTextSize: Float
    private val minimumTextSize: Float
    private val stepTextSize: Float

    // Temporary objects for use in layout methods.
    private val tempPaint: Paint = TextPaint()
    private val tempRect: Rect = Rect()
    private var widthConstraint = -1
    private var onTextSizeChangeListener: OnTextSizeChangeListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)


    init {
        val styledAttrs = context.obtainStyledAttributes(
            attrs, R.styleable.CalculatorEditText, defStyle, 0
        )
        maximumTextSize = styledAttrs.getDimension(
            R.styleable.CalculatorEditText_maxTextSize, textSize
        )
        minimumTextSize = styledAttrs.getDimension(
            R.styleable.CalculatorEditText_minTextSize, textSize
        )
        stepTextSize = styledAttrs.getDimension(
            R.styleable.CalculatorEditText_stepTextSize,
            (maximumTextSize - minimumTextSize) / 3
        )
        styledAttrs.recycle()
        customSelectionActionModeCallback = NO_SELECTION_ACTION_MODE_CALLBACK
        if (isFocusable) {
            movementMethod = ScrollingMovementMethod.getInstance()
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, maximumTextSize)
        minHeight = lineHeight + compoundPaddingBottom + compoundPaddingTop
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            // Hack to prevent keyboard and insertion handle from showing.
            cancelLongPress()
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthConstraint =
            MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getVariableTextSize(text.toString()))
    }

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()

        // EditText will freeze any text with a selection regardless of getFreezesText() ->
        // return null to prevent any state from being preserved at the instance level.
        return null
    }

    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        val textLength = text.length
        if (selectionStart != textLength || selectionEnd != textLength) {
            // Pin the selection to the end of the current text.
            setSelection(textLength)
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, getVariableTextSize(text.toString()))
    }

    override fun setTextSize(unit: Int, size: Float) {
        val oldTextSize = textSize
        super.setTextSize(unit, size)
        if (onTextSizeChangeListener != null && textSize != oldTextSize) {
            onTextSizeChangeListener!!.onTextSizeChanged(this, oldTextSize)
        }
    }

    fun setOnTextSizeChangeListener(listener: OnTextSizeChangeListener?) {
        onTextSizeChangeListener = listener
    }

    fun getVariableTextSize(text: String?): Float {
        if (widthConstraint < 0 || maximumTextSize <= minimumTextSize) {
            // Not measured, bail early.
            return textSize
        }

        // Capture current paint state.
        tempPaint.set(paint)

        // Step through increasing text sizes until the text would no longer fit.
        var lastFitTextSize = minimumTextSize
        while (lastFitTextSize < maximumTextSize) {
            val nextSize =
                (lastFitTextSize + stepTextSize).coerceAtMost(maximumTextSize)
            tempPaint.textSize = nextSize
            lastFitTextSize = if (tempPaint.measureText(text) > widthConstraint) {
                break
            } else {
                nextSize
            }
        }
        return lastFitTextSize
    }

    override fun getCompoundPaddingTop(): Int {
        // Measure the top padding from the capital letter height of the text instead of the top,
        // but don't remove more than the available top padding otherwise clipping may occur.
        paint.getTextBounds("H", 0, 1, tempRect)
        val fontMetrics = paint.fontMetricsInt
        val paddingOffset: Int = -(fontMetrics.ascent + tempRect.height())
        return super.getCompoundPaddingTop() - paddingTop.coerceAtMost(paddingOffset)
    }

    override fun getCompoundPaddingBottom(): Int {
        // Measure the bottom padding from the baseline of the text instead of the bottom, but don't
        // remove more than the available bottom padding otherwise clipping may occur.
        val fontMetrics = paint.fontMetricsInt
        return super.getCompoundPaddingBottom() - paddingBottom.coerceAtMost(fontMetrics.descent)
    }

    interface OnTextSizeChangeListener {
        fun onTextSizeChanged(textView: TextView, oldSize: Float)
    }

    companion object {
        private val NO_SELECTION_ACTION_MODE_CALLBACK: ActionMode.Callback =
            object : ActionMode.Callback {
                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    return false
                }

                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    // Prevents the selection action mode on double tap.
                    return false
                }

                override fun onDestroyActionMode(mode: ActionMode?) {}
                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return false
                }
            }
    }
}