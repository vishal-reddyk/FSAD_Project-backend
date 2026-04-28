package com.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.entity.Recommendation;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}