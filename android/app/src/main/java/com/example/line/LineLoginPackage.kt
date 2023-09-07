package com.example.line

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager

class LineLoginPackage(lineChannelId: String) : ReactPackage {
  private val lineChannelId: String = lineChannelId

  override fun createViewManagers(
      reactContext: ReactApplicationContext
  ): MutableList<ViewManager<View, ReactShadowNode<*>>> = mutableListOf()

  override fun createNativeModules(
      reactContext: ReactApplicationContext
  ): MutableList<NativeModule> =
      listOf(LineLoginModule(reactContext, lineChannelId)).toMutableList()
}
