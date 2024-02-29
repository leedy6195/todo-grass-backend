package com.oxingaxin.todograss.member.repository

import com.oxingaxin.todograss.member.domain.entity.MemberRole
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRoleRepository : JpaRepository<MemberRole, Long>