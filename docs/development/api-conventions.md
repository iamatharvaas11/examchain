# EXAMCHAIN — API Conventions

## Base URL

All API endpoints are versioned under:

```
/api/v1/
```

## Request/Response Format

All requests and responses use JSON (`Content-Type: application/json`).

### Success Response

```json
{
  "success": true,
  "message": "Exam created successfully",
  "data": { ... },
  "timestamp": "2026-09-17T18:00:00Z"
}
```

### Error Response

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/exams",
  "timestamp": "2026-09-17T18:00:00Z",
  "validationErrors": {
    "name": "must not be blank",
    "scheduledDate": "must be a future date"
  }
}
```

## HTTP Methods

| Method | Usage | Response Code |
|---|---|---|
| GET | Retrieve resource(s) | 200 |
| POST | Create resource | 201 |
| PUT | Full update | 200 |
| PATCH | Partial update | 200 |
| DELETE | Remove resource | 204 |

## Pagination

List endpoints support pagination:

```
GET /api/v1/exams?page=0&size=20&sort=createdAt,desc
```

Response includes pagination metadata in the `data` field:

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

## Authentication

All protected endpoints require a JWT Bearer token:

```
Authorization: Bearer <jwt-token>
```

The public QR verification endpoint (`/api/v1/verify/{traceId}`) is the only exception (rate-limited, no auth).

## Error Codes

| HTTP Status | Meaning |
|---|---|
| 200 | Success |
| 201 | Created |
| 204 | No Content (successful delete) |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient role) |
| 404 | Not Found |
| 409 | Conflict (duplicate, invalid state transition) |
| 429 | Too Many Requests (rate limited) |
| 500 | Internal Server Error |

## Naming Conventions

- **Resources:** plural nouns (`/exams`, `/questions`, `/centres`)
- **Actions:** use HTTP methods, not verbs in URLs
- **Nested resources:** `/exams/{examId}/subjects`
- **IDs:** UUID format

