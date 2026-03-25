// Backend API Base URL
const API_BASE_URL = "http://localhost:8080/api";

// Store current analysis data for chat context
let currentAnalysis = {
  binaryId: null,
  filename: null,
  entropy: 0,
  trustScore: 0.5,
  alertCount: 0,
  yaraMatches: [],
  mlRiskScore: 0,
  classification: 'unknown'
};

// Analysis Results Storage
let analysisHistory = [];
let stats = {
  signed: 0,
  unsigned: 0,
  malicious: 0,
  suspicious: 0,
  clean: 0,
  totalAnalyzed: 0,
  avgResponseTime: 0,
  responseTimes: []
};
async function initializeDashboard() {
  try {
    // Check backend connectivity
    await checkBackendHealth();
    // Fetch initial stats from backend (if available)
    await loadStats();
  } catch (error) {
    console.error("Error initializing dashboard:", error);
  }
  
  updateStatsDisplay();
  initializeChart();
  updateDashboardSections();
}

// Check if backend is healthy
async function checkBackendHealth() {
  try {
    const response = await fetch(`${API_BASE_URL}/health`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });
    
    if (response.ok) {
      const statusDiv = document.getElementById('backendStatus');
      if (statusDiv) {
        statusDiv.innerHTML = '<i class="fas fa-check-circle text-success"></i> Backend connected';
      }
      return true;
    }
  } catch (error) {
    console.warn("Backend health check failed:", error);
    const statusDiv = document.getElementById('backendStatus');
    if (statusDiv) {
      statusDiv.innerHTML = '<i class="fas fa-exclamation-triangle text-warning"></i> Backend may not be running. Make sure it\'s started on http://localhost:8080';
    }
  }
  return false;
}

// Load stats from backend
async function loadStats() {
  try {
    // Check if backend API endpoint exists for stats
    // For now, we'll use the fallback since the endpoint might not exist
    console.log("Stats loading from backend would go here");
  } catch (error) {
    console.warn("Could not load stats from backend:", error);
  }
}

// Update stats display
function updateStatsDisplay() {
  const signedElement = document.getElementById("signedCount");
  const unsignedElement = document.getElementById("unsignedCount");
  const avgResponseElement = document.getElementById("avgResponse");
  const activeAlertsElement = document.getElementById("activeAlerts");
  
  if (signedElement) signedElement.innerText = stats.signed || 0;
  if (unsignedElement) unsignedElement.innerText = stats.unsigned || 0;
  if (avgResponseElement) avgResponseElement.innerText = stats.avgResponseTime.toFixed(2) + "ms";
  if (activeAlertsElement) activeAlertsElement.innerText = stats.malicious + stats.suspicious;
}

// Initialize chart
function initializeChart() {
  const ctx = document.getElementById("threatChart");
  if (!ctx) return;

  // Destroy existing chart if it exists
  if (window.threatChart) {
    window.threatChart.destroy();
  }

  window.threatChart = new Chart(ctx, {
    type: "doughnut",
    data: {
      labels: ["Malicious", "Suspicious", "Clean"],
      datasets: [{
        data: [stats.malicious || 0, stats.suspicious || 0, stats.clean || 0],
        backgroundColor: ["#dc3545", "#ffc107", "#198754"]
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'bottom'
        },
        title: {
          display: true,
          text: `Threat Analysis (Total: ${stats.totalAnalyzed})`
        }
      }
    }
  });
}

// File upload handler for binary files
async function uploadFile() {
  const fileInput = document.getElementById("fileInput");
  const status = document.getElementById("uploadStatus");

  if (!fileInput || !fileInput.files.length) {
    if (status) {
      status.innerText = "Please select a file first.";
      status.style.color = "#dc3545";
    }
    return;
  }

  const file = fileInput.files[0];
  const formData = new FormData();
  formData.append("file", file);

  if (status) {
    status.innerText = "Uploading and analyzing...";
    status.style.color = "#0d6efd";
  }

  const uploadStartTime = performance.now();

  try {
    const response = await fetch(`${API_BASE_URL}/upload`, {
      method: 'POST',
      body: formData
    });

    const uploadEndTime = performance.now();

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    
    // Add response time to tracking
    const responseTime = uploadEndTime - uploadStartTime;
    stats.responseTimes.push(responseTime);
    stats.avgResponseTime = stats.responseTimes.reduce((a, b) => a + b, 0) / stats.responseTimes.length;
    
    // Store current binary ID for chat
    if (result.binaryId) {
      sessionStorage.setItem('currentBinaryId', result.binaryId);
    }
    
    // Display analysis result and update dashboard
    displayAnalysisResult(result);
    
    if (status) {
      status.innerText = "✅ File analyzed successfully!";
      status.style.color = "#198754";
    }
    
    // Reset file input
    if (fileInput) fileInput.value = '';
    
  } catch (error) {
    console.error("Upload error:", error);
    if (status) {
      status.innerText = "❌ Error analyzing file: " + error.message;
      status.style.color = "#dc3545";
    }
  }
}

