package com.oxingaxin.todograss.todo.controller

import com.oxingaxin.todograss.common.dto.BaseResponse
import com.oxingaxin.todograss.common.dto.CustomUser
import com.oxingaxin.todograss.member.repository.MemberRepository
import com.oxingaxin.todograss.todo.domain.dto.TodoItemRequest
import com.oxingaxin.todograss.todo.domain.dto.TodoItemResponse
import com.oxingaxin.todograss.todo.service.TodoService
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(
    private val todoService: TodoService
) {
    @GetMapping("/{id}")
    fun getTodo(
        @PathVariable id: Long
    ): BaseResponse<TodoItemResponse> {
        val todo = todoService.getTodo(id)

        return BaseResponse.fetched(todo)
    }
    @GetMapping("/nicknames/{nickname}")
    fun getTodos(@PathVariable nickname: String): BaseResponse<List<TodoItemResponse>> {

        //val memberId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId

        val todos = todoService.getTodos(nickname)

        return BaseResponse.fetched(todos)
    }
    @PostMapping
    fun addTodo(@RequestBody @Valid todoItemRequest: TodoItemRequest): BaseResponse<Unit> {
        val memberId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
        todoItemRequest.memberId = memberId

        todoService.addTodo(todoItemRequest)

        return BaseResponse.created(Unit)
    }

    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: Long,
        @RequestBody @Valid todoItemRequest: TodoItemRequest
    ): BaseResponse<Unit> {
        val memberId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
        todoItemRequest.memberId = memberId

        todoService.updateTodo(id, todoItemRequest)

        return BaseResponse.ok(Unit)
    }

    @DeleteMapping("/{id}")
    fun deleteTodo(
        @PathVariable id: Long
    ) {

        todoService.deleteTodo(id)
    }
}