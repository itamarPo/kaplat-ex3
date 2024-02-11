package com.example.kaplatex3.controller;

import com.example.kaplatex3.model.ToDoClass;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogicToDo {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public LogicToDo(){
        createMongoConnection();
        createProstegConnection();
    }

    private void createProstegConnection() {

    }

    private void createMongoConnection() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("localhost:27017"))
                .serverApi(serverApi)
                .build();
        mongoClient = MongoClients.create(settings);
        mongoDatabase = mongoClient.getDatabase("todos");
        try {
            // Send a ping to confirm a successful connection
            Bson command = new BsonDocument("ping", new BsonInt64(1));
            Document commandResult = mongoDatabase.runCommand(command);
            System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
        } catch (MongoException me) {
            System.err.println(me);
        }
    }

    public boolean checkIfTodoExist(List<ToDoClass> todoList, String title){
        for(ToDoClass todo: todoList){
            if(todo.getTitle().equals(title))
                return true;
        }
        return false;
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

    public int CountTodoFromDataBase(String dataBase, String... status){
        int count = 0;
        MongoCollection<Document> collection;
        if(dataBase.equals("MONGO"))
        {
            List<Integer> rawIds;
            collection = mongoDatabase.getCollection("todos");
            if(status == null)
                rawIds = collection.distinct("rawid", Integer.class).into(new ArrayList<>());
            else {
                Document filter = new Document("state", status);
                rawIds = collection.distinct("rawid", filter, Integer.class).into(new ArrayList<>());
            }
            count = rawIds.size();
        }

        return count;
    }

    public List<ToDoClass> sortListFromDataBase(String sortBy, String dataBase){
        List<ToDoClass> todoList = new ArrayList<>();
        if(dataBase.equals("MONGO")){
            MongoCollection<Document> collection= mongoDatabase.getCollection("todos");
            for (Document doc: collection.find()) {
                ToDoClass todo = new ToDoClass(doc.getInteger("rawid"), doc.getString("title"),
                        doc.getString("content"), doc.getLong("duedate"), doc.getString("state"));
                todoList.add(todo);
            }
        }
        else{}
        return sortList(todoList, sortBy);
    }
}
