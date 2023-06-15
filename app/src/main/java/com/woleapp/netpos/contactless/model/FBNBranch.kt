package com.woleapp.netpos.contactless.model

data class FBNBranch(
    val address: String,
    val branch_name: String,
    val createdAt: String,
    val id: Int,
    val partner_id: String,
    val state_id: Int,
    val updatedAt: String
){
    override fun toString(): String {
        return branch_name
    }
}