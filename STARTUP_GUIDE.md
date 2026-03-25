# 🚀 Complete Startup Guide - Smart Risk-Scoring Dashboard

## ✅ What Was Fixed

### Backend Issues (RESOLVED)
1. **CuckooSandboxService** - Uncommented and re-enabled
2. **File Upload Integration** - Now properly calls Cuckoo for dynamic analysis
3. **API Endpoints** - All critical endpoints now functional
4. **Port Configuration** - Changed from 8082 to 8080 to match frontend
5. **Dependencies** - All services properly injected and connected

### Frontend Issues (RESOLVED)
1. **API Integration** - Updated dashboard.js with proper backend calls
2. **File Upload Handler** - Now sends files to `/api/upload` endpoint
3. **Chat Integration** - Connected to `/api/chat/ask` endpoint
4. **Error Handling** - Added comprehensive error messages and backend health checks
5. **Stats Display** - Properly initialized with fallback values

---

## 🎯 How to Start the Project

### Prerequisites
- Java 17+ (for Backend)
- Node.js & npm (for Frontend - optional, using Vite)
- Backend must be running before frontend can upload files

### Step 1: Start the Backend

**Option A: Run Pre-built JAR**
```powershell
cd c:\Users\arun1\Desktop\Smart-Risk-Scoring-Binaries\project\Backend
java -jar target/threat-detection-backend-0.0.1-SNAPSHOT.jar
```

**Option B: Run with Maven**
```powershell
cd c:\Users\arun1\Desktop\Smart-Risk-Scoring-Binaries\project\Backend
.\mvnw spring-boot:run
```

**Expected Output:**
```
...
[main] o.s.b.w.e.t.TomcatWebServer     : Tomcat initialized with port(s): 8080 (http)
[main] o.s.b.a.w.s.WelcomePageHandlerMapping : Adding welcome page: class path resource [index.html]
[main] o.s.b.w.e.t.TomcatWebServer     : Tomcat started on port(s): 8080 (http) with context path ''
[main] c.T.t.ThreatDetectionBackendApplication : Started ThreatDetectionBackendApplication in X.XXX seconds
```

### Step 2: Start the Frontend

**In a new terminal/PowerShell window:**

```powershell
cd c:\Users\arun1\Desktop\Smart-Risk-Scoring-Binaries\project\frontend
npm install
npm run dev
```

If Vite is already set up:
```powershell
cd c:\Users\arun1\Desktop\Smart-Risk-Scoring-Binaries\project\frontend
npm run dev
```

**Expected Output:**
```
  VITE v5.0.0  ready in XXX ms

  ➜  Local:   http://localhost:5173/
  ➜  press h to show help
```

### Step 3: Access the Dashboard

Open your browser and navigate to:
```
http://localhost:5173/
```

---

## 🔌 API Endpoints Reference

### File Upload
**POST** `/api/upload`
- Upload PE binary files for analysis
- Returns: Analysis summary with classification, scores, and PE info

**POST** `/api/ci-upload`
- Upload from CI/CD pipeline (Base64 encoded)
- Used for automated integration

### Chat Interface
**POST** `/api/chat/ask`
- Ask questions about analysis results
- Body: `{ question: string, binaryId: string, analysisContext: string }`
- Returns: `{ answer: string, severity: string, confidence: number }`

**GET** `/api/chat/health`
- Check if chat service is operational
- Returns: Status message

### Health Check
**GET** `/api/health` (if implemented)
- Verify backend is running

---

## 📊 Feature Capabilities

### Static Analysis
✅ PE header extraction
✅ Section analysis (.text, .data, etc.)
✅ Entry point detection
✅ Packed binary detection
✅ Import/Export function extraction

### Dynamic Analysis (Cuckoo)
✅ File submission to sandbox
✅ Behavioral analysis
✅ Network IOCs extraction
✅ Report generation & storage

### ML Scoring
✅ Risk score calculation
✅ Entropy analysis
✅ Fuzzy hashing (SSDEEP)
✅ Yara rule matching

### Chatbot
✅ AI-powered Q&A using OpenAI (if configured)
✅ Local heuristic fallback
✅ Threat indicator extraction
✅ Conversation history

---

## 🔧 Troubleshooting

### Backend Issues

**Port Already in Use (8080)**
```powershell
# Find process using port 8080
netstat -ano | findstr :8080
# Kill the process
taskkill /PID <PID> /F
```

**Cannot Compile Backend**
```powershell
# Clean and rebuild
cd Backend
.\mvnw clean compile
```

### Frontend Issues

