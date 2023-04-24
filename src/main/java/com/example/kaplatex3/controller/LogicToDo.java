package com.example.kaplatex3.controller;

import com.example.kaplatex3.model.ToDoClass;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogicToDo {

    public boolean checkIfTodoExist(List<ToDoClass> todoList, String title){
        for(ToDoClass todo: todoList){
            if(todo.getTitle().equals(title))
                return false;
        }
        return true;
    }

    public int CountByStatus(List<ToDoClass> todoList, String status){
        int count = 0;
        for(ToDoClass todo: todoList){
            if(todo.getStatus().equals(status))
                count++;
        }
        return count;
    }
}
