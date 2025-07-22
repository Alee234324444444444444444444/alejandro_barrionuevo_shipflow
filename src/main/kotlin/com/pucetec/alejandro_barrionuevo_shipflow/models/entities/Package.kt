package com.pucetec.alejandro_barrionuevo_shipflow.models.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate


@Entity
@Table(name = "packages")
data class Package(

    @Column(name = "tracking_id", unique = true)
    val trackingId: String,

    @Enumerated(EnumType.STRING)
    val type: PackageType,
    val weight: Float,

    @Column(length = 50)
    val description: String,

    @Column(name = "city_from")
    val cityFrom: String,

    @Column(name = "city_to")
    val cityTo: String,

    @Enumerated(EnumType.STRING)
    var status: Status = Status.PENDING,

    @Column(name = "estimated_delivery_date")
    val estimatedDeliveryDate: LocalDate,

    @OneToMany(mappedBy = "packageEntity", cascade = [CascadeType.ALL])
    val events: List<PackageEvent> = emptyList()

) : BaseEntity()

