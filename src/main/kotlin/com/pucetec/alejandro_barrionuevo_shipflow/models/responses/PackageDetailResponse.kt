package com.pucetec.alejandro_barrionuevo_shipflow.models.responses

data class PackageDetailResponse(
    val packageInfo: PackageResponse,
    val eventHistory: List<PackageEventResponse>
)

