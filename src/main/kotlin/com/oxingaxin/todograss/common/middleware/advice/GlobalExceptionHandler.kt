package com.oxingaxin.todograss.common.middleware.advice

import com.oxingaxin.todograss.common.dto.BaseResponse
import com.oxingaxin.todograss.common.exception.ApiException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(ex: ApiException): BaseResponse<Void> =
        BaseResponse(BaseResponse.Header(false, ex.code, ex.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): BaseResponse<Map<String, String>> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as org.springframework.validation.FieldError).field
            val errorMessage = error.defaultMessage
            errors[fieldName] = errorMessage ?: "Not Exception Message"
        }
        return BaseResponse(BaseResponse.Header(false, HttpStatus.BAD_REQUEST.value(), "Invalid argument"), errors)
    }
}