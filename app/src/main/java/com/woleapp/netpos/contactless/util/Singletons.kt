package com.woleapp.netpos.contactless.util

import com.danbamitale.epmslib.entities.*
import com.dsofttech.dprefs.utils.DPrefs
import com.google.gson.Gson
import com.woleapp.netpos.contactless.model.ConfigurationData
import com.woleapp.netpos.contactless.model.NibssResponse
import com.woleapp.netpos.contactless.model.User

fun useStormTerminalId() = DPrefs.getBoolean(PREF_USE_STORM_TERMINAL_ID, true)
fun TransactionResponse.toNibssResponse(remark: String? = null): NibssResponse =
    Singletons.gson.fromJson(
        Singletons.gson.toJson(this),
        NibssResponse::class.java,
    ).also {
        it.responseMessage = try {
            this.responseMessage
        } catch (e: Exception) {
            ""
        }
        it.additionalAmount = this.additionalAmount_54.toLongOrNull() ?: 0
        it.localDate = this.localDate_13
        it.localTime = this.localTime_12
        it.amount = this.amount.div(100)
        remark?.let { r ->
            it.remark = r
        }
    }

object Singletons {
    fun setUseStormTid(useStormTid: Boolean) =
        DPrefs.putBoolean(PREF_USE_STORM_TERMINAL_ID, useStormTid)

    val gson = Gson()
    fun getCurrentlyLoggedInUser(): User? =
        gson.fromJson(DPrefs.getString(PREF_USER, ""), User::class.java)

    fun getNetPlusPayMid(): String =
        getCurrentlyLoggedInUser()?.netplusPayMid ?: UtilityParam.STRING_MERCHANT_ID

    fun getSavedConfigurationData(): ConfigurationData {
        return ConfigurationData(
            UtilityParam.CONFIGURATION_DATA_IP,
            UtilityParam.CONFIGURATION_DATA_PORT,
            UtilityParam.CERT_KEY_1,
            UtilityParam.CERT_KEY_2,
        )
    }

    fun getConfigDataForTest(): ConnectionData {
        return ConnectionData(
            UtilityParam.TEST_IP,
            UtilityParam.TEST_PORT.toInt(),
            true,
        )
    }

    private val keyHolder = if (DPrefs.getString(PREF_KEYHOLDER).isNotEmpty()) {
        DPrefs.getString(
            PREF_KEYHOLDER,
        )
    } else {
        null
    }
    val configData = if (DPrefs.getString(PREF_CONFIG_DATA).isNotEmpty()) {
        DPrefs.getString(
            PREF_KEYHOLDER,
        )
    } else {
        null
    }

    fun getKeyHolder(): KeyHolder? =
        gson.fromJson(keyHolder, KeyHolder::class.java)

    fun getConfigData(): ConfigData? =
        gson.fromJson(configData, ConfigData::class.java)
}

var TransactionResponse.additionalAmount: Long?
    get() = 0
    set(value) {
    }
