package com.example.kaplatex3.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/logs")
public class LoggerEndPoint {

    @Autowired
    public LoggerEndPoint(){}
    @GetMapping(path = "/level")
    public String GetLogLevel(@RequestParam(name = "logger-name") String logName){
        long startTime = System.currentTimeMillis();
        long endTime;
        ToDoController.handleLogRequest("/log/level", "GET");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        if(logName.equals(ToDoController.requestLogger.getName())) {
            ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(ToDoController.requestLogger.getName());
            Level logLevel = logbackLogger.getLevel();
            if(ToDoController.requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                ToDoController.handleLogDebugRequest(endTime - startTime);
            }
            return logLevel.toString().toUpperCase();
        }
        else if(logName.equals(ToDoController.todoLogger.getName())){
            ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(ToDoController.todoLogger.getName());
            Level logLevel = logbackLogger.getLevel();
            if(ToDoController.requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                ToDoController.handleLogDebugRequest(endTime - startTime);
            }
            return logLevel.toString().toUpperCase();
        }
        else{
            if(ToDoController.requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                ToDoController.handleLogDebugRequest(endTime - startTime);
            }

            return "Log-name not found";
        }
    }

    @PutMapping(path = "/level")
    public String SetLogLevel(@RequestParam(name = "logger-name") String logName, @RequestParam(name = "logger-level") String logLevel){
        long startTime = System.currentTimeMillis();
        long endTime;
        ToDoController.handleLogRequest("/log/level", "PUT");
        if(!(logLevel.equals("INFO") || logLevel.equals("DEBUG") | logLevel.equals("ERROR"))){
            return "Bad loglevel set request";
        }
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        if(logName.equals(ToDoController.requestLogger.getName())) {
            ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(ToDoController.requestLogger.getName());
            switch (logLevel){
                case "INFO":
                    logbackLogger.setLevel(Level.INFO);
                    break;
                case "DEBUG":
                    logbackLogger.setLevel(Level.DEBUG);
                    break;
                case "ERROR":
                    logbackLogger.setLevel(Level.ERROR);
                    break;
            }
            if(ToDoController.requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                ToDoController.handleLogDebugRequest(endTime - startTime);
            }
            return logLevel.toUpperCase();
        }
        else if(logName.equals(ToDoController.todoLogger.getName())){
            ch.qos.logback.classic.Logger logbackLogger = loggerContext.getLogger(ToDoController.todoLogger.getName());
            switch (logLevel){
                case "INFO":
                    logbackLogger.setLevel(Level.INFO);
                    break;
                case "DEBUG":
                    logbackLogger.setLevel(Level.DEBUG);
                    break;
                case "ERROR":
                    logbackLogger.setLevel(Level.ERROR);
                    break;
            }
            if(ToDoController.requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                ToDoController.handleLogDebugRequest(endTime - startTime);
            }
            return logLevel.toUpperCase();
        }
        else{
            if(ToDoController.requestLogger.isDebugEnabled()){
                endTime = System.currentTimeMillis();
                ToDoController.handleLogDebugRequest(endTime - startTime);
            }
            return "Bad log-name";
        }
    }
}
