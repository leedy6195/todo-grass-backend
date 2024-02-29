package com.oxingaxin.todograss.member.repository

import com.oxingaxin.todograss.member.domain.entity.MemberRefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRefreshTokenRepository : JpaRepository<MemberRefreshToken, Long> {
    fun findByMemberId(memberId: Long): MemberRefreshToken?
}