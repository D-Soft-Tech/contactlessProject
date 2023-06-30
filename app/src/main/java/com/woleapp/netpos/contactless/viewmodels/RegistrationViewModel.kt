package com.woleapp.netpos.contactless.viewmodels

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.barteksc.pdfviewer.PDFView
import com.woleapp.netpos.contactless.BuildConfig
import com.woleapp.netpos.contactless.R
import com.woleapp.netpos.contactless.model.RegistrationError
import com.woleapp.netpos.contactless.model.RegistrationFBNModel
import com.woleapp.netpos.contactless.model.RegistrationModel
import com.woleapp.netpos.contactless.model.RegistrationZenithModel
import com.woleapp.netpos.contactless.network.ContactlessClient
import com.woleapp.netpos.contactless.util.Event
import com.woleapp.netpos.contactless.util.Singletons
import com.woleapp.netpos.contactless.util.disposeWith
import com.woleapp.netpos.contactless.util.getResponseBody
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RegistrationViewModel : ViewModel() {

    val authInProgress = MutableLiveData(false)
    private val contactlessService = ContactlessClient.getContactlessService()
    private val disposable = CompositeDisposable()

    val registrationModel: MutableLiveData<RegistrationModel> = MutableLiveData(
        RegistrationModel(),
    )
    val registrationFBNModel: MutableLiveData<RegistrationFBNModel> = MutableLiveData(
        RegistrationFBNModel(),
    )
    val registrationZenithModel: MutableLiveData<RegistrationZenithModel> = MutableLiveData(
        RegistrationZenithModel(),
    )
    private val _message = MutableLiveData<Event<String>>()
    private val _authDone = MutableLiveData<Event<Boolean>>()
    val authDone: LiveData<Event<Boolean>>
        get() = _authDone

    val message: LiveData<Event<String>>
        get() = _message

    fun register(context: Context, bank: String, deviceSerialId: String) {
        if (
            BuildConfig.FLAVOR.contains("providuspos") || BuildConfig.FLAVOR.contains("providus") || BuildConfig.FLAVOR.contains(
                "providussoftpos",
            ) ||
            BuildConfig.FLAVOR.contains("fcmbeasypay") ||
            BuildConfig.FLAVOR.contains("easypayfcmb") ||
            BuildConfig.FLAVOR.contains("easypay")
        ) {
//            activity?.getFragmentManager()?.popBackStack()
            if (registrationModel.value?.allFieldsFilled() == false) {
                _message.value = Event("All fields are required")
                return
            } else {
                val dialogView: View = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_terms_and_conditions, null)
                val dialogBuilder: AlertDialog.Builder =
                    AlertDialog.Builder(context)
                dialogBuilder.setView(dialogView)

                val alertDialog: AlertDialog = dialogBuilder.create()
                alertDialog.show()
                if (BuildConfig.FLAVOR.contains("providuspos") || BuildConfig.FLAVOR.contains("providus") || BuildConfig.FLAVOR.contains(
                        "providussoftpos",
                    )
                ) {
                    dialogView.findViewById<PDFView>(R.id.pdf).fromAsset("providus.pdf").load()
                } else if (BuildConfig.FLAVOR.contains("easypay")) {
                    dialogView.findViewById<PDFView>(R.id.pdf).fromAsset("qlick.pdf").load()
                } else if (BuildConfig.FLAVOR.contains("fcmbeasypay")) {
                    dialogView.findViewById<PDFView>(R.id.pdf).fromAsset("qlick.pdf").load()
                } else if (BuildConfig.FLAVOR.contains("easypayfcmb")) {
                    dialogView.findViewById<PDFView>(R.id.pdf).fromAsset("qlick.pdf").load()
                }
                dialogView.findViewById<Button>(R.id.accept_button).setOnClickListener {
                    alertDialog.dismiss()
                    reg(bank, deviceSerialId)
                }
            }
        } else {
            if (BuildConfig.FLAVOR.contains("firstbank")) {
                if (registrationFBNModel.value?.allFieldsFilledFBN() == false) {
                    _message.value = Event("All fields are required")
                    return
                } else {
                    regFBN(bank, deviceSerialId)
                }
            } else if (BuildConfig.FLAVOR.contains("zenith")) {
                if (registrationZenithModel.value?.allFieldsFilledZenith() == false) {
                    _message.value = Event("All fields are required")
                    return
                } else {
                    regZenith(bank, deviceSerialId)
                }
            } else {
                if (registrationModel.value?.allFieldsFilled() == false) {
                    _message.value = Event("All fields are required")
                    return
                } else {
                    reg(bank, deviceSerialId)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    fun setSelectedState(data: String) {
        registrationFBNModel.value = registrationFBNModel.value?.apply {
            state = data
            if (BuildConfig.DEBUG) {
                Log.d("STATEFBN", data)
            }
        }
    }

    fun setSelectedBranch(data: String) {
        registrationFBNModel.value = registrationFBNModel.value?.apply {
            branch_name = data
        }
    }

    private fun reg(bank: String, deviceSerialId: String) {
        authInProgress.value = true
        contactlessService.register(registrationModel.value, bank, deviceSerialId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                authInProgress.value = false
            }
            .subscribe { t1, t2 ->
                t1?.let {
                    registrationModel.value = RegistrationModel()
                    _authDone.value = Event(true)
                }
                t2?.let {
                    val errorMessage = try {
                        Singletons.gson.fromJson(
                            it.getResponseBody(),
                            RegistrationError::class.java,
                        ).message
                    } catch (e: Exception) {
                        "an error occurred during registration, try again or contact support"
                    }
                    _message.value =
                        Event(errorMessage)
                }
            }.disposeWith(disposable)
    }

    private fun regFBN(bank: String, deviceSerialId: String) {
        authInProgress.value = true
        contactlessService.registerFBN(registrationFBNModel.value, bank, deviceSerialId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                authInProgress.value = false
            }
            .subscribe { t1, t2 ->
                t1?.let {
                    registrationFBNModel.value = RegistrationFBNModel()
                    _authDone.value = Event(true)
                }
                t2?.let {
                    val errorMessage = try {
                        Singletons.gson.fromJson(
                            it.getResponseBody(),
                            RegistrationError::class.java,
                        ).message
                    } catch (e: Exception) {
                        "an error occurred during registration, try again or contact support"
                    }
                    _message.value =
                        Event(errorMessage)
                }
            }.disposeWith(disposable)
    }

    private fun regZenith(bank: String, deviceSerialId: String) {
        authInProgress.value = true
        contactlessService.registerExistingAccountForZenith(
            registrationZenithModel.value,
            bank,
            deviceSerialId,
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                authInProgress.value = false
            }
            .subscribe { t1, t2 ->
                t1?.let {
                    registrationZenithModel.value = RegistrationZenithModel()
                    _authDone.value = Event(true)
                }
                t2?.let {
                    val errorMessage = try {
                        Singletons.gson.fromJson(
                            it.getResponseBody(),
                            RegistrationError::class.java,
                        ).message
                    } catch (e: Exception) {
                        "an error occurred during registration, try again or contact support"
                    }
                    _message.value =
                        Event(errorMessage)
                }
            }.disposeWith(disposable)
    }
}
