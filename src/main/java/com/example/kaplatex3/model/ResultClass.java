package com.example.kaplatex3.model;

public class ResultClass <T>{
    private T result;
    private String errorMessage;

    public ResultClass(T result, String error) {
        this.result = result;
        this.errorMessage = error;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public void setError(String error) {
        this.errorMessage = error;
    }
}
