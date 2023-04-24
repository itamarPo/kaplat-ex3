package com.example.kaplatex3.controller;

import com.example.kaplatex3.model.ResultClass;
import com.example.kaplatex3.model.ToDoClass;
import com.example.kaplatex3.model.ToDoJsonClass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.gson.GsonProperties;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(path = "/todo")
public class ToDoController {

    List<ToDoClass> toDoClassList;
    LogicToDo logicEngine;
    int currentID;
    @Autowired
    public ToDoController(LogicToDo logicEngine) {
        this.toDoClassList = new ArrayList<>();
        this.logicEngine = logicEngine;
        this.currentID = 0;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> createNewTodo(@RequestBody ToDoJsonClass todo){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if(logicEngine.checkIfTodoExist(toDoClassList, todo.getTitle())){
            ResultClass<String> resultClass = new ResultClass<>("", "Error: TODO with the title  [" + todo.getTitle()
                    + "] already exists in the system");
           return new ResponseEntity<>(gson.toJson(resultClass)
                   , HttpStatusCode.valueOf(409));
        }
        if(Instant.now().toEpochMilli() >= todo.getDueDate()){
            ResultClass<String> resultClass = new ResultClass<>("", "Error: Can’t create new TODO that its due date is in the past");
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(409));
        }
        this.currentID++;
        toDoClassList.add(new ToDoClass(this.currentID, todo.getTitle(), todo.getContent(), todo.getDueDate(), "PENDING"));
        ResultClass<ToDoClass> resultClass = new ResultClass<>(toDoClassList.get(toDoClassList.size()-1),"");

        return new ResponseEntity<>(gson.toJson(resultClass), HttpStatus.OK);
    }

    @GetMapping("/size")
    public ResponseEntity<Object> countToDoByFilter(@RequestParam String status){
        ResultClass<Integer> resultClass = new ResultClass<>(0,"");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if(status.equals("ALL")){
            resultClass.setResult(toDoClassList.size());
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatus.OK);
        } else if (status.equals("PENDING") || status.equals("LATE") || status.equals("DONE")) {
            resultClass.setResult(logicEngine.CountByStatus(toDoClassList,status));
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(400));
        }
    }
}