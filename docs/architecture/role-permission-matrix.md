# EXAMCHAIN — Role & Permission Matrix (Phase 2)

## 1. Principles

1. **Defense-in-Depth:** Frontend route protection is solely for user experience. The backend strictly enforces role checks on every non-public request.
2. **Least Privilege:** Each role has access strictly scoped to its domain responsibilities.
3. **Keycloak Authority:** Keycloak acts as the OpenID Connect (OIDC) identity provider. The backend parses signed JWT tokens and extracts roles from `realm_access.roles` into Spring Security `GrantedAuthority` representations (`ROLE_<ROLE_NAME>`).

---

## 2. Canonical Roles

| Role | Domain | Responsibilities |
|---|---|---|
| `SUPER_ADMIN` | Governance | System health, configuration, identity syncing, user role assignment audit. |
| `EXAM_AUTHORITY` | Academic / Policy | Exam lifecycle creation, blueprint definition, pool review & approval, release policy. |
| `CONTROLLER` | Integrity / Operations | Secondary approval threshold co-signer, freeze incident execution, paper distribution authorization. |
| `PAPER_SETTER` | Content Creation | Question authoring, metadata tagging, pool submission. (Blind to generation & other setters). |
| `CENTRE_ADMIN` | Centre Logistics | Centre profile management, terminal registration, operator oversight. |
| `EXAM_OPERATOR` | Print Room Execution | Secure session verification, print quota execution, watermark validation. |
| `AUDITOR` | Independent Oversight | Lineage graph inspection, cryptographic proof verification, verification scan logs. |
| `STUDENT` | Candidate | Public trace verification scan, personal exam receipt lookup. |

---

## 3. Endpoint Authorization Matrix

| Endpoint Path | Method | Minimum Role Required | Public? | Purpose |
|---|---|---|---|---|
| `/api/v1/health` | GET | None | Yes | System liveness probe |
| `/api/v1/auth/me` | GET | Any Authenticated Role | No | Current session identity profile |
| `/api/v1/admin/**` | ANY | `SUPER_ADMIN` | No | System administrative operations |
| `/api/v1/authority/**` | ANY | `EXAM_AUTHORITY`, `CONTROLLER` | No | Examination management operations |
| `/api/v1/setter/**` | ANY | `PAPER_SETTER` | No | Question authoring operations |
| `/api/v1/centre/**` | ANY | `CENTRE_ADMIN`, `EXAM_OPERATOR` | No | Examination centre management |
| `/api/v1/operator/**` | ANY | `EXAM_OPERATOR` | No | Controlled print/release execution |
| `/api/v1/audit/**` | ANY | `AUDITOR`, `SUPER_ADMIN` | No | Audit ledger & provenance queries |
| `/api/v1/student/**` | ANY | `STUDENT` | No | Student portal operations |
| `/api/v1/verify/**` | GET | None | Yes (Rate-limited) | Public paper trace verification |

---

## 4. Token & Security Contract

- **Format:** OIDC JSON Web Token (JWT), RS256 signed.
- **Claims Extracted:**
  - `sub`: Keycloak User ID (UUID)
  - `preferred_username`: Username
  - `email`: Email address
  - `name`: Display Name
  - `realm_access.roles`: Array of role strings mapped to `ROLE_<ROLE_NAME>`
- **HTTP Status Codes:**
  - `401 Unauthorized`: Missing, invalid, expired, or untrusted JWT signature.
  - `403 Forbidden`: Authenticated user lacks required role for the requested resource.