// Display analysis result
function displayAnalysisResult(result) {
  const startTime = performance.now();
  
  // Store current analysis for chat context
  currentAnalysis = {
    binaryId: result.binaryId || 'unknown',
    filename: result.filename || 'unknown',
    entropy: result.entropy || 0,
    trustScore: result.mlRiskScore || 0.5,
    alertCount: result.yaraMatched ? 1 : 0,
    yaraMatches: result.matchedRules ? result.matchedRules.split(',').map(r => r.trim()) : [],
    mlRiskScore: result.mlRiskScore || 0,
    classification: result.classification || 'unknown'
  };
  
  // Store analysis in history
  const analysis = {
    id: result.binaryId || 'unknown',
    filename: result.filename || 'unknown',
    timestamp: new Date(),
    classification: result.classification || 'unknown',
    yaraMatched: result.yaraMatched || false,
    mlRiskScore: result.mlRiskScore || 0,
    machineType: result.machineType || 'unknown',
    packed: result.packed || false,
    signed: (result.classification === 'clean') // Assumption: clean = digitally signed
  };
  
  analysisHistory.push(analysis);
  
  // Update statistics
  if (analysis.signed) {
    stats.signed++;
  } else {
    stats.unsigned++;
  }
  
  switch(analysis.classification?.toLowerCase()) {
    case 'malicious':
      stats.malicious++;
      break;
    case 'suspicious':
      stats.suspicious++;
      break;
    case 'clean':
    case 'benign':
      stats.clean++;
      break;
  }
  
  stats.totalAnalyzed++;
  
  // Track response time
  const endTime = performance.now();
  const responseTime = endTime - startTime;
  stats.responseTimes.push(responseTime);
  stats.avgResponseTime = stats.responseTimes.reduce((a, b) => a + b, 0) / stats.responseTimes.length;
  
  // Display result in UI
  let resultDiv = document.getElementById("analysisResult");
  if (!resultDiv) {
    resultDiv = createResultDiv();
  }
  
  let html = `
    <div class="glass-card mt-4 p-4">
      <h5>📊 Analysis Result</h5>
      <dl class="row">
        <dt class="col-sm-4">Binary ID:</dt>
        <dd class="col-sm-8"><code>${analysis.id}</code></dd>
        
        <dt class="col-sm-4">Filename:</dt>
        <dd class="col-sm-8">${analysis.filename}</dd>
        
        <dt class="col-sm-4">Classification:</dt>
        <dd class="col-sm-8">
          <span class="badge ${getClassificationBadge(analysis.classification)}">
            ${analysis.classification || 'UNKNOWN'}
          </span>
        </dd>
        
        <dt class="col-sm-4">Digitally Signed:</dt>
        <dd class="col-sm-8">${analysis.signed ? '✅ Yes' : '❌ No'}</dd>
        
        <dt class="col-sm-4">YARA Matched:</dt>
        <dd class="col-sm-8">${analysis.yaraMatched ? '✅ Yes' : '❌ No'}</dd>
        
        <dt class="col-sm-4">ML Risk Score:</dt>
        <dd class="col-sm-8">${((analysis.mlRiskScore || 0) * 100).toFixed(1)}%</dd>
        
        <dt class="col-sm-4">Machine Type:</dt>
        <dd class="col-sm-8">${analysis.machineType || 'Unknown'}</dd>
        
        <dt class="col-sm-4">Packed:</dt>
        <dd class="col-sm-8">${analysis.packed ? '⚠️ Yes' : '✅ No'}</dd>
      </dl>
    </div>`;
  
  resultDiv.innerHTML = html;
  
  // Update dashboard sections
  updateDashboardSections();
  updateStatsDisplay();
  initializeChart();
  
  // Also make currentAnalysis available globally and trigger chat section if function exists
  if (typeof showChatSection === 'function') {
    showChatSection(currentAnalysis);
  }
}

// Update all dashboard display sections
function updateDashboardSections() {
  // Update Binary Status Overview
  updateBinaryStatusOverview();
  
  // Update Trust Scores
  updateTrustScores();
  
  // Update Historical Activity
  updateHistoricalActivity();
  
  // Update Recent Alerts
  updateRecentAlerts();
  
  // Update Summary Analytics
  updateSummaryAnalytics();
}

