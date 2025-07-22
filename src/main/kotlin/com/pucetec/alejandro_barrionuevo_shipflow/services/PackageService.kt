package com.pucetec.alejandro_barrionuevo_shipflow.services

import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.*
import com.pucetec.alejandro_barrionuevo_shipflow.mappers.PackageEventMapper
import com.pucetec.alejandro_barrionuevo_shipflow.mappers.PackageMapper
import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.PackageType
import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.Status
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.PackageRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.UpdateStatusRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageDetailResponse
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageResponse
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.UpdateStatusResponse
import com.pucetec.alejandro_barrionuevo_shipflow.repositories.PackageEventRepository
import com.pucetec.alejandro_barrionuevo_shipflow.repositories.PackageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
@Transactional
class PackageService(
    private val packageRepository: PackageRepository,
    private val packageEventRepository: PackageEventRepository,
    private val packageMapper: PackageMapper,
    private val eventMapper: PackageEventMapper
) {

    fun createPackage(request: PackageRequest): PackageResponse {
        if (request.cityFrom.trim().equals(request.cityTo.trim(), ignoreCase = true)) {
            throw InvalidCityException("Origin and destination cities cannot be the same.")
        }

        if (request.description.length > 50) {
            throw DescriptionTooLongException("Description must not exceed 50 characters.")
        }

        val packageType = PackageType.entries.find { it.name == request.type.uppercase() }
            ?: throw InvalidTypeException("Invalid package type: ${request.type}. Allowed values are: DOCUMENT, SMALL_BOX, FRAGILE.")

        val lastTrackingId = packageRepository.findAll()
            .mapNotNull { it.trackingId.toLongOrNull() }
            .maxOrNull() ?: 0L

        val trackingId = (lastTrackingId + 1).toString()
        val packageEntity = packageMapper.toEntity(request, trackingId, packageType)
        val savedPackage = packageRepository.save(packageEntity)

        val initialEvent = eventMapper.toEntity(
            UpdateStatusRequest(Status.PENDING.name, "Package registered and pending processing"),
            savedPackage
        )
        packageEventRepository.save(initialEvent)

        return packageMapper.toResponse(savedPackage)
    }

    fun getAllPackages(): List<PackageResponse> =
        packageRepository.findAll().map { packageMapper.toResponse(it) }

    fun getByTrackingId(trackingId: String): PackageResponse {
        val packageEntity = packageRepository.findAll().find { it.trackingId == trackingId }
            ?: throw PackageNotFoundException("Package with tracking ID '$trackingId' not found.")

        return packageMapper.toResponse(packageEntity)
    }

    fun getDetailByTrackingId(trackingId: String): PackageDetailResponse {
        val packageEntity = packageRepository.findAll().find { it.trackingId == trackingId }
            ?: throw PackageNotFoundException("Package with tracking ID '$trackingId' not found.")

        return packageMapper.toDetailResponse(packageEntity, eventMapper)
    }

    fun updateStatusPackage(trackingId: String, request: UpdateStatusRequest): UpdateStatusResponse {
        val packageEntity = packageRepository.findAll().find { it.trackingId == trackingId }
            ?: throw PackageNotFoundException("Package with tracking ID '$trackingId' not found.")

        val newStatus = Status.entries.find { it.name == request.status }
            ?: throw InvalidStatusException("Invalid status: ${request.status}. Allowed values are: PENDING, IN_TRANSIT, DELIVERED, ON_HOLD, CANCELLED.")

        if (!isValidTransition(packageEntity.status, newStatus)) {
            throw InvalidStatusTransitionException("Cannot change status from ${packageEntity.status} to $newStatus.")
        }

        if (newStatus == Status.DELIVERED) {
            val hasBeenInTransit = packageEntity.events.any { it.status == Status.IN_TRANSIT }
            if (!hasBeenInTransit) {
                throw BusinessRuleException("Package can only be marked as DELIVERED if it has previously been IN_TRANSIT.")
            }
        }


        packageEntity.status = newStatus
        packageRepository.save(packageEntity)

        val event = eventMapper.toEntity(
            UpdateStatusRequest(newStatus.name, request.comment),
            packageEntity
        )
        packageEventRepository.save(event)

        return UpdateStatusResponse(
            message = "Status updated successfully.",
            trackingId = trackingId,
            newStatus = newStatus.name,
            updatedAt = LocalDateTime.now()
        )
    }


    private fun isValidTransition(current: Status, next: Status): Boolean {
        return when (current) {
            Status.PENDING -> next == Status.IN_TRANSIT
            Status.IN_TRANSIT -> next == Status.DELIVERED || next == Status.ON_HOLD || next == Status.CANCELLED
            Status.ON_HOLD -> next == Status.IN_TRANSIT || next == Status.CANCELLED
            Status.DELIVERED, Status.CANCELLED -> false
        }
    }
}
