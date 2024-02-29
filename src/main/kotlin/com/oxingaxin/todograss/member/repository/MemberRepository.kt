package com.oxingaxin.todograss.member.repository

import com.oxingaxin.todograss.member.domain.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun existsByEmail(email: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByEmail(email: String): Member?
}