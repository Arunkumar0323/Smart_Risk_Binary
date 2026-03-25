package com.TheSecureSyndicate.threat_detection_backend.service;

import com.github.katjahahn.parser.PELoader;
import com.github.katjahahn.parser.PEData;
import com.github.katjahahn.parser.coffheader.COFFFileHeader;
import com.github.katjahahn.parser.optheader.OptionalHeader;
import com.github.katjahahn.parser.sections.SectionHeader;
import com.github.katjahahn.parser.sections.SectionTable;
import com.github.katjahahn.parser.sections.edata.ExportEntry;
import com.github.katjahahn.parser.sections.idata.ImportDLL;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.TheSecureSyndicate.threat_detection_backend.model.PESectionInfo;
import com.TheSecureSyndicate.threat_detection_backend.model.BinaryFile;
import com.TheSecureSyndicate.threat_detection_backend.model.AnalysisResult;
import com.TheSecureSyndicate.threat_detection_backend.util.PEFunctionParser;
import com.TheSecureSyndicate.threat_detection_backend.util.PEFunctionParser.PEHeaderResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StaticAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(StaticAnalysisService.class);
    private static final int CHARACTERISTICS_DB_MAX = 4000;

    // ========================================================
    // IMPORT / EXPORT PARSING
    // ========================================================
    public static List<String> extractImportedFunctions(File peFile) throws Exception {
        Objects.requireNonNull(peFile, "peFile cannot be null");
        List<String> results = new ArrayList<>();
        PEData data = PELoader.loadPE(peFile);
        if (data == null) return results;

        List<ImportDLL> dlls = data.loadImports();
        if (dlls == null) return results;

        for (ImportDLL dll : dlls) {
            if (dll == null) continue;
            String dllName = safeInvokeString(dll, "getName", "name");
            if (dllName == null) dllName = "UNKNOWN_DLL";

            Object allImports = safeInvokeObject(dll, "getAllImports", "allImports", "getImports");
            if (allImports instanceof List) {
                for (Object imp : (List<?>) allImports) {
                    String fname = extractNameFromImportObject(imp);
                    if (fname != null && !fname.isEmpty())
                        results.add(dllName + "::" + fname);
                    else {
                        String ord = extractOrdinalFromImportObject(imp);
                        if (ord != null && !ord.isEmpty())
                            results.add(dllName + "::Ordinal_" + ord);
                        else {
                            String fallback = safeToString(imp);
                            if (fallback != null && !fallback.isEmpty())
                                results.add(dllName + "::" + fallback);
                        }
                    }
                }
            }
        }
        return results;
    }

    public static List<String> extractExportedFunctions(File peFile) throws Exception {
        Objects.requireNonNull(peFile, "peFile cannot be null");
        List<String> results = new ArrayList<>();
        PEData data = PELoader.loadPE(peFile);
        if (data == null) return results;

        List<ExportEntry> entries = data.loadExports();
        if (entries == null) return results;

        for (ExportEntry e : entries) {
            if (e == null) continue;
            // use shared helper from PEFunctionParser (must be declared public static there)
            String name = PEFunctionParser.tryInvokeAsString(e, "getName", "name", "toString");
            if (name != null && !name.isEmpty()) {
                results.add(name);
            } else {
                String ord = PEFunctionParser.tryInvokeAsString(e, "ordinal", "getOrdinal");
                if (ord != null && !ord.isEmpty()) {
                    results.add("Ordinal_" + ord);
                } else {
                    String fallback = safeToString(e);
                    if (fallback != null && !fallback.isEmpty()) {
                        results.add(fallback);
                    }
                }
            }
        }
        return results;
    }

    // ========================================================
    // HEADER & SECTION PARSING
    // ========================================================
    public static PEHeaderResult extractHeaderAndSections(File peFile) throws Exception {
        Objects.requireNonNull(peFile, "peFile cannot be null");

        PEData data = PELoader.loadPE(peFile);
        if (data == null) return null;

        COFFFileHeader coff = data.getCOFFFileHeader();
        OptionalHeader optional = data.getOptionalHeader();
        SectionTable secTable = data.getSectionTable();

        PEHeaderResult result = new PEHeaderResult();
        result.setFilename(peFile.getName() != null ? peFile.getName() : "unknown");

        // COFF Header
        if (coff != null) {
            result.setMachineType(coff.getMachineType() != null ? coff.getMachineType().name() : "UNKNOWN");
            Date dt = coff.getTimeDate();
            result.setTimestamp(dt != null ? dt.getTime() : 0L);
            result.setNumSections(coff.getNumberOfSections());
            result.setCharacteristics(coff.getCharacteristics() != null ? coff.getCharacteristics().toString() : "");
        }

        // Optional Header
        if (optional != null) {
            Long entryPoint = getOptionalHeaderLong(optional, "getAddressOfEntryPoint", "getEntryPoint");
            Long imageBase = getOptionalHeaderLong(optional, "getImageBase", "getBaseOfImage");
            String subsystem = getOptionalHeaderString(optional, "getSubsystem", "subsystem");
            String dllChars = getOptionalHeaderString(optional, "getDllCharacteristics", "dllCharacteristics");

            result.setEntryPoint(entryPoint != null ? entryPoint : 0L);
            result.setImageBase(imageBase != null ? imageBase : 0L);
            result.setSubsystem(subsystem != null ? subsystem : "UNKNOWN");
            result.setDllCharacteristics(dllChars != null ? dllChars : "");
        }

        // SECTION TABLE
        if (secTable != null && secTable.getSectionHeaders() != null) {
            for (SectionHeader section : secTable.getSectionHeaders()) {
                if (section == null) continue;
                PESectionInfo info = new PESectionInfo();

                String name = getString(section, "getName", "name", "getUnfilteredName");
                Long virtualSize = getLong(section, "getVirtualSize", "getAlignedVirtualSize");
                Long rawSize = getLong(section, "getSizeOfRawData", "getAlignedSizeOfRaw");
                Long rva = getLong(section, "getVirtualAddress", "getAlignedVirtualAddress");
                Double entropy = getDouble(section, "getEntropy", "entropy");

                if (virtualSize == null) virtualSize = 0L;
                if (rawSize == null) rawSize = 0L;
                if (rva == null) rva = 0L;

                String characteristics = getString(section, "getCharacteristics", "characteristics", "toString");
                characteristics = truncateCharacteristics(characteristics);

                // Clamp entropy safely (0.0–8.0)
                if (entropy == null || Double.isNaN(entropy)) {
                    entropy = 0.0;
                } else {
                    while (entropy > 8.0) entropy /= 10.0;
                    entropy = Math.min(8.0, Math.max(0.0, entropy));
                    entropy = Math.round(entropy * 1000.0) / 1000.0;
                }

                info.setName(name != null ? name : "UNKNOWN");
                info.setVirtualSize(virtualSize);
                info.setRawSize(rawSize);
                info.setRva(rva);
                info.setEntropy(entropy);
                info.setCharacteristics(characteristics != null ? characteristics : "");

                result.getSections().add(info);
            }
        }

        logger.info("✅ Parsed PE: MachineType={}, EntryPoint=0x{}, ImageBase=0x{}, Sections={}",
                result.getMachineType(),
                Long.toHexString(result.getEntryPoint()),
                Long.toHexString(result.getImageBase()),
                result.getSections().size());

        return result;
    }

    // ========================================================
    // MAIN ANALYSIS ENTRYPOINT
    // ========================================================
    public AnalysisResult analyze(BinaryFile binary) {
        AnalysisResult result = new AnalysisResult();
        try {
            File peFile = new File(binary.getFilePath());
            // use the shared extractor from this service (which returns PEFunctionParser.PEHeaderResult)
            PEHeaderResult header = extractHeaderAndSections(peFile);

            result.setBinaryId(binary.getId());
            result.setFilename(binary.getOriginalFilename());
            result.setClassification(header != null ? "suspicious" : "clean");
            // use the canonical isPacked() from PEFunctionParser
            result.setPacked(header != null && PEFunctionParser.isPacked(header));
            result.setHeaderResult(header);

            logger.info("🧩 Static Analysis: {} -> Machine: {}, EP: {}, Packed: {}",
                    binary.getOriginalFilename(),
                    header != null ? header.getMachineType() : "unknown",
                    header != null ? header.getEntryPoint() : 0,
                    result.isPacked());

        } catch (Exception e) {
            logger.error("❌ Error analyzing binary {}: {}", binary.getOriginalFilename(), e.getMessage(), e);
            result.setClassification("error");
        }
        return result;
    }

    // ========================================================
    // HELPER METHODS
    // ========================================================
    private static String truncateCharacteristics(String s) {
        if (s == null) return null;
        if (s.length() <= CHARACTERISTICS_DB_MAX) return s;
        String[] tokens = s.split(",\\s*");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (sb.length() + token.length() + 2 > CHARACTERISTICS_DB_MAX) {
                sb.append(" ...");
                break;
            }
            if (sb.length() > 0) sb.append(", ");
            sb.append(token);
        }
        return sb.toString();
    }

    private static String safeInvokeString(Object target, String... candidates) {
        Object val = safeInvokeObject(target, candidates);
        return val == null ? null : val.toString();
    }

    private static Object safeInvokeObject(Object target, String... candidates) {
        if (target == null) return null;
        for (String m : candidates) {
            try {
                Method mm = target.getClass().getMethod(m);
                return mm.invoke(target);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        }
        return null;
    }

    private static String getString(Object obj, String... methods) {
        if (obj == null) return null;
        for (String m : methods) {
            try {
                Method mm = obj.getClass().getMethod(m);
                Object v = mm.invoke(obj);
                if (v != null) return v.toString();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Long getLong(Object obj, String... methods) {
        if (obj == null) return null;
        for (String m : methods) {
            try {
                Method mm = obj.getClass().getMethod(m);
                Object v = mm.invoke(obj);
                if (v instanceof Number) return ((Number) v).longValue();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Double getDouble(Object obj, String... methods) {
        if (obj == null) return null;
        for (String m : methods) {
            try {
                Method mm = obj.getClass().getMethod(m);
                Object v = mm.invoke(obj);
                if (v instanceof Number) return ((Number) v).doubleValue();
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Long getOptionalHeaderLong(Object optional, String... methods) {
        return getLong(optional, methods);
    }

    private static String getOptionalHeaderString(Object optional, String... methods) {
        return getString(optional, methods);
    }

    private static String safeToString(Object o) {
        if (o == null) return null;
        try {
            return o.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    private static String extractNameFromImportObject(Object imp) {
        if (imp == null) return null;
        for (String m : new String[]{"getName", "name", "getImportName", "importName", "toString"}) {
            try {
                Method mm = imp.getClass().getMethod(m);
                Object ret = mm.invoke(imp);
                if (ret != null) return String.valueOf(ret);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static String extractOrdinalFromImportObject(Object imp) {
        if (imp == null) return null;
        for (String m : new String[]{"ordinal", "getOrdinal", "getOrdinalNumber"}) {
            try {
                Method mm = imp.getClass().getMethod(m);
                Object ret = mm.invoke(imp);
                if (ret != null) return String.valueOf(ret);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ========================================================
    // DTO CLASSES - none here: use PEFunctionParser.PEHeaderResult
    // ========================================================
}
