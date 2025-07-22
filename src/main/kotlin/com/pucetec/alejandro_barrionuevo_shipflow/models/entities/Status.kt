package com.pucetec.alejandro_barrionuevo_shipflow.models.entities

enum class Status(val statusName: String) {
    PENDING("PENDING"),
    IN_TRANSIT("IN_TRANSIT"),
    DELIVERED("DELIVERED"),
    ON_HOLD("ON_HOLD"),
    CANCELLED("CANCELLED")
}