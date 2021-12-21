package com.ekh.reactivesample.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

class BaseNavigation(
    private val fragmentManager: FragmentManager,
    private val containerId: Int,
) {
    fun replaceTo(fragment: Fragment, addToBackStackStrategy: AddToBackStackStrategy = AddToBackStackStrategy.NONE) {
        fragmentManager.commit {
            if (addToBackStackStrategy is AddToBackStackStrategy.Default) {
                addToBackStack(addToBackStackStrategy.name)
            }
            replace(containerId, fragment)
        }
    }
}

sealed class AddToBackStackStrategy {
    object NONE : AddToBackStackStrategy()
    class Default(val name: String? = null) : AddToBackStackStrategy()
}

fun AppCompatActivity.navigation(id: Int): Lazy<BaseNavigation> = lazy {
    BaseNavigation(this.supportFragmentManager, id)
}