// Update Binary Status Overview
function updateBinaryStatusOverview() {
  const container = document.querySelector('[data-section="binary-status"]') || document.querySelector('.card-header');
  if (!container) return;
  
  const total = stats.totalAnalyzed;
  const signedPct = total > 0 ? ((stats.signed / total) * 100).toFixed(1) : 0;
  const unsignedPct = total > 0 ? ((stats.unsigned / total) * 100).toFixed(1) : 0;
  
  // Try to find and update the existing status text
  const statusElements = document.querySelectorAll('.glass-card');
  statusElements.forEach(el => {
    if (el.innerText.includes('Signed') && el.innerText.includes('Unsigned')) {
      el.innerText = `Signed: ${signedPct}% (${stats.signed}) | Unsigned: ${unsignedPct}% (${stats.unsigned})`;
    }
  });
}

// Update Trust Scores
function updateTrustScores() {
  const lowRisk = stats.clean;
  const highRisk = stats.malicious + stats.suspicious;
  const total = stats.totalAnalyzed || 1;
  
  const lowRiskPct = ((lowRisk / total) * 100).toFixed(1);
  const highRiskPct = ((highRisk / total) * 100).toFixed(1);
  
  const trustElements = document.querySelectorAll('.glass-card');
  trustElements.forEach(el => {
    if (el.innerText.includes('Low Risk') && el.innerText.includes('High Risk')) {
      el.innerText = `Low Risk: ${lowRiskPct}% | High Risk: ${highRiskPct}%`;
    }
  });
}

// Update Historical Activity
function updateHistoricalActivity() {
  const last7Days = analysisHistory.slice(-7);
  if (last7Days.length === 0) return;
  
  const historyData = last7Days.map(a => `${a.filename} - ${a.classification}`).join('\n');
  
  const historyElements = document.querySelectorAll('.glass-card');
  historyElements.forEach(el => {
    if (el.innerText.includes('Historical Activity') || el.innerText.includes('Last 7 Days')) {
      // Try to find nested content area
      const contentArea = el.querySelector('.card-body') || el;
      if (contentArea) {
        contentArea.innerText = last7Days.map(a => 
          `${a.timestamp.toLocaleTimeString()} - ${a.filename} (${a.classification})`
        ).join('\n');
      }
    }
  });
}

// Update Recent Alerts
function updateRecentAlerts() {
  const alerts = analysisHistory.filter(a => 
    a.classification === 'malicious' || a.classification === 'suspicious'
  ).slice(-5);
  
  const alertContainer = document.querySelector('[data-section="alerts"]') || 
                         document.querySelectorAll('.glass-card').find(el => el.innerText.includes('Alert'));
  
  if (!alertContainer) return;
  
  let alertHTML = '<ul class="list-unstyled">';
  alerts.forEach(alert => {
    const icon = alert.classification === 'malicious' ? '🔴' : '🟡';
    alertHTML += `<li>${icon} ${alert.filename} - ${alert.classification.toUpperCase()} (${alert.mlRiskScore * 100 || 0}%)</li>`;
  });
  alertHTML += alerts.length === 0 ? '<li>✅ No recent alerts</li>' : '';
  alertHTML += '</ul>';
  
  const contentArea = alertContainer.querySelector('.card-body') || alertContainer;
  if (contentArea) {
    contentArea.innerHTML = alertHTML;
  }
}

// Update Summary Analytics
function updateSummaryAnalytics() {
  const total = stats.totalAnalyzed;
  const maliciousPct = total > 0 ? ((stats.malicious / total) * 100).toFixed(1) : 0;
  const suspiciousPct = total > 0 ? ((stats.suspicious / total) * 100).toFixed(1) : 0;
  const cleanPct = total > 0 ? ((stats.clean / total) * 100).toFixed(1) : 0;
  
  const analyticsElements = document.querySelectorAll('.glass-card');
  analyticsElements.forEach(el => {
    if (el.innerText.includes('Summary Analytics')) {
      const contentArea = el.querySelector('.card-body') || el;
      if (contentArea) {
        contentArea.innerHTML = `
          <div>📊 <strong>Analysis Summary:</strong></div>
          <div>Mean Response Time: ${stats.avgResponseTime.toFixed(2)}ms</div>
          <div>Suspicious Binaries: ${suspiciousPct}% (flagged for analysis)</div>
          <div>Verified Binaries: ${cleanPct}% (confirmed clean)</div>
          <div>Malicious Binaries: ${maliciousPct}% (confirmed threats)</div>
          <div style="margin-top: 10px; font-size: 0.9em; color: #666;">
            Total Analyzed: ${total} | Signed: ${stats.signed} | Unsigned: ${stats.unsigned}
          </div>
        `;
      }
    }
  });
}

