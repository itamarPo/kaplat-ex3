package com.example.kaplatex3.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetHealth {

    @GetMapping("/todo/health")
    @ResponseStatus(code = HttpStatus.OK)
    public String ok(){
        return "OK";
    }
}
