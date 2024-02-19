package com.example.kaplatex3.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

@Entity
public class ToDoClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public ToDoClass() {

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

    @Override
    public boolean equals(Object obj) {
        ToDoClass checkedObj = (ToDoClass) obj;
       // return this.id == checkedObj.getId() && this.content == checkedObj.getContent() && this.dueDate == checkedObj.getDueDate()
       //         && this.title == checkedObj.getTitle() && this.Status == checkedObj.getStatus();
        if(this.id != checkedObj.getId())
            return false;
        else if (!this.Status.equals(checkedObj.getStatus()))
            return false;
        else if (!this.title.equals(checkedObj.getTitle()))
            return false;
        else if (this.dueDate.longValue() != checkedObj.getDueDate().longValue()) {
            System.out.println("WHYYY");
            return false;
        }
        else if (!this.content.equals(checkedObj.getContent()))
            return false;
        return true;
    }
}
