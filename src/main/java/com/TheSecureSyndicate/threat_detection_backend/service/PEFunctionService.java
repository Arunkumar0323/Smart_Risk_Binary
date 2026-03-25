package com.TheSecureSyndicate.threat_detection_backend.service;

import com.TheSecureSyndicate.threat_detection_backend.model.PESectionInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PEFunctionService {

    public static class PEFunctionResult {
        private final List<String> importedFunctions;
        private final List<String> exportedFunctions;

        public PEFunctionResult(List<String> importedFunctions, List<String> exportedFunctions) {
            this.importedFunctions = importedFunctions;
            this.exportedFunctions = exportedFunctions;
        }

        public List<String> getImportedFunctions() { return importedFunctions; }
        public List<String> getExportedFunctions() { return exportedFunctions; }
    }

    public static class PEHeaderResult {
        private String machineType;
        private long entryPoint;
        private long imageBase;
        private String subsystem;
        private String dllCharacteristics;
        private int numSections;
        private String characteristics;
        private List<PESectionInfo> sections = new ArrayList<>();

        // Public getters
        public String getMachineType() { return machineType; }
        public long getEntryPoint() { return entryPoint; }
        public long getImageBase() { return imageBase; }
        public String getSubsystem() { return subsystem; }
        public String getDllCharacteristics() { return dllCharacteristics; }
        public int getNumSections() { return numSections; }
        public String getCharacteristics() { return characteristics; }
        public List<PESectionInfo> getSections() { return sections; }
    }

    public static PEFunctionResult parseFunctionsFromUpload(MultipartFile file) throws Exception {
        File tempFile = convertToFile(file);
        try {
            List<String> imports = extractImportedFunctions(tempFile);
            List<String> exports = extractExportedFunctions(tempFile);
            return new PEFunctionResult(imports, exports);
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    public static PEHeaderResult extractHeaderAndSections(File peFile) throws Exception {
        Objects.requireNonNull(peFile, "peFile cannot be null");
        // Example: Use your existing PE library here
        PEHeaderResult result = new PEHeaderResult();

        // Populate dummy data for template purposes (replace with actual PE parsing library)
        result.machineType = "x86_64";
        result.entryPoint = 0x1000;
        result.imageBase = 0x400000;
        result.subsystem = "WINDOWS_GUI";
        result.dllCharacteristics = "0x8160";
        result.numSections = 3;

        PESectionInfo s1 = new PESectionInfo();
        s1.setName(".text");
        s1.setRva(0x1000);
        s1.setVirtualSize(0x2000);
        s1.setRawSize(0x2000);
        s1.setEntropy(6.7);
        s1.setCharacteristics("CODE");
        result.sections.add(s1);

        PESectionInfo s2 = new PESectionInfo();
        s2.setName(".data");
        s2.setRva(0x3000);
        s2.setVirtualSize(0x1000);
        s2.setRawSize(0x1000);
        s2.setEntropy(3.2);
        s2.setCharacteristics("DATA");
        result.sections.add(s2);

        return result;
    }

    private static File convertToFile(MultipartFile multipartFile) throws IOException {
        File convFile = File.createTempFile("upload_", ".bin");
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(multipartFile.getBytes());
        }
        return convFile;
    }

    private static List<String> extractImportedFunctions(File peFile) {
        // Dummy for template, replace with actual library extraction
        List<String> list = new ArrayList<>();
        list.add("KERNEL32::CreateFileA");
        list.add("USER32::MessageBoxA");
        return list;
    }

    private static List<String> extractExportedFunctions(File peFile) {
        // Dummy for template, replace with actual library extraction
        List<String> list = new ArrayList<>();
        list.add("MyExportedFunction");
        return list;
    }
}
