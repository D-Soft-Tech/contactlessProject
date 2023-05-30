package com.woleapp.netpos.contactless.network

import com.google.gson.JsonObject
import com.woleapp.netpos.contactless.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactlessRegRepository @Inject constructor(
    private val accountLookUpService: AccountLookUpService
) {
    fun findAccount(accountNumber: String, partnerId: String, deviceSerialId: String) =
        accountLookUpService.findAccount(
            AccountNumberLookUpRequest(accountNumber), partnerId, deviceSerialId
        )

    fun findAccountForFirstBankUser(
        accountNumber: String, partnerId: String, deviceSerialId: String
    ) = accountLookUpService.findAccountForFirstBankUser(
        AccountNumberLookUpRequest(accountNumber), partnerId, deviceSerialId
    )

    fun confirmOTP(phoneNumber: String, accountNumber: String, otp: String, partnerId: String) =
        accountLookUpService.confirmOTP(
            ConfirmOTPRequest(phoneNumber, accountNumber, otp), partnerId
        )

    fun registerExistingAccount(
        existingAccountRegisterRequest: ExistingAccountRegisterRequest,
        partnerId: String,
        deviceSerialId: String
    ) = accountLookUpService.registerExistingAccount(
        existingAccountRegisterRequest, partnerId, deviceSerialId
    )

    fun registerExistingAccountForFBN(
        existingAccountRegisterRequest: RegistrationForExistingFBNUsersRequest,
        partnerId: String,
        deviceSerialId: String
    ) = accountLookUpService.registerExistingAccountForFBN(
        existingAccountRegisterRequest, partnerId, deviceSerialId
    )

    fun getBranches(
        stateId: Int, partnerId: String, deviceSerialId: String
    ) = accountLookUpService.getBranches(stateId, partnerId, deviceSerialId)

    fun getStates() = accountLookUpService.getStates()

    fun resetPassword(payload: JsonObject, partnerId: String, deviceSerialId: String
    ) = accountLookUpService.resetPassword(payload, partnerId, deviceSerialId)

    fun confirmOTPToSetPassword(payload: JsonObject, partnerId: String, deviceSerialId: String
    ) = accountLookUpService.confirmOTPToSetPassword(payload, partnerId, deviceSerialId)

    fun setNewPassword(payload: JsonObject, partnerId: String, deviceSerialId: String
    ) = accountLookUpService.setNewPassword(payload, partnerId, deviceSerialId)

}