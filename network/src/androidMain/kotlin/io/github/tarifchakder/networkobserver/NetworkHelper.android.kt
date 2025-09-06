package io.github.tarifchakder.networkobserver

actual fun networkHelper(): NetworkHelper = NetworkUtilAndroid(ContextProvider.context)