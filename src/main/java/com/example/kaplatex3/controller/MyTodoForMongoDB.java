package com.example.kaplatex3.controller;

import com.example.kaplatex3.model.TodoClassForMongoDB;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;


public interface MyTodoForMongoDB extends MongoRepository<TodoClassForMongoDB, String> {

}
