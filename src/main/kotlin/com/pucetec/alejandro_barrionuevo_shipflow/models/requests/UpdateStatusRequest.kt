package com.pucetec.alejandro_barrionuevo_shipflow.models.requests

data class UpdateStatusRequest(
    val status: String,
    val comment: String? = null
)

