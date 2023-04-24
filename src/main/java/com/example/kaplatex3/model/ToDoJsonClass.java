package com.example.kaplatex3.model;

import java.util.Date;

public class ToDoJsonClass {
    private String title;
    private String content;
    private Long dueDate;

    public ToDoJsonClass(String title, String content, Long dueDate) {
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
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
}
