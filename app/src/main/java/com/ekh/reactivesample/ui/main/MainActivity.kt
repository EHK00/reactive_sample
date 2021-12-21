package com.ekh.reactivesample.ui.main

import android.os.Bundle
import com.ekh.reactivesample.R
import com.ekh.reactivesample.databinding.ActivityMainBinding
import com.ekh.reactivesample.ui.base.BaseActivity
import com.ekh.reactivesample.ui.base.navigation
import com.ekh.reactivesample.ui.base.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val binding by viewBinding(ActivityMainBinding::inflate)
    override val navigator by navigation(R.id.container)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
