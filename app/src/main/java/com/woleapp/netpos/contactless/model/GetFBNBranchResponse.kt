package com.woleapp.netpos.contactless.model

data class GetFBNBranchResponse(
    val `data`: FBNBranches,
    val status: Boolean
)