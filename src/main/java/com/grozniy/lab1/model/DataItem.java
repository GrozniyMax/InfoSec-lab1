package com.grozniy.lab1.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("data_items")
public class DataItem {

    @Id
    private Long id;
    private String title;
    private String content;
    private Long ownerId;
    private Instant createdAt;

    public DataItem() {
    }

    private DataItem(Long id, String title, String content, Long ownerId, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public static DataItem create(String title, String content, Long ownerId) {
        return new DataItem(null, title, content, ownerId, Instant.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "DataItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", ownerId=" + ownerId +
                ", createdAt=" + createdAt +
                '}';
    }
}