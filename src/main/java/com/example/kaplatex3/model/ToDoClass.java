package com.example.kaplatex3.model;

import java.util.Date;

public class ToDoClass {
    private Integer id;
    private String title;
    private String content;
    private Long dueDate;
    private String Status;

    public ToDoClass(int id, String title, String content, Long dueDate, String status) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        Status = status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public Integer getId() {
        return id;
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
