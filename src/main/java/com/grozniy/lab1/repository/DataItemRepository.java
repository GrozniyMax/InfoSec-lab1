package com.grozniy.lab1.repository;

import com.grozniy.lab1.model.DataItem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DataItemRepository extends CrudRepository<DataItem, Long> {

    List<DataItem> findAllByOrderByCreatedAtDesc();
}