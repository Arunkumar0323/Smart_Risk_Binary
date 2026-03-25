# 🤖 AI Chatbot for Security Analysts - LIVE GenAI Demo Feature

## Overview

This is a **cutting-edge Retrieval-Augmented Generation (RAG)** feature that allows security analysts to **chat with binary analysis reports in real-time**. The system uses an LLM (GPT-4) combined with the analysis data as context to provide intelligent, actionable security insights.

## 🌟 Key Features

### 1. **Context-Aware Intelligence (RAG Pattern)**
- The AI receives the full binary analysis data as context
- Provides answers specifically tailored to the uploaded file
- No generic responses - everything is based on actual analysis results

### 2. **Multiple Example Questions**
Users can ask questions like:
- ❓ "Why is this file suspicious?"
- 🔒 "Does this show ransomware behavior?"
- 🏢 "Is it safe for enterprise deployment?"
- ⚠️ "What is the severity?"
- 🎯 "Are there known malware signatures?"
- 📊 "What is the entropy telling us?"

### 3. **Real-Time Severity Assessment**
- AI evaluates threat level
- Provides confidence scores
- Flags suspicious indicators
- Recommends actions for SOC/DFIR teams

### 4. **Two Deployment Modes**

#### Mode A: **With OpenAI API** (Production)
```
app.openai.api-key=sk-your-api-key-here
app.openai.model=gpt-4
app.openai.enabled=true
```
- Uses GPT-4 for advanced reasoning
- Handles complex threat pattern analysis
- Provides state-of-the-art intelligence

#### Mode B: **Local Heuristic Analysis** (Demo/Offline)
```
app.openai.api-key=
app.openai.enabled=false
```
- Works without API key
- Pattern-based analysis using context
- Demonstrates RAG concept without external dependency
- Perfect for live demos

## 🏗️ Architecture

### Backend Components

#### 1. **Chat Controller** (`ChatController.java`)
- REST endpoint: `POST /api/chat/ask`
- Handles chat requests
- Validates and processes user questions

#### 2. **LLM Service** (`LLMChatService.java`)
- Integrates with OpenAI API or local heuristics
- Builds system and user prompts with context
- Implements RAG pattern
- Analyzes indicators:
  - Trust Score
  - Alert Count
  - YARA Matches
  - Entropy levels
  - Suspicious patterns

#### 3. **DTOs**
- `ChatRequest.java` - User question + analysis context
- `ChatResponse.java` - AI answer + severity + confidence

### Frontend Components

#### 1. **Chat UI** (in `index.html`)
- Messages container with auto-scroll
- Quick question buttons
- Real-time message display
- Severity badges
- Confidence indicators

#### 2. **JavaScript Functions** (in `index.html`)
- `chatAsk()` - Send message to backend
- `addChatMessage()` - Display messages with styling
- `clearChatMessages()` - Reset conversation
- `handleChatKeyPress()` - Enter key support

## 🚀 Getting Started

### Step 1: Backend Setup

**Add OpenAI dependency (optional, for GPT-4 integration):**

Edit `Backend/pom.xml`:
```xml
<!-- OpenAI API (optional - for GPT-4 integration) -->
<dependency>
    <groupId>com.openai</groupId>
    <artifactId>openai-java</artifactId>
    <version>0.8.0</version>
</dependency>
```

**Configure API Key** in `Backend/src/main/resources/application.properties`:
```properties
# For production: Set your OpenAI API key
app.openai.api-key=sk-your-openai-api-key-here
app.openai.model=gpt-4
app.openai.enabled=true

# For demo: Leave empty to use local heuristics
# app.openai.api-key=
# app.openai.enabled=false
```

### Step 2: Run Backend
```bash
cd Backend
mvn spring-boot:run
# Server runs on http://localhost:8080
```

### Step 3: Run Frontend
```bash
cd frontend
npm install
npm run dev
# Dashboard runs on http://localhost:5173 (or as configured)
```

### Step 4: Use the Feature

1. **Upload a JSON analysis report** (Cuckoo, VirusTotal, or custom JSON)
2. **Analysis results display**
3. **Chat section appears automatically**
4. **Click quick question buttons OR type custom questions**
5. **AI responds with context-aware answers**

## 📊 Example Usage Scenario

```
User Question: "Is this file ransomware?"

Context Provided to AI:
- File entropy: 7.85 (high)
- YARA signatures: 3 matches
- Trust score: 0.25
- Alerts: 5 detected
- File: suspicious_binary.exe

AI Response:
"⚠️ HIGH RISK: This file exhibits strong indicators of ransomware:
- High entropy (7.85) suggests encryption/obfuscation
- 3 YARA rule matches detected (known malware patterns)
- Multiple behavioral alerts triggered
- Trust score 0.25 indicates malicious intent
- Recommend: Immediate isolation and forensic analysis"

Severity: CRITICAL
Suspicious: YES
Confidence: HIGH
```

## 🎯 RAG Pattern Explanation

**What is RAG?** Retrieval-Augmented Generation combines:
1. **Retrieval**: Get analysis data (trust score, entropy, signatures)
2. **Augmentation**: Add context to AI prompt
3. **Generation**: AI generates answer based on context

