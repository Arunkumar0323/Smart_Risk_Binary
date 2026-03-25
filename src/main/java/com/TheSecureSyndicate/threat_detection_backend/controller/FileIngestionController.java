package com.TheSecureSyndicate.threat_detection_backend.controller;

import com.TheSecureSyndicate.threat_detection_backend.dto.CIUploadRequest;
import com.TheSecureSyndicate.threat_detection_backend.dto.AnalysisSummaryResponse;
import com.TheSecureSyndicate.threat_detection_backend.model.AnalysisResult;
import com.TheSecureSyndicate.threat_detection_backend.model.BinaryFile;
import com.TheSecureSyndicate.threat_detection_backend.model.PESectionInfo;
import com.TheSecureSyndicate.threat_detection_backend.repository.AnalysisResultRepository;
import com.TheSecureSyndicate.threat_detection_backend.repository.BinaryFileRepository;
import com.TheSecureSyndicate.threat_detection_backend.service.CuckooSandboxService;
import com.TheSecureSyndicate.threat_detection_backend.service.FileStorageService;
import com.TheSecureSyndicate.threat_detection_backend.service.StaticAnalysisService;
import com.TheSecureSyndicate.threat_detection_backend.util.EntropyUtil;
import com.TheSecureSyndicate.threat_detection_backend.util.FuzzyHashUtil;
import com.TheSecureSyndicate.threat_detection_backend.util.HashUtil;
import com.TheSecureSyndicate.threat_detection_backend.util.PEFunctionParser;
import com.TheSecureSyndicate.threat_detection_backend.util.PEFunctionParser.PEFunctionResult;
import com.TheSecureSyndicate.threat_detection_backend.util.PEFunctionParser.PEHeaderResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileIngestionController {

    private static final Logger logger = LoggerFactory.getLogger(FileIngestionController.class);
    private static final long MAX_ALLOWED_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/x-dosexec",
            "application/octet-stream",
            "application/x-msdownload"
    );

    private final FileStorageService fileStorageService;
    private final BinaryFileRepository binaryFileRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final CuckooSandboxService cuckooSandboxService;
    private final StaticAnalysisService staticAnalysisService;

    public FileIngestionController(
            FileStorageService fileStorageService,
            BinaryFileRepository binaryFileRepository,
            AnalysisResultRepository analysisResultRepository,
            CuckooSandboxService cuckooSandboxService,
            StaticAnalysisService staticAnalysisService
    ) {
        this.fileStorageService = fileStorageService;
        this.binaryFileRepository = binaryFileRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.cuckooSandboxService = cuckooSandboxService;
        this.staticAnalysisService = staticAnalysisService;
    }

    @PostMapping("/upload")
    public ResponseEntity<AnalysisSummaryResponse> uploadFile(@RequestParam MultipartFile file) throws Exception {
        if (file.getSize() > MAX_ALLOWED_SIZE) {
            return ResponseEntity.badRequest().body(null);
        }

        String id = UUID.randomUUID().toString();
        Path savedPath = fileStorageService.saveTemp(file, id);
        String mimeType = Files.probeContentType(savedPath);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        if (!ALLOWED_TYPES.contains(mimeType)) {
            Files.deleteIfExists(savedPath);
            return ResponseEntity.badRequest().body(null);
        }

        byte[] fileBytes = Files.readAllBytes(savedPath);

        BinaryFile binary = new BinaryFile(
                id,
                file.getOriginalFilename(),
                savedPath.toString(),
                file.getSize(),
                EntropyUtil.calculateEntropy(fileBytes),
                HashUtil.calculateSHA256(savedPath),
                Instant.now()
        );
        binary.setSsdeepHash(FuzzyHashUtil.calculateSSDEEP(savedPath));
        binaryFileRepository.save(binary);

        // 🔍 Static analysis
        AnalysisResult result = staticAnalysisService.analyze(binary);

        // 🔍 PE function parsing
        try {
            PEFunctionResult functions = PEFunctionParser.parseFunctionsFromUpload(file);
            if (functions != null) {
                result.setImportedFunctions(functions.getImportedFunctions());
                result.setExportedFunctions(functions.getExportedFunctions());
                logger.info("Imported Functions: {}", functions.getImportedFunctions());
                logger.info("Exported Functions: {}", functions.getExportedFunctions());
            }
        } catch (Exception e) {
            logger.warn("Failed to parse PE functions: {}", e.getMessage());
        }

        // 🔍 PE header + section parsing
        try {
            PEHeaderResult header = PEFunctionParser.extractHeaderAndSections(savedPath.toFile());
            if (header != null) {
                // set filename from canonical source
                header.setFilename(binary.getOriginalFilename());
                result.setHeaderResult(header);

                logger.info("PE Machine Type: {}", header.getMachineType());
                logger.info("Entry Point: 0x{}", Long.toHexString(header.getEntryPoint()));
                logger.info("Image Base: 0x{}", Long.toHexString(header.getImageBase()));
                logger.info("Subsystem: {}", header.getSubsystem());
                logger.info("Sections count: {}", header.getSections().size());
                for (PESectionInfo s : header.getSections()) {
                    logger.info("Section: {} RVA=0x{} Entropy={}", s.getName(), Long.toHexString(s.getRva()), s.getEntropy());
                }
                result.setPacked(PEFunctionParser.isPacked(header));
            } else {
                logger.warn("extractHeaderAndSections returned null for file: {}", savedPath);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse PE headers/sections: {}", e.getMessage());
        }

        Files.deleteIfExists(savedPath);
        cuckooSandboxService.submitBinary(binary);

        // choose filename in order of trust: header -> binary -> "unknown"
        String responseFilename = "unknown";
        if (result.getHeaderResult() != null && result.getHeaderResult().getFilename() != null && !result.getHeaderResult().getFilename().isBlank()) {
            responseFilename = result.getHeaderResult().getFilename();
        } else if (binary != null && binary.getOriginalFilename() != null && !binary.getOriginalFilename().isBlank()) {
            responseFilename = binary.getOriginalFilename();
        }

        AnalysisSummaryResponse response = new AnalysisSummaryResponse(
                result.getBinaryId(),
                responseFilename,
                result.isYaraMatched(),
                result.getMatchedRules(),
                result.getMlRiskScore(),
                result.getClassification(),
                result.getMachineType(),
                result.getEntryPoint(),
                result.getImageBase(),
                result.getSubsystem(),
                result.getSections(),
                result.getPacked()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/ci-upload")
    public ResponseEntity<AnalysisSummaryResponse> uploadFromCI(@RequestBody CIUploadRequest request) throws Exception {
        byte[] fileBytes = Base64.getDecoder().decode(request.getBase64File());
        if (fileBytes.length > MAX_ALLOWED_SIZE) {
            return ResponseEntity.badRequest().body(null);
        }

        String id = UUID.randomUUID().toString();
        Path savedPath = fileStorageService.saveTemp(fileBytes, request.getFileName(), id);
        String mimeType = Files.probeContentType(savedPath);
        if (mimeType == null) mimeType = "application/octet-stream";
        if (!ALLOWED_TYPES.contains(mimeType)) {
            Files.deleteIfExists(savedPath);
            return ResponseEntity.badRequest().body(null);
        }

        BinaryFile binary = new BinaryFile(
                id,
                request.getFileName(),
                savedPath.toString(),
                fileBytes.length,
                EntropyUtil.calculateEntropy(fileBytes),
                HashUtil.calculateSHA256(savedPath),
                Instant.now()
        );
        binary.setSsdeepHash(FuzzyHashUtil.calculateSSDEEP(savedPath));
        binaryFileRepository.save(binary);

        AnalysisResult result = staticAnalysisService.analyze(binary);

        // 🔍 PE function parsing
        try {
            MultipartFile wrapped = new CIUploadRequest.MultipartWrapper(request.getFileName(), fileBytes);
            PEFunctionResult functions = PEFunctionParser.parseFunctionsFromUpload(wrapped);
            if (functions != null) {
                result.setImportedFunctions(functions.getImportedFunctions());
                result.setExportedFunctions(functions.getExportedFunctions());
                logger.info("Imported Functions: {}", functions.getImportedFunctions());
                logger.info("Exported Functions: {}", functions.getExportedFunctions());
            }
        } catch (Exception e) {
            logger.warn("Failed to parse PE functions: {}", e.getMessage());
        }

        // 🔍 PE header + section parsing
        try {
            PEHeaderResult header = PEFunctionParser.extractHeaderAndSections(savedPath.toFile());
            if (header != null) {
                header.setFilename(binary.getOriginalFilename());
                result.setHeaderResult(header);
                result.setPacked(PEFunctionParser.isPacked(header));

                logger.info("Machine Type: {}", header.getMachineType());
                logger.info("Entry Point: 0x{}", Long.toHexString(header.getEntryPoint()));
            } else {
                logger.warn("extractHeaderAndSections returned null for file: {}", savedPath);
            }
        } catch (Exception e) {
            logger.warn("Failed to parse PE headers/sections: {}", e.getMessage());
        }

        Files.deleteIfExists(savedPath);
        cuckooSandboxService.submitBinary(binary);

        // choose filename in order of trust: header -> binary -> "unknown"
        String responseFilename = "unknown";
        if (result.getHeaderResult() != null && result.getHeaderResult().getFilename() != null && !result.getHeaderResult().getFilename().isBlank()) {
            responseFilename = result.getHeaderResult().getFilename();
        } else if (binary != null && binary.getOriginalFilename() != null && !binary.getOriginalFilename().isBlank()) {
            responseFilename = binary.getOriginalFilename();
        }

        AnalysisSummaryResponse response = new AnalysisSummaryResponse(
                result.getBinaryId(),
                responseFilename,
                result.isYaraMatched(),
                result.getMatchedRules(),
                result.getMlRiskScore(),
                result.getClassification(),
                result.getMachineType(),
                result.getEntryPoint(),
                result.getImageBase(),
                result.getSubsystem(),
                result.getSections(),
                result.getPacked()
        );

        return ResponseEntity.ok(response);
    }
}
