package com.example.kaplatex3.model;

public class ContentResultClass<T> {
    private T result;

    public ContentResultClass(T result) {
        this.result = result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
