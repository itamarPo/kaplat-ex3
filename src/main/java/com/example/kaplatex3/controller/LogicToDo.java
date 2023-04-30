package com.example.kaplatex3.controller;

import com.example.kaplatex3.model.ToDoClass;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<ToDoClass> sortList(List<ToDoClass> todoList ,String sortBy){
        List<ToDoClass> returnList;
        if(sortBy.equals(""))
            returnList = todoList;
        else if(sortBy.equals("ID"))
            returnList = todoList.stream().sorted(Comparator.comparing(ToDoClass::getId)).collect(Collectors.toList());
        else if(sortBy.equals("DUE_DATE"))
            returnList = todoList.stream().sorted(Comparator.comparing(ToDoClass::getDueDate)).collect(Collectors.toList());
        else
            returnList = todoList.stream().sorted(Comparator.comparing(ToDoClass::getTitle)).collect(Collectors.toList());
        return returnList;
    }

    public ToDoClass getTodoByID(List<ToDoClass> todoList, Integer id){
        for(ToDoClass todo: todoList){
            if(todo.getId().equals(id))
                return todo;
        }
        return null;
    }
}
