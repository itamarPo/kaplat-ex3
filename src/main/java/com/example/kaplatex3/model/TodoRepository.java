package com.example.kaplatex3.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<ToDoClass, Long> {
}
