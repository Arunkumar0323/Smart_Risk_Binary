# 🏢 Enterprise Integration Guide - AI Chatbot

## Production Deployment Checklist

### Pre-Deployment

- [ ] OpenAI API key obtained and validated
- [ ] Backend security configuration reviewed
- [ ] Database schema created
- [ ] SSL/TLS certificates configured
- [ ] Rate limiting configured
- [ ] CORS origins whitelisted
- [ ] Log aggregation setup
- [ ] Monitoring alerts configured

### Environment Configuration

#### Development
```properties
app.openai.enabled=false
# Uses local heuristics - no API key needed
```

#### Staging
```properties
app.openai.enabled=true
app.openai.api-key=${OPENAI_API_KEY}
app.openai.model=gpt-3.5-turbo
```

#### Production
```properties
app.openai.enabled=true
app.openai.api-key=${OPENAI_API_KEY}
app.openai.model=gpt-4
app.chat.max-context-tokens=8000
app.chat.temperature=0.5
```

## API Integration

### REST Endpoint
```
POST /api/chat/ask
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "question": "Is this ransomware?",
  "binaryId": "bin_123",
  "analysisContext": { ... },
  "conversationId": "conv_xyz"
}

Response:
{
  "conversationId": "conv_xyz",
  "question": "Is this ransomware?",
  "answer": "...",
  "confidence": "High",
  "severity": "Critical",
  "isSuspicious": true,
  "timestamp": "2024-02-26T10:30:45Z",
  "model": "gpt-4"
}
```

## SOAR Integration Examples

### Splunk Integration
```python
import requests
import json

def query_threat_analyzer(binary_id, analysis_json):
    """Send analysis to AI chatbot via REST API"""
    response = requests.post(
        'https://your-domain.com/api/chat/ask',
        headers={
            'Authorization': f'Bearer {API_TOKEN}',
            'Content-Type': 'application/json'
        },
        json={
            'question': 'Provide a brief threat assessment',
            'binaryId': binary_id,
            'analysisContext': json.loads(analysis_json),
            'conversationId': f'splunk_{binary_id}'
        }
    )
    return response.json()
```

### Slack Integration
```python
from slack_sdk import WebClient
import requests

def slack_threat_analysis(channel, binary_analysis):
    """Post AI threat analysis to Slack"""
    
    ai_response = requests.post(
        'https://your-domain.com/api/chat/ask',
        json={
            'question': 'Summarize the threat level and recommended actions',
            'binaryId': binary_analysis['binaryId'],
            'analysisContext': binary_analysis
        }
    ).json()
    
    slack = WebClient(token=SLACK_BOT_TOKEN)
    slack.chat_postMessage(
        channel=channel,
        text=f"🤖 AI Security Analysis:\n{ai_response['answer']}",
        blocks=[
            {
                "type": "section",
                "text": {
                    "type": "mrkdwn",
                    "text": f"*Severity:* {ai_response['severity']}\n*Confidence:* {ai_response['confidence']}"
                }
            }
        ]
    )
```

### Email Alert Integration
```python
import smtplib
from email.mime.text import MIMEText

def send_ai_threat_alert(recipient, analysis, ai_response):
    """Send AI-generated threat alert via email"""
    
    email_body = f"""
    File: {analysis['filename']}
    AI Assessment: {ai_response['answer']}
    
    Severity: {ai_response['severity']}
    Recommended Action: {get_action(ai_response['severity'])}
    """
    
    msg = MIMEText(email_body)
    msg['Subject'] = f"[{ai_response['severity']}] AI Threat Alert"
    msg['From'] = 'security@company.com'
    msg['To'] = recipient
    
    with smtplib.SMTP('localhost') as server:
        server.send_message(msg)
```

## Monitoring & Logging

### Prometheus Metrics
```yaml
# Expose these metrics for monitoring
spring.jmx.export.enabled=true
management.metrics.export.prometheus.enabled=true
management.endpoints.web.exposure.include=prometheus,health,metrics
```

### Key Metrics to Monitor
- `chat_requests_total` - Total chat requests
- `chat_response_time_seconds` - Response latency
- `chat_errors_total` - Error rate
- `openai_api_calls_total` - LLM API usage
- `chat_severity_distribution` - Threat severity breakdown

### Example Prometheus Query
```promql
# Average response time
rate(chat_response_time_seconds_sum[5m]) / rate(chat_response_time_seconds_count[5m])

# Error rate percentage
rate(chat_errors_total[5m]) / rate(chat_requests_total[5m])

# Critical severity trends
rate(chat_severity_distribution{severity="Critical"}[5m])
```

### Structured Logging
```json
{
  "timestamp": "2024-02-26T10:30:45Z",
  "level": "INFO",
  "logger": "ChatService",
  "message": "Chat request processed",
  "binaryId": "bin_123",
  "severity": "Critical",
  "confidence": "High",
  "responsetime_ms": 2345,
  "model": "gpt-4"
}
```

## Security Hardening

### 1. API Authentication
```java
// Implement JWT or OAuth2
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/chat/**").authenticated()
            .and()
            .oauth2Login();
        return http.build();
    }
}
```