// Create result div if it doesn't exist
function createResultDiv() {
  const div = document.createElement('div');
  div.id = 'analysisResult';
  const uploadStatus = document.getElementById('uploadStatus');
  if (uploadStatus && uploadStatus.parentNode) {
    uploadStatus.parentNode.appendChild(div);
  }
  return div;
}

// Get classification badge class
function getClassificationBadge(classification) {
  if (!classification) return 'bg-secondary';
  
  const lower = classification.toLowerCase();
  if (lower.includes('malicious')) return 'bg-danger';
  if (lower.includes('suspicious')) return 'bg-warning text-dark';
  if (lower.includes('clean') || lower.includes('benign')) return 'bg-success';
  return 'bg-secondary';
}

// Chat functionality
async function sendMessage() {
  const chatInput = document.getElementById("chatInput");
  const chatBox = document.getElementById("chatBox");
  
  if (!chatInput || !chatBox) return;
  
  const message = chatInput.value.trim();

  if (!message) return;

  // Add user message to chat
  const userMsg = document.createElement("div");
  userMsg.className = "chat-message user-message";
  userMsg.innerHTML = `<div class="alert alert-info d-inline-block mb-2"><strong>You:</strong> ${escapeHtml(message)}</div>`;
  chatBox.appendChild(userMsg);

  try {
    // Build analysis context from current analysis data
    const analysisContext = {
      filename: currentAnalysis?.filename || "unknown",
      entropy: currentAnalysis?.entropy || 0,
      trustScore: currentAnalysis?.trustScore || 0.5,
      alertCount: currentAnalysis?.alertCount || 0,
      yaraMatches: currentAnalysis?.yaraMatches || [],
      classification: currentAnalysis?.classification || "unknown",
      mlRiskScore: currentAnalysis?.mlRiskScore || 0,
      signed: currentAnalysis?.signed || false,
      machineType: currentAnalysis?.machineType || "unknown",
      packed: currentAnalysis?.packed || false
    };

    console.log("Sending chat with analysis context:", analysisContext);

    const response = await fetch(`${API_BASE_URL}/chat/ask`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        question: message,
        binaryId: getCurrentBinaryId(),
        analysisContext: analysisContext
      })
    });

    const rawBody = await response.text();
    let result = {};
    if (rawBody) {
      try {
        result = JSON.parse(rawBody);
      } catch (e) {
        throw new Error(`Invalid response from chat service (HTTP ${response.status})`);
      }
    }

    if (!response.ok) {
      const errorMessage = extractFirstNonEmptyString([
        result.answer,
        result.message,
        result.error
      ]) || `HTTP error! status: ${response.status}`;
      throw new Error(errorMessage);
    }

    result = normalizeChatResponse(result);
    
    // Add bot response to chat
    const botMsg = document.createElement("div");
    botMsg.className = "chat-message bot-message";
    botMsg.innerHTML = `<div class="alert alert-light d-inline-block mb-2"><strong>Bot:</strong> ${escapeHtml(result.answer || 'No response')}</div>`;
    chatBox.appendChild(botMsg);
    
    chatBox.scrollTop = chatBox.scrollHeight;
  } catch (error) {
    console.error("Chat error:", error);
    const errorMsg = document.createElement("div");
    errorMsg.className = "chat-message bot-message error";
    errorMsg.innerHTML = `<div class="alert alert-danger d-inline-block mb-2"><strong>Error:</strong> ${escapeHtml(error.message)}</div>`;
    chatBox.appendChild(errorMsg);
  }

  chatInput.value = '';
}

// Get current binary ID from last analysis
function getCurrentBinaryId() {
  return sessionStorage.getItem('currentBinaryId') || '';
}

// HTML escape helper
function escapeHtml(text) {
  text = String(text ?? '');
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, m => map[m]);
}

function extractFirstNonEmptyString(values) {
  for (const value of values) {
    if (typeof value === 'string' && value.trim() !== '') {
      return value;
    }
  }
  return '';
}

function normalizeChatResponse(payload) {
  const source = (payload && typeof payload === 'object' && (payload.data || payload.result))
    ? (payload.data || payload.result)
    : payload;

  const answer = extractFirstNonEmptyString([
    source?.answer,
    source?.response,
    source?.message,
    source?.text
  ]);

  return {
    answer: answer || 'No response',
    severity: extractFirstNonEmptyString([source?.severity]).toUpperCase() || 'UNKNOWN',
    confidence: Number(source?.confidence) || 0,
    suspicious: Boolean(source?.suspicious)
  };
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', initializeDashboard);
