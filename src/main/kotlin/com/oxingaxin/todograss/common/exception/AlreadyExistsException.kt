package com.oxingaxin.todograss.common.exception

import org.springframework.http.HttpStatus

class AlreadyExistsException(entity: String) : ApiException(
    "$entity already exists",
    HttpStatus.CONFLICT.value()
) {
    constructor(entity: String, field: String, value: String) : this("$entity with $field: $value already exists")
}