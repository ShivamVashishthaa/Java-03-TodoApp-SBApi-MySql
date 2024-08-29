package com.todoRestApi.todo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoJpaResources {

	@Autowired
	TodoRepository todoRepo;

//	@GetMapping("/Todo-List")
//	public List<Todo> listTodo() {
//		return service.listTodo();
//	}

	@GetMapping("/users/{username}/todos")
	public List<Todo> retrieveTodos(@PathVariable String username) {
		List<Todo> all = todoRepo.findAll();
		List<Todo> allTodos = todoRepo.findByUsername(username);
		return allTodos;
	}

	@GetMapping("/users/{username}/todos/{id}")
	public Todo retreiveTodo(@PathVariable String username, @PathVariable Integer id) {
		return todoRepo.findById(id).get();
	}

	@DeleteMapping("/users/{username}/todos/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable String username, @PathVariable Integer id) {
		todoRepo.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/users/{username}/todos/{id}")
	public Todo updateById(@PathVariable String username, @PathVariable Integer id, @RequestBody Todo todo) {
		todoRepo.save(todo);
		return todo;
	}

	@PostMapping("/users/{username}/todos")
	public Todo createTodo(@PathVariable String username, @RequestBody Todo todo) {
		todo.setusername(username);
		todo.setId(null);
		return todoRepo.save(todo);
	}

}
