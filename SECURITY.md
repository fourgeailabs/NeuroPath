# Security Policy

## Supported Versions

We release patches for security vulnerabilities in the following versions:

| Version | Supported          |
| ------- | ------------------ |
| 2.0.x   | ✅ Yes             |
| < 2.0   | ❌ No              |

## Reporting a Vulnerability

If you discover a security vulnerability in NeuroPath, please report it responsibly:

### Do Not

- Create a public GitHub issue
- Discuss the vulnerability publicly before it's fixed
- Share exploit details

### Do

**Email the maintainers directly** at: security@fourgeailabs.com

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Any suggested fixes

We will:
- Acknowledge receipt within 48 hours
- Provide a timeline for fix
- Credit you in the fix (if desired)
- Release a patch as soon as possible

## Security Features

NeuroPath implements several security measures:

### Data Protection
- All user data stored locally on-device (Room database)
- No personal data transmitted without explicit consent
- COPPA compliant - no child data collection
- Encrypted SharedPreferences for sensitive settings

### API Security
- Gemini API keys stored in `.env` (gitignored)
- Secrets loaded via Gradle Secrets plugin at build time
- No hardcoded API keys in source code
- Firebase App Check for backend verification

### Network Security
- HTTPS-only communication
- Certificate pinning for critical endpoints
- Request/response validation

### Local AI
- GGUF models validated on load (magic bytes, size)
- LiteRT-LM models verified before inference
- No arbitrary code execution from model files
- Sandboxed inference processes

## Threat Model

### In Scope
- Local data exfiltration
- API key leakage
- Model file tampering
- Input validation bypasses
- Intent/exported component vulnerabilities

### Out of Scope
- Physical device access
- OS-level vulnerabilities
- Third-party library supply chain (mitigated via pinned versions)
- Social engineering attacks

## Disclosure Timeline

1. **Day 0**: Vulnerability reported
2. **Day 1-2**: Acknowledgment and triage
3. **Day 7**: Initial assessment and timeline
4. **Day 30**: Target fix release (critical issues sooner)
5. **Day 30+**: Public disclosure after patch release

## Security Best Practices for Contributors

- Never commit secrets, API keys, or tokens
- Use `.env` for local development
- Validate all external inputs
- Use parameterized queries for database operations
- Keep dependencies updated (Dependabot alerts monitored)
- Run `./gradlew.bat dependencyCheckAnalyze` periodically

## Contact

For security concerns: security@fourgeailabs.com
For general questions: GitHub Issues