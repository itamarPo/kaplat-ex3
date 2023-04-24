package com.example.kaplatex3.model;

public class ResultClass <T>{
    private T result;
    private String error;

    public ResultClass(T result, String error) {
        this.result = result;
        this.error = error;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public void setError(String error) {
        this.error = error;
    }
}
