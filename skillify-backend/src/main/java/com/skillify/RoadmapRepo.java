package com.skillify;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RoadmapRepo extends JpaRepository<RoadmapEntry, Long> {
    List<RoadmapEntry> findByUserId(Long userId);
    Optional<RoadmapEntry> findByUserIdAndTopic(Long userId, String topic);
}