```
User Question
    ↓
Add Analysis Context
    ↓
Send to LLM with System Prompt
    ↓
AI Generates Context-Aware Response
    ↓
Display Answer + Severity + Confidence
```

## 🔧 Implementation Details

### Chat Request Flow
```json
{
  "question": "Does this show ransomware behavior?",
  "binaryId": "binary_123",
  "analysisContext": {
    "filename": "malware.exe",
    "entropy": 7.85,
    "trustScore": 0.25,
    "alerts": [...],
    "yara": {...},
    "signatures": [...]
  },
  "conversationId": "conv_1234567890_abc123"
}
```

### Chat Response Format
```json
{
  "conversationId": "conv_1234567890_abc123",
  "question": "Does this show ransomware behavior?",
  "answer": "⚠️ HIGH RISK: This file exhibits strong ransomware indicators...",
  "confidence": "High",
  "severity": "Critical",
  "isSuspicious": true,
  "timestamp": "2024-02-26T10:30:45.123Z",
  "model": "gpt-4"
}
```

## 🎓 What This Demonstrates

### For Judges:
✅ **Advanced AI Integration** - Live LLM usage  
✅ **RAG Pattern** - Context-aware intelligence  
✅ **Real-time Interaction** - Live chat interface  
✅ **Security Context** - Domain-specific analysis  
✅ **Scalability** - Handles multiple chat sessions  
✅ **Professional UX** - Modern, responsive design  

### For Security Teams:
✅ **Faster Analysis** - AI assists in threat assessment  
✅ **Consistent Interpretation** - Standardized analysis  
✅ **Evidence-Based** - Answers backed by data  
✅ **Actionable** - Deployment recommendations  
✅ **Audit Trail** - Full conversation history  

## 💡 Advanced Features

### Smart Question Suggestions
The UI provides pre-filled buttons for common questions:
- 💬 Why is this file suspicious?
- 🔒 Does this show ransomware behavior?
- 🏢 Is it safe for enterprise deployment?
- ⚠️ What is the severity?

### Severity Indicators
The chat displays visual indicators:
- 🔴 **CRITICAL** - Immediate action required
- 🟠 **HIGH** - Urgent investigation needed
- 🟡 **MEDIUM** - Monitor and investigate
- 🟢 **LOW** - Standard security posture

### Confidence Scoring
- **High** - AI is very confident in assessment
- **Medium** - Some indicators present
- **Low** - Limited data or conflicting signals

## 📝 Configuration Reference

### application.properties Settings
```properties
# LLM Configuration
app.openai.api-key=sk-your-api-key          # Your OpenAI API key
app.openai.model=gpt-4                       # GPT-4 or gpt-3.5-turbo
app.openai.enabled=true                      # Enable/disable LLM

# Chat Service Settings
app.chat.max-context-tokens=8000             # Max tokens for context
app.chat.temperature=0.7                     # Response creativity (0-1)
```

## 🔒 Security Considerations

1. **API Key Security**: Store API keys in environment variables, not in config files
2. **Data Privacy**: Analysis data stays on your server
3. **Rate Limiting**: Implement rate limits on chat endpoint
4. **Input Validation**: User questions are sanitized
5. **Context Isolation**: Each conversation is independent

## 📚 Example JSON Report Format

The system accepts JSON reports from:
- **Cuckoo Sandbox** - Dynamic analysis
- **VirusTotal** - Multi-engine scanning
- **Custom Scanners** - Any JSON format

Minimum required fields:
```json
{
  "filename": "binary.exe",
  "entropy": 7.5,
  "trustScore": 0.5,
  "signatures": [],
  "yara": {
    "rules": []
  },
  "alerts": []
}
```

## 🚨 Troubleshooting

### Issue: "Cannot connect to backend"
- Ensure backend is running: `mvn spring-boot:run`
- Check port 8080 is not in use
- Verify CORS configuration

### Issue: "Chat endpoint not found"
- Verify `ChatController.java` is in correct package
- Check imports are correct
- Rebuild backend: `mvn clean install`

### Issue: "No response from AI"
- If using OpenAI: Verify API key is valid
- If using local: Check analysis context is present
- Check backend logs for errors

## 📈 Performance Notes

- **Response Time**: 1-5 seconds (with API), <1 second (local)
- **Max Context Size**: 8,000 tokens (~32KB text)
- **Concurrent Users**: Scales with Spring Boot
- **Latency**: ~500ms network + LLM processing time

## 🎯 Future Enhancements

1. **Conversation Memory** - Multi-turn context
2. **Custom Prompts** - User-defined analysis prompts
3. **Batch Analysis** - Chat for multiple binaries
4. **Fine-tuning** - Domain-specific model training
5. **Integration** - Slack, Teams, SOAR platforms
6. **Analytics** - Question/answer patterns
7. **Explainability** - Show reasoning steps

## 📞 Support

For issues or questions:
1. Check backend logs: `Backend/logs/`
2. Check browser console: F12 → Console tab
3. Verify JSON format of uploaded reports
4. Test with sample JSON report

---

**Built with ❤️ for Security Teams** | Demonstrating advanced AI integration in security tools
