package io.github.szpontium.platform

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal var appContext: Context? = null

fun initAppContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        appContext?.let { context ->
            addInterceptor(ChuckerInterceptor.Builder(context).build())
        }
    }
}
