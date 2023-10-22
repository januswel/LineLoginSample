package com.example.line

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.*
import com.linecorp.linesdk.LineApiResponseCode.*
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.api.LineApiClient
import com.linecorp.linesdk.api.LineApiClientBuilder
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import java.util.Arrays

class LineLoginModule(reactContext: ReactApplicationContext, lineChannelId: String) :
    ReactContextBaseJavaModule(reactContext) {
  override fun getName() = "LineLogin"

  private var loginPromise: Promise? = null

  private val reactContext: ReactApplicationContext = reactContext
  private val lineChannelId: String = lineChannelId

  init {
    val activityEventListener: BaseActivityEventListener =
        object : BaseActivityEventListener() {
          override fun onActivityResult(
              activity: Activity?,
              requestCode: Int,
              resultCode: Int,
              intent: Intent?
          ) {
            if (requestCode != REQUEST_CODE) {
              loginPromise?.let { promise ->
                promise.reject(E_FAILED_TO_LOGIN, "Unsupported Request Code: " + requestCode)
              }
              return
            }

            val result = LineLoginApi.getLoginResultFromIntent(intent)

            loginPromise?.let { promise ->
              when (result.getResponseCode()) {
                SUCCESS -> {
                  val ret = Arguments.createMap()
                  ret.putString("displayName", result.lineProfile?.displayName)
                  ret.putString("pictureUrl", result.lineProfile?.pictureUrl.toString())
                  ret.putString("email", result.lineIdToken?.email)
                  ret.putString("idToken", result.lineIdToken?.rawString)
                  promise.resolve(ret)
                }
                CANCEL -> {
                  promise.reject(E_LOGIN_CANCELED, "LINE Login canceled by user")
                }
                else -> {
                  promise.reject(E_FAILED_TO_LOGIN, result.errorData.message)
                }
              }

              loginPromise = null
            }
          }
        }

    reactContext.addActivityEventListener(activityEventListener)

    val apiClientBuilder = LineApiClientBuilder(reactContext, lineChannelId)
    lineApiClient = apiClientBuilder.build()
  }

  @ReactMethod
  fun login(nonce: String, promise: Promise) {
    val activity = currentActivity

    if (activity == null) {
      promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist")
      return
    }

    loginPromise = promise

    try {
      val loginIntent =
          LineLoginApi.getLoginIntent(
              reactContext,
              lineChannelId,
              LineAuthenticationParams.Builder()
                  .scopes(Arrays.asList(Scope.PROFILE, Scope.OPENID_CONNECT, Scope.OC_EMAIL))
                  .nonce(nonce)
                  .build()
          )
      activity.startActivityForResult(loginIntent, REQUEST_CODE)
    } catch (t: Throwable) {
      Log.e("ERROR", t.toString())
      loginPromise?.reject(E_FAILED_TO_LOGIN, t)
      loginPromise = null
    }
  }

  @ReactMethod
  fun logout(dummy: String, promise: Promise) {
    lineApiClient?.let { client ->
      val response = client.logout()

      if (response.isSuccess()) {
        promise.resolve(null)
        return
      }

      promise.reject(E_FAILED_TO_LOGOUT, response.errorData.message)
      return
    }

    // not reachable
    throw Exception("LineApiClient is not initialized")
  }

  companion object {
    const val REQUEST_CODE = 1
    const val E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST"
    const val E_LOGIN_CANCELED = "E_LOGIN_CANCELED"
    const val E_FAILED_TO_LOGIN = "E_FAILED_TO_LOGIN"
    const val E_FAILED_TO_LOGOUT = "E_FAILED_TO_LOGOUT"

    var lineApiClient: LineApiClient? = null
  }
}
