package com.ekh.reactivesample

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader

object Flipper {

    val networkFlipperPlugin = NetworkFlipperPlugin()
    fun init(context: Context) {
        SoLoader.init(context, false)
        if (FlipperUtils.shouldEnableFlipper(context)) {
            AndroidFlipperClient.getInstance(context)
                .apply {
                    addPlugin(networkFlipperPlugin)
                    addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
                    addPlugin(SharedPreferencesFlipperPlugin(context))
                }.also {
                    it.start()
                }
        }
    }
}