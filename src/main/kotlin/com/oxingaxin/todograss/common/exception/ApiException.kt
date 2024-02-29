package com.oxingaxin.todograss.common.exception

open class ApiException(message: String, val code: Int)
    : RuntimeException(message)