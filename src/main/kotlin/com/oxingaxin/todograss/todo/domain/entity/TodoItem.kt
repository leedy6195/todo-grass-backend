package com.oxingaxin.todograss.todo.domain.entity

import com.oxingaxin.todograss.member.domain.entity.Member
import jakarta.persistence.*
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Entity
class TodoItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    @Column(length = 255)
    var title: String,

    @Column(length = 1023)
    var description: String,

    private var createdAt: LocalDateTime? = null
) {
    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
    }

    class Builder {
        private lateinit var member: Member
        private lateinit var title: String
        private lateinit var description: String

        fun member(member: Member) = apply { this.member = member }
        fun title(title: String) = apply { this.title = title }
        fun description(description: String) = apply { this.description = description }

        fun build(): TodoItem {
            return TodoItem(member = member, title = title, description = description)
        }
    }
}