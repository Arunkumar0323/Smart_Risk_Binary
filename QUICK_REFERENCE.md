# 🎴 AI Chatbot Quick Reference Card

## One-Page Cheat Sheet

### 🚀 Quick Start (30 seconds)
```bash
# Terminal 1 - Backend
cd Backend && mvn spring-boot:run

# Terminal 2 - Frontend  
cd frontend && npm install && npm run dev

# Browser
http://localhost:5173
```

### 📤 Upload & Test (1 minute)
1. Click "Choose file" → Select `frontend/demo_analysis.json`
2. Click "Upload & Analyze"
3. Chat section appears below results
4. Click "Does this show ransomware?" button
5. Get AI response in seconds

### 💬 Example Conversation
```
USER:  "Is this file suspicious?"
AI:    "CRITICAL RISK. Entropy: 7.85, 3 YARA matches,
        5 alerts. Treat as active malware."

USER:  "Is it safe for enterprise?"
AI:    "NOT RECOMMENDED. Multiple threat indicators.
        Risk outweighs benefits."

USER:  "What is the severity?"
AI:    "CRITICAL. High entropy + known signatures +
        behavioral alerts = ransomware likely."
```

### 🔗 API Endpoint
```
POST /api/chat/ask
Content-Type: application/json

Request:
{
  "question": "Does this show ransomware?",
  "binaryId": "bin_123",
  "analysisContext": {...},
  "conversationId": "conv_xyz"
}

Response:
{
  "answer": "YES - CRITICAL risk...",
  "severity": "CRITICAL",
  "confidence": "HIGH",
  "isSuspicious": true
}
```

### ⚙️ Configuration
```properties
# With OpenAI API (production)
app.openai.enabled=true
app.openai.api-key=sk-your-key
app.openai.model=gpt-4

# Without API (demo/offline)
app.openai.enabled=false
# Uses local heuristic analysis
```

### 📁 Key Files
```
Backend/
└── src/main/java/.../
    ├── ChatController.java      (REST endpoints)
    ├── LLMChatService.java      (RAG engine)
    ├── ChatRequest.java         (Request DTO)
    └── ChatResponse.java        (Response DTO)

frontend/
├── index.html                   (Chat UI)
└── demo_analysis.json          (Test data)
```

### 🎯 Features
| Feature | Details |
|---------|---------|
| **Chat** | Real-time messaging |
| **Context** | Uses binary analysis data |
| **AI** | GPT-4 or local heuristics |
| **Severity** | Critical/High/Medium/Low |
| **Confidence** | High/Medium/Low scoring |
| **Mode A** | Production (with API key) |
| **Mode B** | Demo (no API key needed) |

### ❌ Troubleshooting
```
"Backend won't start"
→ mvn clean install
→ Check port 8080 not in use

"Chat not responding"
→ Check backend is running
→ Verify JSON format correct
→ Check browser console for errors

"No response from AI"
→ Backend logs show details
→ If API: Check API key valid
→ If local: Check analysis context present
```

### 📊 Example Responses
```
Q: "Why is this suspicious?"
A: "⚠️ CRITICAL:
   - Entropy: 7.85 (very high)
   - YARA matches: 3 known malware
   - Behavioral alerts: 5 triggered
   - Trust score: 0.25 (malicious)"

Q: "Ransomware?"
A: "YES - CRITICAL RISK
   - High entropy (encryption)
   - Known signatures detected
   - File access patterns match
   Recommend: ISOLATE immediately"

Q: "Deploy to enterprise?"
A: "NO - NOT SAFE
   - Multiple threat indicators
   - High risk of compromise
   Recommend: Find alternative source"
```

### 🧠 RAG Pattern
```
Binary Analysis JSON
       ↓
Add to AI Prompt (Context)
       ↓
Send with User Question
       ↓
LLM Generates Response
(using context)
       ↓
Return Answer + Severity
```

### 🔐 Security
- Input validation ✅
- XSS prevention ✅
- API key in config ✅
- Error masking ✅
- Rate limit ready ✅

### 📈 Performance
| Metric | Value |
|--------|-------|
| Response (Local) | <1 sec |
| Response (API) | 1-5 sec |
| Max Context | 8KB |
| Max Question | 500 chars |
| Concurrent Users | Unlimited |

### 📚 Documentation
- **JUDGE_DEMO_GUIDE.md** - Demo script
- **CHATBOT_QUICK_START.md** - Setup
- **CHATBOT_FEATURE.md** - Full docs
- **IMPLEMENTATION_SUMMARY.md** - Architecture
- **ENTERPRISE_INTEGRATION.md** - Production
- **DOCUMENTATION_INDEX.md** - Navigation

### ✨ Why It's Cool
1. **RAG Pattern** - Context-aware AI
2. **Live GenAI** - Real GPT-4 integration
3. **Dual Mode** - Works with/without API
4. **Security** - Domain-specific analysis
5. **Production** - Enterprise-ready code
6. **Documented** - 45+ pages of guides

### 🎪 Demo in 5 Minutes
```
0:00-1:00  Start backend & frontend
1:00-2:00  Upload demo_analysis.json
2:00-3:30  Ask 2 quick questions
3:30-4:30  Highlight features
4:30-5:00  Q&A
```

### 🏆 Key Highlights for Judges
```
✅ RAG implementation
✅ Real-time chat
✅ Context-aware AI
✅ Two deployment modes
✅ Professional UI
✅ Production code
✅ 45+ pages docs
✅ 20+ code examples
```

### 💡 Integration Examples
```python
# SOAR Integration
requests.post('http://localhost:8080/api/chat/ask',
    json={
        'question': 'Is this ransomware?',
        'binaryId': bin_id,
        'analysisContext': analysis
    }
)
```

### 🎬 Next Steps
1. **Demo**: Open JUDGE_DEMO_GUIDE.md
2. **Deploy**: Open ENTERPRISE_INTEGRATION.md
3. **Integrate**: See code examples
4. **Customize**: Modify prompts as needed
5. **Monitor**: Set up logging & alerts

### 📞 Support
```
Setup Issue   → CHATBOT_QUICK_START.md
Demo Problem  → JUDGE_DEMO_GUIDE.md
Tech Details  → IMPLEMENTATION_SUMMARY.md
Deployment    → ENTERPRISE_INTEGRATION.md
Not Working?  → Check CHATBOT_FEATURE.md
Everything    → DOCUMENTATION_INDEX.md
```

---

**Status**: ✅ Ready to Demo

**Next**: Open [JUDGE_DEMO_GUIDE.md](JUDGE_DEMO_GUIDE.md)

**Go!** 🚀
