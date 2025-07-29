package com.pucetec.alejandro_barrionuevo_shipflow.services

import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.*
import com.pucetec.alejandro_barrionuevo_shipflow.mappers.PackageEventMapper
import com.pucetec.alejandro_barrionuevo_shipflow.mappers.PackageMapper
import com.pucetec.alejandro_barrionuevo_shipflow.models.entities.*
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.PackageRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.UpdateStatusRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageDetailResponse
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.PackageResponse
import com.pucetec.alejandro_barrionuevo_shipflow.repositories.PackageEventRepository
import com.pucetec.alejandro_barrionuevo_shipflow.repositories.PackageRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDate
import java.time.LocalDateTime


class PackageServiceTest {

    private lateinit var packageRepository: PackageRepository
    private lateinit var packageEventRepository: PackageEventRepository
    private lateinit var packageMapper: PackageMapper
    private lateinit var eventMapper: PackageEventMapper
    private lateinit var packageService: PackageService

    @BeforeEach
    fun setUp() {
        packageRepository = mock(PackageRepository::class.java)
        packageEventRepository = mock(PackageEventRepository::class.java)
        packageMapper = mock(PackageMapper::class.java)
        eventMapper = mock(PackageEventMapper::class.java)
        packageService = PackageService(packageRepository, packageEventRepository, packageMapper, eventMapper)
    }

    @Test
    fun should_create_package_successfully() {
        val request = PackageRequest("DOCUMENT", 2.5f, "Documentos legales", "Quito", "Guayaquil")
        val entity = Package(
            trackingId = "1",
            type = PackageType.DOCUMENT,
            weight = 2.5f,
            description = "Documentos legales",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            estimatedDeliveryDate = LocalDate.now().plusDays(5)
        ).apply { id = 1L }

        val response = PackageResponse(
            id = 1L,
            createdAt = entity.createdAt,
            trackingId = "1",
            type = "DOCUMENT",
            weight = 2.5f,
            description = "Documentos legales",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            status = "PENDING",
            estimatedDeliveryDate = entity.estimatedDeliveryDate
        )

        val internalStatusRequest = UpdateStatusRequest("PENDING", "Package registered and pending processing")

        `when`(packageRepository.findAll()).thenReturn(emptyList())
        `when`(packageMapper.toEntity(request, "1", PackageType.DOCUMENT)).thenReturn(entity)
        `when`(packageRepository.save(entity)).thenReturn(entity)
        `when`(eventMapper.toEntity(internalStatusRequest, entity)).thenReturn(mock(PackageEvent::class.java))
        `when`(packageMapper.toResponse(entity)).thenReturn(response)

        val result = packageService.createPackage(request)

        assertEquals("DOCUMENT", result.type)
        assertEquals("Quito", result.cityFrom)
        assertEquals("Guayaquil", result.cityTo)
    }

    @Test
    fun should_throw_exception_when_cities_are_equal() {
        val request = PackageRequest("DOCUMENT", 1.0f, "Envío simple", "Quito", "Quito")

        assertThrows<InvalidCityException> {
            packageService.createPackage(request)
        }
    }

    @Test
    fun should_throw_exception_when_description_is_too_long() {
        val longDescription = "A".repeat(51)
        val request = PackageRequest("DOCUMENT", 1.0f, longDescription, "Quito", "Cuenca")

        assertThrows<DescriptionTooLongException> {
            packageService.createPackage(request)
        }
    }

    @Test
    fun should_throw_exception_when_type_is_invalid() {
        val request = PackageRequest("BIG_BOX", 3.0f, "Cajas grandes", "Loja", "Ambato")

        assertThrows<InvalidTypeException> {
            packageService.createPackage(request)
        }
    }

