package com.woleapp.netpos.contactless.util

object UtilityParam {

    init {
        System.loadLibrary("api-keys")
    }

    private external fun getAuthUserName(): String
    private external fun getAuthPassword(): String
    private external fun getGetQrBaseUrl(): String
    private external fun getSendVerveOtpBaseUrl(): String
    private external fun getMerchantId(): String
    private external fun getWebViewBaseUrl(): String
    private external fun getContactlessExistingBaseUrl(): String
    private external fun getContactlessPaymentWithQrBaseUrl(): String
    private external fun getNotificationBaseUrl(): String
    private external fun getMpgsTag(): String
    private external fun getBaseUrlNetPosMqtt(): String
    private external fun getBaseUrlStormUtilities(): String
    private external fun getConfigurationDataIp(): String
    private external fun getConfigurationDataPort(): String
    private external fun getConfigurationDataVendorIp(): String
    private external fun getConfigurationDataVendorPort(): String
    private external fun getAppName(): String
    private external fun getAppPassword(): String
    private external fun getBaseUrlBills(): String
    private external fun getMasterPassQr(): String
    private external fun getSmsBaseUrl(): String
    private external fun getZenithBaseUrl(): String
    private external fun getStringNotificationBaseUrlNoApi(): String

    // Test Config Data
    private external fun getTestIp(): String
    private external fun getTestPort(): String

    // Test Keys
    private external fun getTextKey1(): String
    private external fun getTextKey2(): String

    // Cert Keys
    private external fun getCertKey1(): String
    private external fun getCertKey2(): String

    // Epms keys
    private external fun getEmpsKey1(): String
    private external fun getEmpsKey2(): String

    // Pos Vas Keys
    private external fun getPosVasKey1(): String
    private external fun getPosVasKey2(): String

    val STRING_NOTIFICATION_BASE_URL_NO_API = getStringNotificationBaseUrlNoApi()
    val ZENITH_BASE_URL = getZenithBaseUrl()
    val SMS_BASE_URL = getSmsBaseUrl()
    val MASTER_PASS_QR = getMasterPassQr()
    val BASE_URL_BILLS = getBaseUrlBills()
    val STRING_AUTH_USER_NAME = getAuthUserName()
    val STRING_AUTH_PASSWORD = getAuthPassword()
    val STRING_GET_QR_BASE_URL = getGetQrBaseUrl()
    val STRING_SEND_VERVE_OTP_BASE_URL = getSendVerveOtpBaseUrl()
    val STRING_MERCHANT_ID = getMerchantId()
    val STRING_WEB_VIEW_BASE_URL = getWebViewBaseUrl()
    val STRING_CONTACTLESS_EXISTING_BASE_URL = getContactlessExistingBaseUrl()
    val STRING_CONTACTLESS_PAYMENT_WITH_QR_BASE_URL = getContactlessPaymentWithQrBaseUrl()
    val STRING_NOTIFICATION_BASE_URL = getNotificationBaseUrl()
    val STRING_MPGS_TAG = getMpgsTag()
    val BASE_URL_NETPOS_MQTT = getBaseUrlNetPosMqtt()
    val BASE_URL_STORM_UTILITIES = getBaseUrlStormUtilities()
    val CONFIGURATION_DATA_IP = getConfigurationDataIp()
    val CONFIGURATION_DATA_PORT = getConfigurationDataPort()
    val VEND_IP = getConfigurationDataVendorIp()
    val VEND_PORT = getConfigurationDataPort()
    val APP_NAME = getAppName()
    val APP_PASSWORD = getAppPassword()

    // Test config data
    val TEST_IP = getTestIp()
    val TEST_PORT = getTestPort()

    // Test Keys
    val TEST_KEY_1 = getTextKey1()
    val TEST_KEY_2 = getTextKey2()

    // Cert Keys
    val CERT_KEY_1 = getCertKey1()
    val CERT_KEY_2 = getCertKey2()

    // EPMS Keys
    val EPMS_KEY_1 = getEmpsKey1()
    val EPMS_KEY_2 = getEmpsKey2()

    // Pos Vas Keys
    val POS_VAS_KEY_1 = getPosVasKey1()
    val POS_VAS_KEY_2 = getPosVasKey2()
}
