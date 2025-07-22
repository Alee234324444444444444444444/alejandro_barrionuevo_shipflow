package com.pucetec.alejandro_barrionuevo_shipflow.controllers

import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.PackageRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.UpdateStatusRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageResponse
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageDetailResponse
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.UpdateStatusResponse
import com.pucetec.alejandro_barrionuevo_shipflow.routes.Routes
import com.pucetec.alejandro_barrionuevo_shipflow.services.PackageService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(Routes.PACKAGES)
class PackageController(
    private val packageService: PackageService
) {

    @PostMapping
    fun createPackage(@RequestBody request: PackageRequest): PackageResponse =
        packageService.createPackage(request)

    @GetMapping
    fun listPackages(): List<PackageResponse> =
        packageService.getAllPackages()

    @GetMapping("/{trackingId}")
    fun getPackageByTrackingId(@PathVariable trackingId: String): PackageResponse =
        packageService.getByTrackingId(trackingId)

    @PutMapping("/{trackingId}/status")
    fun updatePackageStatus(
        @PathVariable trackingId: String,
        @RequestBody request: UpdateStatusRequest
    ): UpdateStatusResponse =
        packageService.updateStatusPackage(trackingId, request)

    @GetMapping("/{trackingId}/history")
    fun getPackageHistory(@PathVariable trackingId: String): PackageDetailResponse =
        packageService.getDetailByTrackingId(trackingId)
}
