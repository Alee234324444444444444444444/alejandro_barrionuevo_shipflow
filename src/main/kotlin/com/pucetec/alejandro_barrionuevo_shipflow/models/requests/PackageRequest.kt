package com.pucetec.alejandro_barrionuevo_shipflow.models.requests

import com.fasterxml.jackson.annotation.JsonProperty


data class PackageRequest(
    val type: String,
    val weight: Float,
    val description: String,

    @JsonProperty("city_from")
    val cityFrom: String,

    @JsonProperty("city_to")
    val cityTo: String,
    )

