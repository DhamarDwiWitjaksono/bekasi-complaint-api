# Bekasi City Public Complaint API

A RESTful API built with Spring Boot that enables citizens of Bekasi City to submit public complaints/reports to the government for follow-up.

---

## Tech Stack

| Technology     | Version   |
|----------------|-----------|
| Java           | 25 (25.0.2) |
| Spring Boot    | 4.0.2     |
| Maven          | 3.9.12    |
| MySQL          | 8.x+      |
| JWT            | 0.12.6    |
| Geocoding      | Nominatim (OpenStreetMap, free) |

---

## Features

- **Authentication**: JWT-based Sign Up, Login, Logout
- **Role-Based Access Control**: USER, ADMIN, OFFICER
- **Report Management**: Create reports with image upload, geocoded location, and status tracking
- **Location Validation**: Automatically rejects reports outside Bekasi City boundaries using reverse geocoding (Nominatim)
- **Google Maps Integration**: Reports include a clickable Google Maps URL for admin/officer use
- **Image Validation**: Rejects images ≥ 2 MB; only JPEG, PNG, WebP, GIF allowed
- **Report Status Workflow**: PENDING → IN_PROCESS → COMPLETED (or REJECTED from PENDING)
- **Auto-Seeder**: Admin and Officer accounts seeded on startup

---

## Prerequisites

- Java 25.0.2
- Maven 3.9.12
- MySQL 8.x+

---

## Setup & Installation

### 1. Clone the repository
```bash
git clone <repository-url>
cd bekasi-complaint-api
```

### 2. Create the MySQL database
```sql
CREATE DATABASE bekasi_complaint_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure `application.properties`
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bekasi_complaint_db?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

### 4. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

The app will start on `http://localhost:8080`.

On first run, the seeder will automatically create:
- Roles: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_OFFICER`
- Admin account: `admin@bekasikota.go.id` / `Admin@2024!`
- Officer account: `officer@bekasikota.go.id` / `Officer@2024!`

---

## API Endpoints

### Authentication

| Method | Endpoint            | Access  | Description              |
|--------|---------------------|---------|--------------------------|
| POST   | `/api/auth/signup`  | Public  | Register a new user      |
| POST   | `/api/auth/login`   | Public  | Login and get JWT token  |
| POST   | `/api/auth/logout`  | Auth    | Logout (client-side)     |

#### Sign Up
```json
POST /api/auth/signup
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Login
```json
POST /api/auth/login
{
  "email": "john@example.com",
  "password": "password123"
}
```
Returns a JWT token to use in the `Authorization: Bearer <token>` header.

---

### Reports

Semua endpoint laporan **wajib login** (Bearer token). Hasil yang dikembalikan berbeda berdasarkan role:

| Role | `GET /api/reports` dan filter status |
|------|--------------------------------------|
| USER | Hanya laporan milik sendiri |
| ADMIN | Semua laporan dari semua user |
| OFFICER | Semua laporan dari semua user |

| Method | Endpoint                        | Akses              | Keterangan                           |
|--------|---------------------------------|--------------------|--------------------------------------|
| POST   | `/api/reports`                  | USER               | Buat laporan baru                    |
| GET    | `/api/reports`                  | Semua (login)      | Lihat laporan (sesuai role)          |
| GET    | `/api/reports/{id}`             | Semua (login)      | Detail laporan (USER: hanya miliknya)|
| GET    | `/api/reports/status/pending`   | Semua (login)      | Filter status PENDING                |
| GET    | `/api/reports/status/in-process`| Semua (login)      | Filter status IN_PROCESS             |
| GET    | `/api/reports/status/completed` | Semua (login)      | Filter status COMPLETED              |
| GET    | `/api/reports/status/rejected`  | Semua (login)      | Filter status REJECTED               |
| PATCH  | `/api/reports/{id}/approve`     | ADMIN              | PENDING → IN_PROCESS                 |
| PATCH  | `/api/reports/{id}/reject`      | ADMIN              | PENDING → REJECTED                   |
| PATCH  | `/api/reports/{id}/complete`    | OFFICER            | IN_PROCESS → COMPLETED               |

