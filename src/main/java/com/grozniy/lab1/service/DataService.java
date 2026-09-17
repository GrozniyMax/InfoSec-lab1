package com.grozniy.lab1.service;

import com.grozniy.lab1.dto.DataItemResponse;
import com.grozniy.lab1.model.DataItem;
import com.grozniy.lab1.repository.DataItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataService {

    private final DataItemRepository dataItemRepository;

    public DataService(DataItemRepository dataItemRepository) {
        this.dataItemRepository = dataItemRepository;
    }

    public List<DataItemResponse> getAllData() {
        return dataItemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DataItemResponse toResponse(DataItem item) {
        return new DataItemResponse(item.getId(), item.getTitle(), item.getContent(), item.getCreatedAt());
    }
}