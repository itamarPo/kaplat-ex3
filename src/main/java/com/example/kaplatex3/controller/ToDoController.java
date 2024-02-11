package com.example.kaplatex3.controller;

import com.example.kaplatex3.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.*;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


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
    private static int requestLogNumber;
    public static final Logger requestLogger = LoggerFactory.getLogger("request-logger");
   // private static final Logger stackLogger = LoggerFactory.getLogger("stack-logger");
    public static final Logger todoLogger = LoggerFactory.getLogger("todo-logger");

    @Autowired
    public ToDoController(LogicToDo logicEngine) {
        this.toDoClassList = new ArrayList<>();
        this.logicEngine = logicEngine;
        this.currentID = 0;
        this.requestLogNumber = 0;
        this.ConnectToMongoDB();
    }

    private void ConnectToMongoDB() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("localhost:27017"))
                .serverApi(serverApi)
                .build();
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("admin");
            try {
                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException me) {
                System.err.println(me);
            }
        }
    }

    public static String logEndAddition(){
        return " | request #" + requestLogNumber;
    }
    public static void handleLogRequest(String resource, String verb){
        requestLogNumber++;
        requestLogger.info("Incoming request | #" + requestLogNumber + " | resource: " + resource + " | HTTP Verb " + verb.toUpperCase() + logEndAddition());
    }

    public static void handleLogDebugRequest(long duration){
        requestLogger.debug("request #" + (requestLogNumber - 1) +" duration: " + duration + "ms" + logEndAddition());
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> createNewTodo(@RequestBody ToDoJsonClass todo){
        long startTime = System.currentTimeMillis();
        long endTime;
        handleLogRequest("/todo", "POST");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if(logicEngine.checkIfTodoExist(toDoClassList, todo.getTitle())){
            ResultClass<String> resultClass = new ResultClass<>("", "Error: TODO with the title  [" + todo.getTitle()
                    + "] already exists in the system");
            if(requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                handleLogDebugRequest(endTime - startTime);
            }
            todoLogger.error("Error: TODO with the title  [" + todo.getTitle() + "] already exists in the system" + logEndAddition());
           return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(409));
        }

        if(Instant.now().toEpochMilli() >= todo.getDueDate()){
            ResultClass<String> resultClass = new ResultClass<>("", "Error: Can’t create new TODO that its due date is in the past");
            if(requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                handleLogDebugRequest(endTime - startTime);
            }
            todoLogger.error("Error: Can’t create new TODO that its due date is in the past" + logEndAddition());
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(409));
        }

        endTime = System.currentTimeMillis();
        todoLogger.info("Creating new TODO with Title [" +  todo.getTitle() + "]" + logEndAddition());
        if(todoLogger.isDebugEnabled()) {
            todoLogger.debug("Currently there are " + this.currentID + " TODOs in the system. New TODO will be assigned with id " +
                    (this.currentID + 1) + logEndAddition());
        }
        if(requestLogger.isDebugEnabled()){
            handleLogDebugRequest(endTime - startTime);
        }

        this.currentID++;
        toDoClassList.add(new ToDoClass(this.currentID, todo.getTitle(), todo.getContent(), todo.getDueDate(), "PENDING"));
        FirstResultClass resultClass = new FirstResultClass(toDoClassList.get(toDoClassList.size()-1).getId());

        return new ResponseEntity<>(gson.toJson(resultClass), HttpStatus.OK);
    }

    @GetMapping("/size")
    public ResponseEntity<Object> countToDoByFilter(@RequestParam String status,
                                                    @RequestParam(required = false) String persistenceMethod){
        ResultClass<Integer> resultClass = new ResultClass<>(0,"");
        handleLogRequest("/todo/size", "GET");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        long startTime = System.currentTimeMillis();
        long endTime;

        if(status.equals("ALL")){
            if(persistenceMethod.equals("MONGO") || persistenceMethod.equals("POSTGRES")) {
                resultClass.setResult(logicEngine.CountTodoFromDataBase(persistenceMethod));
            }
            else
                resultClass.setResult(toDoClassList.size());
            if(requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                handleLogDebugRequest(endTime - startTime);
            }
            todoLogger.info("Total TODOs count for state " + status + " is " + toDoClassList.size() + logEndAddition());
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatus.OK);

        } else if (status.equals("PENDING") || status.equals("LATE") || status.equals("DONE")) {
            if(persistenceMethod.equals("MONGO") || persistenceMethod.equals("POSTGRES")) {
                resultClass.setResult(logicEngine.CountTodoFromDataBase(persistenceMethod, status));
            }
            else
                resultClass.setResult(logicEngine.CountByStatus(toDoClassList,status));
            if(requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                handleLogDebugRequest(endTime - startTime);
            }
            todoLogger.info("Total TODOs count for state " + status + " is " + resultClass.getResult() + logEndAddition());
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatus.OK);
        }
        else {
            if(requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                handleLogDebugRequest(endTime - startTime);
            }
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(400));
        }
    }

    @GetMapping("/content")
    public ResponseEntity<Object> getTodoData(@RequestParam String status,
                                              @RequestParam(required = false) String sortBy,
                                              @RequestParam(required = false) String persistenceMethod){
        ContentResultClass<List<ToDoClass>> resultClass = new ContentResultClass<>(new ArrayList<>());
        long endTime, startTime = System.currentTimeMillis();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<ToDoClass> filterdList;
        handleLogRequest("/todo/content", "GET");

        if(sortBy == null || sortBy.equals("")){
            sortBy = "ID";
        }

        if(!(sortBy.equals("ID") || sortBy.equals("DUE_DATE") || sortBy.equals("TITLE")) &&
                !(status.equals("ALL") || status.equals("PENDING") || status.equals("LATE") || status.equals("DONE"))) {
            if(requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                handleLogDebugRequest(endTime - startTime);
            }
            return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(400));
        }
        if(status.equals("ALL")){
            if(persistenceMethod == null)
                filterdList = logicEngine.sortList(toDoClassList,sortBy);
            else
                filterdList = logicEngine.sortListFromDataBase(sortBy, persistenceMethod);
            resultClass.setResult(filterdList);
        }
        else{
            if(persistenceMethod == null) {
                filterdList = logicEngine.sortList(toDoClassList.stream().filter(
                        l -> l.getStatus().equals(status)).collect(Collectors.toList()), sortBy);
            }
            else{
                filterdList = logicEngine.sortListFromDataBase(sortBy, persistenceMethod);
                filterdList = filterdList.stream().filter(l->l.getStatus().equals(status)).collect(Collectors.toList());
            }
            resultClass.setResult(filterdList);
        }
        if(requestLogger.isDebugEnabled()){
            endTime = System.currentTimeMillis();
            handleLogDebugRequest(endTime - startTime);
        }
        todoLogger.info("Extracting todos content. Filter: " + status + " | Sorting by: " + sortBy + logEndAddition());
        if(todoLogger.isDebugEnabled()){
            todoLogger.debug("There are a total of " + toDoClassList.size() +
                    " todos in the system. The result holds " + filterdList.size() + " todos" + logEndAddition());
        }
        return new ResponseEntity<>(gson.toJson(resultClass), HttpStatus.OK);
    }

    @PutMapping()
    public ResponseEntity<Object> updateToDo(@RequestParam Integer id, @RequestParam String status){
        ResultClass<String> resultClass = new ResultClass<>("","");
        ToDoClass todoFromId;
        long endTime, startTime = System.currentTimeMillis();
        String oldStatus;
        handleLogRequest("/todo", "PUT");
        todoLogger.info("Update TODO id [" + id + "] state to " + status + logEndAddition());

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if(!(status.equals("PENDING") || status.equals("LATE") || status.equals("DONE"))){
            if(requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                handleLogDebugRequest(endTime - startTime);
            }
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }


        //todoFromId = logicEngine.getTodoByID(toDoClassList, id);
        todoFromId = logicEngine.getTodoByIDFromDataBase(id);
        if(todoFromId == null) {
            resultClass.setError("Error: no such TODO with id " + id.toString());
            if(requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                handleLogDebugRequest(endTime - startTime);
            }
            todoLogger.error("Error: no such TODO with id " + id + logEndAddition());
            return new ResponseEntity<>(gson.toJson(resultClass),HttpStatusCode.valueOf(404));
        }

        oldStatus = todoFromId.getStatus();
        todoFromId.setStatus(status);
        resultClass.setResult(oldStatus);
        if(requestLogger.isDebugEnabled()){
            endTime = System.currentTimeMillis();
            handleLogDebugRequest(endTime - startTime);
        }
        if(todoLogger.isDebugEnabled()){
            todoLogger.debug("Todo id [" + id + "] state change: " + oldStatus + "--> " + status + logEndAddition());
        }
        return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(200));
    }

    @DeleteMapping()
    public ResponseEntity<Object> deleteToDo(@RequestParam Integer id){
        ResultClass<Integer> resultClass = new ResultClass<>(toDoClassList.size(), "");
        long endTime, startTime = System.currentTimeMillis();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        handleLogRequest("/todo", "DELETE");


        for(ToDoClass todo: toDoClassList){
            if(todo.getId().equals(id)) {
                toDoClassList.remove(todo);
                resultClass.setResult(toDoClassList.size());
                if(requestLogger.isDebugEnabled()){
                    endTime = System.currentTimeMillis();
                    handleLogDebugRequest(endTime - startTime);
                }
                todoLogger.info("Removing todo id " + id + logEndAddition());
                if(todoLogger.isDebugEnabled()){
                    todoLogger.debug("After removing todo id [" + id + "] there are " + toDoClassList.size() + " TODOs in the system" + logEndAddition());
                }
                return new ResponseEntity<>(gson.toJson(resultClass), HttpStatusCode.valueOf(200));
            }
        }
        DeleteErrorClass deleteResultClass = new DeleteErrorClass("Error: no such TODO with id " + id.toString());
        todoLogger.error("Error: no such TODO with id " + id + logEndAddition());
        if(requestLogger.isDebugEnabled()){
            endTime = System.currentTimeMillis();
            handleLogDebugRequest(endTime - startTime);
        }
        return new ResponseEntity<>(gson.toJson(deleteResultClass), HttpStatusCode.valueOf(404));
    }
}