> **Catatan keamanan:** Jika USER mencoba mengakses laporan milik user lain via `GET /api/reports/{id}`, server mengembalikan `404 Not Found` (bukan 403) untuk menghindari enumerasi data.

### Profile User

| Method | Endpoint        | Akses         | Keterangan                          |
|--------|-----------------|---------------|-------------------------------------|
| GET    | `/api/users/me` | Semua (login) | Ambil profil: id, name, email, roles|
| PUT    | `/api/users/me` | Semua (login) | Update nama dan/atau password       |

```json
PUT /api/users/me
Authorization: Bearer <token>
{
  "name": "Nama Baru",
  "currentPassword": "passwordLama",
  "newPassword": "passwordBaru123"
}
```
Jika hanya ingin update nama, cukup kirim `name` tanpa field password.
```
POST /api/reports
Authorization: Bearer <token>
Content-Type: multipart/form-data

data (JSON part):
{
  "title": "Pothole on Jl. Ahmad Yani",
  "category": "INFRASTRUCTURE",
  "latitude": -6.2700,
  "longitude": 106.9500,
  "description": "Large pothole causing traffic hazards near the market."
}

image: <file> (< 2 MB, JPEG/PNG/WebP/GIF)
```

---

### Admin Account Management

| Method | Endpoint              | Access | Description                       |
|--------|-----------------------|--------|-----------------------------------|
| POST   | `/api/admin/accounts` | ADMIN  | Create a new ADMIN or OFFICER account |

```json
POST /api/admin/accounts
Authorization: Bearer <admin-token>
{
  "name": "New Officer",
  "email": "officer2@bekasikota.go.id",
  "password": "SecurePass123!",
  "role": "ROLE_OFFICER"
}
```
Valid roles for this endpoint: `ROLE_ADMIN`, `ROLE_OFFICER`

---

## Report Categories

```
INFRASTRUCTURE, ENVIRONMENT, PUBLIC_SAFETY, HEALTH,
EDUCATION, SOCIAL, TRANSPORTATION, OTHER
```

## Report Status Flow

```
[User creates report]
        ↓
    PENDING
   /       \
ADMIN      ADMIN
APPROVE    REJECT
   ↓           ↓
IN_PROCESS  REJECTED
   ↓
OFFICER
COMPLETES
   ↓
COMPLETED
```

- Status transitions are strictly enforced — no arbitrary changes allowed.

---

## Location Validation

Reports use **reverse geocoding** via [Nominatim (OpenStreetMap)](https://nominatim.openstreetmap.org).

Validation flow:
1. Coordinates checked against Bekasi City bounding box (lat: -6.37 to -6.18, lon: 106.87 to 107.08)
2. Reverse geocoded address verified to contain "Bekasi"
3. If either check fails, the report is rejected with a descriptive error message

After validation, the `googleMapsUrl` field in the response contains a direct link:
```
https://www.google.com/maps?q=-6.270000,106.950000
```

---

## Image Serving

Uploaded images are accessible at:
```
GET /api/images/{filename}
```
The `imageUrl` field in report responses contains the full URL.

---

## Response Format

All endpoints return a consistent response envelope:
```json
{
  "success": true,
  "message": "Report created successfully",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Database Schema Overview

```
roles              → id, name (ROLE_USER | ROLE_ADMIN | ROLE_OFFICER)
users              → id, name, email, password, created_at, updated_at
user_roles         → user_id (FK), role_id (FK)
reports            → id, title, category, image_path, latitude, longitude,
                     address, google_maps_url, description, status,
                     user_id (FK), created_at, updated_at
```

---

## Running Tests

```bash
mvn test
```

Tests use an H2 in-memory database (no MySQL required for tests).

---

## Security Notes

- Passwords are hashed using **BCrypt**
- JWT tokens expire after **24 hours** (configurable)
- All sensitive configuration values should be externalized via environment variables in production
- CORS is currently open — configure appropriately for production