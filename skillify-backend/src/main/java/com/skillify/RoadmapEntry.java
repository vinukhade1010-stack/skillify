package com.skillify;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class RoadmapEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Long userId;
    public String topic;
    public int score;
    @Column(length = 20000) public String body;   // roadmap JSON from the AI
    public Instant updatedAt = Instant.now();
}
