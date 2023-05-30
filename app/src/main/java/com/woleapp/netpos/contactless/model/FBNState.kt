package com.woleapp.netpos.contactless.model

data class FBNState(
    val createdAt: String,
    val id: Int,
    val state: String,
    val updatedAt: String
){
    override fun toString(): String {
        return state
    }
}