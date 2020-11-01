package com.moon.nugasam.menu

import android.view.Menu
import android.view.MenuItem

interface IMeerkatMenu {

    fun onCreateOptionsMenu(menu: Menu): Boolean

    fun onPrepareOptionsMenu(menu: Menu?): Boolean

    fun onOptionsItemSelected(item: MenuItem): Boolean
}