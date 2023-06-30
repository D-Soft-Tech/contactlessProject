package com.woleapp.netpos.contactless.mqtt

import android.content.Context
import android.os.Build
import com.dsofttech.dprefs.utils.DPrefs
import com.hivemq.client.internal.mqtt.MqttClientSslConfigImpl
import com.hivemq.client.internal.mqtt.MqttClientSslConfigImplBuilder
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3RxClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.database.AppDatabase
import com.woleapp.netpos.contactless.database.dao.MqttLocalDao
import com.woleapp.netpos.contactless.model.* // ktlint-disable no-wildcard-imports
import com.woleapp.netpos.contactless.util.PREF_LAST_LOCATION
import com.woleapp.netpos.contactless.util.Singletons
import com.woleapp.netpos.contactless.util.Singletons.gson
import com.woleapp.netpos.contactless.util.UtilityParam
import com.woleapp.netpos.contactless.util.disposeWith
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object MqttHelper {
    private val SERVER_HOST = UtilityParam.BASE_URL_NETPOS_MQTT
    private const val PORT = 8883
    private var client: Mqtt3RxClient? = null
    private var disposables = CompositeDisposable()
    private var mqttLocalDao: MqttLocalDao? = null

    fun <T> init(context: Context, event: MqttEvent<T>? = null, topic: MqttTopics? = null) {
        if (mqttLocalDao == null) {
            mqttLocalDao = AppDatabase.getDatabaseInstance(context).mqttLocalDao()
        }
        if (client != null && client!!.state.isConnected) {
            checkDatabaseForFailedEvents(context)
            return
        }
        val user: User? = Singletons.getCurrentlyLoggedInUser()
        user?.let { u ->
            if (u.terminal_id.isNullOrEmpty()) {
                if (BuildConfig.DEBUG) {
                    Timber.e("Terminal ID Null")
                }
                return@let
            }
            val clientId = "${Build.MODEL}-${u.terminal_id!!}${(10000..999999999).random()}"
            val clientBuilder = MqttClient.builder()
                .identifier(clientId)
                .sslConfig(getMqttClientSSLConfigImpl(context))
                .serverHost(SERVER_HOST)
                .serverPort(PORT)
                .automaticReconnectWithDefaultConfig()
                .addConnectedListener {
                    checkDatabaseForFailedEvents(context)
                    if (BuildConfig.DEBUG) {
                        Timber.e("Client $clientId Connected Successfully to $SERVER_HOST")
                    }
                }.addDisconnectedListener {
                    if (BuildConfig.DEBUG) {
                        Timber.e("Disconnected::cause - ${it.cause} ")
                    }
                }
            client = clientBuilder.useMqttVersion3().buildRx().apply {
//                connect().subscribe { t1, t2 ->
//                    t1?.let {
//                        event?.let {
//                            sendPayload(topic!!, it)
//                        }
//                        Timber.e("Connected:")
//                    }
//                    t2?.let {
//                        Timber.e("Connection Failed")
//                        Timber.e(it)
//                    }
//                }.disposeWith(disposables)
            }
        }
    }

    fun disconnect() {
        Timber.e("disconnecting")
        if (client == null) {
            if (BuildConfig.DEBUG) {
                Timber.e("Client is null or not connected")
            }
            return
        }

        client?.disconnect()?.subscribeOn(Schedulers.io())!!
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (BuildConfig.DEBUG) {
                    Timber.e("Disconnected")
                }
            }, {
                if (BuildConfig.DEBUG) {
                    Timber.e(it)
                }
            }).disposeWith(disposables)
        client = null
        disposables.clear()
    }

    private fun getMqttClientSSLConfigImpl(context: Context): MqttClientSslConfigImpl {
        return MqttClientSslConfigImplBuilder.Default()
            .apply {
                // handshakeTimeout(60, TimeUnit.SECONDS)
                hostnameVerifier { _, _ -> true }
                trustManagerFactory(SSLUtil.getTrustManagerFactory(context))
                keyManagerFactory(SSLUtil.getKeyMangerFactory(context))
            }.build()
    }

    fun <T> sendPayload(
        mqttTopic: MqttTopics,
        event: MqttEvent<T>? = null,
        failedEvent: MqttEventsLocal? = null,
    ) {
        if (event == null && failedEvent == null) {
            if (BuildConfig.DEBUG) {
                Timber.e("Nothing to publish")
            }
            return
        }

        event?.apply {
            if (BuildConfig.DEBUG) {
                Timber.e(event.toString())
            }
            geo = DPrefs.getString(PREF_LAST_LOCATION, "lat:6.5244 long:3.3792")
            if (BuildConfig.DEBUG) {
                Timber.e("Sending to topic: ${mqttTopic.topic}")
            }
            // Timber.e(gson.toJson(event).toString())
        }
        client?.let { client ->
            if (BuildConfig.DEBUG) {
                Timber.e("client state isConnected ${client.state.isConnected}")
                Timber.e("client state isCorD ${client.state.isConnectedOrReconnect}")
            }
            if (client.state.isConnected.not()) {
                if (BuildConfig.DEBUG) {
                    Timber.e("not connected, save")
                }
                val local: MqttEventsLocal? =
                    event?.toLocal(mqttTopic.topic, "client not connected") ?: failedEvent
                local?.let {
                    savePayloadToLocalDatabase(local)
                }
                return@let
            }

            var flowable: Flowable<Mqtt3Publish>? = null
            event?.let {
                flowable = Flowable.just(
                    Mqtt3Publish.builder()
                        .topic(mqttTopic.topic)
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .payload(gson.toJson(event).toByteArray(Charset.forName("UTF-8")))
                        .build(),
                )
            }
            failedEvent?.let { e ->
                flowable = Flowable.just(
                    Mqtt3Publish.builder()
                        .topic(e.topic)
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .payload(e.data.toByteArray(Charset.forName("UTF-8")))
                        .build(),
                )
            }
            client.publish(flowable!!).subscribe(
                {
                    if (it.error.isPresent) {
                        if (BuildConfig.DEBUG) {
                            Timber.e("Error: ${it.error.get().localizedMessage}")
                            Timber.e("There was an error while publishing to topic: ${it.publish.topic}; save")
                        }
                        val failedPublish =
                            String(it.publish.payloadAsBytes, StandardCharsets.UTF_8)
                        savePayloadToLocalDatabase(
                            MqttEventsLocal(
                                it.publish.topic.toString(),
                                failedPublish,
                                "error during publishing",
                            ),
                        )
                    }
                    if (BuildConfig.DEBUG) {
                        Timber.e("Published")
                        Timber.e(String(it.publish.payloadAsBytes, StandardCharsets.UTF_8))
                    }
                },
                {
                    if (BuildConfig.DEBUG) {
                        Timber.e("throwable; save")
                    }
                    val local: MqttEventsLocal? =
                        event?.toLocal(mqttTopic.topic, it.localizedMessage) ?: failedEvent
                    local?.let {
                        savePayloadToLocalDatabase(local)
                    }
                    if (BuildConfig.DEBUG) {
                        Timber.e(it)
                    }
                },
                { Timber.e("Completed") },
            ).disposeWith(disposables)
            return@let
        }
        if (client == null) {
            if (BuildConfig.DEBUG) {
                Timber.e("null, save")
            }
            val local: MqttEventsLocal? =
                event?.toLocal(mqttTopic.topic, "client is null") ?: failedEvent
            local?.let {
                savePayloadToLocalDatabase(it)
            }
        }
    }

    private fun savePayloadToLocalDatabase(mqttEventsLocal: MqttEventsLocal) {
        if (BuildConfig.DEBUG) {
            Timber.e("saving into local")
        }
        mqttLocalDao?.insertNewTransaction(mqttEventsLocal)?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe { t1, t2 ->
                t1?.let {
                    if (BuildConfig.DEBUG) {
                        Timber.e("inserted into local: $it")
                    }
                }
                t2?.let {
                    if (BuildConfig.DEBUG) {
                        Timber.e("did not insert into local: $it")
                    }
                }
            }?.disposeWith(compositeDisposable = disposables)
    }

    private fun checkDatabaseForFailedEvents(context: Context) {
        if (mqttLocalDao == null) {
            mqttLocalDao =
                AppDatabase.getDatabaseInstance(context).mqttLocalDao()
        }
        mqttLocalDao?.apply {
            getLocalEvents().flatMap {
                // NetPosWork.createNotification(context, "Failed Events", "Found ${it.size} failed events", null)
                deleteAllEvents().toSingleDefault(it)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t1, t2 ->
                    t1?.let {
                        it.forEach { localEvent ->
                            if (BuildConfig.DEBUG) {
                                Timber.e(getTopic(localEvent.topic).name)
                            }
                            sendPayload<Nothing>(
                                getTopic(localEvent.topic),
                                failedEvent = localEvent,
                            )
                        }
                    }
                    t2?.let {
                        if (BuildConfig.DEBUG) {
                            Timber.e(it)
                        }
                    }
                }
        }
    }

    private fun getTopic(string: String): MqttTopics {
        MqttTopics.values().forEach {
            if (it.topic == string) {
                return it
            }
        }
        return MqttTopics.TRANSACTIONS
    }
}
