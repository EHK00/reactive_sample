package com.ekh.reactivesample.ui.base

import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentNavigationDelegate(
    private val fragment: Fragment,
) : ReadOnlyProperty<Fragment, BaseNavigation> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): BaseNavigation {
        return (fragment.activity as? BaseActivity)?.navigator
            ?: throw IllegalStateException("not attached to an BaseActivity.")
    }
}

fun BaseFragment.navigation() = FragmentNavigationDelegate(this)