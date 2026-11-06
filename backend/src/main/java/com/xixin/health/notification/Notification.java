package com.xixin.health.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String notificationType;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 512)
    private String content;

    @Column(nullable = false)
    private boolean readFlag;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    protected Notification() {
    }

    public Notification(Long userId, String notificationType, String title, String content) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public void markRead() {
        this.readFlag = true;
        this.readAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getNotificationType() { return notificationType; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isReadFlag() { return readFlag; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
}
