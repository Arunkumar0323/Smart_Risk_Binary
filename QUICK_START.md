# ⚡ Quick Start - 2 Minutes to Running

## 🚀 Start Backend (Terminal 1)
```powershell
cd c:\Users\arun1\Desktop\Smart-Risk-Scoring-Binaries\project\Backend
java -jar target/threat-detection-backend-0.0.1-SNAPSHOT.jar
```

**Wait for:** `Started ThreatDetectionBackendApplication`

---

## 🎨 Start Frontend (Terminal 2)
```powershell
cd c:\Users\arun1\Desktop\Smart-Risk-Scoring-Binaries\project\frontend
npm run dev
```

**Wait for:** `Local: http://localhost:5173/`

---

## 🌐 Open Dashboard
```
http://localhost:5173/
```

✅ You should see:
- Dashboard with stats
- "✅ Backend connected" status
- File upload area
- Chat interface

---

## 📤 Test Upload
1. Select a PE binary file (exe, dll, or sys)
2. Click "Upload & Analyze"
3. Wait for results...

**Expected:**
```
✅ File analyzed successfully!
│
├── Binary ID: [UUID]
├── Classification: [MALICIOUS/SUSPICIOUS/CLEAN]
├── YARA Matched: [YES/NO]
├── ML Risk Score: [0-100%]
├── Machine Type: [x86/x64]
└── Packed: [YES/NO]
```

---

## 💬 Test Chat
1. Type a security question
2. Hit Enter or click Send
3. Get AI response

**Example Questions:**
- "Is this binary malicious?"
- "What are the key indicators?"
- "What actions is it taking?"

---

## 🔌 API Endpoints

### Upload Binary
```
POST http://localhost:8080/api/upload
Body: FormData with file
Response: { binaryId, filename, classification, ... }
```

### Ask Chatbot
```
POST http://localhost:8080/api/chat/ask
Body: { question, binaryId, analysisContext }
Response: { answer, severity, confidence }
```

---

## ❌ Not Working?

**Backend won't start:**
```powershell
# Kill existing process on port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Frontend can't connect to backend:**
- Verify backend is running (check Terminal 1)
- Reload page (Ctrl+Shift+R to hard refresh)
- Check browser console (F12)

**File upload fails:**
- Ensure file is valid PE binary (<10MB)
- Check Backend terminal for error logs
- Verify backend `/api/upload` endpoint responds

---

## 📊 What's Now Working

✅ File Upload → PE Binary Analysis
✅ Static Analysis → Entropy, Hashing, PE Info  
✅ YARA Scanning → Rule Matching (if configured)
✅ Cuckoo Integration → Dynamic Analysis (if available)
✅ ML Scoring → Risk Score Calculation
✅ Chatbot → AI-Powered Q&A
✅ Dashboard → Real-time Stats & Charts
✅ Frontend-Backend Communication → Full Integration

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `Backend/target/threat-detection-backend-*.jar` | Runnable app |
| `Backend/src/main/resources/application.properties` | Configuration |
| `frontend/index.html` | UI Layout |
| `frontend/dashboard.js` | API Integration |
| `frontend/style.css` | Styling |

---

## 🎯 Success Indicators

- [ ] Backend on port 8080, frontend on 5173
- [ ] Dashboard loads without errors
- [ ] "✅ Backend connected" message appears
- [ ] Can select and upload a PE file  
- [ ] Analysis results display properly
- [ ] Chat responds to questions
- [ ] No red errors in browser console

---

## 📜 Full Documentation

See these files for details:
- `STARTUP_GUIDE.md` - Complete setup & troubleshooting
- `FIXES_APPLIED.md` - What was fixed and why
- `README.md` - Project overview

---

**You're all set! Happy binary analyzing! 🛡️**
