package com.oxingaxin.todograss.common.exception

import org.springframework.http.HttpStatus

class NotFoundException(entity: String) : ApiException(
    "$entity not found",
    HttpStatus.NOT_FOUND.value()
) {
    constructor(entity: String, field: String, value: String) : this("$entity with $field: $value not found")
}