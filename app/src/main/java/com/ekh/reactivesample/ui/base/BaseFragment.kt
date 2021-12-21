package com.ekh.reactivesample.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseFragment(id: Int) : Fragment(id){
    val navigator: BaseNavigation by navigation()

    protected inline fun <T> Flow<T>.bindTo(crossinline action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect {
                    action.invoke(it)
                }
            }
        }
    }

    protected inline fun <T> Flow<T>.bindLatestTo(crossinline action: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectLatest {
                    action.invoke(it)
                }
            }
        }
    }
}