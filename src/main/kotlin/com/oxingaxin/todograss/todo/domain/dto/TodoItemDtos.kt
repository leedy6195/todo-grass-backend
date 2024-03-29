package com.oxingaxin.todograss.todo.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


data class TodoItemRequest(
    var memberId: Long?,

    @field:NotBlank
    @JsonProperty("title")
    private val _title: String?,

    @field:NotBlank
    @JsonProperty("description")
    private val _description: String?,

    @field:NotNull
    @JsonProperty("weight")
    private val _weight: Int?
) {
    val title: String
        get() = _title!!

    val description: String
        get() = _description!!

    val weight: Int
        get() = _weight!!
}

data class TodoItemResponse(
    val id: Long,
    val title: String,
    val description: String,
    val weight: Int
)