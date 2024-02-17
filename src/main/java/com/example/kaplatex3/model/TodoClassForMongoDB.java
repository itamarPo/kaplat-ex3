package com.example.kaplatex3.model;

import com.example.kaplatex3.controller.MyTodoForMongoDB;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Document(collection = "todos")
public class TodoClassForMongoDB{
    @Id
    private Integer id;
    private Integer rawid;
    private String title;
    private String content;
    private Long dueDate;
    private String Status;

    public TodoClassForMongoDB(int id, int rawid ,String title, String content, Long dueDate, String status) {
        this.id = id;
        this.rawid = rawid;
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        Status = status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Integer getRawId() {
        return rawid;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return Status;
    }
}
