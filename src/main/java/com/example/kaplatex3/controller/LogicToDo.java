package com.example.kaplatex3.controller;

import com.example.kaplatex3.model.ToDoClass;
import com.example.kaplatex3.model.ToDoJsonClass;
import com.example.kaplatex3.model.TodoClassForMongoDB;
import com.mongodb.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class LogicToDo {

    //private MongoClient mongoClient;

    private final MongoTemplate mongoTemplate;
    //private MongoDatabase mongoDatabase;
    private Connection postGresConnection;

    @Autowired
    public LogicToDo(MongoTemplate mongoTemplate) {
       this.mongoTemplate = mongoTemplate;
        createProstegConnection();
    }

    private void createProstegConnection() {
        String url = "jdbc:postgresql://localhost:5432/todos"; // Replace mydatabase with your actual database name
        String username = "postgres"; // Replace yourusername with your actual username
        String password = "docker"; // Replace yourpassword with your actual password

        // Establishing a connection
        try (Connection postGresConnection = DriverManager.getConnection(url, username, password)) {
            this.postGresConnection = postGresConnection;
            System.out.println("Connected to the PostgreSQL database!");
            //String switchDatabaseQuery = "SET DATABASE_NAME TO todos";
            try (Statement statement = this.postGresConnection.createStatement()) {
                //statement.execute(switchDatabaseQuery);
                System.out.println("Connected to the 'todo' database!");
            } catch (SQLException e) {
                System.err.println("Failed to switch to the 'todo' database!");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to the PostgreSQL database!");
            e.printStackTrace();
        }
    }

    private void createMongoTemplate() {
//        ServerApi serverApi = ServerApi.builder()
//                .version(ServerApiVersion.V1)
//                .build();
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
//                .serverApi(serverApi)
//                .build();
//        String connectionString = "mongodb://127.0.0.1:27017";
//        mongoClient = MongoClients.create(connectionString);
//        mongoDatabase = mongoClient.getDatabase("todos");
//        try {
//            // Send a ping to confirm a successful connection
//            Bson command = new BsonDocument("ping", new BsonInt64(1));
//            Document commandResult = mongoDatabase.runCommand(command);
//            System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
//        } catch (MongoException me) {
//            System.err.println(me);
//        }
      //  return new MongoTemplate(new SimpleMongoClientDatabaseFactory(), "todos");
    }

    public boolean checkIfTodoExist(List<ToDoClass> todoList, String title) {
        for (ToDoClass todo : todoList) {
            if (todo.getTitle().equals(title))
                return true;
        }
        return false;
    }

    public int CountByStatus(List<ToDoClass> todoList, String status) {
        int count = 0;
        for (ToDoClass todo : todoList) {
            if (todo.getStatus().equals(status))
                count++;
        }
        return count;
    }

    public List<ToDoClass> sortList(List<ToDoClass> todoList, String sortBy) {
        List<ToDoClass> returnList;
        if (sortBy.equals(""))
            returnList = todoList;
        else if (sortBy.equals("ID"))
            returnList = todoList.stream().sorted(Comparator.comparing(ToDoClass::getId)).collect(Collectors.toList());
        else if (sortBy.equals("DUE_DATE"))
            returnList = todoList.stream().sorted(Comparator.comparing(ToDoClass::getDueDate)).collect(Collectors.toList());
        else
            returnList = todoList.stream().sorted(Comparator.comparing(ToDoClass::getTitle)).collect(Collectors.toList());
        return returnList;
    }

    public ToDoClass getTodoByID(List<ToDoClass> todoList, Integer id) {
        for (ToDoClass todo : todoList) {
            if (todo.getId().equals(id))
                return todo;
        }
        return null;
    }

    private Integer countTodoFromMongoALL(){
            Aggregation aggregation = newAggregation(
                    group("rawid"),
                    count().as("count")
            );

            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "todo", Document.class);
            return results.getMappedResults().size();
    }

    public Integer getDistinctRawidsWithStatus(String status) {
        Aggregation aggregation = newAggregation(
                match(Criteria.where("state").is(status)),
                group("rawid"),
                project("rawid").andExclude("_id")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "todo", Document.class);
        List<Integer> rawids = new ArrayList<>();
        for (Document document : results.getMappedResults()) {
            rawids.add(document.getInteger("rawid"));
        }
        return rawids.size();
    }

    public int CountTodoFromDataBase(String dataBase, String... status) {
        int count = 0;
        //MongoCollection<Document> collection;
        if (dataBase.equals("MONGO")) {
            //List<Integer> rawIds;
            //collection = mongoDatabase.getCollection("todos");
            if (status == null)
                count = countTodoFromMongoALL();
            else {
               count = getDistinctRawidsWithStatus(status[0]);
            }
        } else {
            String query;
            if(status == null)
                query = "SELECT COUNT(rawid) AS rawIdcount FROM todo";
            else {
                query = "SELECT COUNT(rawid) AS rawIdcount FROM todo WHERE status = " + status;
            }
            try (Statement statement = postGresConnection.createStatement()) {
                // Executing the query
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    // Checking if the result set has any rows
                    if (resultSet.next()) {
                        int rawIdcount = resultSet.getInt("rawIdcount");
                        System.out.println("Number of rawid with status " + status + ": " + rawIdcount);
                        count = rawIdcount;
                    } else {
                        System.out.println("No rows returned from the query.");
                        count = 0;
                    }
                }
            }
            catch (SQLException e) {
                System.err.println("Failed to connect to the PostgreSQL database or execute the query!");
                e.printStackTrace();
                count = -1;
            }
        }

        return count;
    }

    public List<TodoClassForMongoDB> getAllTodos() {
        return mongoTemplate.findAll(TodoClassForMongoDB.class);
    }
    public List<ToDoClass> sortListFromDataBase(String sortBy, String dataBase) throws Exception{
        List<ToDoClass> todoList = new ArrayList<>();
        String query;
        if (dataBase.equals("MONGO")) {
            List<TodoClassForMongoDB> todoListFromMongo = getAllTodos();
            for(TodoClassForMongoDB todoClassForMongoDB: todoListFromMongo){
               todoList.add(new ToDoClass(todoClassForMongoDB.getRawId(), todoClassForMongoDB.getTitle(),
                       todoClassForMongoDB.getContent(), todoClassForMongoDB.getDueDate(), todoClassForMongoDB.getStatus()));
            }
        } else {
            try (Statement statement = postGresConnection.createStatement()) {
                query = "SELECT * FROM todo";
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    // Iterating through the result set
                    while (resultSet.next()) {
                        int rawid = resultSet.getInt("rawid");
                        String title = resultSet.getString("title");
                        String content = resultSet.getString("content");
                        String status = resultSet.getString("status");
                        long duedate = resultSet.getLong("duedate");
                        ToDoClass toDoClass = new ToDoClass(rawid, title, content, duedate, status);
                        todoList.add(toDoClass);
                    }
                }
            }
            catch(Exception e){
                throw e;
            }
        }

        return sortList(todoList, sortBy);
    }

    public ToDoClass getTodoByIDFromDataBase(Integer rawID) {
        ToDoClass toDoClass = getToDoClassFromMongo(rawID);
        if(toDoClass != null)
            if (toDoClass.equals(getToDoClassFromPostGres(rawID)))
                return toDoClass;
            else
                return null;
        else
            return null;
    }

    private ToDoClass getToDoClassFromPostGres(Integer rawID) {
        ToDoClass todo;
        String query = "SELECT * FROM todo WHERE rawid = " + rawID;
        try(Statement statement = postGresConnection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(query)) {
                // Checking if the result set has any rows
                if (resultSet.next()) {
                    int rawid = resultSet.getInt("rawid");
                    String title = resultSet.getString("title");
                    String content = resultSet.getString("content");
                    String status = resultSet.getString("status");
                    long duedate = resultSet.getLong("duedate");
                    todo = new ToDoClass(rawid, title, content, duedate, status);
                }
                else{
                    todo = null;
                }
            }
        }
        catch (Exception e){
            todo = null;
        }
        return todo;
    }

    private ToDoClass getToDoClassFromMongo(Integer rawID) {
//        MongoCollection<Document> collection = mongoDatabase.getCollection("todos");
//        Bson filter = Filters.eq("rawid", rawID);
//        Document todoDoc = collection.find(filter).first();
//        if (todoDoc != null) {
//            ToDoClass todo = new ToDoClass(todoDoc.getInteger("rawid"), todoDoc.getString("title"),
//                    todoDoc.getString("content"), todoDoc.getLong("duedate"), todoDoc.getString("state"));
//            return todo;
//        } else
//            return null;

        Query query = new Query();
        query.addCriteria(Criteria.where("rawid").is(rawID));
        TodoClassForMongoDB todoClassForMongoDB = mongoTemplate.findOne(query, TodoClassForMongoDB.class);
        if(todoClassForMongoDB != null){
            ToDoClass todo = new ToDoClass(todoClassForMongoDB.getRawId(), todoClassForMongoDB.getTitle(), todoClassForMongoDB.getContent()
            , todoClassForMongoDB.getDueDate(), todoClassForMongoDB.getStatus());
            return todo;
        }
        return null;
    }

    public void UpdateDataBasesStatusByID(String newStatus, int rawID) throws Exception {
        Query queryMongo = new Query();
        queryMongo.addCriteria(Criteria.where("rawid").is(rawID));
        Update update = new Update();
        update.set("state", newStatus);
        String query = String.format("UPDATE todo SET state = %s WHERE rawid = %d",newStatus, rawID);
        try {
            mongoTemplate.updateFirst(queryMongo, update, TodoClassForMongoDB.class);
            PreparedStatement preparedStatement = postGresConnection.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            throw exception;
        }
    }

    public boolean checkIfTodoFromDataBasesExist(String title) {
        return checkIfTodoExistInMongo(title) && checkIfTodoExistInPostGres(title);
    }

    private boolean checkIfTodoExistInPostGres(String title) {
        return false;
    }

    private boolean checkIfTodoExistInMongo(String title) {
//        MongoCollection<Document> collection = mongoDatabase.getCollection("todos");
//        Bson filter = Filters.eq("title", title);
//        if (collection.find(filter).first() != null)
//            return true;
//        return false;
        Query query = new Query();
        query.addCriteria(Criteria.where("title").is(title));

        return mongoTemplate.exists(query, TodoClassForMongoDB.class);
    }

    public Integer createNewTodoInDataBases(ToDoJsonClass newTodo) throws Exception {
        //MongoCollection<Document> collection = mongoDatabase.getCollection("todos");
        int maxRawId = createNewTodoInMongoDataBase(newTodo);
        int maxRawIdPostgres = createNewTodoInPostGresDataBase(newTodo);
        if(maxRawId != maxRawIdPostgres)
            throw new Exception("Inconsistent rawid");
        return maxRawId;
    }

    private Integer createNewTodoInPostGresDataBase(ToDoJsonClass newTodo) {
        String query = "SELECT MAX(rawid) AS max_rawid FROM todo";

        // Creating a statement
        try (Statement statement = postGresConnection.createStatement()) {
            // Executing the query
            try (ResultSet resultSet = statement.executeQuery(query)) {
                // Checking if the result set has any rows
                if (resultSet.next()) {
                    int maxRawid = resultSet.getInt("max_rawid");
                    String queryInsert = "INSERT INTO todo (rawid, title, content, duedate, state) VALUES (" + (maxRawid + 1) +
                            ", " + newTodo.getTitle() + ", " + newTodo.getContent() + ", " + newTodo.getDueDate() +
                            ", PENDING)";
                    //System.out.println("Maximum rawid: " + maxRawid);
                    statement.executeUpdate(queryInsert);
                    return maxRawid + 1;
                } else {
                    System.out.println("No rows returned from the query.");
                    return -1;
                }
            }
        }
        catch(SQLException e)
        {
            System.err.println("Failed to connect to the PostgreSQL database or execute the query!");
            e.printStackTrace();
        }
        return -1;
    }

    private int createNewTodoInMongoDataBase(ToDoJsonClass newTodo) {
//        Document groupStage = new Document("$group", new Document("_id", null)
//                .append("maxRawId", new Document("$max", "$rawid")));
//        Document result = collection.aggregate(Arrays.asList(groupStage)).first();
//        int maxRawId = result.getInteger("maxRawId", 0) + 1;
//        Document newDoc = new Document("rawid", maxRawId).append("title", newTodo.getTitle()).append("status", "PENDING")
//                .append("content", newTodo.getContent()).append("duedate", newTodo.getContent());
//        collection.insertOne(newDoc);
//        return maxRawId;
        Aggregation aggregation = newAggregation(group().max("rawid").as("maxRawid"));
        Aggregation aggregationForId = newAggregation(group().max("id").as("maxid"));
        AggregationResults<Document> resultsMaxRawId = mongoTemplate.aggregate(aggregation, "todo", Document.class);
        AggregationResults<Document> resultsMaxId = mongoTemplate.aggregate(aggregation, "todo", Document.class);
        Document resultDocumentMaxRawId = resultsMaxRawId.getUniqueMappedResult();
        Document resultDocumentMaxId = resultsMaxId.getUniqueMappedResult();
        TodoClassForMongoDB newTodoClass = new TodoClassForMongoDB(resultDocumentMaxId.getInteger("maxid"),
                resultDocumentMaxRawId.getInteger("maxRawid"), newTodo.getTitle(), newTodo.getContent(), newTodo.getDueDate(), "PENDING");
        mongoTemplate.insert(newTodoClass);
        return resultDocumentMaxRawId.getInteger("maxRawid");
    }

    public Integer DeleteTodoFromDataBases(Integer rawID) {
        Integer count = 0;
        String query = String.format("DELETE FROM todo WHERE rawid = %d", rawID);
//        MongoCollection<Document> collection = mongoDatabase.getCollection("todos");
//        Document filter = new Document("rawid", rawID);
//        collection.deleteOne(filter);
       // for(Document doc: collection.find(new Document("rawid", new Document("$exists", true)))){
        //    count++;
        //}
        Query queryForMongo = new Query();
        queryForMongo.addCriteria(Criteria.where("rawid").is(rawID));
        mongoTemplate.remove(queryForMongo, TodoClassForMongoDB.class);

        count = CountTodoFromDataBase("MONGO");
        try (PreparedStatement preparedStatement = postGresConnection.prepareStatement(query)) {
            preparedStatement.executeUpdate();
        }
     catch (SQLException e) {
        System.err.println("Failed to connect to the PostgreSQL database or execute the query!");
        e.printStackTrace();
    }
        return count;
    }
}
