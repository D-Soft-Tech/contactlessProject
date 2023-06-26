package com.woleapp.netpos.contactless.model

import com.woleapp.netpos.contactless.util.validatePasswordMismatch

data class RegistrationZenithModel(
    var Address: String,
    var Bvn: String,
    var DateOfBirth: String,
    var EmailAddress: String,
    var FirstName: String,
    var LastName: String,
    var MobileNumber: String,
    var Password: String,
    var Reference: String,
) {
    constructor() : this("", "", "", "", "", "", "", "", "")


    fun allFieldsFilledZenith() =
        Address.isNotEmpty() && Bvn.isNotEmpty() && DateOfBirth.isNotEmpty() && EmailAddress.isNotEmpty() &&
                FirstName.isNotEmpty() && LastName.isNotEmpty() && MobileNumber.isNotEmpty() && Password.isNotEmpty()
                && Reference.isNotEmpty()

}