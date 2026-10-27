package com.skillify;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreRepo extends JpaRepository<Score, Long> {
    List<Score> findByUserIdOrderByCreatedAtAsc(Long userId);
}
