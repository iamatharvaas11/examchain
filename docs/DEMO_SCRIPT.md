# EXAMCHAIN — End-to-End Live Hackathon Demo Script

## Overview
This script demonstrates the complete 16-phase security lifecycle of **EXAMCHAIN**:
"One-Tap for Humans. Zero-Trust for Machines."

---

### Step 1: Health & Observability Check
Verify that all core subsystems (database, object storage, crypto enclave, ledger mirror) are active.
```bash
curl -X GET http://localhost:8080/api/v1/health/extended
```
*Expected output:* `overallStatus: "UP"`, `database: "CONNECTED"`, `cryptoEnclave: "HEALTHY"`, `objectStorage: "HEALTHY_SECURE"`, `ledgerConnectivity: "CONNECTED_LOCAL_MIRROR"`.

---

### Step 2: Role-Based Access & Identity (Phase 2)
Authenticate with Keycloak OIDC JWT token containing one of the 8 canonical roles:
- `SUPER_ADMIN`
- `EXAM_AUTHORITY`
- `CONTROLLER`
- `PAPER_SETTER`
- `CENTRE_ADMIN`
- `EXAM_OPERATOR`
- `AUDITOR`
- `STUDENT`

---

### Step 3: Question Pool Creation & Setter Workflow (Phase 3)
1. Exam Authority creates an exam and subject.
2. Authority assigns Paper Setter to the subject.
3. Paper Setter submits questions with metadata (`unit`, `marks`, `difficulty`, `questionType`, `cognitiveLevel`, SHA-256 hash).
4. Pool lifecycle transitions: `DRAFT` $\rightarrow$ `SUBMITTED` $\rightarrow$ `APPROVED`. Once approved, question content is cryptographically frozen.

---

### Step 4: AES-256-GCM Crypto Enclave Sealing (Phase 4)
When questions and papers are generated:
- Symmetric 256-bit AES keys generated inside Vault Key Management enclave.
- Authenticated encryption using 96-bit IV, 128-bit authentication tag, and AAD binding.
- Plaintext is purged from memory; ciphertext only stored in MinIO object storage.

---

### Step 5: Dynamic Paper Generation & Variant Fairness Engine (Phases 5 & 6)
1. Authority defines Blueprint rules with exact unit/difficulty/type constraints.
2. Dynamic paper generator samples questions without replacement via `SecureRandom`.
3. Fairness engine evaluates Set A vs Set B:
   - Evaluates marks invariance, unit syllabus coverage, difficulty percentage variance, and pairwise question overlap ($\le 30\%$).
   - Returns deterministic `ACCEPT` decision.

---

### Step 6: Hyperledger Fabric Immutable Commit (Phases 8 & 9)
1. Paper cryptographic SHA-256 hash committed to Fabric chaincode (`RegisterPaper`).
2. Two independent authorities submit cryptographic signatures (`RecordApproval`).
3. 2-of-3 quorum threshold is met (`THRESHOLD_MET`).
4. Time-lock releases paper at the scheduled release timestamp (`AuthorizeRelease`).

---

### Step 7: Centre Portal Secure In-Memory Printing (Phase 10)
1. Centre operator checks in from registered terminal.
2. Device continuity engine verifies fingerprint (Risk Score = 0).
3. Paper is retrieved and decrypted strictly in memory.
4. Unalterable forensic watermark embedded:
   `CONFIDENTIAL — CENTRE: CENTRE-01 | TERMINAL: TERM-01 | OP: OP-ALICE | TIME: <timestamp> | COPY: 1`
5. Verifiable print receipt `PRN-REC-...` issued and logged to ledger.

---

### Step 8: Incident Response & Variant Quarantine (Phases 11 & 12)
1. Duplicate print or compromised terminal triggers security anomaly.
2. System freeze escalates to `SUSPICIOUS` or `FROZEN`.
3. Compromised variant is quarantined on-chain (`QuarantineVariant`).
4. Authority triggers variant replacement:
   - Compromised variant is permanently revoked (`REPLACED`).
   - Reserve variant is activated and authorized for emergency release (`ReplaceVariant`).

---

### Step 9: Offline Mode & Disaster Recovery (Phase 13)
1. Network severed to examination centre.
2. Authority generates single-use time-bounded emergency token (`OFL-SEC-...`).
3. Centre terminal consumes token to decrypt pre-positioned bundle. Replay attacks are strictly rejected.
4. On reconnection, `POST /api/v1/offline/sync` bulk reconciles offline print logs with the central ledger.

---

### Step 10: Auditor Dashboard & Public QR Provenance (Phases 7 & 14)
1. Candidate/Public scans QR code on printed paper (`GET /api/v1/verify/paper/{traceId}`):
   - Pre-exam scan: Returns locked metadata with zero plaintext questions or internal keys leaked.
   - Post-exam scan: Returns full authenticated provenance receipt and SHA-256 hash match.
2. Auditor opens `/api/v1/audit/summary` and `/api/v1/audit/papers/{paperId}/graph`:
   - Visualizes full DAG: `Pool -> Blueprint -> Paper -> Enclave -> Quorum Approvals -> Print -> Receipt`.
   - Runs independent cryptographic verification to ensure zero tamper across the lifecycle.

