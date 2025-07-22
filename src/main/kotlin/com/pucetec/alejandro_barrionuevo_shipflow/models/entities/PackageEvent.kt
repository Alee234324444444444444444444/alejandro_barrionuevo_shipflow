package com.pucetec.alejandro_barrionuevo_shipflow.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table


@Entity
@Table(name = "package_events")
data class PackageEvent(

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    val status: Status,

    @Column(name = "comment", length = 255)
    val comment: String? = null,

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    val packageEntity: Package

) : BaseEntity()



