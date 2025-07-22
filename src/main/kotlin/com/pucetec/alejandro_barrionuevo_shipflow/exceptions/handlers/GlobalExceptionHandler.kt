package com.pucetec.alejandro_barrionuevo_shipflow.exceptions.handlers


import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.BusinessRuleException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.DescriptionTooLongException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.InvalidCityException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.InvalidStatusException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.InvalidStatusTransitionException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.InvalidTypeException
import com.pucetec.alejandro_barrionuevo_shipflow.exceptions.PackageNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCityException::class)
    fun handleInvalidCity(ex: InvalidCityException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(DescriptionTooLongException::class)
    fun handleDescriptionTooLong(ex: DescriptionTooLongException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(InvalidStatusTransitionException::class)
    fun handleInvalidTransition(ex: InvalidStatusTransitionException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.CONFLICT)

    @ExceptionHandler(PackageNotFoundException::class)
    fun handlePackageNotFound(ex: PackageNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.NOT_FOUND)

    @ExceptionHandler(InvalidStatusException::class)
    fun handleInvalidStatus(ex: InvalidStatusException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(BusinessRuleException::class)
    fun handleBusinessRule(ex: BusinessRuleException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(InvalidTypeException::class)
    fun handleInvalidType(ex: InvalidTypeException): ResponseEntity<Map<String, String>> =
        ResponseEntity(mapOf("error" to ex.message.orEmpty()), HttpStatus.BAD_REQUEST)

}