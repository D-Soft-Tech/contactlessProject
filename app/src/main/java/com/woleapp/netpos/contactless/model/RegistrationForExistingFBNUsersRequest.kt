package com.woleapp.netpos.contactless.model

data class RegistrationForExistingFBNUsersRequest(
    val accountNumber: String,
    val businessAddress: String,
    val businessName: String,
    val contactInformation: String,
    val username: String,
    val password: String,
    val state: String,
    val branch_name: String,
    val phoneNumber: String
)


