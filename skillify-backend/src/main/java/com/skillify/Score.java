package com.skillify;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Score {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Long userId;
    public String topic;
    public int score;
    public Instant createdAt = Instant.now();
}
