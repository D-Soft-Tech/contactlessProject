package com.woleapp.netpos.contactless.network

import com.google.gson.JsonObject
import com.woleapp.netpos.contactless.model.*
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountLookUpService {

    @POST("account-lookup")
    fun findAccount(
        @Body accountNumber: AccountNumberLookUpRequest,
        @Query("partnerId") partnerId: String,
        @Query("deviceSerialId") deviceSerialId: String
    ): Single<AccountNumberLookUpResponse>

    @POST("account-lookup")
    fun findAccountForFirstBankUser(
        @Body accountNumber: AccountNumberLookUpRequest,
        @Query("partnerId") partnerId: String,
        @Query("deviceSerialId") deviceSerialId: String
    ): Single<ConfirmOTPResponse>

    @POST("confirm-otp")
    fun confirmOTP(
        @Body confirmOTP: ConfirmOTPRequest, @Query("partnerId") partnerId: String
    ): Single<ConfirmOTPResponse>

    @POST("user/register-existing-user")
    fun registerExistingAccount(
        @Body registerExistingAccountRegisterRequest: ExistingAccountRegisterRequest,
        @Query("partnerId") partnerId: String,
        @Query("deviceSerialId") deviceSerialId: String
    ): Single<ExistingAccountRegisterResponse>


    @POST("user/register-existing-user")
    fun registerExistingAccountForFBN(
        @Body registerExistingAccountRegisterRequest: RegistrationForExistingFBNUsersRequest,
        @Query("partnerId") partnerId: String,
        @Query("deviceSerialId") deviceSerialId: String
    ): Single<ExistingAccountRegisterResponse>


    @GET("get-states")
    fun getStates(): Single<GetStatesResponse>

    @GET("get-bank-branches/{stateId}")
    fun getBranches(
        @Path("stateId") stateId: Int,
        @Query("partnerId") partnerId: String,
        @Query("deviceSerialId") deviceSerialId: String
    ): Single<GetFBNBranchResponse>

    @POST("auth/reset-password-request")
    fun resetPassword(
        @Body payload: JsonObject?,
        @Query("partnerId") partnerId: String,
        @Query("deviceSerialId") deviceSerialId: String
    ): Single<GeneralResponse>

    @POST("auth/confirm-reset-otp")
    fun confirmOTPToSetPassword(
        @Body payload: JsonObject?,
        @Query("partnerId") partnerId: String,
        @Query("deviceSerialId") deviceSerialId: String
    ): Single<GeneralResponse>

    @POST("/auth/new-password")
    fun setNewPassword(
        @Body payload: JsonObject?,
        @Query("partnerId") partnerId: String,
        @Query("deviceSerialId") deviceSerialId: String
    ): Single<GeneralResponse>
}