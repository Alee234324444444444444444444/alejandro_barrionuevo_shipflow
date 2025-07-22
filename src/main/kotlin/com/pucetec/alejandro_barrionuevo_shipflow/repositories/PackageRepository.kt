package com.pucetec.alejandro_barrionuevo_shipflow.repositories

import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.Package
import org.springframework.data.jpa.repository.JpaRepository

interface PackageRepository: JpaRepository<Package, Long>{
    fun findTopByOrderByIdDesc(): Package?
}