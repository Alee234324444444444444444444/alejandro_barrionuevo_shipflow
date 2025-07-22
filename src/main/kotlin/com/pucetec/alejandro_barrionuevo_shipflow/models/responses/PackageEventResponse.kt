package com.pucetec.alejandro_barrionuevo_shipflow.models.responses

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class PackageEventResponse(

    var id: Long = 0,

    @JsonProperty("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    val status: String,
    val comment: String? = null,
)
