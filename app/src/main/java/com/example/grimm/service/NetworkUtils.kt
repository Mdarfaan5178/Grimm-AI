package com.example.grimm.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkUtils {
    const val NO_NETWORK_MESSAGE = "Sonion, how do you expect me to work without network? 😭"

    var forceNetworkUnavailableForTesting: Boolean? = null

    /**
     * Checks if the device is currently connected to an active network with internet capability.
     */
    fun isNetworkAvailable(context: Context?): Boolean {
        if (forceNetworkUnavailableForTesting != null) {
            return !forceNetworkUnavailableForTesting!!
        }
        if (context == null) return true
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                val activeNetworkInfo = cm.activeNetworkInfo
                activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
        } catch (e: Exception) {
            // In case of permission/security exception, fail open so calls can attempt
            true
        }
    }

    /**
     * Inspects a Throwable (and its cause chain) to determine if it was caused by a network disconnection or timeout.
     */
    fun isNetworkException(throwable: Throwable?): Boolean {
        if (throwable == null) return false
        var current: Throwable? = throwable
        while (current != null) {
            if (current is UnknownHostException ||
                current is ConnectException ||
                current is SocketTimeoutException ||
                current is NoRouteToHostException ||
                current is SocketException ||
                current is InterruptedIOException
            ) {
                return true
            }

            val msg = current.message?.lowercase() ?: ""
            if (msg.contains("unable to resolve host") ||
                msg.contains("failed to connect") ||
                msg.contains("no route to host") ||
                msg.contains("network is unreachable") ||
                msg.contains("connection refused") ||
                msg.contains("software caused connection abort") ||
                msg.contains("connection timed out") ||
                msg.contains("timed out") ||
                msg.contains("offline") ||
                msg.contains("network error")
            ) {
                return true
            }

            current = current.cause
        }
        return false
    }
}
