package com.woleapp.netpos.contactless.taponphone.mastercard.implementations

import com.mastercard.terminalsdk.listeners.TransactionProcessLogger
import com.woleapp.netpos.contactless.BuildConfig
import timber.log.Timber

class TransactionProcessLoggerImpl(val builder: StringBuilder) : TransactionProcessLogger {
    override fun logCryptoOperations(p0: String?) {
        if (BuildConfig.DEBUG) {
            Timber.e("crypto operation")
        }
        builder.append("CRYPTO OPERATIONS: ").append(p0).append("\n")
        p0?.let {
            Timber.e(it)
        }
    }

    override fun logInternalOperation(p0: String?) {
        if (BuildConfig.DEBUG) {
            Timber.e("internal operation")
        }
        builder.append("INTERNAL OPERATION: ").append(p0).append("\n")
        p0?.let {
            Timber.e(it)
        }
    }

    override fun logVerbose(p0: String?) {
        builder.append("VERBOSE: ").append(p0).append("\n")
        if (BuildConfig.DEBUG) {
            Timber.e("verbose operation")
        }
        p0?.let {
            Timber.v(it)
        }
    }

    override fun logDebug(p0: String?) {
        builder.append("DEBUG: ").append(p0).append("\n")
        if (BuildConfig.DEBUG) {
            Timber.e("debug operation")
        }
        p0?.let {
            if (BuildConfig.DEBUG) {
                Timber.d(it)
            }
        }
    }

    override fun logInfo(p0: String?) {
        builder.append("INFO: ").append(p0).append("\n")
        if (BuildConfig.DEBUG) {
            Timber.e("info operation")
        }
        p0?.let {
            if (BuildConfig.DEBUG) {
                Timber.i(it)
            }
        }
    }

    override fun logApduExchange(p0: String?) {
        builder.append("APDU EXCHANGE: ").append(p0).append("\n")
        if (BuildConfig.DEBUG) {
            Timber.e("apdu }exchange")
        }
        p0?.let {
            if (BuildConfig.DEBUG) {
                Timber.e(it)
            }
        }
    }

    override fun logTlvParsing(p0: String?) {
        builder.append("TLV PARSING: ").append(p0).append("\n")
        if (BuildConfig.DEBUG) {
            Timber.e("tlv p}arsing")
        }
        p0?.let {
            if (BuildConfig.DEBUG) {
                Timber.e(it)
            }
        }
    }

    override fun logStage(p0: String?) {
        builder.append("STAGE: ").append(p0).append("\n")
        if (BuildConfig.DEBUG) {
            Timber.e("stage operation")
        }
        p0?.let {
            if (BuildConfig.DEBUG) {
                Timber.e(it)
            }
        }
    }

    override fun logWarning(p0: String?) {
        builder.append("WARNING: ").append(p0).append("\n")
        if (BuildConfig.DEBUG) {
            Timber.e("warn }operation")
        }
        p0?.let {
            if (BuildConfig.DEBUG) {
                Timber.w(it)
            }
        }
    }

    override fun logError(p0: String?) {
        builder.append("ERROR: ").append(p0).append("\n")
        if (BuildConfig.DEBUG) {
            Timber.e("error} operation")
        }
        p0?.let {
            if (BuildConfig.DEBUG) {
                Timber.e(it)
            }
        }
    }
}
