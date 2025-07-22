package com.pucetec.alejandro_barrionuevo_shipflow.mappers

import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.Package
import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.PackageEvent
import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.Status
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.UpdateStatusRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageEventResponse
import org.springframework.stereotype.Component

@Component
class PackageEventMapper {

    fun toResponse(entity: PackageEvent): PackageEventResponse {
        return PackageEventResponse(
            id = entity.id,
            updatedAt = entity.updatedAt,
            status = entity.status.statusName,
            comment = entity.comment
        )
    }

    fun toEntity(request: UpdateStatusRequest, packageEntity: Package): PackageEvent {
        return PackageEvent(
            status = Status.valueOf(request.status),
            comment = request.comment,
            packageEntity = packageEntity
        )
    }
}
