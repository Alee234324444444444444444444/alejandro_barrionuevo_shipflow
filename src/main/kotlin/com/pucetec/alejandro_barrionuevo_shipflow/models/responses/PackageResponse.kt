package com.pucetec.alejandro_barrionuevo_shipflow.models.responses

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

data class PackageResponse(
    val id: Long,

    @JsonProperty("created_at")
    val createdAt: LocalDateTime,

    @JsonProperty("tracking_id")
    val trackingId: String,

    val type: String,
    val weight: Float,
    val description: String,

    @JsonProperty("city_from")
    val cityFrom: String,

    @JsonProperty("city_to")
    val cityTo: String,

    val status: String,

    @JsonProperty("estimated_delivery_date")
    val estimatedDeliveryDate: LocalDate,
)