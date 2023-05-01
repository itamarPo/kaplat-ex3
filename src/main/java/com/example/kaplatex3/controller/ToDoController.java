package com.example.kaplatex3.controller;

import com.example.kaplatex3.model.ResultClass;
import com.example.kaplatex3.model.ToDoClass;
import com.example.kaplatex3.model.ToDoJsonClass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/content")
    public ResponseEntity<Object> getTodoData(@RequestParam String status, @RequestParam String sortBy){
        ResultClass<List<ToDoClass>> resultClass = new ResultClass<>(new ArrayList<>(),"");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if(!(sortBy.equals("") || sortBy.equals("ID") || sortBy.equals("DUE_DATE") || sortBy.equals("TITLE")) &&
                !(status.equals("ALL") || status.equals("PENDING") || status.equals("LATE") || status.equals("DONE")))
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(400));
        if(status.equals("ALL")){
            resultClass.setResult(logicEngine.sortList(toDoClassList,sortBy));
        }
        else{
            resultClass.setResult(logicEngine.sortList(toDoClassList.stream().filter(
                    l -> l.getStatus().equals(status)).collect(Collectors.toList()), sortBy));
        }
        return new ResponseEntity<>(gson.toJson(resultClass), HttpStatus.OK);
    }

    @PutMapping()
    public ResponseEntity<Object> updateToDo(@RequestParam Integer id, @RequestParam String status){
        ResultClass<String> resultClass = new ResultClass<>("","");
        ToDoClass todoFromId;
        String oldStatus;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if(!(status.equals("PENDING") || status.equals("LATE") || status.equals("DONE"))){
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        todoFromId = logicEngine.getTodoByID(toDoClassList, id);
        if(todoFromId == null) {
            resultClass.setError("Error: no such TODO with id " + id.toString());
            return new ResponseEntity<>(gson.toJson(resultClass),HttpStatusCode.valueOf(404));
        }
        oldStatus = todoFromId.getStatus();
        todoFromId.setStatus(status);
        resultClass.setResult(oldStatus);
        return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(200));
    }

    @DeleteMapping()
    public ResponseEntity<Object> deleteToDo(@RequestParam Integer id){
        ResultClass<Integer> resultClass = new ResultClass<>(toDoClassList.size(), "");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for(ToDoClass todo: toDoClassList){
            if(todo.getId().equals(id)) {
                toDoClassList.remove(todo);
                resultClass.setResult(toDoClassList.size());
                return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(200));
            }
        }
        resultClass.setError("Error: no such TODO with id " + id.toString());
        return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(404));
    }
}
