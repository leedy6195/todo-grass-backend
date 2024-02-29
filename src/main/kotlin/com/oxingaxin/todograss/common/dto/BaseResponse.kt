package com.oxingaxin.todograss.common.dto

import org.springframework.http.HttpStatus

data class BaseResponse<T>(
    val header: Header = Header(),
    val data: T? = null
) {

    data class Header(
        val success: Boolean = false,
        val code: Int = 0,
        val message: String? = null
    )

    companion object {
        fun <T> created(data: T?): BaseResponse<T> =
            BaseResponse(Header(true, HttpStatus.CREATED.value(), "created successfully"), data)

        fun <T> updated(data: T?): BaseResponse<T> =
            BaseResponse(Header(true, HttpStatus.OK.value(), "updated successfully"), data)

        fun <T> deleted(data: T?): BaseResponse<T> =
            BaseResponse(Header(true, HttpStatus.NO_CONTENT.value(), "deleted successfully"), data)

        fun <T> fetched(data: T?): BaseResponse<T> =
            BaseResponse(Header(true, HttpStatus.OK.value(), "fetched successfully"), data)

        fun <T> ok(data: T?): BaseResponse<T> =
            BaseResponse(Header(true, HttpStatus.OK.value(), "ok"), data)
    }
}