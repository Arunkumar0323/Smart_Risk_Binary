# 🚀 AI Chatbot Quick Start Guide

## 30-Second Setup for Live Demo

### Step 1: Backend (30 seconds)
```bash
cd Backend
mvn clean install
mvn spring-boot:run
```
✅ Backend running on `http://localhost:8080`

### Step 2: Frontend (30 seconds)
```bash
cd frontend
npm install
npm run dev
```
✅ Dashboard running at output URL (usually `http://localhost:5173`)

### Step 3: Test the Feature (60 seconds)

1. **Open the dashboard** in your browser
2. **Create a test JSON file** or use existing analysis report
3. **Click "Upload & Analyze"** button
4. **Chat section appears** automatically below results
5. **Click any suggested question** or type your own

## Example Questions to Ask

```
"Why is this file suspicious?"
→ AI analyzes trust score, entropy, signatures

"Does this show ransomware behavior?"
→ AI checks entropy (7.5+) and behavioral patterns

"Is it safe for enterprise deployment?"
→ AI provides deployment recommendation

"What is the severity?"
→ AI calculates risk level based on indicators
```

## Demo with Sample JSON

Create file: `sample_analysis.json`
```json
{
  "filename": "test.exe",
  "sha256": "abc123def456",
  "entropy": 7.8,
  "trustScore": 0.3,
  "fileSize": 524288,
  "alerts": [
    {"severity": "High", "message": "Suspicious API calls detected"},
    {"severity": "Critical", "message": "Encryption routine detected"}
  ],
  "yara": {
    "rules": [
      "Trojan.Generic.Z",
      "Ransomware.CryptoLocker"
    ]
  },
  "signatures": ["CmdExec", "NetworkAccess"]
}
```

Then upload this file → Ask "Is this ransomware?" → Get intelligent answer!

## Expected Output

### User Asks:
```
"Does this show ransomware behavior?"
```

### AI Responds:
```
⚠️ HIGH ENTROPY DETECTED: Strong indicator of encryption/packing.
- Entropy: 7.85 (very high)
- Suggests compression, encryption, or code obfuscation
- Ransomware often exhibits high entropy patterns

⚠️ RANSOMWARE ASSESSMENT: High risk. Combined with other indicators, 
this warrants closer inspection.

[Severity: CRITICAL | Confidence: HIGH]
```

## Mode Selection

### Development/Demo (No API Key)
Just run - it works immediately with local heuristics!

### Production (With GPT-4)
```properties
# In Backend/src/main/resources/application.properties
app.openai.api-key=sk-your-openai-api-key
app.openai.enabled=true
```

## Features Demonstrated

| Feature | Status |
|---------|--------|
| RAG Pattern | ✅ Context-aware responses |
| Real-time Chat | ✅ Instant messaging |
| Severity Detection | ✅ Critical/High/Medium/Low |
| Confidence Scores | ✅ AI reliability indicators |
| Mobile Friendly | ✅ Responsive design |
| Offline Mode | ✅ Works without API key |
| Multi-turn Chat | ✅ Conversation context |
| Auto-scroll | ✅ New messages visible |

## Troubleshooting Quick Fix

**Backend not connecting?**
```bash
# Kill port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Restart
mvn spring-boot:run
```

**Frontend not loading?**
```bash
# Clear and reinstall
rm -rf node_modules package-lock.json
npm install
npm run dev
```

**Chat not working?**
- Check backend is running: `http://localhost:8080/api/health`
- Check browser console (F12) for errors
- Verify JSON format of analysis file

## Live Demo Talking Points

1. **"This is RAG in action"** - Context from analysis drives AI response
2. **"Security-focused questions"** - Not generic AI, security-aware
3. **"Real-time intelligence"** - Live answers during incident response
4. **"Works offline"** - Doesn't require external API (demo mode)
5. **"Enterprise-ready"** - Severity, confidence, audit trail

## Next Steps

- ✅ Share with SOC team
- ✅ Test with real malware samples (safely)
- ✅ Add custom prompt instructions
- ✅ Integrate with SOAR platforms
- ✅ Add conversation export/audit logging

---

**You're all set! Start the backend and frontend, then watch the magic happen.** ✨
