package com.woleapp.netpos.contactless.app

import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import com.dsofttech.dprefs.utils.DPrefs
import com.mastercard.terminalsdk.ConfigurationInterface
import com.mastercard.terminalsdk.TerminalSdk
import com.mastercard.terminalsdk.TransactionInterface
import com.pixplicity.easyprefs.library.Prefs
import com.visa.app.ttpkernel.ContactlessConfiguration
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.app.loggers.DebugTree
import com.woleapp.netpos.contactless.app.loggers.ReleaseTree
import com.woleapp.netpos.contactless.taponphone.mastercard.implementations.* // ktlint-disable no-wildcard-imports
import com.woleapp.netpos.contactless.taponphone.mastercard.implementations.TransactionProcessLoggerImpl
import com.woleapp.netpos.contactless.taponphone.mastercard.implementations.nfc.NfcProvider
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NetPosApp : Application() {

    lateinit var transactionsApi: TransactionInterface
    lateinit var outcomeObserver: OutcomeObserver
    private lateinit var configuration: ConfigurationInterface
    lateinit var builder: StringBuilder
    lateinit var nfcProvider: NfcProvider

    companion object {
        lateinit var INSTANCE: NetPosApp

        fun assignInstance(instance: NetPosApp) {
            INSTANCE = instance
        }
    }

    val terminalSdk: TerminalSdk = TerminalSdk.getInstance()

    override fun onCreate() {
        super.onCreate()
        assignInstance(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        DPrefs.initializeDPrefs(applicationContext)
        initVisaLib()
    }

    private fun initVisaLib() {
        val contactlessConfiguration = ContactlessConfiguration.getInstance()
        val myData = contactlessConfiguration.terminalData
        myData.forEach {
            if (BuildConfig.DEBUG) {
                Timber.e(it.key)
            }
        }
        myData["9F1A"] = byteArrayOf(0x05, 0x66) // set terminal country code
        myData["5F2A"] = byteArrayOf(0x05, 0x66) // set currency code
        myData["9F35"] = byteArrayOf(0x22) // Terminal Type
        myData["009C"] = byteArrayOf(0x00) // Transaction Type 00 - Purchase; 20 - Refund
        myData["9F09"] = byteArrayOf(0x00, 0x8C.toByte())
        myData["9F66"] =
            byteArrayOf(0xE6.toByte(), 0x00.toByte(), 0x40.toByte(), 0x00.toByte()) // TTQ E6004000
        myData["9F33"] =
            byteArrayOf(0xE0.toByte(), 0xF8.toByte(), 0xC8.toByte()) // Terminal Capabilities
        myData["9F40"] = byteArrayOf(
            0x60.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x50.toByte(),
            0x01.toByte(),
        ) // Additional Terminal Capabilities
        val mercahnt = "NetPOS Contactless"
        val merchantByte = mercahnt.toByteArray()
        myData["9F4E"] = merchantByte // Merchant Name and location
        myData["9F1B"] = byteArrayOf(0x00, 0x00, 0x00, 0x00) // terminal floor limit
        myData["DF01"] = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x01) // Reader CVM Required Limit
        contactlessConfiguration.terminalData = myData
    }

    fun initMposLibrary(context: Activity) {
        outcomeObserver =
            OutcomeObserver()
        configuration = terminalSdk.configuration
        builder = StringBuilder()
        nfcProvider =
            NfcProvider(
                context,
            )
        val cardCommProviderStub =
            CardCommProviderStub()
        val logger = TransactionProcessLoggerImpl(builder)
        configuration
            .withResourceProvider(
                ResourceProviderImplementation(
                    this.applicationContext,
                ),
            )
            .withLogger(logger)
            .withCardCommunication(nfcProvider, cardCommProviderStub)
            .withTransactionObserver(outcomeObserver)
            .withUnpredictableNumberProvider(UnpredictableNumberImplementation())
            .withMessageDisplayProvider(
                DisplayImplementation(
                    logger,
                ),
            )
        transactionsApi = configuration.initializeLibrary()
        configuration.selectProfile("MPOS")
    }
}
