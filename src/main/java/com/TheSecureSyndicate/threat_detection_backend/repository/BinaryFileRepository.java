package com.TheSecureSyndicate.threat_detection_backend.repository;

import com.TheSecureSyndicate.threat_detection_backend.model.BinaryFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryFileRepository extends JpaRepository<BinaryFile, String> {
}
