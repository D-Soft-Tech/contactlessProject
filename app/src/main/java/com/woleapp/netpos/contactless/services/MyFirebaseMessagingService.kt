package com.woleapp.netpos.contactless.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.model.FirebaseNotificationModelResponse
import com.woleapp.netpos.contactless.ui.activities.MainActivity
import com.woleapp.netpos.contactless.util.AppConstants.INT_FIREBASE_PENDING_INTENT_REQUEST_CODE
import com.woleapp.netpos.contactless.util.AppConstants.MERCHANT_QR_PREFIX
import com.woleapp.netpos.contactless.util.AppConstants.STRING_FIREBASE_INTENT_ACTION
import com.woleapp.netpos.contactless.util.AppConstants.TAG_NOTIFICATION_RECEIVED_FROM_BACKEND
import com.woleapp.netpos.contactless.util.AppConstants.WORKER_INPUT_FIREBASE_DEVICE_TOKEN_TAG
import com.woleapp.netpos.contactless.util.AppConstants.WORKER_INPUT_PBT_TRANSACTION_TAG
import com.woleapp.netpos.contactless.util.Mappers.mapToTransactionResponse
import com.woleapp.netpos.contactless.util.RandomPurposeUtil.formatCurrencyAmountUsingCurrentModule
import com.woleapp.netpos.contactless.util.Singletons.gson
import com.woleapp.netpos.contactless.worker.RegisterDeviceTokenToBackendOnTokenChangeWorker
import com.woleapp.netpos.contactless.worker.SaveTransactionFromFirebaseMessagingServiceToDbWorker
import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        sendTokenToServer(token)
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.data.toString().isNotEmpty()) {
            val transactionNotificationFromFirebase = remoteMessage.data["TransactionNotification"]
            if (BuildConfig.DEBUG) {
                Timber.tag("INCOMING_M").d(gson.toJson(transactionNotificationFromFirebase))
            }
            val temporalTransaction: FirebaseNotificationModelResponse =
                gson.fromJson(
                    transactionNotificationFromFirebase,
                    FirebaseNotificationModelResponse::class.java,
                )
            val transaction = gson.toJson(
                temporalTransaction.copy(
                    email = temporalTransaction.email.removePrefix(MERCHANT_QR_PREFIX),
                ).mapToTransactionResponse(),
            )

            if (temporalTransaction.email.contains(MERCHANT_QR_PREFIX)) {
                transaction?.let {
                    scheduleJobToSaveTransactionToDatabase(it)
                }
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.action = STRING_FIREBASE_INTENT_ACTION
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(TAG_NOTIFICATION_RECEIVED_FROM_BACKEND, true)
            this.startActivity(intent)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        remoteMessage.data["TransactionNotification"]?.let {
            val transactionNotificationFromFirebase = remoteMessage.data["TransactionNotification"]
            val temporalTransaction: FirebaseNotificationModelResponse =
                gson.fromJson(
                    transactionNotificationFromFirebase,
                    FirebaseNotificationModelResponse::class.java,
                )

            if (temporalTransaction.email.contains(MERCHANT_QR_PREFIX)) {
                sendNotification(
                    getString(
                        R.string.notification_message_body,
                        temporalTransaction.amount.toDouble()
                            .formatCurrencyAmountUsingCurrentModule(),
                        temporalTransaction.status,
                        temporalTransaction.customerName,
                        temporalTransaction.maskedPan,
                    ),
                )
            }
        }
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = STRING_FIREBASE_INTENT_ACTION
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(TAG_NOTIFICATION_RECEIVED_FROM_BACKEND, true)
        val pendingIntent = PendingIntent.getActivity(
            this,
            INT_FIREBASE_PENDING_INTENT_REQUEST_CODE, /* Request code */
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.transacion_received))
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setSmallIcon(R.drawable.ic_netpos_logo)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.transacion_received),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    private fun sendTokenToServer(token: String) {
        scheduleJobToSendTokenToServer(token) // This method schedules job to send new token to the server
    }

    private fun scheduleJobToSaveTransactionToDatabase(transactionFromFireBaseInStringFormat: String) {
        val inputData: Data = Data.Builder()
            .putString(WORKER_INPUT_PBT_TRANSACTION_TAG, transactionFromFireBaseInStringFormat)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work =
            OneTimeWorkRequest.Builder(SaveTransactionFromFirebaseMessagingServiceToDbWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        WorkManager.getInstance(this)
            .beginWith(work)
            .enqueue()
    }

    private fun scheduleJobToSendTokenToServer(newToken: String) {
        val inputData = Data.Builder()
            .putString(WORKER_INPUT_FIREBASE_DEVICE_TOKEN_TAG, newToken)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work =
            OneTimeWorkRequest.Builder(RegisterDeviceTokenToBackendOnTokenChangeWorker::class.java)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        WorkManager.getInstance(this)
            .beginWith(work)
            .enqueue()
    }
}
