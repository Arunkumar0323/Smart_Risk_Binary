package com.TheSecureSyndicate.threat_detection_backend.repository;

import com.TheSecureSyndicate.threat_detection_backend.model.YaraResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YaraResultRepository extends JpaRepository<YaraResult, String> {
}
