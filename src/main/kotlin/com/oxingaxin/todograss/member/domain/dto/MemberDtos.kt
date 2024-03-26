package com.oxingaxin.todograss.member.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.oxingaxin.todograss.member.domain.entity.Member
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.io.Serializable


data class MemberRequest(
    var id: Long?,

    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    private val _email: String?,

    @field:NotBlank
    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#\$%^&*])[a-zA-Z0-9!@#\$%^&*]{8,20}\$",
        message = "영문, 숫자, 특수문자를 포함한 8~20자리로 입력하세요"
    )
    @JsonProperty("password")
    private val _password: String?,

    @field:NotBlank
    @field:Pattern(
        regexp = "^[a-zA-Z0-9]{4,20}\$",
        message = "영문 대소문자, 숫자로 구성된 4~20 자리 닉네임을 입력하세요"
    )
    @JsonProperty("nickname")
    private val _nickname: String?

) {
    val email: String
        get() = _email!!
    private val password: String
        get() = _password!!
    val nickname: String
        get() = _nickname!!

    fun toEntity() = Member(
        email = email,
        password = password,
        nickname = nickname
    )
}

data class MemberUpdateRequest(
    val profileImage: ByteArray
) : Serializable

data class SigninRequest(
    @field:NotBlank
    @JsonProperty("email")
    private val _email: String?,

    @field:NotBlank
    @JsonProperty("password")
    private val _password: String?

) {
    val email: String
        get() = _email!!

    val password: String
        get() = _password!!
}

data class PublicMemberInfo(
    val email: String,
    val nickname: String,
    val profileImgPath: String
)