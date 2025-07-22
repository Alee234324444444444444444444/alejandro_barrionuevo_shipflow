package com.pucetec.alejandro_barrionuevo_shipflow.mappers

import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.Package
import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.PackageType
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.PackageRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageResponse
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageDetailResponse
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class PackageMapper {

    fun toEntity(request: PackageRequest, trackingId: String, packageType: PackageType): Package {
        return Package(
            trackingId = trackingId,
            type = packageType,
            weight = request.weight,
            description = request.description,
            cityFrom = request.cityFrom,
            cityTo = request.cityTo,
            estimatedDeliveryDate = LocalDate.now().plusDays(5)
        )
    }

    fun toResponse(entity: Package): PackageResponse {
        return PackageResponse(
            id = entity.id,
            trackingId = entity.trackingId,
            type = entity.type.name,
            description = entity.description,
            weight = entity.weight,
            status = entity.status.statusName,
            cityFrom = entity.cityFrom,
            cityTo = entity.cityTo,
            createdAt = entity.createdAt,
            estimatedDeliveryDate = entity.estimatedDeliveryDate
        )
    }

    fun toDetailResponse(entity: Package, eventMapper: PackageEventMapper): PackageDetailResponse {
        return PackageDetailResponse(
            packageInfo = toResponse(entity),
            eventHistory = entity.events.map { eventMapper.toResponse(it) }
        )
    }
}
