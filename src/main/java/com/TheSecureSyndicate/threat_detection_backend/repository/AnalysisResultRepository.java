package com.TheSecureSyndicate.threat_detection_backend.repository;

import com.TheSecureSyndicate.threat_detection_backend.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, String> {

    Optional<AnalysisResult> findByBinaryId(String binaryId);

    Optional<AnalysisResult> findByCuckooTaskId(Integer cuckooTaskId);
}
