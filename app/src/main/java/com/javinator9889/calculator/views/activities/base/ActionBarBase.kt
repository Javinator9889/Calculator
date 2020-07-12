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
 * Created by Javinator9889 on 7/06/20 - Calculator.
 */
package com.javinator9889.calculator.views.activities.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import com.javinator9889.calculator.R
import com.javinator9889.calculator.utils.dpToPixel

abstract class ActionBarBase : AppCompatActivity() {
    @get:LayoutRes
    protected abstract val layoutId: Int

    @get:MenuRes
    protected abstract val menuRes: Int

    protected var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        findViewById<Toolbar>(R.id.topAppBar)?.let {
            setSupportActionBar(it)
            ViewCompat.setElevation(it, dpToPixel(2F, this))
            toolbar = it
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        with(menuInflater) {
            inflate(menuRes, menu)
        }
        with(DrawableCompat.wrap(AppCompatResources.getDrawable(this, R.drawable.ic_github)!!)) {
            menu?.findItem(R.id.github)?.icon = this
        }
        with(DrawableCompat.wrap(AppCompatResources.getDrawable(this, R.drawable.ic_history)!!)) {
            menu?.findItem(R.id.history)?.icon = this
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.github -> {
                val website = Uri.parse("https://gitlab.javinator9889.com/Javinator9889/calculator")
                with(Intent(Intent.ACTION_VIEW, website)) {
                    if (resolveActivity(this@ActionBarBase.packageManager) != null)
                        startActivity(this)
                    else {
                        Toast.makeText(this@ActionBarBase, R.string.no_browser, Toast.LENGTH_LONG)
                            .show()
                    }
                }
                true
            }
            R.id.history -> {
                onHistoryPressed()
                true
            }
            else -> false
        }
    }

    abstract fun onHistoryPressed()
}