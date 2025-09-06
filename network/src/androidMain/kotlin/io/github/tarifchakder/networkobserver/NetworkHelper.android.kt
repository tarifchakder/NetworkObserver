package io.github.tarifchakder.networkobserver

actual fun networkHelper(): NetworkHelper = NetworkUtil(ContextProvider.context)