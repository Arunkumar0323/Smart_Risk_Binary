package com.TheSecureSyndicate.threat_detection_backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.TheSecureSyndicate.threat_detection_backend.dto.ChatRequest;
import com.TheSecureSyndicate.threat_detection_backend.dto.ChatResponse;
import com.TheSecureSyndicate.threat_detection_backend.service.LLMChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private LLMChatService llmChatService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody(required = false) JsonNode body) {
        try {
            ChatRequest request = mapToChatRequest(body);
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                ChatResponse badRequestResponse = new ChatResponse();
                badRequestResponse.setAnswer("Question is required. Please type a question and try again.");
                badRequestResponse.setSeverity("LOW");
                badRequestResponse.setConfidence(0.0);
                badRequestResponse.setSuspicious(false);
                return ResponseEntity.badRequest().body(badRequestResponse);
            }

            // Generate conversation ID if not provided
            if (request.getConversationId() == null) {
                request.setConversationId("conv_" + System.currentTimeMillis() + "_" + 
                    Math.random());
            }

            ChatResponse response = llmChatService.askQuestion(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setAnswer("Error processing question: " + e.getMessage());
            errorResponse.setSeverity("ERROR");
            errorResponse.setConfidence(0.0);
            errorResponse.setSuspicious(false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private ChatRequest mapToChatRequest(JsonNode body) {
        ChatRequest request = new ChatRequest();
        ChatRequest.AnalysisContext context = new ChatRequest.AnalysisContext();

        if (body != null && body.isObject()) {
            request.setQuestion(getText(body.get("question")));
            request.setBinaryId(getText(body.get("binaryId")));
            request.setConversationId(getText(body.get("conversationId")));

            JsonNode contextNode = body.get("analysisContext");
            if (contextNode != null && contextNode.isObject()) {
                context.setFilename(getText(contextNode.get("filename")));
                context.setEntropy(getDouble(contextNode.get("entropy")));
                context.setTrustScore(getDouble(contextNode.get("trustScore")));
                context.setAlertCount(getInteger(contextNode.get("alertCount")));
                context.setYaraMatches(getStringList(contextNode.get("yaraMatches")));
                context.setSignatures(getStringList(contextNode.get("signatures")));
                context.setFileHash(getText(contextNode.get("fileHash")));
                context.setRiskScore(getDouble(contextNode.get("riskScore")));
            }
        }

        request.setAnalysisContext(context);
        return request;
    }

    private String getText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null ? null : value.trim();
    }

    private Double getDouble(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.doubleValue();
        }
        try {
            return Double.parseDouble(node.asText().trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer getInteger(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt() || node.isLong()) {
            return node.intValue();
        }
        try {
            return Integer.parseInt(node.asText().trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> getStringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node == null || node.isNull()) {
            return values;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item != null && !item.isNull()) {
                    values.add(item.asText());
                }
            }
            return values;
        }

        if (node.isObject()) {
            node.fieldNames().forEachRemaining(values::add);
            return values;
        }

        values.add(node.asText());
        return values;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat service is running");
    }
}