### 2. Rate Limiting
```java
@Bean
public WebFilter rateLimitingFilter() {
    return (exchange, chain) -> {
        // Implement rate limiting per user/IP
        String clientId = getClientId(exchange);
        if (isRateLimited(clientId)) {
            return Mono.error(new TooManyRequestsException());
        }
        return chain.filter(exchange);
    };
}
```

### 3. Input Validation
```java
@PostMapping("/api/chat/ask")
public ResponseEntity<ChatResponse> ask(@Valid @RequestBody ChatRequest request) {
    // @Valid ensures validation
    // Question length: max 500 chars
    // Binary ID: alphanumeric only
    // Context: max 8KB
    
    if (request.getQuestion().length() > 500) {
        throw new BadRequestException("Question too long");
    }
    
    return ResponseEntity.ok(chatService.chat(request));
}
```

### 4. Sensitive Data Protection
```java
// Never log full analysis context
logger.info("Chat request - Binary: {}, Question length: {}", 
    binaryId, question.length()); // ✅ Safe

// Don't expose error details
catch (Exception e) {
    logger.error("Chat error", e);
    return ChatResponse.error("Processing failed"); // ✅ Generic error
}
```

## Performance Optimization

### Caching Strategy
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        // Cache AI responses for identical questions within TTL
        return new ConcurrentMapCacheManager("chatResponses");
    }
}

@Cacheable(value = "chatResponses", 
           key = "T(java.lang.String).format('%s:%s', #request.binaryId, #request.question)",
           unless = "#result == null",
           cacheManager = "cacheManager")
public ChatResponse chat(ChatRequest request) {
    // AI analysis will be cached for 1 hour
    return chatService.chat(request);
}
```

### Load Testing
```bash
# Apache JMeter script for load testing
ab -n 1000 -c 10 -p payload.json \
   -T application/json \
   http://localhost:8080/api/chat/ask
```

### Database Optimization
```sql
-- Index for conversation queries
CREATE INDEX idx_conversation_id ON chat_history(conversation_id);
CREATE INDEX idx_binary_id ON chat_history(binary_id);
CREATE INDEX idx_timestamp ON chat_history(created_at);

-- Analyze query performance
EXPLAIN ANALYZE 
SELECT * FROM chat_history 
WHERE binary_id = ? 
ORDER BY created_at DESC;
```

## Database Schema (Optional)

```sql
CREATE TABLE chat_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(50) NOT NULL,
    binary_id VARCHAR(100) NOT NULL,
    user_question TEXT NOT NULL,
    ai_response LONGTEXT NOT NULL,
    severity VARCHAR(20),
    confidence VARCHAR(20),
    response_time_ms INT,
    model VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation (conversation_id),
    INDEX idx_binary (binary_id),
    INDEX idx_created (created_at)
);

CREATE TABLE conversation_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id VARCHAR(50) UNIQUE NOT NULL,
    binary_id VARCHAR(100) NOT NULL,
    analyst_id VARCHAR(100),
    session_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_end TIMESTAMP,
    total_questions INT DEFAULT 0,
    critical_findings INT DEFAULT 0
);
```

## Compliance & Audit

### GDPR Compliance
```properties
# Data retention policy
app.chat.data-retention-days=90
# Auto-delete conversations after 90 days
```

### Audit Logging
```java
@Aspect
@Component
public class ChatAuditLogging {
    @After("@annotation(Auditable)")
    public void auditChat(JoinPoint joinPoint) {
        // Log all chat operations for compliance
        auditLog.log(
            new AuditEvent(
                USER_ID,
                "CHAT_REQUEST",
                BINARY_ID,
                QUESTION,
                RESPONSE,
                Instant.now()
            )
        );
    }
}
```

## Cost Optimization

### OpenAI API Cost Tracking
```java
@Component
public class OpenAICostTracker {
    private final MeterRegistry meterRegistry;
    
    public void trackApiCall(String model, int inputTokens, int outputTokens) {
        double cost = calculateCost(model, inputTokens, outputTokens);
        meterRegistry.counter("openai.api.cost", "model", model)
            .increment(cost);
    }
}
```

### Cost-Saving Tips
1. **Use GPT-3.5-turbo** for non-critical analysis (50% cheaper)
2. **Implement caching** to avoid duplicate API calls
3. **Batch analysis** where possible
4. **Monitor token usage** to catch runaway costs
5. **Set spending limits** in OpenAI console

## Troubleshooting Guide

### Issue: High API Costs
```
Solution:
1. Check cache hit ratio
2. Reduce token context (implement summarization)
3. Use gpt-3.5-turbo for lower-risk files
4. Batch similar questions
```

### Issue: Slow Responses
```
Solution:
1. Check OpenAI API status
2. Reduce context size
3. Implement response streaming
4. Scale backend horizontally
```

### Issue: Accuracy Concerns
```
Solution:
1. Fine-tune prompts
2. Provide better context
3. Implement human-in-the-loop review
4. Add confidence thresholds
```

## Support & Escalation

### Critical Issues (30 min SLA)
- Chat service down
- Security breach detected
- API key compromised

### High Priority (4 hour SLA)
- Accuracy issues
- Performance degradation
- Integration failures

### Medium Priority (24 hour SLA)
- Feature requests
- Documentation updates
- Training needs

---

**Questions?** Contact: security-team@company.com
