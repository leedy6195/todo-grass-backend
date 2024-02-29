package com.oxingaxin.todograss.todo.domain.entity

import com.oxingaxin.todograss.member.domain.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class TodoItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    val description: String,

    private var createdAt: LocalDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }
}