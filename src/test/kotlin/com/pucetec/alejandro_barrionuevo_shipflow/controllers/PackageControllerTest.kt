package com.pucetec.alejandro_barrionuevo_shipflow.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.BusinessRuleException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.DescriptionTooLongException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.InvalidCityException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.InvalidStatusException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.InvalidStatusTransitionException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.InvalidTypeException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.PackageNotFoundException
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.PackageRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.requests.UpdateStatusRequest
import com.pucetec.alejandro_barrionuevo_shipflow.models.responses.*
import com.pucetec.alejandro_barrionuevo_shipflow.routes.Routes
import com.pucetec.alejandro_barrionuevo_shipflow.services.PackageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

@WebMvcTest(PackageController::class)
@Import(PackageControllerTest.MockConfig::class)
class PackageControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var packageService: PackageService

    private lateinit var objectMapper: ObjectMapper
    private val BASE_URL = Routes.PACKAGES

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @Test
    fun should_create_package() {
        reset(packageService)

        val request = PackageRequest("DOCUMENT", 1.5f, "Test package", "Quito", "Guayaquil")
        val response = PackageResponse(
            id = 1L,
            createdAt = LocalDateTime.now(),
            trackingId = "1001",
            type = "DOCUMENT",
            weight = 1.5f,
            description = "Test package",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            status = "PENDING",
            estimatedDeliveryDate = LocalDate.now().plusDays(3)
        )

        `when`(packageService.createPackage(request)).thenReturn(response)
        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.tracking_id") { value("1001") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_all_packages() {
        val response = listOf(
            PackageResponse(
                id = 1L,
                createdAt = LocalDateTime.now(),
                trackingId = "1001",
                type = "DOCUMENT",
                weight = 1.5f,
                description = "Desc",
                cityFrom = "Quito",
                cityTo = "Guayaquil",
                status = "PENDING",
                estimatedDeliveryDate = LocalDate.now()
            )
        )

        `when`(packageService.getAllPackages()).thenReturn(response)

        val result = mockMvc.get(BASE_URL)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].city_from") { value("Quito") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_package_by_tracking_id() {
        val response = PackageResponse(
            id = 1L,
            createdAt = LocalDateTime.now(),
            trackingId = "1001",
            type = "DOCUMENT",
            weight = 1.5f,
            description = "Desc",
            cityFrom = "Quito",
            cityTo = "Guayaquil",
            status = "PENDING",
            estimatedDeliveryDate = LocalDate.now()
        )

        `when`(packageService.getByTrackingId("1001")).thenReturn(response)

        val result = mockMvc.get("$BASE_URL/1001")
            .andExpect {
                status { isOk() }
                jsonPath("$.type") { value("DOCUMENT") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_return_package_history() {
        val event = PackageEventResponse(
            id = 1L,
            updatedAt = LocalDateTime.now(),
            status = "PENDING",
            comment = "Created"
        )

        val response = PackageDetailResponse(
            packageInfo = PackageResponse(
                id = 1L,
                createdAt = LocalDateTime.now(),
                trackingId = "1001",
                type = "DOCUMENT",
                weight = 1.5f,
                description = "Desc",
                cityFrom = "Quito",
                cityTo = "Guayaquil",
                status = "PENDING",
                estimatedDeliveryDate = LocalDate.now()
            ),
            eventHistory = listOf(event)
        )

        `when`(packageService.getDetailByTrackingId("1001")).thenReturn(response)

        val result = mockMvc.get("$BASE_URL/1001/history")
            .andExpect {
                status { isOk() }
                jsonPath("$.packageInfo.city_to") { value("Guayaquil") }
            }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_update_package_status() {
        val request = UpdateStatusRequest("IN_TRANSIT", "Shipped")
        val response = UpdateStatusResponse(
            message = "Updated",
            trackingId = "1001",
            newStatus = "IN_TRANSIT",
            updatedAt = LocalDateTime.now()
        )

        `when`(packageService.updateStatusPackage("1001", request)).thenReturn(response)
        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.newStatus") { value("IN_TRANSIT") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }


    @Test
    fun should_return_400_when_city_from_equals_city_to() {
        val request = PackageRequest("DOCUMENT", 1.0f, "desc", "Quito", "Quito")

        `when`(packageService.createPackage(request))
            .thenThrow(InvalidCityException("Origin and destination cities cannot be the same."))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_400_when_description_too_long() {
        val request = PackageRequest("DOCUMENT", 1.0f, "x".repeat(51), "Quito", "Guayaquil")

        `when`(packageService.createPackage(request))
            .thenThrow(DescriptionTooLongException("Description must not exceed 50 characters."))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_400_when_package_type_is_invalid() {
        val request = PackageRequest("FOOD", 1.0f, "desc", "Quito", "Guayaquil")

        `when`(packageService.createPackage(request))
            .thenThrow(InvalidTypeException("Invalid type"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.post(BASE_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_404_when_package_not_found() {
        `when`(packageService.getByTrackingId("9999"))
            .thenThrow(PackageNotFoundException("Package not found"))

        val result = mockMvc.get("$BASE_URL/9999")
            .andExpect {
                status { isNotFound() }
            }.andReturn()

        assertEquals(404, result.response.status)
    }

    @Test
    fun should_return_400_when_status_is_invalid() {
        val request = UpdateStatusRequest("TELEPORTED", "error")

        `when`(packageService.updateStatusPackage("1001", request))
            .thenThrow(InvalidStatusException("Invalid status"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }

    @Test
    fun should_return_409_when_invalid_status_transition() {
        val request = UpdateStatusRequest("CANCELLED", "no se puede cancelar desde entregado")

        `when`(packageService.updateStatusPackage("1001", request))
            .thenThrow(InvalidStatusTransitionException("Invalid transition"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
        }.andReturn()

        assertEquals(409, result.response.status)
    }

    @Test
    fun should_return_400_when_delivered_without_in_transit() {
        val request = UpdateStatusRequest("DELIVERED", "directo sin tránsito")

        `when`(packageService.updateStatusPackage("1001", request))
            .thenThrow(BusinessRuleException("Cannot deliver without being IN_TRANSIT"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isBadRequest() }
        }.andReturn()

        assertEquals(400, result.response.status)
    }


    @Test
    fun should_allow_transition_from_pending_to_in_transit() {
        val request = UpdateStatusRequest("IN_TRANSIT", "Salida inicial")
        val response = UpdateStatusResponse("OK", "1001", "IN_TRANSIT", LocalDateTime.now())

        `when`(packageService.updateStatusPackage("1001", request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.newStatus") { value("IN_TRANSIT") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_reject_transition_from_pending_to_delivered() {
        val request = UpdateStatusRequest("DELIVERED", "Salto directo inválido")

        `when`(packageService.updateStatusPackage("1001", request))
            .thenThrow(InvalidStatusTransitionException("Cannot go from PENDING to DELIVERED"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1001/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
        }.andReturn()

        assertEquals(409, result.response.status)
    }

    @Test
    fun should_allow_transition_from_in_transit_to_delivered() {
        val request = UpdateStatusRequest("DELIVERED", "Entregado correctamente")
        val response = UpdateStatusResponse("OK", "1002", "DELIVERED", LocalDateTime.now())

        `when`(packageService.updateStatusPackage("1002", request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1002/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.newStatus") { value("DELIVERED") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_allow_transition_from_in_transit_to_cancelled() {
        val request = UpdateStatusRequest("CANCELLED", "Cancelado por el cliente")
        val response = UpdateStatusResponse("OK", "1003", "CANCELLED", LocalDateTime.now())

        `when`(packageService.updateStatusPackage("1003", request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1003/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.newStatus") { value("CANCELLED") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_reject_transition_from_in_transit_to_pending() {
        val request = UpdateStatusRequest("PENDING", "Retroceso inválido")

        `when`(packageService.updateStatusPackage("1003", request))
            .thenThrow(InvalidStatusTransitionException("IN_TRANSIT cannot go back to PENDING"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1003/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
        }.andReturn()

        assertEquals(409, result.response.status)
    }

    @Test
    fun should_allow_transition_from_on_hold_to_in_transit() {
        val request = UpdateStatusRequest("IN_TRANSIT", "Reanudado")
        val response = UpdateStatusResponse("OK", "1004", "IN_TRANSIT", LocalDateTime.now())

        `when`(packageService.updateStatusPackage("1004", request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1004/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.newStatus") { value("IN_TRANSIT") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_allow_transition_from_on_hold_to_cancelled() {
        val request = UpdateStatusRequest("CANCELLED", "Cancelado en pausa")
        val response = UpdateStatusResponse("OK", "1005", "CANCELLED", LocalDateTime.now())

        `when`(packageService.updateStatusPackage("1005", request)).thenReturn(response)

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1005/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isOk() }
            jsonPath("$.newStatus") { value("CANCELLED") }
        }.andReturn()

        assertEquals(200, result.response.status)
    }

    @Test
    fun should_reject_transition_from_on_hold_to_delivered() {
        val request = UpdateStatusRequest("DELIVERED", "No permitido desde ON_HOLD")

        `when`(packageService.updateStatusPackage("1005", request))
            .thenThrow(InvalidStatusTransitionException("Cannot go from ON_HOLD to DELIVERED"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1005/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
        }.andReturn()

        assertEquals(409, result.response.status)
    }

    @Test
    fun should_reject_any_transition_from_delivered() {
        val request = UpdateStatusRequest("CANCELLED", "No puede cancelar entregado")

        `when`(packageService.updateStatusPackage("1006", request))
            .thenThrow(InvalidStatusTransitionException("DELIVERED cannot be changed"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1006/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
        }.andReturn()

        assertEquals(409, result.response.status)
    }

    @Test
    fun should_reject_any_transition_from_cancelled() {
        val request = UpdateStatusRequest("IN_TRANSIT", "Cancelado no puede moverse")

        `when`(packageService.updateStatusPackage("1007", request))
            .thenThrow(InvalidStatusTransitionException("CANCELLED cannot be changed"))

        val json = objectMapper.writeValueAsString(request)

        val result = mockMvc.put("$BASE_URL/1007/status") {
            contentType = MediaType.APPLICATION_JSON
            content = json
        }.andExpect {
            status { isConflict() }
        }.andReturn()

        assertEquals(409, result.response.status)
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun packageService(): PackageService = mock(PackageService::class.java)
    }
}