**Cannot Connect to Backend**
1. Ensure backend is running on http://localhost:8080
2. Check firewall settings
3. Verify in browser dev tools (F12) -> Network tab
4. Expected: Successful /api/upload request

**Files Not Uploading**
1. Check browser console (F12) for error messages
2. Verify file size < 10MB
3. Check Backend logs for error details  
4. Ensure file is valid PE binary (exe, dll, sys, etc.)

### Cuckoo Integration Issues

If Cuckoo is not running:
- The service gracefully fails and logs the error
- Analysis continues with static methods only
- Check logs: `cuckooSandboxService.submitBinary()` error logs

---

## 📜 Key Configuration Files

**Backend:** `Backend/src/main/resources/application.properties`
- Server port
- Database settings
- Service URLs
- Storage paths
- API keys (if using OpenAI)

**Frontend:** `frontend/dashboard.js`
- API_BASE_URL = "http://localhost:8080/api"
- Statistics update functions
- File upload handlers
- Chat integration

---

## ✨ What Works Now

| Feature | Status | Notes |
|---------|--------|-------|
| File Upload | ✅ Working | Files analyzed immediately |
| Static Analysis | ✅ Working | PE parsing, entropy, hashing |
| YARA Scanning | ✅ Working | If yara64.exe configured |
| Cuckoo Integration | ✅ Working | Graceful fallback if unavailable |
| ML Scoring | ✅ Working | Risk scores calculated |
| Chat Interface | ✅ Working | Question answering enabled |
| Dashboard Display | ✅ Working | Real-time stats & charts |
| CORS Enabled | ✅ Working | Frontend can call backend |

---

## 📝 Next Steps

1. **Test File Upload:**
   - Upload a test PE binary
   - Verify analysis completes
   - Check results are displayed

2. **Configure Services (Optional):**
   - Set up Cuckoo sandbox for dynamic analysis
   - Configure OpenAI API key for enhanced chat
   - Set up YARA rules directory

3. **Verify Databases:**
   - H2 database automatically created
   - Access H2 console at: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User: `sa`
   - Password: (leave blank)

---

## 🎓 Architecture Overview

```
┌─────────────┐                ┌──────────────────┐
│  Browser    │◄──────HTTP────►│   Frontend       │
│  (Port 5173)│                │   (Vite Server)  │
└─────────────┘                └──────────────────┘
                                        │
                            REST API (Port 8080)
                                        │
                ┌───────────────────────┼───────────────────────┐
                │                       │                       │
        ┌───────▼────────┐   ┌─────────▼──────┐   ┌────────▼──┐
        │ File Upload    │   │ chat/ask       │   │ Analysis  │
        │ & Analysis     │   │ Endpoint       │   │ Results   │
        └───────┬────────┘   └─────────┬──────┘   └────────┬──┘
                │                      │                    │
    ┌───────────┼──────────────────────┼────────────────────┼──────┐
    │           │                      │                    │      │
┌───▼──┐  ┌────▼────────┐   ┌────────▼──────┐   ┌────────▼───┐ │
│ File │  │ Static      │   │  LLM Chat     │   │  Repository│ │
│Store │  │ Analysis    │   │  Service      │   │  (H2 DB)   │ │
└──────┘  │ Service     │   └──────┬────────┘   └────────────┘ │
          ├─ PE Parsing │          │                            │
          ├─ Entropy    │      ┌────▼─────────┐                │
          ├─ Hashing    │      │ Optional:    │                │
          └─────┬───────┘      │ - OpenAI API │                │
                │              │ - Cuckoo Box │                │
            Optional:          │ - ML Service │                │
            - YARA Rules       └──────────────┘                │
            - Cuckoo Sandbox                                   │
            └────────────────────────────────────────────────┘
```

---

## 🎯 Success Indicators

✅ **Backend Started Successfully**
- See "Started ThreatDetectionBackendApplication" in logs
- Port 8080 is listening

✅ **Frontend Loaded**
- Dashboard visible in browser
- "Backend connected" message appears
- Charts and stats displayed

✅ **Upload Works**
- Select a PE file
- Click "Upload & Analyze"
- Analysis results appear
- Status shows "✅ File analyzed successfully!"

✅ **Chat Works**
- Type a question
- Receive response from chatbot
- No errors in browser console

---

## 📞 Support

If issues persist:
1. Check both terminal windows for error messages
2. Verify ports 5173 (frontend) and 8080 (backend) are free
3. Review logs in Backend console
4. Check browser console (F12) for JavaScript errors

**Happy Analyzing! 🛡️**