    @Test
    fun should_return_package_by_tracking_id() {
        val trackingId = "123"
        val entity = Package(
            trackingId = trackingId,
            type = PackageType.FRAGILE,
            weight = 1.2f,
            description = "Florero de cerámica",
            cityFrom = "Ambato",
            cityTo = "Quito",
            estimatedDeliveryDate = LocalDate.now().plusDays(3)
        ).apply { id = 10L }

        val response = PackageResponse(
            id = 10L,
            createdAt = entity.createdAt,
            trackingId = trackingId,
            type = "FRAGILE",
            weight = 1.2f,
            description = "Florero de cerámica",
            cityFrom = "Ambato",
            cityTo = "Quito",
            status = "PENDING",
            estimatedDeliveryDate = entity.estimatedDeliveryDate
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))
        `when`(packageMapper.toResponse(entity)).thenReturn(response)

        val result = packageService.getByTrackingId(trackingId)

        assertEquals("Florero de cerámica", result.description)
    }

    @Test
    fun should_throw_exception_when_tracking_id_not_found() {
        `when`(packageRepository.findAll()).thenReturn(emptyList())

        assertThrows<PackageNotFoundException> {
            packageService.getByTrackingId("XYZ")
        }
    }

    @Test
    fun should_update_status_successfully() {
        val trackingId = "200"
        val request = UpdateStatusRequest("IN_TRANSIT", "En camino")
        val entity = Package(
            trackingId = trackingId,
            type = PackageType.SMALL_BOX,
            weight = 0.8f,
            description = "Audífonos",
            cityFrom = "Cuenca",
            cityTo = "Manta",
            estimatedDeliveryDate = LocalDate.now().plusDays(4),
            status = Status.PENDING
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))
        `when`(packageRepository.save(entity)).thenReturn(entity)
        `when`(eventMapper.toEntity(request, entity)).thenReturn(mock(PackageEvent::class.java))

        val result = packageService.updateStatusPackage(trackingId, request)

        assertEquals("Status updated successfully.", result.message)
        assertEquals(trackingId, result.trackingId)
        assertEquals("IN_TRANSIT", result.newStatus)
    }


    @Test
    fun should_throw_exception_when_status_is_invalid() {
        val trackingId = "456"
        val request = UpdateStatusRequest("CANCELED", "Estado inválido")
        val entity = Package(
            trackingId = trackingId,
            type = PackageType.FRAGILE,
            weight = 3.0f,
            description = "Electrodoméstico",
            cityFrom = "Latacunga",
            cityTo = "Tena",
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            status = Status.IN_TRANSIT,
            events = emptyList()
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))

        assertThrows<InvalidStatusException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }


    @Test
    fun should_throw_exception_when_tracking_id_does_not_exist_on_update() {
        val trackingId = "000"
        val request = UpdateStatusRequest("IN_TRANSIT", "Intentando actualizar inexistente")

        `when`(packageRepository.findAll()).thenReturn(emptyList())

        assertThrows<PackageNotFoundException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }


    @Test
    fun should_return_package_detail_by_tracking_id() {
        val trackingId = "ABC123"
        val entity = Package(
            trackingId = trackingId,
            type = PackageType.FRAGILE,
            weight = 1.0f,
            description = "Jarrón",
            cityFrom = "Quito",
            cityTo = "Ambato",
            estimatedDeliveryDate = LocalDate.now().plusDays(2)
        ).apply { id = 99L }

        val detailResponse = mock(PackageDetailResponse::class.java)

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))
        `when`(packageMapper.toDetailResponse(entity, eventMapper)).thenReturn(detailResponse)

        val result = packageService.getDetailByTrackingId(trackingId)

        assertEquals(detailResponse, result)
    }

    @Test
    fun should_throw_exception_when_package_detail_not_found() {
        `when`(packageRepository.findAll()).thenReturn(emptyList())

        assertThrows<PackageNotFoundException> {
            packageService.getDetailByTrackingId("NO_EXISTE")
        }
    }


    @Test
    fun should_throw_exception_when_status_transition_is_invalid() {
        val trackingId = "TRK123"
        val request = UpdateStatusRequest("DELIVERED", "Finalizado")

        val entity = Package(
            trackingId = trackingId,
            type = PackageType.FRAGILE,
            weight = 2.5f,
            description = "Florero",
            cityFrom = "Cuenca",
            cityTo = "Quito",
            status = Status.PENDING,
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            events = emptyList()
        ).apply { id = 101L }

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))

        assertThrows<InvalidStatusTransitionException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }


    @Test
    fun should_throw_business_rule_exception_when_delivered_without_in_transit() {
        val trackingId = "TRK124"
        val request = UpdateStatusRequest("DELIVERED", "Entregado")

        val entity = Package(
            trackingId = trackingId,
            type = PackageType.FRAGILE,
            weight = 5.0f,
            description = "Cámara",
            cityFrom = "Guayaquil",
            cityTo = "Loja",
            status = Status.IN_TRANSIT,
            estimatedDeliveryDate = LocalDate.now().plusDays(2),
            events = emptyList()
        ).apply { id = 202L }

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))

        assertThrows<BusinessRuleException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }

    @Test
    fun should_update_from_on_hold_to_in_transit() {
        val trackingId = "HOLD123"
        val request = UpdateStatusRequest("IN_TRANSIT", "Retomado")

        val entity = Package(
            trackingId = trackingId,
            type = PackageType.SMALL_BOX,
            weight = 1.0f,
            description = "Libro",
            cityFrom = "Cuenca",
            cityTo = "Quito",
            status = Status.ON_HOLD,
            estimatedDeliveryDate = LocalDate.now().plusDays(1),
            events = listOf(mock(PackageEvent::class.java))
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))
        `when`(packageRepository.save(entity)).thenReturn(entity)
        `when`(eventMapper.toEntity(request, entity)).thenReturn(mock(PackageEvent::class.java))

        val result = packageService.updateStatusPackage(trackingId, request)

        assertEquals("IN_TRANSIT", result.newStatus)
    }

    @Test
    fun should_update_from_on_hold_to_cancelled() {
        val trackingId = "HOLD456"
        val request = UpdateStatusRequest("CANCELLED", "Cancelado por cliente")

        val entity = Package(
            trackingId = trackingId,
            type = PackageType.DOCUMENT,
            weight = 0.3f,
            description = "Contrato",
            cityFrom = "Loja",
            cityTo = "Riobamba",
            status = Status.ON_HOLD,
            estimatedDeliveryDate = LocalDate.now().plusDays(2),
            events = listOf(mock(PackageEvent::class.java))
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))
        `when`(packageRepository.save(entity)).thenReturn(entity)
        `when`(eventMapper.toEntity(request, entity)).thenReturn(mock(PackageEvent::class.java))

        val result = packageService.updateStatusPackage(trackingId, request)

        assertEquals("CANCELLED", result.newStatus)
    }


    @Test
    fun should_throw_exception_when_transition_from_delivered() {
        val trackingId = "DEL123"
        val request = UpdateStatusRequest("IN_TRANSIT", "No debe poder")

        val entity = Package(
            trackingId = trackingId,
            type = PackageType.DOCUMENT,
            weight = 0.3f,
            description = "Documento",
            cityFrom = "Loja",
            cityTo = "Quito",
            status = Status.DELIVERED,
            estimatedDeliveryDate = LocalDate.now(),
            events = listOf(mock(PackageEvent::class.java))
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))

        assertThrows<InvalidStatusTransitionException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }

    @Test
    fun should_throw_exception_when_transition_from_cancelled() {
        val trackingId = "CAN123"
        val request = UpdateStatusRequest("IN_TRANSIT", "Tampoco debe poder")

        val entity = Package(
            trackingId = trackingId,
            type = PackageType.SMALL_BOX,
            weight = 0.5f,
            description = "Regalo",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            status = Status.CANCELLED,
            estimatedDeliveryDate = LocalDate.now(),
            events = listOf(mock(PackageEvent::class.java))
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))

        assertThrows<InvalidStatusTransitionException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }


    @Test
    fun should_update_from_in_transit_to_cancelled() {
        val trackingId = "INT123"
        val request = UpdateStatusRequest("CANCELLED", "Cancelado en ruta")

        val entity = Package(
            trackingId = trackingId,
            type = PackageType.DOCUMENT,
            weight = 1.0f,
            description = "Documentación",
            cityFrom = "Ambato",
            cityTo = "Cuenca",
            status = Status.IN_TRANSIT,
            estimatedDeliveryDate = LocalDate.now().plusDays(1),
            events = listOf(mock(PackageEvent::class.java))
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))
        `when`(packageRepository.save(entity)).thenReturn(entity)
        `when`(eventMapper.toEntity(request, entity)).thenReturn(mock(PackageEvent::class.java))

        val result = packageService.updateStatusPackage(trackingId, request)

        assertEquals("CANCELLED", result.newStatus)
    }


    @Test
    fun should_update_from_in_transit_to_on_hold() {
        val trackingId = "ONHOLD123"
        val request = UpdateStatusRequest("ON_HOLD", "Demorado por clima")

        val entity = Package(
            trackingId = trackingId,
            type = PackageType.DOCUMENT,
            weight = 1.0f,
            description = "Papeles importantes",
            cityFrom = "Riobamba",
            cityTo = "Ibarra",
            status = Status.IN_TRANSIT,
            estimatedDeliveryDate = LocalDate.now().plusDays(2),
            events = listOf(mock(PackageEvent::class.java))
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(entity))
        `when`(packageRepository.save(entity)).thenReturn(entity)
        `when`(eventMapper.toEntity(request, entity)).thenReturn(mock(PackageEvent::class.java))

        val result = packageService.updateStatusPackage(trackingId, request)

        assertEquals("ON_HOLD", result.newStatus)
    }


    @Test
    fun should_update_to_delivered_when_in_transit_event_exists() {
        val trackingId = "TRK999"
        val request = UpdateStatusRequest("DELIVERED", "Entregado correctamente")

        val packageEntity = Package(
            trackingId = trackingId,
            type = PackageType.FRAGILE,
            weight = 1.5f,
            description = "Cuadro",
            cityFrom = "Quito",
            cityTo = "Ambato",
            status = Status.IN_TRANSIT,
            estimatedDeliveryDate = LocalDate.now().plusDays(1),
            events = emptyList()
        ).apply {
            id = 901L
        }

        val inTransitEvent = PackageEvent(
            status = Status.IN_TRANSIT,
            packageEntity = packageEntity
        )

        val packageWithEvent = packageEntity.copy(events = listOf(inTransitEvent))

        `when`(packageRepository.findAll()).thenReturn(listOf(packageWithEvent))
        `when`(packageRepository.save(packageWithEvent)).thenReturn(packageWithEvent)
        `when`(eventMapper.toEntity(request, packageWithEvent)).thenReturn(mock(PackageEvent::class.java))

        val result = packageService.updateStatusPackage(trackingId, request)

        assertEquals("DELIVERED", result.newStatus)
    }


    @Test
    fun should_return_all_packages() {
        val packageEntity = Package(
            trackingId = "TRK001",
            type = PackageType.DOCUMENT,
            weight = 0.5f,
            description = "Documento",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            status = Status.PENDING,
            estimatedDeliveryDate = LocalDate.now(),
            events = emptyList()
        )

        val response = PackageResponse(
            id = 1L,
            createdAt = LocalDateTime.now(),
            trackingId = packageEntity.trackingId,
            type = packageEntity.type.name,
            weight = packageEntity.weight,
            description = packageEntity.description,
            cityFrom = packageEntity.cityFrom,
            cityTo = packageEntity.cityTo,
            status = packageEntity.status.name,
            estimatedDeliveryDate = packageEntity.estimatedDeliveryDate
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(packageEntity))
        `when`(packageMapper.toResponse(packageEntity)).thenReturn(response)

        val result = packageService.getAllPackages()

        assertEquals(1, result.size)
        assertEquals("TRK001", result.first().trackingId)
    }


    @Test
    fun should_throw_exception_when_trying_to_return_to_pending_from_in_transit() {
        val trackingId = "TRK789"
        val request = UpdateStatusRequest("PENDING", "Intento inválido de retroceso")

        val packageEntity = Package(
            trackingId = trackingId,
            type = PackageType.FRAGILE,
            weight = 1.0f,
            description = "Documento",
            cityFrom = "Latacunga",
            cityTo = "Quito",
            status = Status.IN_TRANSIT,
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            events = emptyList()
        ).apply { id = 777L }

        `when`(packageRepository.findAll()).thenReturn(listOf(packageEntity))

        assertThrows<InvalidStatusTransitionException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }


    @Test
    fun should_throw_exception_when_tracking_id_does_not_match_any_package() {
        val trackingId = "TRK404"
        val request = UpdateStatusRequest("IN_TRANSIT", "Cambio de estado")

        val otherPackage = Package(
            trackingId = "OTRO",
            type = PackageType.FRAGILE,
            weight = 2.0f,
            description = "Otro paquete",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            status = Status.PENDING,
            estimatedDeliveryDate = LocalDate.now().plusDays(2),
            events = emptyList()
        )
        `when`(packageRepository.findAll()).thenReturn(listOf(otherPackage))

        assertThrows<PackageNotFoundException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }


    @Test
    fun should_throw_business_rule_exception_when_no_in_transit_event_before_delivered() {
        val trackingId = "PKG123"
        val request = UpdateStatusRequest("DELIVERED", "Intento sin tránsito")

        val packageEntity = Package(
            trackingId = trackingId,
            type = PackageType.SMALL_BOX,
            weight = 1.0f,
            description = "Contenido delicado",
            cityFrom = "Santa Elena",
            cityTo = "Guayaquil",
            status = Status.IN_TRANSIT,
            estimatedDeliveryDate = LocalDate.now(),
            events = listOf(
                PackageEvent(
                    status = Status.ON_HOLD,
                    comment = "Retenido",
                    packageEntity = mock(Package::class.java)
                )
            )
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(packageEntity))

        assertThrows<BusinessRuleException> {
            packageService.updateStatusPackage(trackingId, request)
        }
    }


    @Test
    fun should_return_package_when_tracking_id_matches() {
        val trackingId = "PKG100"

        val otherPackage = Package(
            trackingId = "PKG001",
            type = PackageType.FRAGILE,
            weight = 5.0f,
            description = "Contenido pesado",
            cityFrom = "Guayaquil",
            cityTo = "Quito",
            status = Status.IN_TRANSIT,
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            events = emptyList()
        ).apply {
            id = 2L
        }

        val targetPackage = Package(
            trackingId = trackingId,
            type = PackageType.SMALL_BOX,
            weight = 2.5f,
            description = "Contenido frágil",
            cityFrom = "Quito",
            cityTo = "Lima",
            status = Status.PENDING,
            estimatedDeliveryDate = LocalDate.now(),
            events = emptyList()
        ).apply {
            id = 1L
        }

        val expectedResponse = PackageResponse(
            id = 1L,
            createdAt = targetPackage.createdAt,
            trackingId = trackingId,
            type = "SMALL_BOX",
            weight = 2.5f,
            description = "Contenido frágil",
            cityFrom = "Quito",
            cityTo = "Lima",
            status = "PENDING",
            estimatedDeliveryDate = targetPackage.estimatedDeliveryDate
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(otherPackage, targetPackage))
        `when`(packageMapper.toResponse(targetPackage)).thenReturn(expectedResponse)

        val result = packageService.getByTrackingId(trackingId)

        assertEquals(expectedResponse.trackingId, result.trackingId)
        assertEquals(expectedResponse.type, result.type)
        assertEquals(expectedResponse.status, result.status)
    }


    @Test
    fun should_return_package_detail_when_tracking_id_matches() {
        val trackingId = "PKG999"

        val package1 = Package(
            trackingId = "PKG001",
            type = PackageType.FRAGILE,
            weight = 4.5f,
            description = "Paquete grande",
            cityFrom = "Guayaquil",
            cityTo = "Ambato",
            status = Status.IN_TRANSIT,
            estimatedDeliveryDate = LocalDate.now().plusDays(3),
            events = emptyList()
        ).apply {
            id = 1L
        }

        val package2 = Package(
            trackingId = trackingId,
            type = PackageType.DOCUMENT,
            weight = 0.7f,
            description = "Documentos importantes",
            cityFrom = "Loja",
            cityTo = "Quito",
            status = Status.PENDING,
            estimatedDeliveryDate = LocalDate.now().plusDays(2),
            events = emptyList()
        ).apply {
            id = 2L
        }

        val expected = PackageDetailResponse(
            packageInfo = PackageResponse(
                id = 2L,
                createdAt = package2.createdAt,
                trackingId = trackingId,
                type = "ENVELOPE",
                weight = 0.7f,
                description = "Documentos importantes",
                cityFrom = "Loja",
                cityTo = "Quito",
                status = "PENDING",
                estimatedDeliveryDate = package2.estimatedDeliveryDate
            ),
            eventHistory = emptyList()
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(package1, package2))
        `when`(packageMapper.toDetailResponse(package2, eventMapper)).thenReturn(expected)

        val result = packageService.getDetailByTrackingId(trackingId)

        assertEquals(expected.packageInfo.trackingId, result.packageInfo.trackingId)
        assertEquals(expected.packageInfo.status, result.packageInfo.status)
        assertEquals(expected.packageInfo.type, result.packageInfo.type)
        assertEquals(expected.packageInfo.weight, result.packageInfo.weight)
        assertEquals(expected.eventHistory.size, result.eventHistory.size)
    }

    @Test
    fun should_create_package_with_incremented_tracking_id() {
        val request = PackageRequest(
            type = "DOCUMENT",
            weight = 0.5f,
            description = "Documentos importantes",
            cityFrom = "Quito",
            cityTo = "Guayaquil"
        )

        val existingPackage = Package(
            trackingId = "15",
            type = PackageType.DOCUMENT,
            weight = 0.3f,
            description = "Papeles",
            cityFrom = "Quito",
            cityTo = "Cuenca",
            status = Status.PENDING,
            estimatedDeliveryDate = LocalDate.now(),
            events = emptyList()
        )

        val trackingIdGenerado = "16"

        val mappedEntity = Package(
            trackingId = trackingIdGenerado,
            type = PackageType.DOCUMENT,
            weight = 0.5f,
            description = "Documentos importantes",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            status = Status.PENDING,
            estimatedDeliveryDate = LocalDate.now(),
            events = emptyList()
        )

        val savedEntity = mappedEntity.copy().apply { id = 99L }

        val initialEventRequest = UpdateStatusRequest(
            status = "PENDING",
            comment = "Package registered and pending processing"
        )

        val mappedEvent = PackageEvent(
            status = Status.PENDING,
            packageEntity = savedEntity
        )

        val expectedResponse = PackageResponse(
            id = 99L,
            createdAt = savedEntity.createdAt,
            trackingId = trackingIdGenerado,
            type = "DOCUMENT",
            weight = 0.5f,
            description = "Documentos importantes",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            status = "PENDING",
            estimatedDeliveryDate = savedEntity.estimatedDeliveryDate
        )

        `when`(packageRepository.findAll()).thenReturn(listOf(existingPackage))
        `when`(packageMapper.toEntity(request, trackingIdGenerado, PackageType.DOCUMENT)).thenReturn(mappedEntity)
        `when`(packageRepository.save(mappedEntity)).thenReturn(savedEntity)
        `when`(eventMapper.toEntity(initialEventRequest, savedEntity)).thenReturn(mappedEvent)
        `when`(packageMapper.toResponse(savedEntity)).thenReturn(expectedResponse)

        val result = packageService.createPackage(request)

        assertEquals(expectedResponse.trackingId, result.trackingId)
        assertEquals(expectedResponse.cityFrom, result.cityFrom)
        assertEquals(expectedResponse.cityTo, result.cityTo)
        assertEquals(expectedResponse.description, result.description)
    }
}
