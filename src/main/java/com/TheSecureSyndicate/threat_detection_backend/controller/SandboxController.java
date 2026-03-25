//package com.TheSecureSyndicate.threat_detection_backend.controller;
//
//import com.TheSecureSyndicate.threat_detection_backend.dto.CuckooReport;
//import com.TheSecureSyndicate.threat_detection_backend.model.BinaryFile;
//import com.TheSecureSyndicate.threat_detection_backend.service.CuckooSandboxService;
//import com.TheSecureSyndicate.threat_detection_backend.service.FileStorageService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.nio.file.Path;
//import java.time.Instant;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/sandbox")
//public class SandboxController {
//
//    private static final Logger logger = LoggerFactory.getLogger(SandboxController.class);
//
//    private final FileStorageService fileStorageService;
////    private final CuckooSandboxService cuckooSandboxService;
//
////    public SandboxController(FileStorageService fileStorageService,
////                             CuckooSandboxService cuckooSandboxService) {
////        this.fileStorageService = fileStorageService;
////        this.cuckooSandboxService = cuckooSandboxService;
////    }
//
//    /**
//     * Uploads a file and triggers Cuckoo analysis.
//     */
//    @PostMapping("/analyze")
//    public ResponseEntity<String> analyzeFile(@RequestParam("file") MultipartFile file) {
//        try {
//            String id = UUID.randomUUID().toString();
//            Path savedPath = fileStorageService.saveTemp(file, id);
//
//            BinaryFile binary = new BinaryFile();
//            binary.setId(id);
//            binary.setFileName(file.getOriginalFilename());
//            binary.setFilePath(savedPath.toString());
//            binary.setSize(file.getSize());
//            binary.setEntropy(0.0); // Optional: compute entropy later
//            binary.setHash("TODO"); // Optional: compute hash later
//            binary.setUploadedAt(Instant.now());
//
////            cuckooSandboxService.submitBinary(binary);
//
//            return ResponseEntity.ok("✅ File submitted for analysis. ID: " + id);
//
//        } catch (Exception e) {
//            logger.error("❌ Failed to analyze file", e);
//            return ResponseEntity.status(500).body("❌ Analysis failed: " + e.getMessage());
//        }
//    }
//
//    /**
//     * (Optional) Endpoint to retrieve parsed report by task ID.
//     */
//    @GetMapping("/report/{taskId}")
//    public ResponseEntity<CuckooReport> getReport(@PathVariable int taskId) {
//        try {
//            CuckooReport report = cuckooSandboxService.getReport(taskId);
//            return ResponseEntity.ok(report);
//        } catch (Exception e) {
//            logger.error("❌ Failed to retrieve report", e);
//            return ResponseEntity.status(500).build();
//        }
//    }
//}
