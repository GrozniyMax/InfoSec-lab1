package com.grozniy.lab1.controller;

import com.grozniy.lab1.dto.DataItemResponse;
import com.grozniy.lab1.service.DataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DataController {

    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/data")
    public List<DataItemResponse> getData() {
        return dataService.getAllData();
    }
}