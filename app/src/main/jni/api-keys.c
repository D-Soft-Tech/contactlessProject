#include <jni.h>


JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getAuthUserName(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "qrUser2022");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getAuthPassword(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "Qr3*fgZ(vBcdfP0^%klo51r");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getGetQrBaseUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://getqr.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getSendVerveOtpBaseUrl(JNIEnv *env,
                                                                             jobject thiz) {
    return (*env)->NewStringUTF(env, "https://webpay.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getMerchantId(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "MID635ff140365c5");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getWebViewBaseUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://webpay.netpluspay.com/transactions/requery/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getContactlessExistingBaseUrl(JNIEnv *env,
                                                                                    jobject thiz) {
    return (*env)->NewStringUTF(env, "https://contactless.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getContactlessPaymentWithQrBaseUrl(
        JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://qrapi.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getMpgsTag(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "MPGS-QR");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getNotificationBaseUrl(JNIEnv *env,
                                                                             jobject thiz) {
    return (*env)->NewStringUTF(env, "https://netpos.netpluspay.com/api/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getBaseUrlNetPosMqtt(JNIEnv *env,
                                                                           jobject thiz) {
    return (*env)->NewStringUTF(env, "storm-mqtt.netpluspay.com");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getBaseUrlStormUtilities(JNIEnv *env,
                                                                               jobject thiz) {
    return (*env)->NewStringUTF(env, "https://storm-utilities.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getConfigurationDataIp(JNIEnv *env,
                                                                             jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.18");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getConfigurationDataPort(JNIEnv *env,
                                                                               jobject thiz) {
    return (*env)->NewStringUTF(env, "5016");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getConfigurationDataVendorIp(JNIEnv *env,
                                                                                   jobject thiz) {
    return (*env)->NewStringUTF(env, "192.168.100.68");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getConfigurationDataVendorPort(JNIEnv *env,
                                                                                     jobject thiz) {
    return (*env)->NewStringUTF(env, "3535");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getAppName(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "storm_app");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getAppPassword(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "C0R3MELTDOWN!");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getBaseUrlBills(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://storm.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getMasterPassQr(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://masterpassqr.netpluspay.com/api/v1/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getSmsBaseUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://sms.netpluspay.com");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getZenithBaseUrl(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "https://api.zenith.netpluspay.com/qr/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getStringNotificationBaseUrlNoApi(JNIEnv *env,
                                                                                        jobject thiz) {
    return (*env)->NewStringUTF(env, "https://netpos.netpluspay.com/");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getTextKey1(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "5D25072F04832A2329D93E4F91BA23A2");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getTextKey2(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "86CBCDE3B0A22354853E04521686863D");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getCertKey1(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "5D25072F04832A2329D93E4F91BA23A2");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getCertKey2(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "86CBCDE3B0A22354853E04521686863D");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getEmpsKey1(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "E6891F73948F16C4D6E979D68534D0F4");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getEmpsKey2(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "3D10EF707F98E3543E32B570E9E9AE86");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getPosVasKey1(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "9BF76D3E13ADD67A51549B7C3EB0E3AD");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getPosVasKey2(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "A4BAEC5E31BFD913919262C7A7A76D52");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getTestIp(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "196.6.103.10");
}

JNIEXPORT jstring JNICALL
Java_com_woleapp_netpos_contactless_util_UtilityParam_getTestPort(JNIEnv *env, jobject thiz) {
    return (*env)->NewStringUTF(env, "55533");
}