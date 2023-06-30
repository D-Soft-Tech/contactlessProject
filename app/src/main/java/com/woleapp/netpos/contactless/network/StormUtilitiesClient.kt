package com.woleapp.netpos.contactless.network

import android.content.Context
import com.dsofttech.dprefs.utils.DPrefs
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.util.PREF_BILLS_TOKEN
import com.woleapp.netpos.contactless.util.UtilityParam
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

object StormUtilitiesApiClient {

    private val BASE_URL = UtilityParam.BASE_URL_STORM_UTILITIES

    @Volatile
    private var INSTANCE: StormUtilitiesApiService? = null
    fun getStormUtilitiesApiClientInstance(context: Context): StormUtilitiesApiService {
        return INSTANCE ?: synchronized(this) {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getBillsOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(StormUtilitiesApiService::class.java)
                .also {
                    INSTANCE = it
                }
        }
    }
}

fun getBillsOkHttpClient(context: Context): OkHttpClient = OkHttpClient.Builder()
    .callTimeout(60, TimeUnit.SECONDS)
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(BillsTokenInterceptor(context))
    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
    .build()

class BillsTokenInterceptor(val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token: String? = if (DPrefs.getString(PREF_BILLS_TOKEN).isNotEmpty()) {
            DPrefs.getString(
                PREF_BILLS_TOKEN,
            )
        } else {
            null
        }
        val request = chain.request()
        if (BuildConfig.DEBUG) {
            Timber.e(request.url.toString())
            Timber.e(request.body.toString())
        }
        val method = request.method
        if (method == "POST" || method == "PUT") {
            val reqBody = request.body!!
            val contentLength = reqBody.contentLength()
            if (BuildConfig.DEBUG) {
                Timber.e("$contentLength")
            }
        }
        val response = chain.proceed(
            request.newBuilder().run {
                token?.let {
                    if (BuildConfig.DEBUG) {
                        Timber.e("Token: Bearer $it")
                    }
                    addHeader(
                        "Authorization",
                        "Bearer $it",
                    )
                }
                build()
            },
        )
        val body = response.body
        val bodyString = body?.string()
        if (BuildConfig.DEBUG) {
            Timber.e("response %s", bodyString!!)
        }
        return response.newBuilder().body(bodyString?.let {
            ResponseBody.create(body.contentType(),
                it
            )
        })
            .build()
    }
}
