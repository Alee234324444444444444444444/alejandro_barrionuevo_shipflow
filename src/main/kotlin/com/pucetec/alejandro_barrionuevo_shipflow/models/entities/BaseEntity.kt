package com.pucetec.alejandro_barrionuevo_shipflow.models.entities

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long = 0

    @Column(name = "created_at")
    open val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at")
    open var updatedAt: LocalDateTime = LocalDateTime.now()

    @PreUpdate
    open protected fun update() {
        updatedAt = LocalDateTime.now()
    }
}