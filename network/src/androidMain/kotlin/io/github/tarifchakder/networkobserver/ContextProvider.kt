package io.github.tarifchakder.networkobserver

import android.annotation.SuppressLint
import android.content.Context
import androidx.startup.Initializer


@SuppressLint("StaticFieldLeak")
internal object ContextProvider {
    lateinit var context: Context
}

internal class ContextInitializer : Initializer<ContextProvider> {

    override fun create(context: Context): ContextProvider =
        ContextProvider.apply {
            this.context = context.applicationContext
        }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}